# 固定签名说明

本项目已经固定 release APK 的应用签名，用于保证后续版本可以覆盖安装旧版本，避免出现“签名不一致，需要卸载旧版”的问题。

## 当前签名方式

当前不是把 keystore 二进制内容写进代码里，而是：

1. 固定使用项目内的 keystore 文件
2. 在 `app/build.gradle.kts` 中写死 release 签名配置
3. 在 GitHub Actions 中校验最终 APK 的签名指纹

固定 keystore 文件路径：

```text
app/chubaichuan-fixed-release.keystore
```

固定 release 签名配置：

```kotlin
signingConfigs {
    create("fixedRelease") {
        storeFile = file("chubaichuan-fixed-release.keystore")
        storePassword = "chubaichuan_fixed_2026"
        keyAlias = "chubaichuan-fixed-release"
        keyPassword = "chubaichuan_fixed_2026"
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("fixedRelease")
    }
}
```

只要构建时执行：

```bash
./gradlew assembleRelease
```

Gradle 就会使用 `app/chubaichuan-fixed-release.keystore` 签名 release APK。

## 禁止修改

为了保持同签名覆盖安装，禁止修改以下内容：

- 不要删除 `app/chubaichuan-fixed-release.keystore`
- 不要替换 `app/chubaichuan-fixed-release.keystore`
- 不要重新生成 keystore
- 不要修改 `storeFile`
- 不要修改 `storePassword`
- 不要修改 `keyAlias`
- 不要修改 `keyPassword`
- 不要把 release 包改成 debug 签名
- 不要修改 `applicationId`

当前应用包名：

```text
com.kuzulabz.waifutaggercn
```

允许修改：

- 可以修改 APP 代码
- 可以修改资源文件
- 可以修改 UI
- 可以修改 `versionName`
- 可以递增 `versionCode`

注意：`versionCode` 必须递增，不能比已发布版本小。

## 当前签名指纹

最终 release APK 的签名 SHA256 必须是：

```text
DF:6B:9A:AE:02:74:DE:0F:DC:5C:7C:03:32:39:1B:15:2E:6F:FB:92:4E:52:D6:C9:B0:61:C7:77:A3:41:39:3F
```

SHA1：

```text
1D:5D:EF:2B:71:75:C4:09:2B:D1:85:73:09:5A:0B:66:43:ED:47:B5
```

如果最终 APK 的 SHA256 不是上面的值，就不是同签名 APK，不能覆盖安装旧版本。

## 构建后验证

可以使用下面命令检查最终 APK 签名：

```bash
apksigner verify --print-certs app-release.apk
```

需要确认输出中的：

```text
Signer #1 certificate SHA-256 digest
```

等于：

```text
DF:6B:9A:AE:02:74:DE:0F:DC:5C:7C:03:32:39:1B:15:2E:6F:FB:92:4E:52:D6:C9:B0:61:C7:77:A3:41:39:3F
```

## GitHub Actions 校验

项目的 `.github/workflows/build.yml` 已经加入签名校验步骤：

```text
Verify fixed release signature
```

如果构建出来的 release APK 签名不等于固定 SHA256，工作流会直接失败，不会继续上传或发布错误签名的 APK。

## 给其他 AI 或打包平台的说明

如果把本项目交给其他 AI 或打包平台修改，请明确告诉对方：

```text
本项目 release 签名已固定。
必须保留 app/chubaichuan-fixed-release.keystore。
必须保留 app/build.gradle.kts 中 fixedRelease 签名配置。
必须执行 ./gradlew assembleRelease 构建 release APK。
不能重新生成 keystore，不能使用 debug 签名，不能平台重签名。
最终 APK 的 SHA256 签名必须是：
DF:6B:9A:AE:02:74:DE:0F:DC:5C:7C:03:32:39:1B:15:2E:6F:FB:92:4E:52:D6:C9:B0:61:C7:77:A3:41:39:3F
```

