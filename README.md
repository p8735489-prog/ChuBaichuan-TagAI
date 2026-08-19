<p align="center">
  <img src="https://raw.githubusercontent.com/p8735489-prog/ChuBaichuan-TagAI/main/docs/assets/app_icon.png" width="128" alt="Local Cue Word icon" />
</p>

<h1 align="center">Local Cue Word</h1>

<p align="center">
  On-Device AI Tag Generator · Image Label Recognition · Prompt Organizer
</p>

<p align="center">
  <img alt="Android" src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img alt="Local AI" src="https://img.shields.io/badge/Local_AI-ONNX-005CED?style=for-the-badge&logo=onnx&logoColor=white" />
  <img alt="Version" src="https://img.shields.io/badge/Version-3.6.1-FF6B35?style=for-the-badge" />
  <img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-D22128?style=for-the-badge" />
</p>

<p align="center">
  <a href="https://github.com/p8735489-prog/ChuBaichuan-TagAI/releases/latest">Download Latest APK</a>
  ·
  <a href="https://qm.qq.com/q/6jViPcR9le">QQ Group</a>
  ·
  <a href="https://t.me/Local_Cue_Word">Telegram</a>
  ·
  <a href="https://www.ifdian.net/a/cubaicuan">Sponsor</a>
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/p8735489-prog/ChuBaichuan-TagAI/main/docs/assets/showcase.jpg" alt="Local Cue Word showcase" width="100%" />
</p>

---

## Overview

Local Cue Word is an Android on-device AI image tagging tool. It runs entirely offline on your phone to recognize image content, generate tags, and automatically organize them into prompts suitable for AI painting.

The app focuses on "select an image, get usable prompts fast." Think of it as a pocket prompt organizer for analyzing reference images, organizing character sheets, extracting visual elements, and saving frequently used tags.

---

## Features

| Feature | Description |
| --- | --- |
| On-Device AI Recognition | Runs ONNX models directly on the phone for fully offline recognition of characters, clothing, actions, scenes, and visual elements |
| Multi-Model Fusion | Combines WD Tagger, Camie, PixAI, AnimeTimm and more for broader tag coverage |
| Smart Prompt Weighting | Dynamically calculates tag weights based on recognition confidence instead of fixed values |
| Smart Tag Selection | Five-dimensional filtering: confidence + model feedback + importance + similarity + redundancy |
| GLOBAL/LOCAL Fusion | Fuses whole-image and region-level recognition with source differentiation, semantic risk assessment, and conflict detection |
| Tag Translation | Translates tags into Chinese, Japanese, Korean, Russian, and more |
| Precision Mode | YOLO detection + segmentation + cropping + WD Tagger joint inference for precise subject localization |
| Snapdragon Optimization | Optimized inference, memory management, and hardware acceleration (NNAPI) for Snapdragon devices |
| History | Saves recognized images and tags for review and reuse |
| Favorites | Bookmark frequently used tags to build character, style, and composition libraries |
| Batch Recognition | Select multiple images at once for continuous tag generation |
| Custom Appearance | Dark mode, dynamic color, custom background image, dimming, and theme colors |
| Share | Share prompts, source link, QQ group, and Telegram group |

---

## Quick Start

1. Download and install the APK.
2. Open the app and download recognition models from the Model Manager (first use requires internet; after that, fully offline).
3. Select an image and tap Recognize.
4. Wait for the local model to generate tags and auto-organize them into a prompt.
5. Copy, translate, bookmark, save, or share as needed.

---

## Download

APKs are available on the GitHub Releases page.

| Source | Link |
| --- | --- |
| Latest Release | https://github.com/p8735489-prog/ChuBaichuan-TagAI/releases/latest |
| All Versions | https://github.com/p8735489-prog/ChuBaichuan-TagAI/releases |

> Version 30.2 and earlier can upgrade directly to 3.6. Data and settings are preserved.

---

## Supported Models

All models run locally on the device. No internet connection required after download.

### Tagger Models

| Model | Family | Size | Accuracy | Source |
| --- | --- | --- | --- | --- |
| WD EVA02 Large Tagger v3 | WD v3 | ~1.4GB | ★★★★★ | [SmilingWolf/wd-eva02-large-tagger-v3](https://huggingface.co/SmilingWolf/wd-eva02-large-tagger-v3) |
| WD ConvNeXt Tagger v3 | WD v3 | ~377MB | ★★★★☆ | [SmilingWolf/wd-convnext-tagger-v3](https://huggingface.co/SmilingWolf/wd-convnext-tagger-v3) |
| WD SwinV2 Tagger v3 | WD v3 | ~342MB | ★★★★ | [SmilingWolf/wd-swinv2-tagger-v3](https://huggingface.co/SmilingWolf/wd-swinv2-tagger-v3) |
| WD ViT Tagger v3 | WD v3 | ~327MB | ★★★☆ | [SmilingWolf/wd-vit-tagger-v3](https://huggingface.co/SmilingWolf/wd-vit-tagger-v3) |
| WD v1.4 MOAT Tagger v2 | WD v1.4 | ~300MB | ★★★☆ | [SmilingWolf/wd-v1-4-moat-tagger-v2](https://huggingface.co/SmilingWolf/wd-v1-4-moat-tagger-v2) |
| WD v1.4 ConvNeXtV2 Tagger v2 | WD v1.4 | ~300MB | ★★★ | [SmilingWolf/wd-v1-4-convnextv2-tagger-v2](https://huggingface.co/SmilingWolf/wd-v1-4-convnextv2-tagger-v2) |
| WD v1.4 ConvNeXt Tagger v2 | WD v1.4 | ~300MB | ★★★ | [SmilingWolf/wd-v1-4-convnext-tagger-v2](https://huggingface.co/SmilingWolf/wd-v1-4-convnext-tagger-v2) |
| WD v1.4 SwinV2 Tagger v2 | WD v1.4 | ~300MB | ★★☆ | [SmilingWolf/wd-v1-4-swinv2-tagger-v2](https://huggingface.co/SmilingWolf/wd-v1-4-swinv2-tagger-v2) |
| WD v1.4 ViT Tagger v2 | WD v1.4 | ~300MB | ★★☆ | [SmilingWolf/wd-v1-4-vit-tagger-v2](https://huggingface.co/SmilingWolf/wd-v1-4-vit-tagger-v2) |
| WD v1.4 ViT Tagger | WD v1.4 | ~300MB | ★★ | [SmilingWolf/wd-v1-4-vit-tagger](https://huggingface.co/SmilingWolf/wd-v1-4-vit-tagger) |
| Camie Tagger v2 | Camie | ~430MB | ★★★★☆ | [Camais03/camie-tagger-v2](https://huggingface.co/Camais03/camie-tagger-v2) |
| PixAI Tagger v0.9 | PixAI | ~380MB | ★★★★ | [deepghs/pixai-tagger-v0.9-onnx](https://huggingface.co/deepghs/pixai-tagger-v0.9-onnx) |
| AnimeTimm ResNet34 DBv4-full | AnimeTimm | ~350MB | ★★★★ | [animetimm/resnet34.dbv4-full](https://huggingface.co/animetimm/resnet34.dbv4-full) |

### Detection Models

| Model | Size | Accuracy | Description |
| --- | --- | --- | --- |
| YOLO11n | ~3MB | ★★☆ | Nano detection, ultra-fast inference |
| YOLO11s | ~9MB | ★★★ | Small detection, balanced speed and accuracy |
| YOLO11m | ~20MB | ★★★☆ | Medium detection, higher accuracy |
| YOLOv8n | ~6MB | ★★ | Classic v8 Nano detection |

### Segmentation Models

| Model | Size | Description |
| --- | --- | --- |
| YOLO11n-seg | ~4MB | Nano instance segmentation, person mask |
| YOLO11s-seg | ~11MB | Small instance segmentation, more precise mask |
| YOLOv8n-seg | ~6MB | Classic v8 Nano instance segmentation |

---

## Changelog

### v3.6

#### 1. Fixed Local Recognition Polluting Whole-Image Results

Refactored the GLOBAL / LOCAL tag fusion pipeline:

```
Image → GLOBAL whole-image recognition + LOCAL segmentation recognition
     → Source differentiation (tag each label's origin)
     → Semantic risk assessment (high-risk identity tags from LOCAL require GLOBAL confirmation)
     → Conflict detection (GLOBAL takes priority when GLOBAL vs LOCAL conflict)
     → Confidence fusion (weighted max)
     → UNCERTAIN filtering
     → Final Tags
```

Prioritizes whole-image evidence to prevent local regions from generating high-risk identity tags that incorrectly override the global judgment.

#### 2. Smart Prompt Weighting

New prompt generation pipeline:

```
GLOBAL / LOCAL recognition → Semantic fusion → Final confidence → Tag filtering → Weight calculation from confidence → Prompt generation
```

Tag weights are no longer fixed; they are dynamically calculated based on the final recognition confidence.

#### 3. More Powerful Tagger Models

Added the following models to join the existing WD series for recognition and fusion:

| New Model | Size | Highlights | Source |
| --- | --- | --- | --- |
| Camie Tagger v2 | ~430MB | Excels at character and detail recognition, broad tag coverage | [Camais03/camie-tagger-v2](https://huggingface.co/Camais03/camie-tagger-v2) |
| PixAI Tagger v0.9 | ~380MB | Optimized for anime illustrations, high accuracy on style tags | [deepghs/pixai-tagger-v0.9-onnx](https://huggingface.co/deepghs/pixai-tagger-v0.9-onnx) |
| AnimeTimm ResNet34 DBv4-full | ~350MB | Danbooru DBv4 tag set, strong cross-style generalization | [animetimm/resnet34.dbv4-full](https://huggingface.co/animetimm/resnet34.dbv4-full) |

#### 4. Snapdragon Performance & Stability Optimization

Low-level optimizations for Snapdragon Android devices:

- **Inference Optimization**: Dynamically configures inference threads based on device big-core count (Snapdragon 8-series: 4 threads / 7-series: 2 threads / low-end: 1 thread)
- **Memory Management**: Disables memory pattern optimization, enables environment allocators for improved long-running stability
- **Hardware Acceleration**: YOLO detection engine supports NNAPI (Android 9.0+), with automatic CPU fallback on failure
- **Safety Strategy**: WD Tagger models default to pure CPU inference (NNAPI has poor compatibility with Tagger models and can trigger native crashes); NNAPI is only enabled in the detection engine
- **Model Loading & Release**: Unified session lifecycle management prevents concurrent access to closed sessions

#### 5. Smart Tag Limit

Redesigned Tag Limit — no longer simply truncates in order. Instead, tags are intelligently filtered across five dimensions:

| Dimension | Description |
| --- | --- |
| Confidence | Raw model output score |
| Model Feedback | How many models co-recognize the tag (consensus) |
| Tag Importance | Character/core tags take priority over secondary descriptions |
| Similarity | Semantically similar tags keep only the highest-scoring one |
| Redundancy | Same-category tags capped at a configurable maximum |

Limited tag slots are prioritized for higher-information, higher-confidence tags, reducing the impact of low-probability and duplicate tags on the prompt.

#### 6. Image Picker Fix

- Migrated single-image picker and custom background picker from `GetContent` to `PickVisualMedia` for better compatibility
- Reduced maximum bitmap decode size (4096px → 2048px) to reduce OOM crash risk
- Enhanced image loading error handling: added `SecurityException` catch, OOM retry mechanism, and failure toast prompt

---

## Community

| Platform | Link |
| --- | --- |
| QQ Group | https://qm.qq.com/q/6jViPcR9le |
| Telegram | https://t.me/Local_Cue_Word |
| GitHub | https://github.com/p8735489-prog/ChuBaichuan-TagAI |

---

## Sponsor

If this project helps you, consider sponsoring the developer:

[Sponsor on aifadian](https://www.ifdian.net/a/cubaicuan)

---

## Credits & Open Source Dependencies

| Project / Service | Description | Link |
| --- | --- | --- |
| ONNX Runtime | Cross-platform ML inference engine, core of local model execution | https://onnxruntime.ai |
| WD Tagger Series | Danbooru image tag recognition models by SmilingWolf | https://huggingface.co/SmilingWolf |
| Camie Tagger | Aspect-based tag recognition model by Camais03 | https://huggingface.co/Camais03/camie-tagger-v2 |
| PixAI Tagger | Anime illustration tag recognition model (ONNX version by deepghs) | https://huggingface.co/deepghs/pixai-tagger-v0.9-onnx |
| AnimeTimm | ResNet34 Danbooru DBv4 tag recognition model by animetimm | https://huggingface.co/animetimm/resnet34.dbv4-full |
| YOLO11 / YOLOv8 | Object detection and instance segmentation models by Ultralytics | https://github.com/ultralytics/ultralytics |
| MyMemory Translation API | Multi-language tag translation service | https://mymemory.translated.net/doc/spec.php |
| Jinrishici API | Random classical poetry subtitle for the hero section | https://www.jinrishici.com |

---

## License

This project is open source and available for learning and communication.
