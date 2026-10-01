# Local Cue Word Gallery, Tags, and Theme Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Local Cue Word's gallery/detail/tag/share/theme flows match the approved design without breaking existing recognition and model flows.

**Architecture:** Keep the existing Compose/MainActivity architecture for this bounded release, but move serialization and pure tag transformations into focused helpers where practical. Extend TagRecord and the JSON share schema additively so old records/files remain readable. Reuse the existing ZoomableImageOverlay and translation API instead of introducing a second image or translation stack.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, SharedPreferences/JSON, existing translation API, existing ZoomableImageOverlay, Gradle Android build.

**Spec:** `docs/superpowers/specs/2026-10-01-local-cue-word-gallery-tags-theme-design.md`

## Global Constraints

- App name remains `Local Cue Word`.
- Existing recognition/model inference behavior must remain unchanged.
- Old share files remain importable.
- Full tag strings are never truncated when copied.
- Untranslated tags display `暂未翻译`.
- Turning off Monet/dynamic color uses the black preset rather than the old blue fallback.
- Light and dark foreground colors must contrast with their actual containers.

## Review Focus

- A long tag remains fully copyable even when its visual row wraps.
- A translated record keeps translations after restart and after sharing/importing.
- Old schema-1 share files still import.
- Missing/partial translation data never crashes rendering.
- Light/dark theme containers never receive the wrong black/white foreground.

### Task 1: TagRecord persistence and share-schema translation data

**Files:**
- Modify: `app/src/main/java/Shirakawa/LocalCueWord/MainActivity.kt`
- Test: `app/src/test/java/Shirakawa/LocalCueWord/TagRecordSerializationTest.kt`

- [ ] Write pure serialization/compatibility tests for translation maps and schema-1 import.
- [ ] Run the tests and verify the new tests fail before implementation.
- [ ] Extend `TagRecord` with persisted translation data and optional negative/tag metadata without changing existing callers' required constructor arguments.
- [ ] Update load/save record JSON to persist translations and preserve unknown/missing fields safely.
- [ ] Extend share payload to write translations and read them additively.
- [ ] Run the focused tests and verify they pass.

### Task 2: Complete tag copy/translation presentation

**Files:**
- Modify: `app/src/main/java/Shirakawa/LocalCueWord/MainActivity.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: localized string files only where required for compilation/fallback.
- Test: `app/src/test/java/Shirakawa/LocalCueWord/TagDisplayTest.kt`

- [ ] Write failing tests for full-string copying, untranslated fallback, and translation-map merging.
- [ ] Implement a reusable pure tag display/translation helper.
- [ ] Replace ellipsized tag presentation with wrapping/full content while keeping copy actions independent.
- [ ] Add separate copy actions for positive tags, negative tags, and translations.
- [ ] Persist translation results back to the current record.
- [ ] Run focused tests.

### Task 3: Gallery and full-screen image interaction polish

**Files:**
- Modify: `app/src/main/java/Shirakawa/LocalCueWord/MainActivity.kt`
- Modify: `app/src/main/java/Shirakawa/LocalCueWord/ui/components/ZoomableImageOverlay.kt`
- Test: Compose/UI tests where the existing test setup supports them.

- [ ] Add a regression test or pure helper coverage for ordered gallery navigation.
- [ ] Make gallery tiles consistently image-first, two-column, and long-press selectable.
- [ ] Reuse the existing zoom viewer for swipe/previous/next and overlay actions.
- [ ] Ensure favorite/share/delete/copy actions operate on the currently viewed record.
- [ ] Verify missing-image records render safely.

### Task 4: Header spacing and theme behavior

**Files:**
- Modify: `app/src/main/java/Shirakawa/LocalCueWord/MainActivity.kt`
- Modify: `app/src/main/java/Shirakawa/LocalCueWord/ui/theme/Theme.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/Shirakawa/LocalCueWord/ThemeColorTest.kt`

- [ ] Write failing tests for Monet-off => black palette and foreground contrast selection.
- [ ] Increase hero title/subtitle-to-image spacing without changing image aspect ratio.
- [ ] Remove/hide the separate black palette from the user-facing palette choices while retaining it as the Monet-off fallback.
- [ ] Centralize actual-container foreground selection for Material color roles.
- [ ] Run focused tests.

### Task 5: Full build and regression verification

**Files:**
- Modify only if verification reveals a concrete defect.

- [ ] Run unit tests.
- [ ] Run Android debug compilation.
- [ ] Run lint/available static checks.
- [ ] Inspect generated APK and changed-source diff.
- [ ] Package the corrected source as a ZIP for delivery.
