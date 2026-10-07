package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.entity.Book;
import com.library.mapper.BookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String HOT_BOOK_KEY = "library:hot:books";
    private static final String BOOK_CACHE_PREFIX = "library:book:";

    public Page<Book> getBookPage(Integer page, Integer size, String keyword, Long categoryId, Integer status) {
        Page<Book> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Book::getStatus, status);
        }
        wrapper.orderByDesc(Book::getBorrowCount);
        return bookMapper.selectPage(pageParam, wrapper);
    }

    public Book getBookById(Long id) {
        String cacheKey = BOOK_CACHE_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (Book) cached;
        }
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        redisTemplate.opsForValue().set(cacheKey, book, 30, TimeUnit.MINUTES);
        return book;
    }

    public List<Book> getHotBooks(Integer limit) {
        Set<Object> ids = redisTemplate.opsForZSet().reverseRange(HOT_BOOK_KEY, 0, limit - 1);
        if (ids != null && !ids.isEmpty()) {
            List<Long> idList = new java.util.ArrayList<>();
            for (Object id : ids) {
                idList.add(Long.valueOf(id.toString()));
            }
            List<Book> books = bookMapper.selectBatchIds(idList);
            books.sort((a, b) -> idList.indexOf(a.getId()) - idList.indexOf(b.getId()));
            return books;
        }
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1).orderByDesc(Book::getBorrowCount).last("LIMIT " + limit);
        return bookMapper.selectList(wrapper);
    }

    public List<Book> getNewBooks(Integer limit) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1).orderByDesc(Book::getCreateTime).last("LIMIT " + limit);
        return bookMapper.selectList(wrapper);
    }

    public void addBook(Book book) {
        Book exist = bookMapper.selectOne(new LambdaQueryWrapper<Book>().eq(Book::getIsbn, book.getIsbn()));
        if (exist != null) {
            throw new BusinessException("ISBN已存在");
        }
        book.setAvailableCount(book.getTotalCount());
        book.setBorrowCount(0);
        book.setVersion(0);
        bookMapper.insert(book);
    }

    public void updateBook(Book book) {
        Book exist = bookMapper.selectById(book.getId());
        if (exist == null) {
            throw new BusinessException("图书不存在");
        }
        if (book.getTotalCount() != null) {
            int diff = book.getTotalCount() - exist.getTotalCount();
            book.setAvailableCount(Math.max(0, exist.getAvailableCount() + diff));
        }
        bookMapper.updateById(book);
        redisTemplate.delete(BOOK_CACHE_PREFIX + book.getId());
    }

    public void deleteBook(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (book.getAvailableCount() < book.getTotalCount()) {
            throw new BusinessException("图书有未归还的借阅，无法删除");
        }
        bookMapper.deleteById(id);
        redisTemplate.delete(BOOK_CACHE_PREFIX + id);
    }

    public void updateStatus(Long id, Integer status) {
        Book book = new Book();
        book.setId(id);
        book.setStatus(status);
        bookMapper.updateById(book);
        redisTemplate.delete(BOOK_CACHE_PREFIX + id);
    }
}
