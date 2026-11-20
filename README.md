# Shiori

Aplicativo Android open source e extensível de leitura e organização de obras (mangás, manhwas, manhuas, webtoons, novels, light novels e livros), com funcionamento local em SQLite.

## O que já existe

- **`:core:domain`:** modelos de domínio (`Work`, `Edition`, `LibraryEntry`, `ReadingProgress`), filtros (`LibraryFilterEngine`) e contrato `LibraryRepository`.
- **`:core:data`:** persistência local em SQLite com Room, implementação `RoomLibraryRepository` e exportação/importação JSON (`ShioriExportSerializer`).
- **`:feature:library`:** biblioteca em Jetpack Compose com busca, filtros (status, formato, favoritos), cadastro, incremento de capítulo, tela de detalhes/edição e exportação/importação JSON pelo seletor de arquivos do Android.
- **`:app`:** navegação Compose e ponto de entrada Android (`dev.shiori.app`, `minSdk 24`, `targetSdk 35`).

## Requisitos para compilar

- Android Studio (com Android SDK 35)
- JDK 21 (para execução do Gradle/AGP)
