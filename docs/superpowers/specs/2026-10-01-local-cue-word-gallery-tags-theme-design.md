# Local Cue Word Gallery, Tag Translation & Theme Redesign

## Goal
完善 Local Cue Word 3.6.4 的图片图库/收藏夹交互、Tag 完整显示与复制、翻译持久化及分享兼容性，并统一 Monet 关闭后的黑色回退主题，同时修复浅色/深色模式前景色对比错误。

## Scope
- 首页标题区视觉间距。
- 历史/收藏图库浏览与全屏图片查看交互。
- TagRecord 数据与旧数据兼容迁移。
- Tag、反向 Tag、翻译的独立显示与独立复制。
- 翻译结果持久化并进入分享文件，导入后可继续查看。
- 关闭 Monet 动态取色时直接采用黑色回退主题；隐藏独立 Black 预设入口。
- 修复所有主要 Material 3 `on*` 角色与实际容器背景不匹配导致的黑字/白字反转。

## Non-Goals
- 不修改模型推理算法、模型文件格式、ONNX runtime 或模型下载链路。
- 不改变 Local Cue Word 的 applicationId、签名配置或版本号。
- 不删除已有旧版 `localcueword://tags` 文本链接兼容能力。

## Design

### 1. 首页标题层级
标题 `Local Cue Word` 与副标题 `亲爱真诚` 保持原内容，在副标题与主图片之间增加固定的视觉间距；标题区整体使用内容驱动的 Column，而不是依赖图片顶部对齐来制造间距。图片尺寸与圆角保持原视觉方向，避免为了增加间距而压缩主图片。

### 2. 图片图库
现有 `TagRecordDialog` 改为以图片为主要视觉内容的两列网格，继续支持：点击打开、长按进入多选、批量收藏/分享/删除。无图片记录使用一致的占位卡片。

全屏查看器继续复用 `ZoomableImageOverlay`：双指缩放、拖动、缩放重置、左右切换；顶部操作保持悬浮在查看器上层。详情操作增加“标签详情”入口，用于查看完整正向 Tag、完整反向 Tag、翻译以及独立复制操作。查看器中的任何列表切换都不能丢失翻译数据。

### 3. TagRecord 数据模型
`TagRecord` 增加结构化字段：
- `negativeText: String = ""`
- `translations: Map<String, String> = emptyMap()`，key 为目标语言代码，value 为完整翻译文本。
- `tagTranslations: Map<String, String> = emptyMap()`，key 为原始 tag，value 为该 tag 的译文。

旧版 SharedPreferences JSON 没有这些字段时使用默认值，不影响旧记录读取。

保存/收藏历史时需要把当前记录的 positive、negative 与已存在翻译一起写入。已有仅有 `text + imagePath` 的记录继续可使用。

### 4. Tag 完整显示与复制
UI 不再对完整 Tag 字符串使用 `maxLines + TextOverflow.Ellipsis`。长文本使用可换行文本容器并允许滚动查看。

复制操作拆分为：
- 复制 Tag：完整原始正向 Tag。
- 复制反向 Tag：完整反向 Tag。
- 复制翻译：当前目标语言下完整翻译。
- 详情页中的单个 Tag 行还可以单独复制该 Tag 或该 Tag 对应译文。

复制使用数据源中的完整字符串，不使用 UI 截断后的文本。

### 5. 翻译流程
首次翻译继续使用 App 已有翻译 API/网络流程。翻译完成后，同时更新 UI state 与 `TagRecord.tagTranslations` / `TagRecord.translations`，并写回当前历史/收藏记录。

已存在翻译时直接读取持久化译文，不重复调用 API。没有翻译时显示 `暂未翻译`。对当前记录重新翻译时覆盖指定目标语言下的旧译文。

批量识别产生历史记录时，保存推理结果即可；翻译发生后再增量更新记录。

### 6. 分享文件
现有 `SharedPayload` 增加：
- `translations: JSONObject` / `Map<String,String>`
- `tagTranslations: JSONObject` / `Map<String,String>`

分享文件 schema 升级到 2，同时读取 schema 1。schema 1 导入时字段缺失按空 map 处理。

导入分享文件后，解析出的 `TagRecord` 保留图片、positive、negative 与翻译字段；分享文件没有翻译时详情页显示 `暂未翻译`，并允许再次调用 App 内翻译 API。

### 7. 黑色回退主题
设置页面隐藏 `black` 独立主题预设的可选入口，但保留其颜色定义作为关闭 Monet 时的固定回退 scheme。

规则：
- `useDynamicColor == true` 且采用设备 Monet：继续动态取色。
- 关闭 Monet 或系统不支持动态取色：直接使用 `LightMonetBlack/DarkMonetBlack`。
- 不允许再回退到旧蓝色主题。

主题颜色统一通过实际容器亮度推导 `on*` 角色；至少覆盖 `onBackground`, `onSurface`, `onSurfaceVariant`, `onPrimary`, `onPrimaryContainer`, `onSecondary`, `onSecondaryContainer`, `onTertiary`, `onTertiaryContainer`, `onError`, `onErrorContainer`, `inverseOnSurface`。不要用 `dark` 布尔值一刀切推导全部前景色。

### 8. Compatibility
旧版记录 JSON 读取时未知字段忽略；旧版分享 schema 1 正常导入；旧版文本链接正常导入。保存后的新 JSON 应稳定 round-trip：save -> load -> save 后数据字段不丢失。

## Testing Strategy
由于工程当前没有 `app/src/test`，新增 JVM 单元测试覆盖：
1. TagRecord JSON 旧数据读取、新字段读取与 round-trip。
2. Tag 完整复制数据源不发生 UI 截断。
3. 分享 schema 1/2 的解析与翻译字段保留。
4. 无翻译时返回 `暂未翻译` 状态，已有翻译优先使用持久化值。
5. 黑色回退主题在亮/暗模式下主要 `on*` 角色与容器亮度方向一致。

构建验证：`./gradlew test`、`./gradlew assembleDebug`、`./gradlew assembleRelease`。
