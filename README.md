# Yeniproje (Android)

Bu depo, Android Studio ile geliştirilmiş bir Android uygulamasını içerir. Modül yapısı klasik Gradle/Android projesidir (app modülü).

## Gereksinimler
- Android Studio (son sürüm önerilir)
- JDK 17
- Gradle Wrapper (repo ile birlikte gelir)

## Kurulum
1. Depoyu klonlayın:
   - HTTPS: `git clone https://github.com/ErenPervan/yeniproje.git`
   - SSH: `git clone git@github.com:ErenPervan/yeniproje.git`
2. Android Studio ile projeyi açın.
3. Gerekli bağımlılıklar otomatik indirilecektir.

## Derleme ve Çalıştırma
- Komut satırından derleme (Windows):
  - `gradlew.bat assembleDebug`
- Testleri çalıştırma:
  - `gradlew.bat test`
- IDE üzerinden “Run” ile cihaza/emülatöre çalıştırabilirsiniz.

## Notlar
- Firebase kullanımı varsa `app/google-services.json` dosyası projede bulunur.
- Keystore dosyaları ve yerel makine ayarları (local.properties) commit dışıdır.

## Katkı
- Değişiklik önerileri için Pull Request açabilirsiniz.
- Basit düzeyde kod stili: mevcut yapıya uyumlu, küçük ve odaklı değişiklikler tercih edilir.
