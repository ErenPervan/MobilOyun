# Mobil Oyun (Android)

Basit ve akıcı bir hafıza eşleştirme oyunu. Kartları çevir, eşleşen çiftleri bul ve süreye karşı yarış. Proje Android Studio ile geliştirildi; modüler yapı app altındadır.

## Özellikler
- Eşleştirme temelli oyun mekaniği (kart çiftleri)
- Ses efektleri (doğru/yanlış/click) ve görsel geri bildirim
- Farklı ekran yoğunlukları için uyumlu görseller
- Yerel veritabanı yardımıyla skor takibi (uygulama içi)

## Ekran Görüntüleri

![ Oyun İçi Görsel](Image1.jpeg)
![Oyun İçi Görsel](Image2.jpeg)



## Gereksinimler
- Android Studio (güncel sürüm tavsiye edilir)
- JDK 17
- Gradle Wrapper (proje ile birlikte gelir)
- En az Android 8.0 (API 26) hedefi önerilir

## Kurulum
1. Depoyu klonlayın:
  - HTTPS: https://github.com/ErenPervan/MobilOyun.git
  - SSH: git@github.com:ErenPervan/MobilOyun.git
2. Android Studio ile projeyi açın.
3. Gerekli bağımlılıklar otomatik indirilecektir.

## Derleme ve Çalıştırma
- Komut satırından derleme (Windows):
  - `gradlew.bat assembleDebug`
- Testleri çalıştırma:
  - `gradlew.bat test`
- IDE üzerinden Run ile cihaza/emülatöre çalıştırabilirsiniz.

## Katkı ve Commit Mesajları
- Commit mesajları Türkçe ve öz olmalıdır (ör. "Skor tablosu filtreleme eklendi").
- Anlamlı başlık kullanın ve gerekirse açıklama kısmında detay verin.
- Büyük değişikliklerde ayrı commit’ler tercih edin.

## Lisans
Bu repo eğitim ve örnek amaçlıdır. Gerektiğinde kendi lisansınızı ekleyin.

## Proje Yapısı (Kısaca)
- `app/src/main/java/tr/edu/atauni/yeniproje/` oyun ve yardımcı sınıflar
- `app/src/main/res/layout/` arayüz düzenleri (ör. `activity_oyun.xml`)
- `app/src/main/res/drawable/` kart görselleri ve arkaplan
- `app/src/main/res/raw/` ses efektleri

## Notlar
- Firebase yapılandırması varsa `app/google-services.json` dosyası bulunur.
- Keystore dosyaları ve yerel makine ayarları (`local.properties`) commit dışıdır.
- Windows ortamında CRLF uyarıları görülebilir; işlevselliği etkilemez.

## 