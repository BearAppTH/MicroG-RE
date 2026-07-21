# Security Policy

## เวอร์ชันที่รองรับ

| เวอร์ชัน | รองรับ |
|---------|--------|
| 2.x.x   | ✅ ใช่  |
| 1.x.x   | ❌ ไม่  |

## การรายงาน Vulnerability

หากพบช่องโหว่ด้านความปลอดภัย **อย่าเปิด public issue**

กรุณาแจ้งโดยตรงผ่านทาง:
- **GitHub Private Advisory:** [Report a vulnerability](https://github.com/BearAppTH/MicroG-RE/security/advisories/new)

### ข้อมูลที่ควรระบุ
- คำอธิบายของช่องโหว่
- ขั้นตอนการ reproduce
- ผลกระทบที่อาจเกิดขึ้น
- แนวทางแก้ไข (ถ้ามี)

เราจะตอบกลับภายใน **72 ชั่วโมง**

## Scope

ช่องโหว่ที่อยู่ใน scope:
- Authentication bypass
- Data leakage ผ่าน GmsCore API
- Privilege escalation
- การดักจับข้อมูล Google Account
- Deserialization ที่ไม่ปลอดภัยใน SafeParcel (parcelable ที่ generate โดย `safe-parcel-processor`)
