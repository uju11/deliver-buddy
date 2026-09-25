# Deployment Guide - Delivery Buddy Landing Page & APK

Welcome to the **Delivery Buddy** website and APK deployment guide! This document explains how to build your Android APK, place it into the website folder, and deploy your landing page to **Vercel** or **GitHub Pages**.

---

## 📂 Project Structure

Inside the `website/` directory, you have:
- `index.html` — Modern, responsive landing page styled with Tailwind CSS showcasing app features (Floating Buddy for Ekart Field X, Runsheet Parser, Route Optimization, Fuel Cost Calculator, Petrol Pump Finder) and a download button for `delivery-buddy.apk`.
- `vercel.json` — Configuration file for seamless Vercel deployment.
- `DEPLOY.md` — This deployment guide.

---

## 🛠️ Step 1: Build the Android APK

Before deploying the website, you need to generate your signed or debug APK from Android Studio and place it in the `website/` folder.

### Option A: Using Android Studio
1. Open the project in Android Studio.
2. In the top menu, go to **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
3. Once built, click **Locate** in the notification popup to find the APK file (typically located at `app/build/outputs/apk/debug/app-debug.apk` or `app-release.apk`).

### Option B: Using Gradle (Terminal)
Run the following command in your terminal at the project root:
```bash
./gradlew :app:assembleDebug
```
The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Copy & Rename the APK
1. Copy your built APK file (`app-debug.apk` or `app-release.apk`).
2. Paste it directly inside the `website/` directory.
3. Rename the file to **`delivery-buddy.apk`** so it matches the download link in `index.html`.

---

## 🚀 Step 2: Deploy to Vercel (Recommended)

Vercel provides lightning-fast global hosting for static websites.

### Method 1: Using Vercel CLI (Fastest)
1. Install Vercel CLI globally (if you haven't already):
   ```bash
   npm i -g vercel
   ```
2. Navigate to the `website/` directory:
   ```bash
   cd website
   ```
3. Run the deployment command:
   ```bash
   vercel
   ```
4. Follow the prompts on your terminal:
   - Set up and deploy? **Y**
   - Which scope? Select your account.
   - Link to existing project? **N**
   - What's your project name? `delivery-buddy`
   - In which directory is your code located? `./` (or press Enter)
5. Vercel will build and give you a live HTTPS URL where users can view your landing page and download `delivery-buddy.apk`!

### Method 2: Using Vercel Dashboard (Web UI)
1. Push your project repository to GitHub.
2. Go to [Vercel Dashboard](https://vercel.com/new).
3. Import your GitHub repository (`deliverybuddy`).
4. Set the **Root Directory** to `website`.
5. Click **Deploy**.

---

## 🌐 Alternative: Deploy to GitHub Pages

If you prefer GitHub Pages:
1. In your repository settings, navigate to **Pages**.
2. Set the build source branch to `main` (or `gh-pages`) and folder to `/website`.
3. Save changes. Your landing page will be published at `https://<your-username>.github.io/deliverybuddy/`.

---

✨ **You're all set!** Share your Vercel / GitHub Pages link with delivery executives so they can easily download and install **Delivery Buddy**!
