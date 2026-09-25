# חבילת Mobile ל-GitHub

ה-ZIP הזה מכיל את גרסאות Android ו-iOS יחד, כולל שני קבצי GitHub Actions.

## העלאה ל-GitHub

1. צור repository חדש וריק.
2. חלץ את ה-ZIP.
3. העלה ל-GitHub את **התוכן שבתוך התיקייה**, כך שבתיקיית השורש יופיעו:
   - `Android`
   - `FC Tools iOS`
   - `shared`
   - `.github/workflows`
4. היכנס ללשונית `Actions`.
5. להפעלת Android: בחר `Build Android APK`, לחץ `Run workflow`, ולאחר סיום הורד את `fc-tools-android-debug`.
6. להפעלת iOS: בחר `Build FC Tools iOS IPA`, לחץ `Run workflow`, ולאחר סיום הורד את `FC-Tools-unsigned-IPA`.

אין צורך להוסיף סיסמאות או Certificates עבור ה-builds האלה. גרסת iOS היא IPA לא חתומה, ולכן התקנה על iPhone מתבצעת באמצעות Sideloadly או כלי חתימה דומה.
