# 固定签名说明

本项目已经把 release APK 的签名配置固定在项目文件内。

请注意：这里不是把 keystore 二进制内容写进代码，而是固定使用项目内的 keystore 文件，并在 Gradle 配置里写死签名信息。

## 固定 keystore 文件

签名文件必须保留在：

```text
app/chubaichuan-fixed-release.keystore
```

这个文件不能删除、不能替换、不能重新生成。

## 已写死的签名配置

`app/build.gradle.kts` 中必须保持以下配置：

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

只要构建平台执行：

```bash
./gradlew assembleRelease
```

release APK 就会使用这个固定签名。

## 禁止修改

为了保证新版 APK 可以覆盖安装旧版本，禁止修改：

- `app/chubaichuan-fixed-release.keystore`
- `storeFile`
- `storePassword`
- `keyAlias`
- `keyPassword`
- `applicationId`
- `release` 的 `signingConfig`

也不要改成 debug 签名，不要让打包平台重新签名。

当前包名必须保持：

```text
com.kuzulabz.waifutaggercn
```

## 当前签名指纹

最终 APK 的 SHA256 签名必须是：

```text
DF:6B:9A:AE:02:74:DE:0F:DC:5C:7C:03:32:39:1B:15:2E:6F:FB:92:4E:52:D6:C9:B0:61:C7:77:A3:41:39:3F
```

SHA1：

```text
1D:5D:EF:2B:71:75:C4:09:2B:D1:85:73:09:5A:0B:66:43:ED:47:B5
```

## 验证 APK 签名

构建完成后可以用：

```bash
apksigner verify --print-certs app-release.apk
```

确认输出中的 SHA256 等于：

```text
DF:6B:9A:AE:02:74:DE:0F:DC:5C:7C:03:32:39:1B:15:2E:6F:FB:92:4E:52:D6:C9:B0:61:C7:77:A3:41:39:3F
```

如果不是这个值，就不是同签名 APK，不能覆盖安装旧版本。

## 给其他 AI 或平台的话

```text
本项目签名已经固定。
请保留 app/chubaichuan-fixed-release.keystore。
请保留 app/build.gradle.kts 中 fixedRelease 签名配置。
请执行 ./gradlew assembleRelease 构建 release APK。
不要重新生成 keystore，不要使用 debug 签名，不要平台重签名。
最终 APK 的 SHA256 必须是：
DF:6B:9A:AE:02:74:DE:0F:DC:5C:7C:03:32:39:1B:15:2E:6F:FB:92:4E:52:D6:C9:B0:61:C7:77:A3:41:39:3F
```

