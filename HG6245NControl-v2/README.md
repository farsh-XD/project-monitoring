# HG6245N Control (Support Android 10 / API 29+)

Aplikasi kontrol Android modern untuk memantau dan mengelola ONT/Router **FiberHome HG6245N** (`192.168.1.100`), dikonfigurasi agar kompatibel penuh dengan **Android 10 (API 29)** hingga Android 15 (API 35).

## Konfigurasi Kompatibilitas Android 10
- `minSdk = 29` (Android 10)
- `targetSdk = 35`
- Cleartext HTTP traffic (`http://192.168.1.100`) diizinkan secara eksplisit via `network_security_config.xml` dan `android:usesCleartextTraffic="true"` agar tidak diblokir oleh kebijakan keamanan jaringan default Android 10.

## Cara Build Menjadi File `.apk`
Lingkungan sandbox ini tidak memiliki compiler Android SDK biner, sehingga paket ini menyertakan **2 jalur build otomatis** agar Anda bisa menghasilkan file `.apk` dengan mudah:

1. **Via GitHub Actions (Tanpa PC / Langsung di HP)**:
   - Buat repositori baru di GitHub dan unggah seluruh isi folder ini.
   - Buka tab **Actions** di GitHub, pilih **Build Android APK (Android 10+ Compatible)**, lalu klik **Run workflow**.
   - Setelah selesai (~2-3 menit), unduh file `HG6245NControl-android10-debug.zip` yang berisi `app-debug.apk` siap instal di Android 10.
2. **Via Termux / AndroidIDE / Android Studio**:
   - Ekstrak folder ini, lalu jalankan `gradle assembleDebug` di direktori root proyek.
   - File `.apk` tersedia di `app/build/outputs/apk/debug/app-debug.apk`.
