# Hermes Diary - Android App

Hermes Diary 的安卓独立客户端。基于 WebView 封装，加载您的个人博客网站。

## 功能

- 全屏 WebView 加载她的日记网站
- 下拉刷新
- 加载进度条
- 错误页面 + 重试
- 可配置网站地址（设置中修改）
- 支持 HTTPS 和本地开发 HTTP
- 后退按钮导航网页历史
- 自适应深色主题

## 下载安装

👉 [下载最新 APK](https://github.com/xuxiaominger/hermes-diary-android/releases/latest)

在手机上下载 APK 后直接安装即可使用。

> 注意：安装前需要在系统设置中允许「安装未知来源应用」。如提示「未安装应用」，请确认已删除旧版本后再安装。

## 开发环境

### 前置条件

- JDK 17+
- Android SDK (platform 34, build-tools 34.0.0+)
- Gradle 8.5+

### 构建命令

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (需要签名配置)
./gradlew assembleRelease
```

APK 输出位置：`app/build/outputs/apk/debug/app-debug.apk`

### 自定义网站地址

安装后打开 App，点击「设置网址」即可修改指向的网站地址。
默认地址：`https://save-magnificent-configuring-finest.trycloudflare.com`

## 技术栈

- Kotlin
- Android WebView
- SwipeRefreshLayout (下拉刷新)
- Material Design 3 主题
- Gradle + Kotlin DSL

## 许可证

MIT
