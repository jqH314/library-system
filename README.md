<div align="center">

# 📚 在线图书借阅管理系统

**基于 Spring Boot + Vue.js + 微信小程序的全栈图书借阅管理平台**

[![Java](https://img.shields.io/badge/JDK-21-orange?style=flat-square)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-green?style=flat-square)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-blue?style=flat-square)](https://vuejs.org/)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.5-red?style=flat-square)](https://baomidou.com/)
[![Redis](https://img.shields.io/badge/Redis-5.0+-dc382d?style=flat-square)](https://redis.io/)
[![License](https://img.shields.io/badge/license-MIT-yellow?style=flat-square)](#许可证)

[功能特性](#-功能特性) · [技术架构](#-技术架构) · [技术亮点](#-技术亮点) · [快速开始](#-快速开始) · [API文档](#-api文档)

</div>

---

## 📖 项目简介

本系统是一个功能完善的在线图书借阅管理平台，采用 **B/S 架构 + 前后端分离** 设计，同时支持 **Web 管理后台** 与 **微信小程序端** 双端访问。系统实现了图书信息管理、多条件检索、借阅归还、逾期罚款、数据统计可视化等核心功能，并针对高频借阅场景下的库存并发控制、图书热榜查询性能优化等技术难点给出了完整解决方案。

系统采用 **管理员与普通读者双角色权限模型**，不同角色拥有差异化的操作界面与功能权限，确保系统安全性与业务隔离性。

---

## ✨ 功能特性

### 🔧 管理员端（Web 管理后台）

| 模块 | 功能说明 |
|------|----------|
| 📊 **数据仪表盘** | 图书/借阅/读者/逾期多维度统计，ECharts 借阅趋势折线图、分类占比饼图、热门借阅排行榜 |
| 📚 **图书管理** | 图书 CRUD、上下架管理、ISBN/书名/作者多条件模糊检索、分类联动筛选 |
| 📖 **借阅管理** | 全量借阅记录查询、办理归还/续借、逾期罚款缴纳、状态筛选（借阅中/已归还/已逾期） |
| 👥 **读者管理** | 读者 CRUD、账号禁用/启用、密码重置、最大借阅数量调整 |
| 🗂️ **分类管理** | 二级分类树、分类 CRUD、排序管理 |
| 👤 **个人中心** | 个人资料编辑、密码修改、我的借阅记录 |

### 📱 读者端（Web + 微信小程序）

| 模块 | 功能说明 |
|------|----------|
| 🔍 **图书检索** | 关键词模糊搜索、分类筛选、热门推荐、新书上架 |
| 📖 **在线借阅** | 图书详情查看、一键借阅、借阅期限提示 |
| 🔄 **续借管理** | 借阅中图书续借（最多2次，每次延长30天） |
| 💰 **罚款缴纳** | 逾期罚款在线缴纳、罚款状态追踪 |
| 📋 **我的借阅** | 借阅记录多状态筛选、逾期高亮提醒 |
| ⚙️ **个人设置** | 资料编辑、密码修改 |

### 🛡️ 系统级特性

- **JWT 无状态认证**：基于 Token 的跨端身份认证，支持 Web 与小程序统一登录
- **双角色权限控制**：路由级 + 接口级双重权限校验，读者无法越权访问管理功能
- **借阅资格校验**：逾期未还或存在未缴罚款时禁止借阅新书
- **操作日志记录**：关键操作审计追踪

---

## 🏗️ 技术架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                           客户端层                                │
│  ┌──────────────────────┐          ┌──────────────────────┐     │
│  │   Web 管理后台        │          │   微信小程序端        │     │
│  │   Vue3 + Element Plus │          │   原生小程序          │     │
│  └──────────┬───────────┘          └──────────┬───────────┘     │
└─────────────┼───────────────────────────────────┼─────────────────┘
              │          RESTful API               │
              ▼                                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                        网关 / Nginx (可选)                        │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                       应用层 (Spring Boot)                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐  │
│  │ 认证授权  │ │ 业务逻辑  │ │ 数据访问  │ │  全局异常处理     │  │
│  │ JWT+拦截器│ │ Service  │ │MyBatisPlus│ │  统一响应封装     │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘  │
└──────┬───────────────┬───────────────────────┬──────────────────┘
       │               │                       │
       ▼               ▼                       ▼
┌─────────────┐ ┌─────────────┐      ┌─────────────────┐
│   MySQL     │ │    Redis    │      │   Redisson      │
│  数据持久化  │ │  热榜缓存   │      │  分布式锁       │
│  (H2开发用)  │ │  会话缓存   │      │  库存并发控制   │
└─────────────┘ └─────────────┘      └─────────────────┘
```

### 技术栈详情

| 层级 | 技术选型 | 版本 | 用途 |
|------|----------|------|------|
| **后端框架** | Spring Boot | 2.7.18 | 应用基础框架 |
| **ORM 框架** | MyBatis Plus | 3.5.5 | 数据访问、自动填充、分页插件 |
| **数据库** | MySQL / H2 | 8.0+ / - | 生产环境 / 开发环境 |
| **缓存** | Redis | 5.0+ | 图书热榜数据缓存 |
| **分布式锁** | Redisson | 3.20.0 | 借阅库存并发控制 |
| **认证** | JWT (jjwt) | 0.12.5 | 无状态身份认证 |
| **安全** | Spring Security Crypto | - | BCrypt 密码加密 |
| **构建工具** | Maven | 3.9.6 | 项目构建与依赖管理 |
| **JDK** | Java | 21 | 运行环境 |
| **前端框架** | Vue.js | 3.x | 响应式前端框架 |
| **UI 组件库** | Element Plus | - | 管理后台 UI 组件 |
| **图表库** | ECharts | - | 数据可视化 |
| **HTTP 客户端** | Axios | - | 前后端数据交互 |
| **构建工具** | Vite | - | 前端构建与热更新 |
| **移动端** | 微信小程序 | 原生 | 读者移动端 |

---

## 🌟 技术亮点

### 1. 高频借阅场景下的库存并发控制

**问题场景**：新学期教材借阅高峰期，多个读者同时提交同一本热门图书的借阅请求，可能导致库存被减至负数（超借现象）。

**解决方案**：采用 **Redis 分布式锁 + 数据库乐观锁** 双重保障机制：

- **Redisson 分布式锁**：以 `borrow:book:{bookId}` 为锁 key，将同一本图书的并发借阅请求串行化处理，锁等待时间5秒，自动释放时间10秒
- **库存原子扣减**：在分布式锁保护下执行库存扣减，确保数据一致性
- **业务校验完整性**：在锁内完成最大借阅数、逾期校验、重复借阅、库存充足性等全部前置校验

```java
// 核心代码片段
String lockKey = "borrow:book:" + bookId;
RLock lock = redissonClient.getLock(lockKey);
try {
    if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
        throw new BusinessException("系统繁忙，请稍后重试");
    }
    // 业务校验 + 库存扣减 + 借阅记录创建（原子操作）
} finally {
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

### 2. 图书热榜的高效查询与缓存策略

**问题场景**：图书热门排行榜是高频查询接口，若每次都从数据库 `GROUP BY` 聚合统计，会对数据库造成较大压力。

**解决方案**：采用 **Redis 缓存 + 借阅量实时累加** 策略：

- **缓存热榜数据**：热门图书查询结果存入 Redis，设置合理过期时间
- **借阅量实时更新**：每次借阅成功时，对应图书的 `borrow_count` 字段原子 +1，保证排行榜数据实时性
- **数据库索引优化**：在借阅记录表为 `book_id` 和 `borrow_time` 建立复合索引，加速统计查询

### 3. 双角色权限体系设计

**设计思路**：采用 **路由级 + 接口级 + 数据级** 三层权限控制：

- **路由级**：前端路由配置 `meta.roles`，路由守卫根据用户角色拦截未授权页面访问，自动重定向
- **接口级**：JWT 拦截器对受保护接口进行身份认证，管理员专属接口通过角色校验
- **数据级**：读者只能查询自己的借阅记录（`/borrow/my`），管理员可查询全量记录（`/borrow/admin/page`）
- **菜单动态渲染**：侧边栏菜单根据用户角色动态生成，读者端仅展示「图书借阅、我的借阅、个人信息」

### 4. 借阅业务规则引擎

**完整的借阅生命周期管理**：

- **借阅资格校验链**：最大借阅数 → 逾期未还检查 → 未缴罚款检查 → 图书状态 → 库存检查 → 重复借阅检查
- **逾期自动判定**：归还时根据当前时间与应还时间自动计算逾期天数，按 `0.5元/天` 规则计算罚款金额
- **续借规则约束**：仅借阅中图书可续借，逾期图书禁止续借，单本最多续借2次
- **罚款状态追踪**：`fine_amount`（罚款金额）+ `fine_paid`（是否缴纳）双字段管理，支持已归还但未缴罚款的场景

### 5. 数据库自动填充与审计

- **MyBatis Plus 自动填充**：`create_time`、`update_time` 字段通过 `MetaObjectHandler` 自动填充，无需业务代码手动维护
- **逻辑删除与状态管理**：图书采用 `status` 字段软删除（上架/下架），避免物理删除导致借阅记录关联异常
- **操作日志**：关键操作记录到 `operation_log` 表，支持审计追踪

---

## 📁 项目结构

```
library-system/
├── database/
│   └── init.sql                          # MySQL 数据库初始化脚本（6张表+测试数据）
├── backend/                               # 后端服务（Spring Boot）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/library/
│       │   ├── LibraryApplication.java           # 启动类
│       │   ├── common/                            # 通用组件
│       │   │   ├── Result.java                   # 统一响应封装
│       │   │   ├── JwtUtil.java                  # JWT 工具类
│       │   │   ├── UserContext.java              # 当前用户上下文（ThreadLocal）
│       │   │   ├── BusinessException.java        # 业务异常
│       │   │   └── GlobalExceptionHandler.java   # 全局异常处理器
│       │   ├── config/                            # 配置类
│       │   │   ├── WebConfig.java                # Web 配置（拦截器注册）
│       │   │   ├── MybatisPlusConfig.java        # MyBatis Plus 配置（分页插件）
│       │   │   ├── RedisConfig.java              # Redis 配置（序列化）
│       │   │   ├── RedissonConfig.java           # Redisson 分布式锁配置
│       │   │   ├── DataInitializer.java          # 启动数据初始化
│       │   │   └── MyMetaObjectHandler.java      # 自动填充处理器
│       │   ├── controller/                        # 控制层（5个模块）
│       │   ├── service/                           # 服务层（业务逻辑）
│       │   ├── mapper/                            # 数据访问层
│       │   ├── entity/                            # 实体类
│       │   ├── dto/                               # 数据传输对象
│       │   └── interceptor/                       # JWT 认证拦截器
│       └── resources/
│           ├── application.yml                     # 主配置
│           ├── application-h2.yml                  # H2 开发环境配置
│           └── db/
│               ├── schema-h2.sql                   # H2 建表脚本
│               └── data-h2.sql                     # H2 测试数据
├── frontend/                              # Web 管理后台（Vue 3）
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js / App.vue
│       ├── router/index.js                   # 路由（含角色权限守卫）
│       ├── api/request.js                    # Axios 封装（请求/响应拦截器）
│       ├── layout/Layout.vue                 # 布局组件（动态菜单）
│       └── views/
│           ├── Login.vue                      # 登录页
│           ├── Dashboard.vue                  # 仪表盘（ECharts 图表）
│           ├── Books.vue                      # 图书管理
│           ├── Borrow.vue                     # 借阅管理
│           ├── Users.vue                      # 读者管理
│           ├── Categories.vue                 # 分类管理
│           ├── BookBrowse.vue                 # 读者图书借阅
│           ├── BookDetail.vue                 # 图书详情
│           ├── Profile.vue                    # 我的借阅
│           └── Settings.vue                   # 个人信息/设置
└── miniprogram/                           # 微信小程序端
    ├── app.js / app.json / app.wxss
    └── pages/                                # 6个页面（首页/搜索/详情/借阅/我的/登录）
```

---

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- Node.js 16+
- Redis 5.0+
- MySQL 8.0+（生产环境，开发环境可用 H2 内存数据库）

### 1. 克隆项目

```bash
git clone <仓库地址>
cd library-system
```

### 2. 启动 Redis

```bash
redis-server
```

### 3. 启动后端（开发模式，使用 H2 内存数据库）

```bash
cd backend
mvn spring-boot:run
```

后端启动成功后，接口地址：`http://localhost:8080/api`

> H2 数据库控制台：`http://localhost:8080/api/h2-console`（JDBC URL: `jdbc:h2:mem:library`，用户名: `sa`，密码空）

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 `http://localhost:3000`

### 5. 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 读者 | 2023001 | 123456 |

### 切换到 MySQL

1. 创建数据库并执行 `database/init.sql`
2. 创建 `application-mysql.yml` 配置文件
3. 修改 `application.yml` 中 `spring.profiles.active: mysql`
4. 重启后端

---

## 📊 数据库设计

### 数据表（6张）

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `sys_user` | 用户表 | username, password(BCrypt), real_name, role(ADMIN/READER), status, max_borrow_count, borrowed_count |
| `book` | 图书表 | isbn, name, author, publisher, category_id, price, total_count, available_count, borrow_count, status, location, description |
| `book_category` | 分类表 | name, code, parent_id(支持二级), sort, status |
| `borrow_record` | 借阅记录表 | user_id, book_id, borrow_time, due_time, return_time, status(BORROWED/RETURNED/OVERDUE), renew_count, fine_amount, fine_paid |
| `fine_record` | 罚款记录表 | user_id, record_id, amount, pay_time, status |
| `operation_log` | 操作日志表 | user_id, operation, method, params, ip, create_time |

### 借阅规则配置

| 规则项 | 默认值 | 配置项 |
|--------|--------|--------|
| 最大借阅数量 | 5本 | `borrow.max-borrow-count` |
| 借阅期限 | 30天 | `borrow.max-days` |
| 最大续借次数 | 2次 | `borrow.max-renew-count` |
| 逾期罚款 | 0.5元/天 | `borrow.fine-per-day` |

---

## 📡 API 文档

### 接口概览

| 模块 | 接口数 | 认证要求 |
|------|--------|----------|
| 认证模块 | 2 | 公开 |
| 图书模块 | 9 | 公开查询 / 管理员操作 |
| 借阅模块 | 8 | 登录用户 / 管理员 |
| 用户模块 | 9 | 登录用户 / 管理员 |
| 分类模块 | 5 | 公开查询 / 管理员操作 |

### 核心接口示例

**登录**
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**借阅图书**（需登录）
```
POST /api/borrow/{bookId}
Authorization: Bearer {token}
```

**分页查询图书**（公开）
```
GET /api/books/page?page=1&size=10&keyword=Java&categoryId=1&status=1
```

> 完整接口文档请参考 `完整操作流程.md`

---

## 📝 开发计划

### ✅ 已完成

- [x] 后端基础框架搭建（Spring Boot + MyBatis Plus + Redis + Redisson）
- [x] 数据库设计与初始化脚本（6张表 + 测试数据）
- [x] JWT 认证与双角色权限控制
- [x] 图书管理模块（CRUD、上下架、多条件检索）
- [x] 借阅管理模块（借阅、归还、续借、逾期罚款计算）
- [x] 借阅资格校验（逾期/欠款限制借阅）
- [x] 用户管理模块（读者管理、禁用/启用、密码重置）
- [x] 分类管理模块（二级分类树）
- [x] 数据统计与可视化（仪表盘 ECharts 图表）
- [x] 前端管理后台（Vue 3 + Element Plus，8个页面）
- [x] 读者端 Web 界面（图书借阅、我的借阅、个人信息）
- [x] 微信小程序端基础页面（6个页面）
- [x] 库存并发控制（Redisson 分布式锁）
- [x] 图书热榜 Redis 缓存

### 🔄 待优化

- [ ] 小程序端完整功能对接与联调
- [ ] 图书封面图片上传功能
- [ ] Excel 批量导入导出图书数据
- [ ] 逾期催还通知（微信模板消息）
- [ ] 操作日志管理后台页面
- [ ] 图书推荐算法（基于借阅历史）

---

## 👤 作者信息

- **姓名**：何佳奇
- **学校**：韶关学院 · 信息工程学院
- **专业**：计算机科学与技术
- **指导教师**：谢莉 副教授
- **项目类型**：本科毕业设计

---

## 📄 许可证

本项目仅用于毕业设计学习与技术交流用途。

---

<div align="center">

**如果这个项目对你有帮助，欢迎给个 Star ⭐**

</div>
