# Privacy Policy for File Bodlai

**Effective Date:** October 2026  
**Last Updated:** October 2026

**File Bodlai** is designed from the ground up with a privacy-first, zero-knowledge architecture. This Privacy Policy outlines our commitment to user privacy and details how data is handled.

---

### 1. Summary of Privacy Commitments

- **No Data Collection:** The application does not collect, transmit, store, sell, or share personal data, files, or telemetry.
- **100% Offline Processing:** All file processing, rendering, compressing, and conversions occur entirely on your local Android device.
- **Zero Internet Permissions:** The application manifest (`AndroidManifest.xml`) does not request `android.permission.INTERNET`. The app cannot establish network connections.
- **No Third-Party Analytics or Trackers:** The application contains zero advertising frameworks, tracking SDKs, crash analytics reporters, or user behavioral monitoring tools.

---

### 2. Permissions and Data Access

The app requests only the minimum Android operating system permissions necessary to perform user-initiated file conversions:

| Permission | Purpose |
| :--- | :--- |
| `READ_EXTERNAL_STORAGE` (API $\le$ 32) | Allows you to select files from your storage for local conversion. |
| `WRITE_EXTERNAL_STORAGE` (API $\le$ 28) | Allows saving converted documents to your local `Downloads` folder on legacy Android versions. |
| `VIBRATE` | Provides tactile haptic feedback for button clicks and conversion completion. |

On modern Android versions (API 29+ / Android 10 through 15), the app uses Android's **Storage Access Framework (SAF)** and **Scoped MediaStore API** (`MediaStore.Downloads`). We only access files you explicitly pick via the system file picker.

---

### 3. File Processing and Storage

- **Document Conversions:** When you convert or compress a file, processing is performed entirely in memory or temporary app cache on your local device.
- **Bitmap and Memory Recycling:** Intermediate image buffers and rendered pages are actively recycled and released upon completion.
- **Output Storage:** Converted files are saved to your device's public `Downloads/FileBodlai` directory so that you retain full ownership and access through your device's file manager.
- **Telemetry Counter:** The "Storage Saved" and "Files Processed" metrics are stored exclusively in your device's local storage via `SharedPreferences`. They are never sent to external servers.

---

### 4. Children’s Privacy

File Bodlai is completely safe for all audiences, including children under the age of 13. Since no personal data or files are collected or transmitted, there is zero risk of data harvesting.

---

### 5. Open Source Transparency

File Bodlai is open source. You can audit the complete source code, dependencies, and build scripts yourself on GitHub:  
[https://github.com/SMMahmudIqbal/FileBodlai](https://github.com/SMMahmudIqbal/FileBodlai)

---

### 6. Author and Contact

**Developed by S. M. Mahmud Iqbal**  
If you have questions, feedback, or concerns regarding this Privacy Policy, please open an issue on the official GitHub repository.
