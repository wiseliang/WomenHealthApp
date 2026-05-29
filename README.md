# WomenHealth — 女性生理健康管理 App

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Comose-BOM%202025.01-blue.svg)](https://developer.android.com/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-green.svg)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

一款基于 Android 平台的女性生理健康管理应用，提供经期追踪、激素评估、分阶段个性化建议、AI 饮食记录和华为健康数据同步。

## 功能

| 功能 | 说明 |
|------|------|
| 经期追踪 | 日历视图记录经期，加权移动平均算法预测下次经期、排卵日和易孕窗口 |
| 激素评估 | 基于症状（基础体温、宫颈黏液、情绪、皮肤、睡眠）的雌激素/孕激素水平估计，附可信度评分 |
| 个性化建议 | 24 条基于周期相位和健身目标（减脂/减重/增肌）的饮食/运动/睡眠建议，全部标注公开医学文献来源 |
| AI 饮食记录 | CameraX 拍照 → ML Kit 食物识别 → 内置 20 种常见食物营养数据库 → 热量和宏量营养素自动估算 |
| 健康数据同步 | 华为健康（HMS Health Kit）步数、体重数据同步，WorkManager 定时后台拉取 |
| 数据导出 | CSV 格式导出经期记录、症状数据和饮食记录，通过系统分享功能发送 |

## 技术栈

| 层 | 技术 |
|---|---|
| 语言 | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Clean Architecture（多层模块化） |
| 导航 | Navigation Compose（类型安全路由） |
| DI | Hilt |
| 本地存储 | Room + DataStore |
| 网络 | Retrofit + Kotlinx Serialization |
| 相机 | CameraX |
| AI | ML Kit Food Detection（设备端识别） |
| 健康 | HMS Health Kit |
| 后台任务 | WorkManager |
| 测试 | JUnit 5 + Turbine |

## 项目结构

```
WomenHealthApp/
├── app/                            # Application、MainActivity、导航、DI
├── core/
│   ├── model/                      # 纯 Kotlin 领域模型（零 Android 依赖）
│   ├── database/                   # Room Entity、DAO、AppDatabase
│   ├── network/                    # Retrofit 网络层
│   ├── datastore/                  # Preferences DataStore
│   └── common/                     # 工具类、数据导出
├── feature/
│   ├── cycle/                      # 经期追踪 + 预测算法
│   ├── hormone/                    # 激素评估 + 症状记录
│   ├── recommendation/             # 分阶段建议 + 文献引用
│   ├── diet/                       # 拍照记录饮食 + 热量评估
│   ├── healthsync/                 # 华为健康数据同步
│   ├── profile/                    # 用户资料 + 设置
│   └── onboarding/                 # 首次启动引导
└── gradle/
    └── libs.versions.toml          # 统一版本管理
```

## 快速开始

### 环境要求

- Android Studio Hedgehog (2024.1+) 或更新版本
- JDK 17
- Android SDK 35
- Gradle 8.7+

### 构建步骤

```bash
# 1. 克隆仓库
git clone https://github.com/<your-username>/WomenHealthApp.git
cd WomenHealthApp

# 2. 用 Android Studio 打开项目目录
# File → Open → 选择 WomenHealthApp/ 目录

# 3. 等待 Gradle Sync 完成，选择目标设备，点击 Run
```

或在命令行构建：
```bash
./gradlew assembleDebug
```

### 华为健康集成（可选）

1. 在 [AppGallery Connect](https://developer.huawei.com/consumer/cn/service/josp/agc/index.html) 注册应用
2. 下载 `agconnect-services.json` 放入 `app/` 目录
3. 将 `feature/healthsync/build.gradle.kts` 中 `compileOnly(libs.hms.health)` 改为 `implementation(libs.hms.health)`
4. 仅华为设备支持；非华为设备自动使用模拟数据

### 运行测试

```bash
./gradlew :feature:cycle:test          # 经期预测算法测试
./gradlew :feature:hormone:test        # 激素评估算法测试
```

## 数据来源

所有健康建议均标注公开医学文献来源，存储在 `core/database/src/main/assets/citations.json`。用户在应用内点击引用标签即可查看完整的文献信息（标题、作者、期刊、PMID/DOI）。

## License

MIT License
