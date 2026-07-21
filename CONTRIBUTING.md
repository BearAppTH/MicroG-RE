# Contributing to Bear MicroG

ขอบคุณที่สนใจร่วมพัฒนา Bear MicroG! เอกสารนี้อธิบายวิธีการ contribute ให้กับโปรเจกต์

การมีส่วนร่วมในโปรเจกต์นี้อยู่ภายใต้ [Code of Conduct](CODE_OF_CONDUCT.md) กรุณาอ่านก่อนเริ่ม contribute

## 🛠️ การตั้งค่า Development Environment

### ความต้องการ
- Android Studio Hedgehog (2023.1.1) หรือใหม่กว่า
- Java 21
- Android SDK (Compile SDK 37)
- Git

### การ Build โปรเจกต์

1. Fork และ clone repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/MicroG-RE.git
   cd MicroG-RE
   ```

2. Build debug APK:
   ```bash
   ./gradlew assembleDefaultDebug
   ```

3. ไฟล์ APK จะอยู่ที่:
   ```
   play-services-core/build/outputs/apk/default/debug/
   ```

## 🌿 Branching Strategy

| Branch | ใช้สำหรับ |
|--------|----------|
| `main` | Stable releases เท่านั้น |
| `dev` | การพัฒนาหลัก — ส่ง PR มาที่นี่ |
| `feature/*` | Feature ใหม่ |
| `fix/*` | Bug fixes |

## 📝 การส่ง Pull Request

1. Fork repository และสร้าง branch จาก `dev`:
   ```bash
   git checkout -b feature/your-feature dev
   ```

2. ทำการเปลี่ยนแปลงและ commit:
   ```bash
   git commit -m "feat: add your feature"
   ```

3. Push ขึ้น fork ของคุณ:
   ```bash
   git push origin feature/your-feature
   ```

4. เปิด Pull Request ไปที่ branch `dev` ของ repository หลัก

### Commit Message Format

ใช้รูปแบบ [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>: <description>
```

| Type | ใช้เมื่อ |
|------|----------|
| `feat` | เพิ่ม feature ใหม่ |
| `fix` | แก้ bug |
| `docs` | แก้ไขเอกสาร |
| `ci` | แก้ไข CI/CD |
| `refactor` | refactor โค้ด |
| `chore` | งานอื่น ๆ |

ตัวอย่าง:
- `feat: add push notification support`
- `fix: resolve auth token refresh issue`
- `docs: update installation guide`

## 🐛 การรายงาน Bug

ใช้ [Bug Report Template](https://github.com/BearAppTH/MicroG-RE/issues/new?template=bug_report.yml)

## 💡 การขอ Feature ใหม่

ใช้ [Feature Request Template](https://github.com/BearAppTH/MicroG-RE/issues/new?template=feature_request.yml)

## 🌍 Translation / การแปลภาษา

โปรเจกต์นี้ใช้ [Crowdin](https://crowdin.com) สำหรับการแปลภาษา สามารถช่วยแปลได้ผ่าน Crowdin platform
