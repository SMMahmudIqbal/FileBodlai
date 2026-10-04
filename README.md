# File Bodlai (ফাইল বদলাই) ⚡

> **Next-gen private file processing studio for Android.**  
> 100% on-device, zero tracking, zero ads, and zero internet permissions.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-green.svg?style=flat-square&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-4285F4.svg?style=flat-square)](https://developer.android.com/jetpack/compose)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline%20(Air--Gapped)-brightgreen.svg?style=flat-square)](AndroidManifest.xml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

---

## 💡 Why File Bodlai?

Most document converter apps on mobile require you to upload your sensitive PDFs, tax returns, and ID photos to third-party cloud servers. They often bombard users with full-screen ads, track analytics, or lock basic tools behind recurring subscriptions.

**File Bodlai** (*"File Change / Convert"*) is built on a single uncompromising principle: **Your files should never leave your phone.**

Everything is processed locally using Android’s native rendering engines and coroutine pipelines. Check `AndroidManifest.xml` yourself: **there is zero internet permission requested.**

---

## ✨ Features

- **⚡ Smart PDF Compressor**: Intelligently downsample and optimize heavy PDF documents to target file sizes with multi-level DPI and JPEG stream compression.
- **📊 PDF → PPTX Presentation**: Converts document pages into clean, presentation-ready slides formatted with OpenXML specs.
- **📑 Merge & Organize PDFs**: Combine multiple documents, reorder, rotate pages 90°/180°/270°, and selectively extract or delete pages.
- **🖼️ Batch Image ↔ PDF Studio**: Turn galleries of JPG, PNG, and WebP images into standardized A4/Letter PDF sheets, or export PDF pages as high-resolution images.
- **🎨 6 Handcrafted Aesthetic Themes**:
  - *Neumorphic Glass* (Default frosted glass aesthetic)
  - *Neo-Brutalist High-Contrast* (Bold retro typography and borders)
  - *Cyberpunk OLED* (Pure `#000000` blacks with neon cyan/magenta accents)
  - *Tokyo Night* (Subtle deep indigo synth aesthetic)
  - *Swiss International Editorial* (Clean modernist monochrome grid)
  - *Velvet Sunset Warm Amber* (Soft organic terracotta glow)
- **📈 Real-Time Impact Telemetry**: On-device tracker that logs cumulative storage space saved and documents converted.
- **🔍 Quick Look & Share**: Instant full-screen document preview with system sharing and file management integrations.

---

## 🛠️ Architecture & Tech Stack

```
FileBodlai/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/example/fileconverter/
│   │   │   ├── converter/      # Core on-device conversion engines (PdfConverter, PptxBuilder, etc.)
│   │   │   ├── ui/
│   │   │   │   ├── screens/    # Jetpack Compose screen composables
│   │   │   │   ├── theme/      # Dynamic theme engine & 6 custom palettes
│   │   │   │   └── components/ # Micro-animations, GlassCard, QuickLook
│   │   │   ├── util/           # ImpactStatsManager, MediaStoreHelper, FileUtils
│   │   │   └── viewmodel/      # MVVM reactive state holders
│   │   └── res/                # Vector drawables, adaptive launcher icons, localized strings
└── build.gradle.kts
```

- **Framework**: Kotlin 2.0 + Jetpack Compose BOM 2024.04 + Material 3
- **PDF Engine**: Native `android.graphics.pdf.PdfRenderer` + `PdfDocument` + lightweight `pdfbox-android` for lossless merging
- **Concurrency**: Kotlin Coroutines (`Dispatchers.IO`) with lifecycle-aware ViewModels and atomic bitmap recycling
- **Storage**: Scoped Storage with `MediaStore.Downloads` compliant across Android 7 through 15

---

## 🚀 Building from Source

### Prerequisites
- JDK 17
- Android SDK (compileSdk 34)

### Clone & Build
```bash
git clone https://github.com/your-username/FileBodlai.git
cd FileBodlai

# Build debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
