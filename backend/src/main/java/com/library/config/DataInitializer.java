package com.library.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(ApplicationArguments args) {
        // 初始化管理员账号，确保密码正确
        initUser("admin", "admin123", "系统管理员", "ADMIN", "13800138000", "admin@library.com", 999);
        // 初始化测试读者账号
        initUser("2023001", "123456", "张三", "READER", "13900139001", "zhangsan@student.edu", 5);
        initUser("2023002", "123456", "李四", "READER", "13900139002", "lisi@student.edu", 5);
        initUser("2023003", "123456", "王五", "READER", "13900139003", "wangwu@student.edu", 5);
        System.out.println("========================================");
        System.out.println("  用户数据初始化完成！");
        System.out.println("  管理员：admin / admin123");
        System.out.println("  读者：2023001 / 123456");
        System.out.println("========================================");
    }

    private void initUser(String username, String password, String realName, String role, String phone, String email, int maxBorrowCount) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setRealName(realName);
            user.setRole(role);
            user.setPhone(phone);
            user.setEmail(email);
            user.setStatus(1);
            user.setMaxBorrowCount(maxBorrowCount);
            user.setBorrowedCount(0);
            user.setPassword(passwordEncoder.encode(password));
            userMapper.insert(user);
        } else {
            // 更新密码，确保密码正确
            user.setPassword(passwordEncoder.encode(password));
            userMapper.updateById(user);
        }
    }
}
