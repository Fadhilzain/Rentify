# Rentify — Sewa Motor & Mobil dengan Deposit Aman

Aplikasi web rental kendaraan berbasis **PHP 8.1+ (tanpa framework) + MySQL/MariaDB**, dengan tampilan sesuai desain *Rentify Fintech Mobility*. Database mengikuti **ERD**, dan logikanya memakai nama method dari **Class Diagram** (lihat `docs/KESESUAIAN-ERD-CLASS-DIAGRAM.md`).

## Menjalankan di VS Code (Windows + XAMPP)

1. **Ekstrak** `rentify.zip`, lalu di VS Code pilih **File → Open Folder** dan buka folder `rentify` (folder yang berisi `app`, `public`, dan `database`).
2. Buka **XAMPP Control Panel**, lalu klik **Start** pada **MySQL**. Apache boleh ikut dinyalakan untuk phpMyAdmin.
3. Impor database: buka `http://localhost/phpmyadmin` → tab **Import** → pilih **`database/rentify.sql`** → **Import**.
   > File ini otomatis menghapus dan membuat ulang database `rentify`, termasuk menimpa versi lama.
4. Jalankan server dengan **salah satu** cara berikut:
   - Tekan **`Ctrl + Shift + B`**. Task *Rentify: Jalankan server* sudah disiapkan di `.vscode/tasks.json`.
   - Atau buka **Terminal → New Terminal** lalu ketik:
     ```powershell
     C:\xampp\php\php.exe -S localhost:8000 -t public
     ```
5. Buka **http://localhost:8000**. Untuk menghentikan server, tekan `Ctrl + C` di terminal.

Jika XAMPP terpasang di lokasi lain (misalnya `D:\xampp`), ubah path `php.exe` di `.vscode/tasks.json`. Jika MySQL memakai password, salin `app/config/config.example.php` menjadi `app/config/config.php` lalu isi `'pass'`.

Ekstensi yang disarankan (VS Code akan menawarkannya otomatis): PHP Intelephense, Tailwind CSS IntelliSense, PHP Debug, dan SQLTools.

### Akun demo

| Peran | Email | Kata sandi |
|---|---|---|
| Admin (Pengelola Armada) | `admin@rentify.id` | `Admin#2026` |
| Penyewa | `penyewa@rentify.id` | `Rentify#2026` |
| Penyewa lain | `siti@`, `dimas@`, `jessica@`, `hendra@`, `rina@` (menunggu verifikasi), `agus@` (ditolak) + `rentify.id` | `Rentify#2026` |

Tanggal di data demo dihitung dari waktu impor, sehingga selalu ada unit yang sedang disewa, jatuh tempo kembali, menunggu pembayaran (dengan hitung mundur), dan menunggu verifikasi.

## Fitur

**Penyewa:** beranda, katalog dengan filter (jenis, kategori, tarif, transmisi, bahan bakar, kursi, jadwal), detail unit dengan simulasi biaya langsung, checkout (verifikasi KTP/SIM + rincian biaya), pembayaran (Virtual Account BCA/Mandiri/BNI/BRI, QRIS, kartu kredit, transfer manual + unggah bukti) dengan hitung mundur, Reservasi Saya, Kelola Deposit & Refund, Bantuan & Syarat SIM, profil (kontak, rekening refund, dokumen, sandi), pendaftaran 3 tahap, lupa/atur ulang sandi.

**Admin:** Ringkasan Unit, Serah Terima & Reservasi (serah terima awal, inspeksi pengembalian dengan kalkulator denda langsung, settlement/refund deposit, cetak BAST), Verifikasi Pembayaran, Rekonsiliasi Saldo Deposit, Validasi SIM & KTP, Kendaraan (CRUD + foto + status), Kategori (CRUD).

## Aturan bisnis (`app/config/config.example.php` → `aturan`)

- **Biaya sewa**: per jam (dibulatkan ke atas). Setiap 24 jam penuh memakai paket harian = 8 × tarif per jam; sisa jam × tarif per jam, maksimal 1 paket. Ada diskon long-trip 10% untuk sewa ≥ 72 jam.
- **Pembayaran pertama** = biaya sewa + deposit, dan harus dibayar dalam **60 menit**. Lewat dari itu reservasi otomatis kedaluwarsa.
- **Syarat sewa**: akun terverifikasi admin, SIM A untuk mobil / SIM C untuk motor, SIM berlaku sampai sewa selesai, dan rekening refund sudah diisi.
- **Denda**: terlambat lebih dari 60 menit dikenai tarif per jam untuk setiap jam keterlambatan. Selisih BBM dikenai Rp 4.800/1% (mobil) atau Rp 1.000/1% (motor). Denda kerusakan ditetapkan sesuai inspeksi.
- **Deposit**: potongan = total denda, maksimal sebesar deposit. Kekurangannya ditagihkan sebagai pembayaran `denda`. Status akhir deposit: dikembalikan / dikembalikan sebagian / hangus.
- Unit yang dikembalikan dalam kondisi **rusak berat** otomatis berstatus *perawatan*.

## Struktur folder

```
rentify/
├── .vscode/            tasks (Ctrl+Shift+B), launch (Xdebug), settings, ekstensi
├── app/
│   ├── config/         config.example.php (salin ke config.php bila perlu)
│   ├── core/           Router, Database (PDO), Auth, Csrf, Validator, Upload, helpers
│   ├── models/         User, Admin, Kategori, Kendaraan, Reservasi, Pembayaran, Deposit, Transaksi
│   ├── controllers/    halaman penyewa + admin/
│   ├── views/          layouts, partials, halaman (PHP + Tailwind)
│   └── routes.php      seluruh rute (?r=...)
├── database/           schema.sql, seed.sql, rentify.sql (gabungan — impor yang ini)
├── docs/               pemetaan ERD & class diagram
├── public/             index.php (front controller), assets (CSS, JS, font lokal), foto kendaraan
├── resources/          sumber Tailwind (opsional untuk build ulang CSS)
├── storage/uploads/    dokumen KTP/SIM & bukti bayar (privat, tidak bisa diakses langsung)
└── cli/kedaluwarsa.php
```

## Keamanan

Aplikasi memakai prepared statement PDO di semua query, token CSRF di setiap form POST, kata sandi bcrypt, dan regenerasi sesi saat login. Unggahan divalidasi berdasarkan isi berkas (bukan ekstensinya). Dokumen privat hanya bisa dibuka pemiliknya dan admin, dan sesi penyewa terpisah dari sesi admin. Saldo dan jadwal diproses dalam transaksi database dengan penguncian baris (`SELECT … FOR UPDATE`).

## Opsional: build ulang CSS

CSS sudah jadi di `public/assets/css/app.css`. Kalau Anda mengubah class Tailwind di view, jalankan task **Rentify: Build ulang CSS** (butuh Node.js), atau jalankan `cd resources && npm install && npm run build:css`.

## Catatan versi lokal

Gateway pembayaran dan pengiriman email disimulasikan. Nomor VA dan QRIS hanya ilustrasi, dan semua pembayaran diverifikasi admin. Pada `env = local`, tautan reset sandi ditampilkan di layar (sekaligus dicatat di log PHP).
