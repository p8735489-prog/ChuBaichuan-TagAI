# GitHub Actions 自动化发布指南

本项目已在 `.github/workflows/release-apk.yml` 配置好 GitHub Actions 自动构建脚本。
推送到 GitHub 后，满足触发条件即会自动编译并发布已签名的 release APK。

## 1. 触发方式

| 触发方式 | 行为 |
| :--- | :--- |
| 推送 `v` 开头的标签（如 `v29.7`、`v1.0.0`） | 编译 release APK **并自动发布到 GitHub Releases** |
| 推送到 `main` 分支 | 仅编译并上传构建构件（CI 校验，不发布），用于提前发现编译问题 |
| 在 Actions 页面手动运行（workflow_dispatch） | 仅编译并上传构建构件，不发布 |

## 2. 签名说明（无需配置 Secrets）

release 构建类型已绑定项目内的**固定签名**：

- 密钥库：`app/chubaichuan-fixed-release.keystore`（已随仓库提交）
- 别名 / 密码等在 `app/build.gradle.kts` 的 `signingConfigs.fixedRelease` 中写死

因此执行 `./gradlew assembleRelease` 会**直接产出已签名 APK**，**不需要再配置任何 GitHub Secrets**。
直接推送标签即可触发发布。

> 如果你希望改用 Secrets 注入签名（更安全、不把 keystore 提交进仓库），可以：
> 1. 把 keystore 转 Base64 后填入 GitHub Secret `SIGNING_KEY`，并填 `ALIAS`、`KEY_STORE_PASSWORD`、`KEY_PASSWORD`；
> 2. 在 workflow 里加一步把 Base64 解码回 `app/chubaichuan-fixed-release.keystore`，再执行构建。
> 当前默认配置已经够用，普通使用无需改动。

## 3. 如何发布一个新版本

```bash
# 1. 确保改动已提交并推送到 main
git push origin main

# 2. 打一个 v 开头的标签并推送（标签名即版本号）
git tag v29.7
git push origin v29.7
```

3. 前往仓库的 **Actions** 选项卡查看编译进度。
4. 编译完成后，已签名的 APK（文件名形如 `LocalCueWord-v29.7.apk`）会自动出现在 **Releases** 页面：
   https://github.com/p8735489-prog/ChuBaichuan-TagAI/releases

## 4. 构建环境

- 运行环境：`ubuntu-latest`
- JDK：Temurin 17
- Gradle：使用项目自带的 wrapper（`gradle-wrapper.properties` 指定版本）
- 依赖缓存：由 `gradle/actions/setup-gradle` 自动处理

## 5. 排查构建失败

- 在 **Actions** 选项卡点开失败的运行，查看 `Build release APK` 步骤的完整日志。
- 常见问题：依赖下载超时（重跑即可）、SDK 组件缺失（runner 一般已自带 SDK 35）。
- 若只想验证能否编译、不想发布，可手动触发（workflow_dispatch）或推送到 `main`，构建产物会出现在该次运行的 Artifacts 中。
