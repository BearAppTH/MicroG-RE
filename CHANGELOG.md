# Changelog

All notable changes to Bear MicroG are documented in this file.

Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

> ⚠️ **หมายเหตุถึงผู้ดูแล:** เวอร์ชันปัจจุบันใน `build.gradle` คือ `3.6.1` แต่ไฟล์นี้มีบันทึกล่าสุดถึงแค่ `2.0.0` การ export โปรเจกต์ครั้งนี้ไม่มี git history แนบมาด้วย จึงไม่สามารถดึงรายการ commit จริงระหว่าง 2.0.1–3.6.1 มาเติมให้อัตโนมัติได้ กรุณารัน `git log --oneline v2.0.0..v3.6.1` (หรือเทียบ tag ที่มีจริง) แล้วเติมเนื้อหาแต่ละเวอร์ชันด้านล่างนี้แทนหมายเหตุ TODO

---

## [3.6.1] — TODO: ใส่วันที่จริง

### TODO
- เติมรายการเปลี่ยนแปลงจริงจาก git log/PR ระหว่าง 2.0.0 ถึง 3.6.1

---

## [2.0.0] — 2026-05-14

### Added
- Google Account Authentication support
- Material 3 Expressive UI redesign
- Base package name `app.bear.android.gms`
- Crowdin translation integration
- Huawei device flavor (`-hw` suffix)
- CI/CD workflows: build, release, back-merge, dependency submission
- Issue templates: bug report, feature request, discussion

---

## [1.0.2] — 2026-05-13

### Fixed
- Bug fixes and stability improvements

---

## [1.0.1] — 2026-05-12

### Added
- Initial public release
- Basic GmsCore support under custom package name `app.bear.android.gms`
- No root required
- Android 7.0+ (API 24) compatibility
