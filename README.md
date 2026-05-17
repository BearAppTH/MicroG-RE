<div align="center">

<img
  width="160px"
  src="https://raw.githubusercontent.com/BearAppTH/bearapp-branding/main/assets/bearappth-logo/bearappth_logo_with_frame_dark.svg"
  alt="BearAppTH Logo"
/>

<h1>Bear MicroG</h1>
<p><b>microG GmsCore เวอร์ชัน BearAppTH</b></p>

[![Website](https://img.shields.io/badge/Website-0a0a0a?style=for-the-badge&logo=googlechrome&logoColor=white)](https://www.bearappth.online)
[![GitHub](https://img.shields.io/badge/GitHub-0a0a0a?style=for-the-badge&logo=github&logoColor=white)](https://github.com/BearAppTH/MicroG-RE)
[![Latest Release](https://img.shields.io/github/v/release/BearAppTH/MicroG-RE?style=for-the-badge&color=4f8ef7&label=Latest)](https://github.com/BearAppTH/MicroG-RE/releases/latest)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge)](LICENSE)
[![Min SDK](https://img.shields.io/badge/Android-7.0%2B-green?style=for-the-badge&logo=android&logoColor=white)](https://github.com/BearAppTH/MicroG-RE)

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

## ⚙️ Bear MicroG (GmsCore)

**Bear MicroG** คือ Fork ของ [microG GmsCore](https://github.com/microg/GmsCore) ที่พัฒนาโดยทีม **BearAppTH** เพื่อใช้งานร่วมกับแอปที่ผ่านการ patch แล้ว โดยไม่ต้องการสิทธิ์ root

- ✅ ใช้งานได้โดยไม่ต้อง root
- ✅ รองรับ Google Account Authentication
- ✅ ทดแทน Google Play Services
- ✅ ออกแบบ UI ด้วย Material 3 Expressive
- ✅ รันภายใต้ package name `app.bear.android.gms`

Repository นี้ดัดแปลงมาจาก [microG GmsCore](https://github.com/microg/GmsCore) เพื่อรองรับแอปที่ patch แล้วภายใต้ชื่อ package ทางเลือก โดยใช้ **GmsCore support** patch เพื่อเปิดใช้งาน Google account authentication และ services แทนที่ Google Play Services

&nbsp;

## 📸 ภาพหน้าจอ

> ภาพหน้าจอจะถูกเพิ่มในเวอร์ชันถัดไป

&nbsp;

## 📥 วิธีติดตั้ง

1. ดาวน์โหลดไฟล์ APK ล่าสุดจาก [Releases](https://github.com/BearAppTH/MicroG-RE/releases/latest)
2. เปิดไฟล์ APK บนอุปกรณ์ Android แล้วกด **ติดตั้ง**
3. หากติดตั้งทับเวอร์ชันเดิม ให้กด **อัพเดท**
4. เปิดแอป **Bear MicroG** แล้วเพิ่ม Google Account

> **หมายเหตุ:** ต้องใช้งานร่วมกับแอปที่ผ่านการ patch ด้วย GmsCore support เท่านั้น

&nbsp;

## ❓ คำถามที่พบบ่อย (FAQ)

**Q: Bear MicroG แตกต่างจาก microG ต้นฉบับอย่างไร?**
> Bear MicroG ใช้ package name `app.bear.android.gms` แทน `com.google.android.gms` เพื่อให้ทำงานร่วมกับแอปที่ patch แล้วโดยเฉพาะ ไม่ได้แทนที่ Google Play Services บนระบบ

**Q: ต้อง root เครื่องไหม?**
> ไม่ต้อง root ติดตั้งเหมือนแอปทั่วไปได้เลย

**Q: รองรับ Android เวอร์ชันอะไรบ้าง?**
> Android 7.0 (API 24) ขึ้นไป

**Q: ใช้งานได้กับแอปอะไรบ้าง?**
> ใช้งานได้กับแอปที่ผ่านการ patch ด้วย GmsCore support patch (เช่น แอปที่ patch โดยทีม BearAppTH)

**Q: ปลอดภัยไหม?**
> Bear MicroG เป็น open source สามารถตรวจสอบ source code ได้ที่ repository นี้ทั้งหมด

&nbsp;

## 🔧 สำหรับนักพัฒนา

ดูรายละเอียดการ setup และการส่ง contribution ได้ที่ [CONTRIBUTING.md](CONTRIBUTING.md)

สำหรับการตั้งค่า GitHub Secrets ที่จำเป็นสำหรับ release workflow:

| Secret | คำอธิบาย |
|--------|----------|
| `KEYSTORE_B64` | Keystore ที่ encode ด้วย Base64 (`base64 -w0 release.keystore`) |
| `KEYSTORE_ENTRY_ALIAS` | Key alias ใน Keystore |
| `KEYSTORE_PASSWORD` | Password ของ Keystore |
| `KEYSTORE_ENTRY_PASSWORD` | Password ของ Key entry |
| `GradleEncryptionKey` | Key สำหรับ Gradle build cache encryption |

&nbsp;

## 🤝 Credits

- [microG Project](https://github.com/microg) สำหรับ GmsCore ทางเลือกของ Play Services — [wiki](https://github.com/microg/GmsCore/wiki)
- [MorpheApp](https://github.com/MorpheApp/MicroG-RE) สำหรับ GmsCore Redesign ต้นฉบับ
- [Shadow578](https://github.com/shadow578) และ [ReVanced Team](https://github.com/ReVanced) สำหรับ ReVanced GmsCore group ID vendor
- [AyushTNM](https://github.com/ayushTNM) สำหรับ implementations และ ideas

## 📜 License

    Copyright 2013-2025 microG Project Team

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
