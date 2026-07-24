# GitHub Actions 自动化发布指南

我已经为你配置好了 GitHub Actions 脚本。当你将代码上传到 GitHub 并推送一个以 `v` 开头的标签（例如 `v1.0.0`）时，系统会自动编译并发布修复后的 APK。

## 1. 配置 GitHub Secrets

为了让自动化脚本能够正常签名 APK，你需要在 GitHub 仓库的 **Settings -> Secrets and variables -> Actions** 中添加以下 Secrets：

| Secret 名称 | 说明 |
| :--- | :--- |
| `SIGNING_KEY` | 签名文件的 Base64 编码字符串（见下文获取方法） |
| `ALIAS` | 签名别名：`chubaichuan-fixed-release` |
| `KEY_STORE_PASSWORD` | 密钥库密码：`chubaichuan_fixed_2026` |
| `KEY_PASSWORD` | 密钥密码：`chubaichuan_fixed_2026` |

## 2. 如何获取 SIGNING_KEY (Base64)

在你的本地电脑上，使用以下命令将项目中的 `app/chubaichuan-fixed-release.keystore` 转换为 Base64 字符串：

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("app/chubaichuan-fixed-release.keystore"))
```

**macOS / Linux:**
```bash
base64 -i app/chubaichuan-fixed-release.keystore | tr -d '\n'
```

将输出的长字符串复制并粘贴到 GitHub 的 `SIGNING_KEY` Secret 中。

## 3. 如何触发发布

1. 将代码上传到 GitHub。
2. 在本地或 GitHub 页面创建一个新标签并推送：
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. 前往 GitHub 仓库的 **Actions** 选项卡，查看编译进度。
4. 编译完成后，APK 会自动出现在 **Releases** 页面中。
