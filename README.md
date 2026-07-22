<div align="center">

<img
  width="140px"
  src=".github/assets/logo.svg"
  alt="Bear MicroG Logo"
/>

<h1>Bear MicroG</h1>
<p><b>Google Play Services แบบ Open Source สำหรับ Android</b></p>

[![Website](https://img.shields.io/badge/Website-0a0a0a?style=for-the-badge&logo=googlechrome&logoColor=white)](https://bearappth.github.io)
[![GitHub](https://img.shields.io/badge/GitHub-0a0a0a?style=for-the-badge&logo=github&logoColor=white)](https://github.com/BearAppTH/MicroG-RE)
[![Latest Release](https://img.shields.io/github/v/release/BearAppTH/MicroG-RE?style=for-the-badge&color=4f8ef7&label=Latest)](https://github.com/BearAppTH/MicroG-RE/releases/latest)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge)](LICENSE)
[![Min SDK](https://img.shields.io/badge/Android-10%2B-green?style=for-the-badge&logo=android&logoColor=white)](https://github.com/BearAppTH/MicroG-RE)
[![Build](https://img.shields.io/github/actions/workflow/status/BearAppTH/MicroG-RE/build.yml?branch=main&style=for-the-badge&label=Build)](https://github.com/BearAppTH/MicroG-RE/actions/workflows/build.yml)

<br/>

<a href="https://github.com/BearAppTH/MicroG-RE/releases/latest" title="Download Bear MicroG">
  <img
    src="https://img.shields.io/badge/⬇️%20Download%20Bear%20MicroG-4f8ef7?style=for-the-badge&logoColor=white"
    alt="Download Bear MicroG"
    width="260"
  />
</a>

</div>

&nbsp;

## ⚙️ Bear MicroG คืออะไร?

**Bear MicroG** คือระบบทดแทน Google Play Services แบบ Open Source ที่พัฒนาและดูแลรักษาโดย **BearAppTH** ช่วยให้แอป Android สามารถใช้งาน Google Services ได้โดยไม่ต้องพึ่งพา Google Play Services ต้นฉบับ และไม่ต้องการสิทธิ์ root

- ✅ ติดตั้งและใช้งานได้โดยไม่ต้อง root
- ✅ รองรับ Google Account Authentication
- ✅ Push Notifications ผ่าน GCM/FCM
- ✅ รองรับ Google Cast (Chromecast)
- ✅ UI ออกแบบด้วย Material 3
- ✅ รองรับ Android 10 – Android 17

Bear MicroG ใช้ Package name `app.bear.android.gms` แทน `com.google.android.gms` ทำให้ทำงานควบคู่กับ Google Play Services ต้นฉบับได้โดยไม่ขัดแย้ง และรองรับเฉพาะแอปที่ patch ด้วย GmsCore support

&nbsp;

## 📦 ฟีเจอร์ที่รองรับ

| ฟีเจอร์ | สถานะ |
|---------|-------|
| Google Account Sign-in | ✅ รองรับ |
| Push Notifications (GCM/FCM) | ✅ รองรับ |
| Device Check-in | ✅ รองรับ |
| Google People / Contacts Sync | ✅ รองรับ |
| Google Cast (Chromecast) | ✅ รองรับ |
| Instance ID / Firebase IID | ✅ รองรับ |
| GServices Provider | ✅ รองรับ |
| Conscrypt TLS Provider | ✅ รองรับ |
| Huawei Device Flavor (`huawei`, suffix `-hw`) | ✅ รองรับ |
| SafetyNet / Play Integrity | ⚠️ Stub เท่านั้น (`play-services-gmscompliance` ตอบ `compliant = true` เสมอ ไม่ได้เชื่อมกับ Google attestation จริง — แอปที่เช็คแบบเข้มงวดจะยังผ่านไม่ได้) |
| Google Pay | ❌ ไม่รองรับ |

&nbsp;

## 📸 ภาพหน้าจอ

> ยังไม่มีภาพหน้าจอในเอกสารนี้ — ทีมดูแลควรแนบภาพ UI จริงจากแอป (เช่น หน้า Sign-in, หน้า Account management, หน้า Cast) ไว้ที่ `.github/assets/` แล้วอ้างอิงด้วย `![alt](.github/assets/screenshot-x.png)` แทนข้อความนี้

&nbsp;

## 📥 วิธีติดตั้ง

1. ดาวน์โหลดไฟล์ `.apk` ล่าสุดจาก [Releases](https://github.com/BearAppTH/MicroG-RE/releases/latest)
2. เปิดไฟล์ APK บนอุปกรณ์ Android แล้วกด **ติดตั้ง**
3. หากอัปเดตจากเวอร์ชันเดิม ให้กด **อัพเดท**
4. เปิดแอป **Bear MicroG** แล้วเพิ่ม Google Account

> **หมายเหตุ:** ต้องใช้งานร่วมกับแอปที่ผ่านการ patch ด้วย GmsCore support เท่านั้น

&nbsp;

## ❓ คำถามที่พบบ่อย

**Q: Bear MicroG แตกต่างจาก Google Play Services อย่างไร?**
> Bear MicroG เป็น Open Source และทำงานแบบ user-level ไม่ฝังอยู่ในระบบ ไม่รวบรวมข้อมูล และสามารถตรวจสอบ Source Code ได้ทั้งหมดที่ repository นี้

**Q: ต้อง root เครื่องไหม?**
> ไม่ต้อง ติดตั้งเหมือนแอปทั่วไปได้เลย

**Q: รองรับ Android เวอร์ชันอะไรบ้าง?**
> Android 10 (API 29) ขึ้นไป รองรับถึง Android 17 (API 37)

**Q: ใช้งานได้กับแอปอะไรบ้าง?**
> ใช้งานได้กับแอปที่ผ่านการ patch ด้วย GmsCore support patch ซึ่งพัฒนาโดยทีม BearAppTH

**Q: ปลอดภัยไหม?**
> Bear MicroG เป็น Open Source ทั้งหมด ตรวจสอบ Source Code ได้เองที่ repository นี้

&nbsp;

## ⚠️ ข้อจำกัดที่ทราบอยู่แล้ว

- แอปที่มีการตรวจสอบ SafetyNet / Play Integrity แบบเข้มงวด (เช่นแอปธนาคาร) **ยังใช้งานไม่ได้จริง** แม้ `play-services-gmscompliance` จะตอบกลับว่า compliant เสมอ เพราะไม่ใช่ attestation ที่เซ็นรับรองโดย Google จริง แอปฝั่งเซิร์ฟเวอร์ตรวจสอบแล้วจะปฏิเสธ
- แอปที่ต้องพึ่ง Google Pay จะใช้งานไม่ได้
- ต้องใช้คู่กับแอปที่ผ่านการ patch ด้วย GmsCore support เท่านั้น แอปทั่วไปที่ยังไม่ patch จะไม่สามารถเชื่อมต่อกับ Bear MicroG ได้

&nbsp;

## 📜 License

Bear MicroG เผยแพร่ภายใต้ Apache License 2.0

    Copyright 2026 BearAppTH

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
