# 素读

一款简洁优雅，专为阅读器制作的 Android RSS 阅读器，基于 Material 3 设计，支持多主题切换和后台自动同步。

## 功能特性

- **RSS 订阅管理**：轻松添加、编辑和删除 RSS 订阅源
- **文章阅读**：舒适的阅读体验，支持标记已读和书签功能
- **搜索功能**：快速查找已订阅的文章内容
- **后台同步**：自动定时同步订阅源，支持自定义同步间隔
- **主题切换**：支持跟随系统、浅色和深色三种主题模式
- **离线阅读**：文章内容本地存储，支持离线阅读

## 技术栈

- **UI 框架**：Jetpack Compose + Material 3
- **架构模式**：MVVM + Clean Architecture
- **依赖注入**：Hilt
- **本地存储**：Room + DataStore
- **网络请求**：Retrofit + OkHttp
- **图片加载**：Coil
- **内容解析**：Jsoup + Readability4J
- **后台任务**：WorkManager
- **导航**：Navigation Compose

## 项目结构

```
app/src/main/java/show/log/reader/
├── data/                    # 数据层
│   ├── db/                  # Room 数据库
│   ├── mapper/              # 数据映射器
│   ├── network/             # 网络请求
│   ├── parser/              # RSS 解析器
│   └── repository/          # 数据仓库实现
├── di/                      # Hilt 依赖注入模块
├── domain/                  # 领域层
│   └── model/               # 数据模型
├── ui/                      # 界面层
│   ├── articledetail/       # 文章详情
│   ├── common/              # 公共组件
│   ├── feeds/               # 订阅源管理
│   ├── home/                # 首页
│   ├── navigation/          # 导航路由
│   ├── search/              # 搜索
│   ├── settings/            # 设置
│   └── theme/               # 主题
├── util/                    # 工具类
└── worker/                  # 后台同步任务
```

## 构建与运行

### 环境要求

- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 36

### 构建步骤

1. 克隆项目
   ```bash
   git clone https://github.com/yourusername/Soodo.git
   ```

2. 打开 Android Studio，选择 "Open an existing project"

3. 等待 Gradle 同步完成

4. 连接 Android 设备或启动模拟器

5. 点击 "Run" 按钮或使用快捷键 `Shift + F10`

### 命令行构建

```bash
# 调试版本
./gradlew assembleDebug

# 发布版本（需要配置签名信息）
./gradlew assembleRelease
```

## 发布流程

项目使用 GitHub Actions 自动构建和发布：

1. 推送带有 `v` 前缀的标签（如 `v1.0.0`）
2. GitHub Actions 自动构建 Release APK
3. 自动创建 GitHub Release 并上传 APK

```bash
git tag v1.0.0
git push origin v1.0.0
```

## 配置说明

### 签名配置

发布版本需要配置以下环境变量：

- `KEYSTORE_PATH`：签名文件路径
- `KEYSTORE_PASSWORD`：签名密码
- `KEY_ALIAS`：密钥别名
- `KEY_PASSWORD`：密钥密码

### 同步设置

- **同步间隔**：支持 15 分钟、30 分钟、1 小时、3 小时或手动同步
- **主题模式**：跟随系统、浅色模式、深色模式

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证

## 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)
- [Hilt](https://dagger.dev/hilt/)
- [Room](https://developer.android.com/training/data-storage/room)
- [Retrofit](https://square.github.io/retrofit/)
- [Jsoup](https://jsoup.org/)
- [Readability4J](https://github.com/dankito/Readability4J)