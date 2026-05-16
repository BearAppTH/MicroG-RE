# Contributing to Bear MicroG

ขอบคุณที่สนใจร่วมพัฒนา Bear MicroG! กรุณาอ่านแนวทางต่อไปนี้ก่อนส่ง contribution

## วิธีการมีส่วนร่วม

### รายงาน Bug
- ค้นหา [issue ที่มีอยู่แล้ว](https://github.com/BearAppTH/MicroG-RE/issues?q=is%3Aissue) ก่อน
- ใช้ [Bug Report template](https://github.com/BearAppTH/MicroG-RE/issues/new?template=bug_report.yml)
- ระบุขั้นตอน reproduce, เวอร์ชัน Android, และ device

### เสนอ Feature
- ใช้ [Feature Request template](https://github.com/BearAppTH/MicroG-RE/issues/new?template=feature_request.yml)
- อธิบาย use case และประโยชน์ที่จะได้รับ

### ส่ง Pull Request

1. **Fork** repository นี้
2. **สร้าง branch** จาก `dev`:
   ```bash
   git checkout -b feature/your-feature-name dev
   ```
3. **ทำการเปลี่ยนแปลง** โดยปฏิบัติตาม coding style ของโปรเจกต์
4. **Build และทดสอบ**:
   ```bash
   ./gradlew assemble check
   ```
5. **Commit** ด้วย message ที่ชัดเจน (ดูรูปแบบด้านล่าง)
6. **Push** และเปิด PR ไปที่ branch `dev`

## Commit Message Format

```
type: short description

[optional body]
```

ประเภท (`type`):
- `feat` — feature ใหม่
- `fix` — แก้ bug
- `refactor` — refactor โค้ด (ไม่ใช่ fix หรือ feature)
- `docs` — อัพเดท documentation
- `chore` — งาน maintenance (update dependency ฯลฯ)
- `ci` — เปลี่ยน CI/CD workflow

## การ Setup สภาพแวดล้อม

### ความต้องการ
- Android Studio Meerkat (2024.3) หรือใหม่กว่า
- JDK 21
- Android SDK (minSdk 24, compileSdk 36)

### การ Build
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# รัน lint และ tests
./gradlew check
```

## แนวทางการเขียนโค้ด

- ใช้ Kotlin เป็นหลัก
- ปฏิบัติตาม Android Kotlin Style Guide
- ไม่แนะนำให้เพิ่ม dependency ใหม่โดยไม่จำเป็น
- UI ใช้ Material 3 Expressive

## License

การส่ง contribution ถือว่าคุณยอมรับว่าโค้ดของคุณจะถูก license ภายใต้ [Apache License 2.0](LICENSE)
