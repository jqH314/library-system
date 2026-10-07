package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.entity.BorrowRecord;
import com.library.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @PostMapping("/{bookId}")
    public Result<Map<String, Object>> borrowBook(@PathVariable Long bookId) {
        return Result.success(borrowService.borrowBook(bookId));
    }

    @PutMapping("/return/{recordId}")
    public Result<Void> returnBook(@PathVariable Long recordId) {
        borrowService.returnBook(recordId);
        return Result.success();
    }

    @PutMapping("/renew/{recordId}")
    public Result<Void> renewBook(@PathVariable Long recordId) {
        borrowService.renewBook(recordId);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<Page<BorrowRecord>> getMyBorrowRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.success(borrowService.getMyBorrowRecords(page, size, status));
    }

    @PutMapping("/fine/{recordId}")
    public Result<Void> payFine(@PathVariable Long recordId) {
        borrowService.payFine(recordId);
        return Result.success();
    }

    @GetMapping("/admin/page")
    public Result<Page<BorrowRecord>> getAllBorrowRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(borrowService.getAllBorrowRecords(page, size, keyword, status));
    }

    @PutMapping("/admin/return/{recordId}")
    public Result<Void> adminReturnBook(@PathVariable Long recordId) {
        borrowService.returnBook(recordId);
        return Result.success();
    }

    @PutMapping("/admin/renew/{recordId}")
    public Result<Void> adminRenewBook(@PathVariable Long recordId) {
        borrowService.renewBook(recordId);
        return Result.success();
    }

    @PutMapping("/admin/fine/{recordId}")
    public Result<Void> adminPayFine(@PathVariable Long recordId) {
        borrowService.payFine(recordId);
        return Result.success();
    }
}
