# Contributing to File Bodlai

Thank you for your interest in contributing to **File Bodlai**! Contributions such as bug fixes, UI enhancements, new converter pipelines, and performance optimizations are welcome.

---

## Getting Started

1. **Fork the Repository**: Fork on GitHub and clone locally:
   ```bash
   git clone https://github.com/SMMahmudIqbal/FileBodlai.git
   cd FileBodlai
   ```
2. **Environment Requirements**:
   - Android Studio Ladybug or newer
   - JDK 17
   - Android SDK (API 34)
3. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

---

## Contribution Guidelines

### 1. 100% Offline Integrity (Non-Negotiable)
- File Bodlai has a **zero-internet guarantee**.
- **Never** add `android.permission.INTERNET` to `AndroidManifest.xml`.
- **Never** introduce remote dependencies that connect to analytics servers or require network connectivity.

### 2. Code Quality and Standards
- Kotlin-first codebase using **Jetpack Compose (Material 3)**.
- Follow MVVM architecture with clear state boundaries (`StateFlow` / `collectAsState`).
- Keep converter algorithms non-blocking using `Dispatchers.IO` inside Coroutines.
- Explicitly recycle Bitmaps and close file descriptors to prevent native memory leaks.

### 3. Pull Request Process
1. Create a feature branch: `git checkout -b feat/my-feature`.
2. Commit your changes with conventional commit messages (`feat: ...`, `fix: ...`, `docs: ...`).
3. Ensure `./gradlew assembleDebug` succeeds with zero errors.
4. Open a Pull Request describing your changes, with screenshots or recordings for UI updates.

---

## Attribution

File Bodlai was originally designed and **Developed by S. M. Mahmud Iqbal**.
