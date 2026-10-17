# File Bodlai (ফাইল বদলাই)

> **Developed by S. M. Mahmud Iqbal**  
> *Next-Gen 100% Offline Private File Processing Studio for Android*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-green.svg?style=flat-square&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-4285F4.svg?style=flat-square)](https://developer.android.com/jetpack/compose)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline%20(Air--Gapped)-brightgreen.svg?style=flat-square)](app/src/main/AndroidManifest.xml)
[![Author](https://img.shields.io/badge/Developed%20By-S.%20M.%20Mahmud%20Iqbal-6366f1.svg?style=flat-square)](https://github.com/SMMahmudIqbal)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

---

## Overview

Most mobile document conversion applications require uploading personal documents, financial statements, and identification photos to third-party cloud servers. Many bundle intrusive advertisements, third-party analytics trackers, or recurring paywalls for standard operations.

**File Bodlai** (*"File Change / Convert"*) is built on an absolute principle: **your files never leave your device.**

All conversion algorithms execute locally using native Android rendering APIs and optimized coroutine pipelines. The application manifest (`AndroidManifest.xml`) requests **zero internet permissions**, guaranteeing complete air-gapped security and privacy.

---

## Core Features

- **Smart PDF Compression**: Intelligently downsamples and optimizes PDF file sizes across multi-level DPI thresholds with adaptive JPEG stream compression.
- **PDF to PPTX Presentation**: Transmutes document pages into clean presentation slides compliant with OpenXML specifications.
- **PDF Merging and Page Organization**: Seamlessly merges multiple documents, adjusts page order, supports 90/180/270 degree rotation, and allows selective page extraction or deletion.
- **Batch Image and PDF Studio**: Converts image collections (JPG, PNG, WebP) into standardized A4 and Letter PDF documents, or extracts PDF pages as high-resolution images.
- **Six Bespoke Themes**:
  - *Neumorphic Glass* (Frosted glass visual aesthetic)
  - *Neo-Brutalist High-Contrast* (Bold retro layout with defined borders)
  - *Cyberpunk OLED* (Pure dark palette with high-contrast accenting)
  - *Tokyo Night* (Subtle deep indigo synth styling)
  - *Swiss International Editorial* (Clean modernist monochrome layout)
  - *Velvet Sunset Warm Amber* (Soft organic terracotta ambiance)
- **Real-Time Storage Telemetry**: Local, on-device logging of cumulative disk space saved and total document processing volume.
- **Quick Look and Native Sharing**: Full-screen preview with system file sharing integration.

---

## Architecture and Tech Stack

```
FileBodlai/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/example/fileconverter/
│   │   │   ├── converter/      # On-device conversion pipelines (PdfConverter, PptxBuilder, etc.)
│   │   │   ├── ui/
│   │   │   │   ├── screens/    # Jetpack Compose UI screens
│   │   │   │   ├── theme/      # Dynamic theme engine with 6 bespoke color profiles
│   │   │   │   └── components/ # Micro-interactions, GlassCard, QuickLook
│   │   │   ├── util/           # ImpactStatsManager, MediaStoreHelper, FileUtils
│   │   │   └── viewmodel/      # MVVM reactive state managers
│   │   └── res/                # Drawables, launcher icons, and localized resources
└── build.gradle.kts
```

- **Framework**: Kotlin 2.0 + Jetpack Compose BOM 2024.04 + Material 3
- **PDF Engine**: Native `android.graphics.pdf.PdfRenderer` + `PdfDocument` + lightweight `pdfbox-android` for lossless merging
- **Concurrency**: Kotlin Coroutines (`Dispatchers.IO`) with lifecycle-aware ViewModels and atomic bitmap recycling
- **Storage**: Scoped Storage with `MediaStore.Downloads` compliant across Android 7 through 15

---

## Building from Source

### Prerequisites
- JDK 17
- Android SDK (compileSdk 34)

### Clone and Build
```bash
git clone https://github.com/SMMahmudIqbal/FileBodlai.git
cd FileBodlai

# Build debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```

Release builds require a private keystore and the `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` values supplied through a protected environment or Gradle properties. `RELEASE_STORE_FILE` may specify the keystore path; otherwise the build looks for the existing `bodlai-release-key.jks` outside version control. The release build fails if the keystore or any required signing value is missing. Never add the keystore or credentials to Git.

---

## Author and Attribution

**Developed by S. M. Mahmud Iqbal**  
- GitHub: [@SMMahmudIqbal](https://github.com/SMMahmudIqbal)

---

## License

This project is licensed under the [MIT License](LICENSE).
