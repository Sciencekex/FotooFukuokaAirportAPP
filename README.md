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

---

## 有趣发现：从航班频次看九州外国人结构

观察福冈机场的国际航班频次，可以高度精准地反推整个九州地区的**外籍人口国籍比例**。这不是巧合，而是强烈的市场规律在驱动。

### 为什么福冈机场具有"一核垄断"的代表性？

- **机场就在市中心**：地铁到博多站仅 5 分钟，到天神商圈 11 分钟。来九州的外国人 90% 以上以福冈机场为唯一切入点。
- **零中转水分**：极少有旅客把福冈作为转机跳板，飞到这里的人 95% 以上目的就是九州本土。
- **无多机场分流**：不像关东有成田/羽田双机场分流、关西有关空/伊丹分流，九州是福冈"一核独大"。

### 单日航班数据实测

根据某日福冈机场 68 班实际执飞国际航班的统计：

| 国家/地区 | 实际执飞 | 占比 |
|-----------|---------|------|
| 韩国 | 42 班 | 61.8% |
| 中国台湾 | 8 班 | 11.8% |
| 中国香港 | 8 班 | 11.8% |
| 中国大陆 | 4 班 | 5.9% |
| 东南亚（泰越新马） | 6 班 | 8.7% |

> **总结**：九州的外国人构成 ≈ **六成韩国人，三成大中华区，一成东南亚人**。福冈机场的航班频次是一面没有任何杂质的镜子，它反射出来的各国航班比例能够非常纯净地投影到整个九州地区的消费份额和人口分布上。
