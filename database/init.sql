-- ============================================
-- 在线图书借阅管理系统 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS library_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_system;

-- --------------------------------------------
-- 1. 用户表
-- --------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名/学号',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `role` VARCHAR(20) NOT NULL DEFAULT 'READER' COMMENT '角色：ADMIN-管理员 READER-读者',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
  `max_borrow_count` INT NOT NULL DEFAULT 5 COMMENT '最大可借数量',
  `borrowed_count` INT NOT NULL DEFAULT 0 COMMENT '当前已借数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- --------------------------------------------
-- 2. 图书分类表
-- --------------------------------------------
DROP TABLE IF EXISTS `book_category`;
CREATE TABLE `book_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `code` VARCHAR(20) NOT NULL COMMENT '分类编码',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- --------------------------------------------
-- 3. 图书表
-- --------------------------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图书ID',
  `isbn` VARCHAR(20) NOT NULL COMMENT 'ISBN编号',
  `title` VARCHAR(200) NOT NULL COMMENT '书名',
  `author` VARCHAR(100) DEFAULT NULL COMMENT '作者',
  `publisher` VARCHAR(100) DEFAULT NULL COMMENT '出版社',
  `publish_date` DATE DEFAULT NULL COMMENT '出版日期',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '价格',
  `total_count` INT NOT NULL DEFAULT 1 COMMENT '总数量',
  `available_count` INT NOT NULL DEFAULT 1 COMMENT '可借数量',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
  `description` TEXT COMMENT '图书简介',
  `location` VARCHAR(50) DEFAULT NULL COMMENT '馆藏位置',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-上架 0-下架',
  `borrow_count` INT NOT NULL DEFAULT 0 COMMENT '借阅次数',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_isbn` (`isbn`),
  KEY `idx_category` (`category_id`),
  KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- --------------------------------------------
-- 4. 借阅记录表
-- --------------------------------------------
DROP TABLE IF EXISTS `borrow_record`;
CREATE TABLE `borrow_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `borrow_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借阅时间',
  `due_time` DATETIME NOT NULL COMMENT '应还时间',
  `return_time` DATETIME DEFAULT NULL COMMENT '实际归还时间',
  `status` VARCHAR(20) NOT NULL DEFAULT 'BORROWED' COMMENT '状态：BORROWED-借阅中 RETURNED-已归还 OVERDUE-已逾期',
  `renew_count` INT NOT NULL DEFAULT 0 COMMENT '续借次数',
  `fine_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '罚款金额',
  `fine_paid` TINYINT NOT NULL DEFAULT 0 COMMENT '罚款是否已缴：0-未缴 1-已缴',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_book` (`book_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- --------------------------------------------
-- 5. 罚款记录表
-- --------------------------------------------
DROP TABLE IF EXISTS `fine_record`;
CREATE TABLE `fine_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '罚款ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `borrow_id` BIGINT NOT NULL COMMENT '借阅记录ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '罚款金额',
  `reason` VARCHAR(200) DEFAULT NULL COMMENT '罚款原因',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未缴纳 1-已缴纳',
  `pay_time` DATETIME DEFAULT NULL COMMENT '缴纳时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='罚款记录表';

-- --------------------------------------------
-- 6. 操作日志表
-- --------------------------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
  `operation` VARCHAR(100) DEFAULT NULL COMMENT '操作描述',
  `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `params` TEXT COMMENT '请求参数',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `cost_time` BIGINT DEFAULT 0 COMMENT '耗时（毫秒）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 初始化数据
-- ============================================

-- 管理员账号（密码：admin123，BCrypt加密）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `phone`, `email`, `status`, `max_borrow_count`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIu', '系统管理员', 'ADMIN', '13800138000', 'admin@library.com', 1, 999);

-- 读者账号（密码：123456，BCrypt加密）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `phone`, `email`, `status`, `max_borrow_count`) VALUES
('2023001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIu', '张三', 'READER', '13900139001', 'zhangsan@student.edu', 1, 5),
('2023002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIu', '李四', 'READER', '13900139002', 'lisi@student.edu', 1, 5),
('2023003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIu', '王五', 'READER', '13900139003', 'wangwu@student.edu', 1, 5);

-- 图书分类
INSERT INTO `book_category` (`name`, `code`, `parent_id`, `sort`) VALUES
('计算机科学', 'CS', 0, 1),
('文学小说', 'LIT', 0, 2),
('历史人文', 'HIS', 0, 3),
('经济管理', 'ECO', 0, 4),
('自然科学', 'SCI', 0, 5),
('艺术设计', 'ART', 0, 6),
('编程语言', 'CS-PL', 1, 1),
('数据库', 'CS-DB', 1, 2),
('人工智能', 'CS-AI', 1, 3),
('中国文学', 'LIT-CN', 2, 1),
('外国文学', 'LIT-FN', 2, 2),
('中国历史', 'HIS-CN', 3, 1),
('世界历史', 'HIS-WW', 3, 2),
('经济学', 'ECO-EC', 4, 1);

-- 图书数据
INSERT INTO `book` (`isbn`, `title`, `author`, `publisher`, `publish_date`, `category_id`, `price`, `total_count`, `available_count`, `description`, `location`, `status`, `borrow_count`) VALUES
('9787111213826', 'Java编程思想（第4版）', 'Bruce Eckel', '机械工业出版社', '2007-06-01', 7, 108.00, 5, 3, '本书赢得了全球程序员的广泛赞誉，作者以其广博且深邃的视角，将Java语言的各种特性娓娓道来。', 'A区-01-01', 1, 128),
('9787115428028', '深入理解Java虚拟机', '周志明', '人民邮电出版社', '2016-06-01', 7, 89.00, 3, 2, '本书是近年来国内出版的原创技术著作中最具系统性和深度的作品之一，从实战角度出发，详细讲解了JVM的各种特性。', 'A区-01-02', 1, 95),
('9787121362040', 'MySQL技术内幕：InnoDB存储引擎', '姜承尧', '电子工业出版社', '2011-01-01', 8, 69.00, 4, 4, '本书从源代码的角度深度剖析了InnoDB存储引擎的实现原理，适合数据库开发和运维人员阅读。', 'A区-02-01', 1, 67),
('9787115461476', 'Python深度学习', 'François Chollet', '人民邮电出版社', '2018-08-01', 9, 119.00, 3, 1, '本书由Keras之父、Google人工智能研究员François Chollet亲笔撰写，是深度学习领域的经典入门书。', 'A区-03-01', 1, 156),
('9787020002207', '红楼梦', '曹雪芹', '人民文学出版社', '1996-12-01', 10, 59.70, 10, 8, '中国古典四大名著之首，清代作家曹雪芹创作的章回体长篇小说，是一部具有世界影响力的人情小说作品。', 'B区-01-01', 1, 203),
('9787020042487', '百年孤独', '加西亚·马尔克斯', '人民文学出版社', '2011-06-01', 11, 39.50, 6, 5, '魔幻现实主义文学的代表作，描写了布恩迪亚家族七代人的传奇故事，以及加勒比海沿岸小镇马孔多的百年兴衰。', 'B区-02-01', 1, 178),
('9787108009821', '万历十五年', '黄仁宇', '生活·读书·新知三联书店', '1997-05-01', 12, 18.00, 5, 4, '本书以1587年即明万历十五年为切入点，通过对当时历史人物的细致描写，展现了明代中国的社会全貌。', 'C区-01-01', 1, 145),
('9787100015707', '全球通史', '斯塔夫里阿诺斯', '北京大学出版社', '2006-10-01', 13, 88.00, 4, 3, '本书是斯塔夫里阿诺斯最重要的一部著作，分为《1500年以前的世界》和《1500年以后的世界》两册。', 'C区-02-01', 1, 112),
('9787111407010', '经济学原理（第7版）', '曼昆', '北京大学出版社', '2015-05-01', 14, 128.00, 5, 4, '经济学领域的经典入门教材，被称为经济学的"圣经"，以通俗易懂的语言和生动的案例讲解经济学原理。', 'D区-01-01', 1, 89),
('9787111544937', '算法导论（原书第3版）', 'Thomas H.Cormen', '机械工业出版社', '2013-01-01', 7, 128.00, 3, 2, '算法领域的经典教材，全面覆盖了算法理论的方方面面，是计算机专业学生和程序员的必读书籍。', 'A区-01-03', 1, 134),
('9787115279460', '你不知道的JavaScript', 'Kyle Simpson', '人民邮电出版社', '2015-04-01', 7, 49.00, 4, 3, '本书是JavaScript领域的经典著作，深入讲解了JavaScript语言的核心机制和容易被忽视的细节。', 'A区-01-04', 1, 76),
('9787111617631', '深入理解计算机系统', 'Randal E.Bryant', '机械工业出版社', '2016-11-01', 7, 139.00, 3, 2, '本书从程序员的视角详细阐述了计算机系统的本质概念，是理解计算机系统的经典教材。', 'A区-01-05', 1, 98),
('9787544253994', '解忧杂货店', '东野圭吾', '南海出版公司', '2014-05-01', 11, 39.50, 8, 6, '日本作家东野圭吾写作的长篇小说，讲述了在僻静街道旁的一家杂货店，只要写下烦恼投进店前门卷帘门的投信口，第二天就会在店后的牛奶箱里得到回答。', 'B区-02-02', 1, 234),
('9787544291170', '白夜行', '东野圭吾', '南海出版公司', '2013-01-01', 11, 42.00, 6, 4, '日本作家东野圭吾创作的长篇小说，也是其代表作，故事围绕着一对有着不同寻常情愫的小学生展开。', 'B区-02-03', 1, 189),
('9787108044754', '人类简史', '尤瓦尔·赫拉利', '中信出版社', '2014-11-01', 13, 68.00, 5, 3, '以色列新锐历史学家的一部重磅作品，从十万年前有生命迹象开始到21世纪资本、科技交织的人类发展史。', 'C区-02-02', 1, 267);

-- 借阅记录
INSERT INTO `borrow_record` (`user_id`, `book_id`, `borrow_time`, `due_time`, `return_time`, `status`, `renew_count`, `fine_amount`, `fine_paid`) VALUES
(2, 1, '2026-09-20 10:30:00', '2026-10-20 10:30:00', NULL, 'BORROWED', 0, 0.00, 0),
(2, 4, '2026-09-25 14:20:00', '2026-10-25 14:20:00', NULL, 'BORROWED', 1, 0.00, 0),
(3, 5, '2026-09-15 09:00:00', '2026-10-15 09:00:00', NULL, 'OVERDUE', 0, 10.50, 0),
(3, 13, '2026-09-10 16:45:00', '2026-10-10 16:45:00', '2026-10-08 11:30:00', 'RETURNED', 0, 0.00, 1),
(4, 15, '2026-09-28 11:15:00', '2026-10-28 11:15:00', NULL, 'BORROWED', 0, 0.00, 0);

-- 更新用户已借数量
UPDATE `sys_user` SET `borrowed_count` = 2 WHERE `id` = 2;
UPDATE `sys_user` SET `borrowed_count` = 1 WHERE `id` = 3;
UPDATE `sys_user` SET `borrowed_count` = 1 WHERE `id` = 4;

-- 更新图书可借数量
UPDATE `book` SET `available_count` = 3 WHERE `id` = 1;
UPDATE `book` SET `available_count` = 1 WHERE `id` = 4;
UPDATE `book` SET `available_count` = 7 WHERE `id` = 5;
UPDATE `book` SET `available_count` = 3 WHERE `id` = 15;

SELECT 'Database initialization completed!' AS result;
