# FotooFukuokaAirportAPP

旧手机改造：福冈机场离線航班显示板 + 电子相册轮播，全屏常亮，开机自启。

## 效果

```
┌──────────────────────────┬────────────┐
│   原生 ImageView          │  WebView   │
│   照片轮播 (1440px)        │  FIDS 时刻表│
│   5分钟渐变切换            │  960px     │
│   Shuffle 随机乱序        │  福冈机场离線│
│                          │  航班信息   │
└──────────────────────────┴────────────┘
          2400 × 1080 横屏，20:9
```

## 功能

- **左侧**：原生 ImageView，读取 `/BoardAlbum/` 目录图片，Shuffle 随机 + 5分钟渐变轮播
- **右侧**：WebView 加载 Vue 静态页面，显示福冈机场离線航班时刻表
- **全屏沉浸**：Sticky Immersive，隐藏状态栏和导航栏
- **屏幕常亮**：FLAG_KEEP_SCREEN_ON
- **强制横屏**：sensorLandscape，支持 180° 翻转
- **开机自启**：BootReceiver 监听 BOOT_COMPLETED
- **纯离线**：不联网，WebView 加载本地 assets

## 项目结构

```
app/
└── src/main/
    ├── AndroidManifest.xml          # 权限、横屏、开机自启
    ├── java/.../MainActivity.kt     # 全屏沉浸、相册轮播、WebView
    ├── java/.../BootReceiver.kt     # 开机自启广播
    ├── res/layout/activity_main.xml # ConstraintLayout 双栏布局
    ├── res/values/themes.xml        # AppCompat 全屏主题
    └── assets/fukuoka/              # Vue 静态项目（离線时刻表）
```

## 构建

Android Studio 打开项目 → Build → Make Project

或命令行：

```bash
./gradlew assembleDebug
```

APK 输出：`app/build/outputs/apk/debug/app-debug.apk`

## 使用

1. 将图片放入手机 `/BoardAlbum/` 目录
2. 安装 APK，首次启动授予存储权限
3. 如需开机自启，在系统设置中允许该 App 自启动

## 技术栈

- Kotlin + ConstraintLayout + AppCompat
- WebView + AndroidX WebKit
- Vue 3 + Vite（离线静态打包）
- minSdk 21, targetSdk 34

## License

MIT
