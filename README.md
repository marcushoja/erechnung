# E-Rechnungs-Generator

Pragmatische Web-App zum schnellen Erstellen von EN-16931-konformen E-Rechnungen.

Der Fokus liegt auf **Zeitersparnis im Alltag**: bestehende Rechnungen importieren, als Vorlage wiederverwenden und neue Rechnungen ohne erneute Dateneingabe erzeugen.

## Warum dieses Projekt?

Wenn du regelmaessig aehnliche Rechnungen schreibst, willst du nicht jedes Mal Absender, Empfaenger und Positionen neu tippen. Dieses Tool hilft dir dabei:

- bestehende `ZUGFeRD`- oder `XRechnung`-Dateien importieren
- Daten direkt im Formular weiterverwenden
- neue Rechnung als `ZUGFeRD (PDF + XML)` oder `XRechnung (XML)` exportieren

## Features

- Erstellung von EN-16931-konformen Rechnungen via Web-Formular
- Import von `ZUGFeRD-PDF` und `XRechnung-XML` als Vorlage
- Schnelles Wiederverwenden vorhandener Rechnungsdaten
- Dynamische Rechnungspositionen inkl. automatischer Netto/Steuer/Brutto-Berechnung
- Serverseitige Validierung mit klaren Fehlermeldungen
- Stateless Betrieb (keine Datenbank, keine persistente Speicherung)
- Optionaler HTTP Basic Auth Schutz fuer Deployments

## Tech-Stack

- Java 17
- Spring Boot
- Thymeleaf (SSR)
- Maven
- Docker (optional)

## Schnellstart (lokal)

Voraussetzungen:

- Java 17
- Maven 3.9+

Start:

```bash
mvn spring-boot:run
```

Danach im Browser aufrufen: `http://localhost:8080`

## Sicherheit / Basic Auth

Basic Auth ist per Default **deaktiviert**.

Aktivieren per Umgebungsvariablen:

```bash
APP_BASIC_ENABLED=true \
APP_BASIC_USER=myuser \
APP_BASIC_PASS='strong-password' \
mvn spring-boot:run
```

Hinweis: In produktiven Umgebungen immer ein starkes Passwort verwenden und HTTPS ueber Reverse Proxy erzwingen.

## Build

```bash
mvn clean package
```

## Docker

Image bauen:

```bash
docker build -t erechnung:latest .
```

Container starten (ohne Basic Auth):

```bash
docker run --rm -p 8080:8080 erechnung:latest
```

Container starten (mit Basic Auth):

```bash
docker run --rm -p 8080:8080 \
  -e APP_BASIC_ENABLED=true \
  -e APP_BASIC_USER=myuser \
  -e APP_BASIC_PASS='strong-password' \
  erechnung:latest
```

## Deployment-Hinweise

- App lauscht intern auf Port `8080`
- Reverse Proxy (Nginx/Traefik/Coolify) davor schalten
- TLS/HTTPS am Proxy terminieren (z. B. Let's Encrypt)
- Optional: Basic Auth in der App zusaetzlich aktivieren

## Roadmap (moeglich)

- Vorlagenverwaltung direkt in der UI
- Bessere Plausibilitaetschecks fuer Sonderfaelle
- Mehrsprachige Oberflaeche

## Lizenz

Derzeit keine Lizenzdatei hinterlegt. Wenn du das Projekt oeffentlich weitergeben willst, fuege eine passende Open-Source-Lizenz hinzu (z. B. MIT oder Apache-2.0).
