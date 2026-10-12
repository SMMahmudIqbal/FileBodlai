# File Bodlai Web (ফাইল বদলাই ওয়েব) 🌐

[![Deploy with Vercel](https://vercel.com/button)](https://vercel.com/new)
![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)
![React 19](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-38B2AC?logo=tailwind-css&logoColor=white)
![Zero Server Cost](https://img.shields.io/badge/Server%20Cost-$0%20Forever-success)

The official web edition of **File Bodlai (ফাইল বদলাই)** • the ultra-fast, 100% offline and private file conversion suite.

Built with **React 19 + TypeScript + Vite + Tailwind CSS**, designed to be deployed directly to **Vercel** with zero server costs, zero cold-starts, and infinite scalability.

---

## 🔒 100% Offline & Air-Gapped Privacy

Traditional online file converters force you to upload sensitive contracts, financial statements, and private photos to remote cloud servers.

**File Bodlai works completely differently:**
* **Zero Server Uploads:** All PDF rendering, page merging, image compression, and presentation generation executes directly in your browser's RAM memory using WebAssembly and client-side JavaScript.
* **Zero Payload Limits:** Because files are processed locally, you are not subject to Vercel's standard 4.5 MB serverless upload limits.
* **Zero Analytics or Tracking:** No tracking cookies, no Google Analytics, no telemetry.

---

## ⚡ 6 Powerful Studio Tools Included

1. **PDF to PPTX:** High-definition page rendering into widescreen PowerPoint presentation slides (`.pptx`).
2. **Merge PDF:** Combine multiple PDFs into a single organized document with drag-and-drop reordering.
3. **Image(s) to PDF:** Convert JPG, PNG, and WebP images into clean PDFs with orientation and margin controls.
4. **PDF to Images:** Extract every page as PNG or JPG images with custom DPI settings and 1-click ZIP export.
5. **Image to Image:** Convert formats between PNG, JPG, and WebP with real-time quality compression sliders and resizing.
6. **Text to PDF:** Convert `.txt` files or raw text into elegant, paginated PDFs with header titles and page numbers.

---

## 🎨 19 Dynamic Color Themes & Bengali Localization

* **19 Theme Presets:** Exact 100% parity with the native Android app (Nocturnal Dark, Elegant Day, and Art & Avant-Garde).
* **Native Bengali (বাংলা) & English:** 1-click bilingual language switcher.

---

## 🚀 How to Deploy to Vercel

### Option 1: 1-Click via Vercel Web Dashboard (Easiest)

1. Create a new GitHub repository (e.g. `FileBodlai-Web`) and push this folder.
2. Go to **[vercel.com](https://vercel.com)** and log in.
3. Click **"Add New..."** $\rightarrow$ **"Project"**.
4. Import your `FileBodlai-Web` repository.
5. Vercel will automatically detect **Vite**:
   - **Framework Preset:** Vite
   - **Build Command:** `npm run build`
   - **Output Directory:** `dist`
6. Click **Deploy**. Your website will be live on `https://your-project.vercel.app` in under 30 seconds!

---

### Option 2: Deploy from your Terminal using Vercel CLI

In this project directory, run:
```bash
npx vercel
```
* Follow the interactive prompts (press Enter for default settings).
* To deploy directly to production, run:
```bash
npx vercel --prod
```

---

## 💻 Local Development

```bash
# 1. Install dependencies
npm install

# 2. Run local development server
npm run dev

# 3. Test production build
npm run build
npm run preview
```

---

## 📄 License & Attribution

Created by **S. M. Mahmud Iqbal**.  
Distributed under the permissive **MIT License**.
