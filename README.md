# Shiori

Aplicativo Android open source e extensível de leitura e organização de obras (mangás, manhwas, manhuas, webtoons, novels, light novels e livros), com funcionamento local em SQLite.

## O que já existe

- **`:core:domain`:** modelos de domínio (`Work`, `Edition`, `LibraryEntry`, `ReadingProgress`) e contrato `LibraryRepository`.
- **`:core:data`:** persistência local em SQLite com Room, implementação `RoomLibraryRepository` e serialização JSON (`ShioriExportSerializer`).
- **`:feature:library`:** tela inicial da biblioteca em Jetpack Compose com filtros por status, cadastro de obras e atualização de capítulo.
- **`:app`:** ponto de entrada Android (`dev.shiori.app`, `minSdk 24`, `targetSdk 35`).

## Requisitos para compilar

- Android Studio (com Android SDK 35)
- JDK 21
