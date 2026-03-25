# E-Rechnungs-Generator (Spring Boot)

Stateless Web-App zur manuellen Erstellung von EN-16931-konformen E-Rechnungen für Freiberufler.

## Funktionen

- SSR-Frontend mit Thymeleaf (Absender, Empfänger, Metadaten, dynamische Positionen)
- Import bestehender ZUGFeRD-PDF/XRechnung-XML als Formular-Template
- Automatische Netto-/Steuer-/Brutto-Berechnung
- Export als:
  - `ZUGFeRD (PDF)` mit eingebettetem XML
  - `XRechnung (XML)`
- Serverseitige Validierung mit klaren Fehlermeldungen
- HTTP Basic Auth (Credentials via ENV)
- Keine Datenbank, keine persistente Speicherung

## Lokaler Start

```bash
mvn spring-boot:run
```

Standardzugang (anpassen!):

- Benutzer: `freelancer`
- Passwort: `change-me-now`

Override per ENV:

```bash
APP_BASIC_USER=myuser APP_BASIC_PASS='strong-password' mvn spring-boot:run
```

## Build

```bash
mvn clean package
```

## Docker

Build:

```bash
docker build -t erechnung:latest .
```

Run:

```bash
docker run --rm -p 8080:8080 \
  -e APP_BASIC_USER=myuser \
  -e APP_BASIC_PASS='strong-password' \
  erechnung:latest
```

## Deployment-Hinweis (Hetzner)

- App intern auf Port `8080`
- Reverse-Proxy (Nginx/Traefik) davor
- HTTPS via Let's Encrypt am Proxy terminieren
