package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.common.UserContext;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BorrowService {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Value("${borrow.max-days:30}")
    private Integer maxDays;

    @Value("${borrow.max-renew-count:2}")
    private Integer maxRenewCount;

    @Value("${borrow.fine-per-day:0.50}")
    private BigDecimal finePerDay;

    @Transactional
    public Map<String, Object> borrowBook(Long bookId) {
        Long userId = UserContext.getUserId();
        String lockKey = "borrow:book:" + bookId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
            User user = userMapper.selectById(userId);
            if (user.getBorrowedCount() >= user.getMaxBorrowCount()) {
                throw new BusinessException("已达最大借阅数量");
            }
            // 检查是否有逾期未还的图书
            long overdueCount = borrowRecordMapper.selectCount(
                    new LambdaQueryWrapper<BorrowRecord>()
                            .eq(BorrowRecord::getUserId, userId)
                            .eq(BorrowRecord::getStatus, "OVERDUE")
            );
            if (overdueCount > 0) {
                throw new BusinessException("您有" + overdueCount + "本逾期未还的图书，请先归还后再借阅新书");
            }
            // 检查是否有未缴纳的罚款（包括已归还但未缴罚款的情况）
            long unpaidFineCount = borrowRecordMapper.selectCount(
                    new LambdaQueryWrapper<BorrowRecord>()
                            .eq(BorrowRecord::getUserId, userId)
                            .eq(BorrowRecord::getFinePaid, 0)
                            .gt(BorrowRecord::getFineAmount, BigDecimal.ZERO)
            );
            if (unpaidFineCount > 0) {
                throw new BusinessException("您有未缴纳的罚款，请先缴纳罚款后再借阅新书");
            }
            Book book = bookMapper.selectById(bookId);
            if (book == null || book.getStatus() == 0) {
                throw new BusinessException("图书不存在或已下架");
            }
            if (book.getAvailableCount() <= 0) {
                throw new BusinessException("图书库存不足");
            }
            long count = borrowRecordMapper.selectCount(
                    new LambdaQueryWrapper<BorrowRecord>()
                            .eq(BorrowRecord::getUserId, userId)
                            .eq(BorrowRecord::getBookId, bookId)
                            .eq(BorrowRecord::getStatus, "BORROWED")
            );
            if (count > 0) {
                throw new BusinessException("您已借阅此书，请勿重复借阅");
            }
            book.setAvailableCount(book.getAvailableCount() - 1);
            book.setBorrowCount(book.getBorrowCount() + 1);
            bookMapper.updateById(book);
            user.setBorrowedCount(user.getBorrowedCount() + 1);
            userMapper.updateById(user);
            BorrowRecord record = new BorrowRecord();
            record.setUserId(userId);
            record.setBookId(bookId);
            record.setBorrowTime(LocalDateTime.now());
            record.setDueTime(LocalDateTime.now().plusDays(maxDays));
            record.setStatus("BORROWED");
            record.setRenewCount(0);
            record.setFineAmount(BigDecimal.ZERO);
            record.setFinePaid(0);
            borrowRecordMapper.insert(record);
            Map<String, Object> result = new HashMap<>();
            result.put("recordId", record.getId());
            result.put("dueTime", record.getDueTime());
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统异常");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public void returnBook(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if ("RETURNED".equals(record.getStatus())) {
            throw new BusinessException("该书已归还");
        }
        Book book = bookMapper.selectById(record.getBookId());
        book.setAvailableCount(book.getAvailableCount() + 1);
        bookMapper.updateById(book);
        User user = userMapper.selectById(record.getUserId());
        user.setBorrowedCount(Math.max(0, user.getBorrowedCount() - 1));
        userMapper.updateById(user);
        BigDecimal fine = BigDecimal.ZERO;
        if (LocalDateTime.now().isAfter(record.getDueTime())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueTime(), LocalDateTime.now());
            fine = finePerDay.multiply(BigDecimal.valueOf(overdueDays));
        }
        record.setReturnTime(LocalDateTime.now());
        record.setStatus("RETURNED");
        record.setFineAmount(fine);
        if (fine.compareTo(BigDecimal.ZERO) > 0) {
            record.setFinePaid(0);
        } else {
            record.setFinePaid(1);
        }
        borrowRecordMapper.updateById(record);
    }

    @Transactional
    public void renewBook(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if (!"BORROWED".equals(record.getStatus())) {
            throw new BusinessException("只有借阅中的图书才能续借");
        }
        if (record.getRenewCount() >= maxRenewCount) {
            throw new BusinessException("已达最大续借次数");
        }
        if (LocalDateTime.now().isAfter(record.getDueTime())) {
            throw new BusinessException("图书已逾期，无法续借，请先归还并缴纳罚款");
        }
        record.setDueTime(record.getDueTime().plusDays(maxDays));
        record.setRenewCount(record.getRenewCount() + 1);
        borrowRecordMapper.updateById(record);
    }

    public Page<BorrowRecord> getMyBorrowRecords(Integer page, Integer size, String status) {
        Long userId = UserContext.getUserId();
        Page<BorrowRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BorrowRecord::getStatus, status);
        }
        wrapper.orderByDesc(BorrowRecord::getCreateTime);
        return borrowRecordMapper.selectPage(pageParam, wrapper);
    }

    public Page<BorrowRecord> getAllBorrowRecords(Integer page, Integer size, String keyword, String status) {
        Page<BorrowRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(BorrowRecord::getUserId, keyword)
                    .or().like(BorrowRecord::getBookId, keyword));
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BorrowRecord::getStatus, status);
        }
        wrapper.orderByDesc(BorrowRecord::getCreateTime);
        return borrowRecordMapper.selectPage(pageParam, wrapper);
    }

    @Transactional
    public void payFine(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if (record.getFinePaid() == 1) {
            throw new BusinessException("罚款已缴纳");
        }
        if (record.getFineAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("无需缴纳罚款");
        }
        record.setFinePaid(1);
        borrowRecordMapper.updateById(record);
    }
}
