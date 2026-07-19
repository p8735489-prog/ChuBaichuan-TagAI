# 本地图片反绘画提示词(Local Cue Word)

作者：楚百川

获取转换后的apk：
telegram：https://t.me/Local_Cue_Word
QQ：https://qm.qq.com/q/6J08LyN5Pq

一个可离线 输入图片反提示词的Android 应用。核心功能：
选图 → 本地 ONNX 模型推理 → 显示标签 → 复制标签/一键复制提示词 → 系统分享。
界面支持语言切换、莫奈取色（Material You 动态取色）开关、深色模式切换，
识别结果下方带实时提示词预览。

内置初始模型文件来源于https://huggingface.co/SmilingWolf/wd-convnext-tagger-v3
灵感来源
https://github.com/KuzuLabz/WaifuTagger

Local Cue Word并不是基于WaifuTagger的修改版本，Local Cue Word是独立开发的项目内核代码完全不同

## 你需要准备的环境

1. 安装 [Android Studio](https://developer.android.com/studio)（自带 JDK、Gradle、Android SDK）
2. 一部 Android 手机（或用 Android Studio 自带的模拟器），开启"开发者模式" + "USB 调试"

## 构建步骤

1. 用 Android Studio 打开 `waifutagger-cn` 这个文件夹（File → Open）
2. 首次打开会自动下载 Gradle 依赖，需要联网，等它跑完（右下角进度条）
3. 去 Hugging Face 下载一个 WD-tagger 系列的开源 ONNX 模型
   （具体说明见 `app/src/main/assets/PUT_MODEL_FILES_HERE.txt`），
   把 `model.onnx` 和 `selected_tags.csv` 两个文件放进
   `app/src/main/assets/` 目录
4. 手机用数据线连电脑，Android Studio 顶部选中你的设备，点绿色的 ▶️ 运行按钮
5. 第一次运行会在手机上装好并自动打开这个 App

## 已修复的两个问题

- **复制按钮**：原来的问题通常是构建了 `ClipData` 但没有调用
  `clipboard.setPrimaryClip(...)`，导致系统剪贴板没真正收到内容。
  这里 `copyTagsToClipboard()` 里已经调用了。
- **分享按钮**：原来的问题通常是 `Intent` 类型（MIME type）设置错误，
  或者构建了 `Intent` 但没有 `startActivity(...)`。
  这里 `shareTags()` 用的是标准 `ACTION_SEND` + `text/plain` + `createChooser`。

## 如果想改文案 / 加更多语言

界面文字都在 `app/src/main/res/values/strings.xml`，改这个文件里的内容就行，
不需要碰 Kotlin 代码。

## 不想在本地装 Android Studio?用 GitHub Actions 云端构建

工程里已经带了 `.github/workflows/build.yml`，推送到 GitHub 后会自动在云端
构建出 APK，不用你电脑装任何东西，模型文件也会在云端自动从 Hugging Face
下载，**你不需要自己上传 model.onnx**。

### 有电脑的话

```
git init
git add .
git commit -m "init"
git branch -M main
git remote add origin <你的仓库地址>
git push -u origin main
```

推送后去仓库的 "Actions" 标签页，等构建跑完，在 "Artifacts" 里下载
`WaifuTaggerCN-debug-apk`，解压就是 APK。

### 只有手机、没有电脑

在 Android 手机上装 **Termux**（一个终端 App，去 F-Droid 或 GitHub Releases
装，不是 Play 商店那个已停止维护的版本），然后在 Termux 里：

```
pkg install git
cd 存放这份代码的目录
git init
git add .
git commit -m "init"
git branch -M main
git remote add origin <你的仓库地址>
git push -u origin main
```

第一次 push 会要求登录 GitHub —— 建议提前在 GitHub 网页上生成一个
Personal Access Token（Settings → Developer settings → Personal access
tokens），push 时用户名填你的 GitHub 用户名，密码填这个 token。

推送成功后，用手机浏览器打开你的仓库页面 → "Actions" 标签页，等构建跑完，
在 "Artifacts" 里直接用手机浏览器下载编译好的 APK，安装即可。

全程不需要电脑，也不需要处理那个 395MB 的大模型文件。

## 已知限制

- 模型输入假设是 NHWC、RGB、0-255 float（常见的 WD-tagger ONNX 导出格式）。
  如果你用的模型预处理方式不同，需要改 `TaggerEngine.kt` 里的 `preprocess()`。
- 没有做多语言切换 UI，目前就是纯中文。
