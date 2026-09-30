# MinimalOS Launcher

MinimalOS Launcher adalah **Custom Android Launcher** modern, ultra-ringan, cepat, dan distraction-free yang dibangun menggunakan **Kotlin Native Android** dan **Jetpack Compose**. 

Launcher ini dirancang untuk menggantikan launcher OEM vendor (Samsung One UI, Xiaomi HyperOS/MIUI, Oppo ColorOS, Vivo FuntouchOS, Realme UI) dengan pengalaman pengguna yang bersih, berbasis tipografi minimalis, tanpa iklan, tanpa pelacakan data, dan 100% offline-first.

---

## Fitur Utama

- **Android Default HOME Launcher**: Terdaftar dengan intent filter `CATEGORY_HOME` dan `CATEGORY_DEFAULT` agar dapat dipilih sebagai launcher bawaan.
- **Minimal Home Screen**: Tampilan waktu dan tanggal yang bersih, daftar aplikasi favorit yang dipin, kategori khusus, dan pill pencarian cepat.
- **Tipografi & Desain Minimalis**:
  - Pilihan mode tampilan: **Text Only**, **Icon + Text**, dan **Icon Only**.
  - Skala ukuran font dan ikon yang dapat disesuaikan.
- **Theme Engine**:
  - **AMOLED**: True pure black (`#000000`) untuk efisiensi baterai layar OLED/AMOLED.
  - **Minimal Dark**: Nuansa dark mode modern yang nyaman di mata.
  - **Minimal Light**: Tampilan terang dan bersih.
  - **Monochrome**: Kontras tinggi bernuansa hitam-putih.
  - **Developer**: Tema terminal bernuansa GitHub Dark & code accents.
- **App Drawer & Realtime Search**:
  - Pengelompokan alfabetik instan (A, B, C...).
  - Filter chip berdasarkan kategori (All, Work, Communication, dll).
  - Pencarian real-time tanpa lag dengan query ter-cache.
- **Kategori & Pengorganisasian Aplikasi**:
  - Buat, ubah nama, dan hapus kategori kustom.
  - Tambahkan atau hapus aplikasi ke dalam beberapa kategori.
- **Sembunyikan Aplikasi (Hidden Apps)**:
  - Sembunyikan aplikasi dari layar utama dan drawer dengan opsi pemulihan 1-tap di Pengaturan.
- **App Context Menu**:
  - Tekan lama (long press) aplikasi untuk:
    - Buka aplikasi
    - Tambah/Hapus dari Home (Favorite)
    - Ganti nama tampilan aplikasi (Rename)
    - Kelola kategori aplikasi
    - Sembunyikan aplikasi (Hide)
    - Info Aplikasi (System App Info)
    - Hapus Instalan (Uninstall)
- **Dukungan Gestur**:
  - Geser ke atas (Swipe Up): Buka App Drawer
  - Geser ke bawah (Swipe Down): Buka Pencarian Cepat
  - Tekan lama di layar kosong (Long Press): Buka Pengaturan
  - Ketuk dua kali (Double Tap): Pengaturan atau Pencarian (dapat dikonfigurasi)
- **Deteksi Perubahan Paket Otomatis**:
  - Mendeteksi pemasangan (`PACKAGE_ADDED`), pembaruan (`PACKAGE_REPLACED`), dan penghapusan (`PACKAGE_REMOVED`) aplikasi secara reaktif di latar belakang.
- **Zero Bloat & Privasi Penuh**:
  - Tanpa analitik latar belakang, tanpa akun, tanpa cloud sync eksternal. Semua data tersimpan lokal menggunakan SQLite/Room dan DataStore Preferences.

---

## Arsitektur & Teknologi

- **Bahasa**: Kotlin (100% Coroutines & Flow)
- **UI Framework**: Jetpack Compose dengan Material 3 Design
- **Arsitektur**: MVVM + Clean Architecture
- **State Management**: StateFlow dengan `collectAsStateWithLifecycle`
- **Penyimpanan Data**:
  - **Room Database** (dengan KSP): Menyimpan data terstruktur (Favorit, Aplikasi Tersembunyi, Label Kustom, Kategori).
  - **Jetpack DataStore Preferences**: Menyimpan preferensi UI (Tema, Format Jam, Gestur, Ukuran Font & Ikon).
- **Optimasi Kinerja**:
  - In-memory icon caching dengan `LruCache`
  - Tidak ada query `PackageManager` berulang saat rekomposisi Compose
  - R8 Minification & Resource Shrinking pada build release

---

## Persyaratan Lingkungan (Requirements)

- **JDK**: Java Development Kit 11 atau 17+
- **Android Studio**: Android Studio Ladybug (2024.2+) atau yang lebih baru
- **Android SDK**:
  - `minSdk`: 24 (Android 7.0 Nougat)
  - `targetSdk`: 36 (Android 15+)
  - `compileSdk`: 36

---

## Cara Menjalankan & Build Project

Project menyertakan **Gradle Wrapper** lengkap sehingga tidak memerlukan instalasi Gradle global.

### 1. Build Debug APK
Untuk pengembangan lokal:
```bash
./gradlew assembleDebug
```
Untuk Windows PowerShell:
```powershell
.\gradlew.bat assembleDebug
```
Hasil APK debug akan tersedia di:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### 2. Build Release APK (R8 & Resource Shrinking)
Untuk menghasilkan APK release yang teroptimasi, terkompresi, dan di-obfuscate:
```bash
./gradlew assembleRelease
```
Untuk Windows PowerShell:
```powershell
.\gradlew.bat assembleRelease
```
Hasil APK release akan tersedia di:
```text
app/build/outputs/apk/release/app-release.apk
```

### 3. Menjalankan Unit Tests & Robolectric
```bash
./gradlew testDebugUnitTest
```

---

## Konfigurasi Release Signing

Project telah dikonfigurasi agar build release dapat dijalankan baik di lingkungan lokal maupun CI/CD:

1. Secara default, jika file keystore release tidak ditemukan, build release akan otomatis menggunakan fallback debug signing agar pengujian build lokal tetap berhasil.
2. Untuk menandatangani APK release dengan upload key Anda sendiri, tentukan variabel lingkungan berikut:
   ```bash
   export KEYSTORE_PATH="/path/ke/keystore/anda.jks"
   export STORE_PASSWORD="password_keystore"
   export KEY_PASSWORD="password_kunci"
   ```
   Atau simpan konfigurasi di luar repositori Git Anda.

---

## Cara Menjadikan MinimalOS sebagai Default Launcher

1. Pasang APK pada perangkat Android fisik atau emulator:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
2. Buka aplikasi **MinimalOS**.
3. Di bagian atas layar Pengaturan (Settings), ketuk tombol **Set as Default Launcher**.
4. Sistem Android akan membuka dialog pemilih Home App. Pilih **MinimalOS** dan pilih **Always** (Selalu).
5. Sekarang, setiap kali Anda menekan tombol Home atau melakukan gestur kembali ke Home, MinimalOS akan tampil secara langsung dan instan.
