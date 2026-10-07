package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.entity.Book;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/page")
    public Result<Page<Book>> getBookPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(bookService.getBookPage(page, size, keyword, categoryId, status));
    }

    @GetMapping("/{id}")
    public Result<Book> getBookById(@PathVariable Long id) {
        return Result.success(bookService.getBookById(id));
    }

    @GetMapping("/hot")
    public Result<List<Book>> getHotBooks(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(bookService.getHotBooks(limit));
    }

    @GetMapping("/new")
    public Result<List<Book>> getNewBooks(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(bookService.getNewBooks(limit));
    }

    @PostMapping
    public Result<Void> addBook(@RequestBody Book book) {
        bookService.addBook(book);
        return Result.success();
    }

    @PutMapping
    public Result<Void> updateBook(@RequestBody Book book) {
        bookService.updateBook(book);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        bookService.updateStatus(id, status);
        return Result.success();
    }
}
