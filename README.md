# Anteman Java26 Lab 01
# Laboration 1 — Elpris-analysator

En CLI-applikation i Java för att analysera dagens elpriser från elprisetjustnu.se.

## Implementation

Applikationen är uppdelad enligt enkel separation av ansvarsområden (Separation of Concerns):
- `model`: Innehåller `QuarterlyData` som mappar JSON-strukturen från API:et.
- `service`: Innehåller logik för nätverk/cache (`ApiService`) samt beräkningsalgoritmer (`PriceAnalyzer`).
- `ui`: `Main`-klassen som hanterar menyslingan och inmatning från användaren.

För **Bästa laddningstid (4h)** har en *Sliding Window*-algoritm implementerats för att beräkna summan för 4 sammanhängande timmar över dygnet.

## Cachning till disk

Cachningsfunktionaliteten kontrollerar om en JSON-fil för det valda datumet och elområdet redan finns i mappen `cache/`. Om den finns läses datan direkt från disken med `java.nio.file.Files.readString()` i stället för att göra ett HTTP-anrop. Om filen saknas eller är skadad så att den inte går att läsa görs ett försök att radera den. Sedan görs anropet till API och svaret sparas ner till disk via `java.nio.file.Files.writeString()`.

### Källor för Disk-I/O:
1. **Dokumentation för `java.nio.file.Files` (Oracle Java Docs)**
   - *Tillförlitlighet:* Mycket hög. Detta är den officiella API-dokumentationen från skaparna av språket.
2. **Baeldung (baeldung.com/java-nio-file)**
   - *Tillförlitlighet:* Hög. En etablerad och granskad community-källa för moderna Java-mönster som ofta uppdateras för nya Java-versioner.

## Reflektion: Java vs Andra språk

Att lösa den här uppgiften i Java kändes på vissa sätt enklare än med t ex Javascript. Exempelvis så är Java strikt typat så man får felindikeringar direkt under programmeringen, däremot så kändes dokumentationen också striktare. Det fanns inte, inga som jag hittat ännu i alla fall, källor där det var enkelt att bara söka upp funktioner eller metoder och få enkla förklaringar till hur de används, kanske för att Java är ett ganska "konservativt" språk med tanke på att det är bakåtkompatibelt ända till Java 1, vad jag har förstått...