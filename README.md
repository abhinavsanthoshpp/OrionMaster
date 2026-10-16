# PDF Master (Open-Source PDF Management App)

An open-source, native Android PDF managing & viewing application inspired by **iLovePDF**, rebuilt from the ground up for privacy, zero account requirements, and 100% on-device processing.

---

## 🌟 Key Highlights & Design Philosophy

1. **100% Open & Free**: No login, no sign-up, no subscriptions, no ads, and no tracking.
2. **Offline & Private**: All PDF processing (merge, split, compress, watermark, protect, encrypt, rotate) runs strictly on-device using Apache PDFBox for Android. Documents never leave your phone.
3. **Modern UI/UX**: Built with Jetpack Compose & Material Design 3, dynamic theming (Dark & Light mode), fluid transitions, and categorized tools.
4. **Full System Integration**: Registers as a system PDF viewer (`android.intent.action.VIEW`), supports Android Sharesheet, Storage Access Framework, and Scoped Storage.

---

## 🛠️ Feature Comparison Matrix: iLovePDF vs PDF Master

| Feature / Aspect | iLovePDF (Web / Official App) | **PDF Master (This App)** |
| :--- | :--- | :--- |
| **Account & Login** | Requires sign-up/login for higher quotas | **Zero login / No account needed** |
| **File Privacy** | Uploads files to cloud servers | **100% On-device, Zero cloud upload** |
| **Pricing / Paywalls** | Freemium (Limits on file count & sizes) | **100% Free & Open Source** |
| **Internet Dependency**| Requires active internet connection | **Full offline capability** |
| **UI Design System** | Traditional web wrapper / Ad banners | **Jetpack Compose + Material 3** |
| **History & Management**| Server-based account dashboard | **Local Room Database + Quick Share** |

---

## 🧰 Implemented PDF Tools (Categorized)

### 1. Organize PDF
- **Merge PDF**: Combine multiple PDFs into a single file with custom ordering.
- **Split PDF**: Split each page into individual documents, split every *N* pages, or extract custom ranges (`1-3, 5-8`).
- **Remove Pages**: Delete specific pages from any document.
- **Extract Pages**: Extract select pages into an independent document.
- **Organize PDF**: Reorder pages visually or numerically.
- **Rotate PDF**: Rotate all or specific pages by 90°, 180°, or 270°.

### 2. Optimize PDF
- **Compress PDF**: Optimize streams and metadata with 3 preset compression levels (Low, Medium, High).
- **Repair PDF**: Re-index and reconstruct corrupted or broken PDF streams.
- **OCR PDF**: Optical Character Recognition engine placeholder for scanned documents.

### 3. Convert PDF
- **JPG to PDF**: Convert photos, gallery pictures, and scans into a single PDF document.
- **PDF to JPG**: High-resolution rendering (72, 150, 300 DPI) of PDF pages to JPG images.

### 4. Edit PDF
- **Watermark**: Overlay custom text watermarks with control over angle, opacity, font size, and color.
- **Page Numbers**: Add customizable page numbers (Top/Bottom, Left/Center/Right, prefix/suffix).
- **Crop PDF**: Trim margins from documents.
- **Edit & Annotations**: Visual text and shape markup.

### 5. PDF Security
- **Protect PDF**: Standard 128-bit AES encryption with password protection and permissions toggling (Print / Copy).
- **Unlock PDF**: Remove passwords from protected documents.
- **Digital Sign**: Draw or place digital signatures.
- **Compare PDF**: Side-by-side comparison.

---

## 🏗️ Architecture & Tech Stack

- **UI**: Jetpack Compose, Material 3, Navigation Compose
- **Language**: Kotlin 2.0 (Coroutines + StateFlow)
- **PDF Engine**: PDFBox Android (Apache 2.0) + Native `android.graphics.pdf.PdfRenderer`
- **Database**: AndroidX Room for persistent recent files history
- **Image Loading**: Coil 2.7.0
- **Storage**: Android Storage Access Framework (SAF) & FileProvider

---

## 🚀 How to Build & Run

1. Open the project in **Android Studio Meerkat / Ladybug** or newer.
2. Ensure JDK 17+ is configured.
3. Sync Gradle and build:
   ```bash
   ./gradlew assembleDebug
   ```
4. Output APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`
