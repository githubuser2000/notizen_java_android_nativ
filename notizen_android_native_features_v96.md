# Notizen Android Native v96 – behauptetes Feature-Inventar

Stand: **v96**  
Paket: `de.notizen.android`  
Basis: Portierung aus **Notizen .NET** und **Notizen PyQt** zu einer nativen Java-Android-App.

> Hinweis: Dieses Dokument listet die Funktionen auf, die laut bisherigem Änderungsprotokoll und Portierungsbeschreibung im Projektstand **v96** enthalten sein sollen. Es ist kein Beweis dafür, dass jede Funktion auf jedem Android-Gerät vollständig oder fehlerfrei läuft. Der Java-Kern wurde wiederholt getestet; der echte APK-/Runtime-Test findet auf dem Zielgerät statt.

---

## 1. Grunddaten der App

- Native Java-Android-App.
- Paketname: `de.notizen.android`.
- Aktueller Stand:

```text
versionCode 96
versionName 1.0.96-java-native
```

- Android-Zielwerte:

```text
compileSdk 34
targetSdk 34
minSdk 23
```

- Projektstruktur für Android Studio / Gradle.
- Zusätzlich eigener **Termux-No-Gradle-Buildpfad** über:

```sh
./notizen-build-apk
```

- Buildpfad für:
  - `aapt2`
  - `javac`
  - `d8`
  - `apksigner`
  - `zipalign`
- `d8`-Aufruf mit Android-Library:

```sh
d8 --lib "$ANDROID_JAR"
```

- Wiederholt gepflegte Patch-Scripte zwischen Versionsständen.
- Reine Java-Kerntests über:

```sh
./tools/run_core_tests.sh
```

---

## 2. ALX-Dateiformat

- Öffnen von `.alx`-Dateien.
- Speichern von `.alx`-Dateien.
- Unterstützung des alten ALX-Aufbaus:
  - GZip-komprimiert
  - UTF-16-XML
  - Legacy-Struktur aus Notizen.NET / PyQt
- Legacy-DES-Passwortpfad.
- Drei-stufige alte DES-Kette als Pipeline-Modell.
- Erhalt unbekannter ALX-Rootattribute.
- Erhalt unbekannter ALX-Root-XML-Elemente.
- Erhalt unbekannter `Notiz`-Attribute.
- Erhalt unbekannter `Notiz`-Kindelemente.
- Erhalt alter Desktop-Haftnotiz-Metadaten.
- ALX-Load→Dump→Load-Roundtripprüfung im Validierungsmodell.
- Privacy-light ALX-Validierung:
  - Strukturprüfung
  - Knotenzählung
  - SHA-256-Baumhash
  - SHA-256-Inhaltshash
  - keine rohe Textausgabe in der Diagnose
- ALX-Stream-Pipeline für:
  - lokale Dateien
  - Android-SAF/`content://`
  - FTP
  - existierendes Speicherziel
- Backup-Vorentscheidung bei lokalem Speichern.
- Android-Manifest-Filter für `.alx` / `.ALX`.
- MIME-/Dateifilter für ALX/XML-Öffnen.

---

## 3. Dateiöffnen und Speicherziele

- Einheitliche OpenTarget-Normalisierung.
- Erkennung und Normalisierung von:
  - `content://...`
  - `file://...`
  - rohen Dateipfaden
  - `ftp://...`
- Prozent-kodierte Pfade werden dekodiert.
- Alte Windows-Dateipfade wie `file:///C:/Users/...` werden normalisiert.
- Recent-Files können wieder geöffnet werden.
- `openOnceFile` / `openOnceTimestamp` werden beim Start einmalig verbraucht.
- Danach wird `openOnceFile` geleert.
- Speicherzielmodell für:
  - Android-URI/SAF
  - lokale Datei
  - FTP
  - Save-As-Fallback
- Separates Save-Workflow-Modell.
- Dokumentstatusmodell:
  - geändert
  - gespeichert
  - Speicherziel vorhanden
  - Passwortstatus
  - Fenstertitelstatus
- Drei-Wege-Schließen-/Reset-Workflow:
  - speichern
  - nicht speichern
  - abbrechen
- Passwortspeicher-Wipe nach Dokumentabschluss modelliert.

---

## 4. Baum / Notizstruktur

- Baumknotenmodell.
- Root-Knoten.
- Neuer Unterknoten.
- Neuer Nachbarknoten.
- Leere Titel werden nach Legacy-Regel zu:

```text
...
```

- Titelbearbeitung / Label-Normalisierung.
- Knoten löschen.
- Knoten ausschneiden.
- Knoten kopieren.
- Knoten einfügen.
- Root-Löschung wird blockiert.
- Fallback-Knoten nach Löschung.
- Zählen betroffener Knoten beim Löschen.
- Zählen geschlossener Haftnotiz-Metadaten beim Löschen.
- Knoten nach oben bewegen.
- Knoten nach unten bewegen.
- Knoten einrücken.
- Knoten ausrücken.
- Knoten vor Ziel verschieben.
- Mobile Ersatzlogik für TreeView-Drag-and-drop.
- Drop-Blockaden für:
  - Root
  - Self
  - Descendant
- Alle Knoten aufklappen.
- Alle Knoten zuklappen.
- Expansion-Status speichern/rekonstruieren.
- Baum-Traversal mit:
  - Tiefe
  - Legacy-Pfadnummern
  - Enter/Exit-Modell
- Teilbaumoperationen.
- Desktop-Haftnotiz-Metadaten in Teilbäumen zählen.
- Desktop-Haftnotiz-Metadaten in Teilbäumen entfernen.
- Baum-Kontextmenü aus WinForms modelliert.
- Doppelte alte `Neu`-Einträge unterscheidbar:
  - neuer Unterknoten
  - neuer Knoten daneben
- Alte Labels wie `Einfuegen` / `Loechen` werden zu `Einfügen` / `Löschen`.

---

## 5. Editor / Notizinhalt

- Editor lädt Notiztext aus RTF-Rohdaten.
- Editor speichert geänderte Notizen zurück.
- Titel- und Inhaltssync zwischen Baum und Editor.
- RTF-Rohdaten bleiben erhalten, solange ein Knoten nicht bearbeitet wird.
- Wird ein Knoten bearbeitet, wird der Inhalt als RTF neu gespeichert.
- Markierung von Knoten mit Desktop-Haftnotiz, wenn Inhalt neu geladen werden müsste.
- Horizontaler Scrollmodus / Zeilenumbruch-Umschaltung.
- Bugfix: kein nicht vorhandenes `EditText.isHorizontallyScrolling()` mehr.
- Legacy-Aufzählungspunkt-Aktion:

```text
•   
```

- Datumseinfügen im alten WinForms-Format:

```text
 Tag.Monat.Jahr Stunde:Minute 
```

- Editor-Kontextmenü aus WinForms:
  - Kopieren
  - Ausschneiden
  - Einfügen
  - Bild einfügen
  - Datum einfügen
  - Löschen
  - Suchen
- Fokusmodell zwischen Baum und Editor:
  - Kopieren
  - Ausschneiden
  - Einfügen
  - Löschen
- Paste in falschen Zielkontexten wird blockiert.
- Cut/Delete des Root-Knotens wird blockiert.

---

## 6. RTF lesen, erhalten und anzeigen

- RTF wird in lesbaren Text umgewandelt.
- `\ansicpgNNNN`-Codepage-Erkennung.
- CP1252-/ANSI-Fallback.
- UTF-8-/UTF-16-Erkennung bei Importpfaden.
- Feldresultate werden lesbar gemacht.
- Hyperlink-Felder werden erkannt.
- Generische RTF-Felder werden erkannt.
- OLE-/Objektgruppen werden erkannt.
- OLE-/Objektgruppen werden als Platzhalter sichtbar.
- `objclass`, zum Beispiel `Paint.Picture`, wird erkannt.
- PNG-Bilder werden aus RTF extrahiert.
- JPEG-Bilder werden aus RTF extrahiert.
- BMP-/DIB-Bilder werden behandelt.
- RTF-`pict`-Bilder werden nach HTML als `data:`-Bilder ausgegeben.
- DIB-Payloads bleiben im kombinierten RTF-Export erhalten.
- Legacy-Listentextgruppen:
  - `\*\pntext`
  - `\*\listtext`
- `\line` als sichtbarer Zeilenbruch.
- `\fi` First-Line-Indent.
- `\slmult`.
- `\cbpat`.
- `\chcbpat`.
- `\expnd`.
- `\expndtw`.
- `\super` / `\sub`.
- `\super0` / `\sub0`.
- Font-Aliasgruppen `\*\falt`.
- Fonttable-Extraktion.
- Colortable-Extraktion.
- Versteckter Text `\v` wird nicht normal sichtbar gemacht.
- RTF-Tabellensteuerwörter:
  - `\trowd`
  - `\cell`
  - `\row`
- RTF-Tabellen werden in HTML als echte Tabellen dargestellt.
- RTF-Metriken:
  - Absätze
  - weiche Zeilenumbrüche
  - Tabs
  - Felder
  - Hyperlinks
  - Bilder
  - OLE-/Objektgruppen
- RTF-Info-Anzeige für aktuelle Notiz.

---

## 7. RTF → HTML

- RTF→HTML-Brücke.
- HTML-Dokumentausgabe für WebView.
- Darstellung von:
  - Fett
  - Kursiv
  - Unterstrichen
  - Durchgestrichen
  - Textfarbe
  - Hintergrundfarbe
  - Highlight
  - Schriftart
  - Schriftgröße
  - Ausrichtung
  - Einzüge
  - First-Line-Indent
  - Absatzabstände
  - Zeilenabstand
  - Hochstellung
  - Tiefstellung
  - Caps
  - Small-Caps
  - RTL
  - LTR
  - Zeichenabstand
  - verstecktem Text
- Zusammenhängende gleich formatierte Textsegmente werden gebündelt.
- Hyperlinks werden als Links in der HTML-Vorschau dargestellt.
- Bilder werden als eingebettete `data:`-Bilder dargestellt.
- OLE-/Objektgruppen werden als Platzhalter dargestellt.
- RTF-Tabellen werden zu HTML-Tabellen.
- Kompakte Desktop-Haftnotiz-HTML-Ausgabe.
- WebView-kompatibles HTML-Dokumentmodell.
- Desktop-Haftnotiz-CSS mit `line-height:100%`.

---

## 8. HTML → RTF

- HTML-Dateien können importiert werden.
- HTML wird zu RTF konvertiert.
- Unterstützte Tags unter anderem:
  - `b`
  - `strong`
  - `i`
  - `em`
  - `u`
  - `strike`
  - `sub`
  - `sup`
  - `p`
  - `div`
  - `h1` bis `h6`
  - `blockquote`
  - `a`
  - `img`
  - `kbd`
  - `samp`
- Inline-CSS-Unterstützung:
  - Farbe
  - Hintergrund
  - Schriftart
  - Schriftgröße
  - Text-Deko
  - Ausrichtung
  - Margins
  - Padding-left
  - Line-height
  - Direction
  - Text-indent
  - Visibility hidden
- `data:image/...;base64,...` wird zu RTF-`pict`.
- Hyperlinks werden zu RTF-`field` / `HYPERLINK`.
- Geordnete Listen werden als lesbare Nummern übertragen.
- Ungeordnete Listen werden als Bullets übertragen.
- Tabellen werden als tab-separierte RTF-Zeilen übernommen.
- CSS `direction: rtl` wird berücksichtigt.
- HTML-Body-Attribute:
  - `text`
  - `bgcolor`
- Erhaltene Notizen-Spezialfelder können aus HTML zurück nach RTF gerettet werden.
- Objektgruppen können aus HTML zurück nach RTF gerettet werden.

---

## 9. RTF-Export

- Kombinierter RTF-Gesamtexport.
- Rich-RTF-Gesamtexport mit Formatierungen.
- Reihenfolge zwischen Text, Bildern, Hyperlinks, Feldern und Objekten wird besser rekonstruiert.
- Export von:
  - Fett
  - Kursiv
  - Unterstrichen
  - Durchgestrichen
  - Schriftart
  - Schriftgröße
  - Vordergrundfarbe
  - Highlight/Hintergrundfarbe
  - Ausrichtung
  - Einzügen
  - First-Line-Indent
  - Absatzabständen
  - Zeilenhöhe
  - RTL/LTR
  - Hoch-/Tiefstellung
  - Caps
  - Small-Caps
  - Zeichenabstand
- Eigene Fonttabellen.
- Eigene Farbtabellen.
- Erhalt von Hyperlinks.
- Erhalt von Bildern.
- Erhalt von generischen Feldern.
- Erhalt von OLE-/Objektgruppen.
- Erhalt alter BMP-/DIB-Payloads.
- Aktueller Knoten kann als RTF exportiert werden.
- Plaintext-Fallback wird automatisch zu RTF gewandelt.

---

## 10. RTF-Formatierung in Android

- Aktion **RTF Format**.
- Markierter Text kann als RTF formatiert gespeichert werden.
- Fallback: Wenn keine Auswahl vorhanden ist, kann auf gesamten Text angewendet werden.
- Unterstützte Formatierungen:
  - Fett
  - Kursiv
  - Unterstrichen
  - Durchgestrichen
  - Schriftfamilie
  - Schriftgröße
  - Textfarbe
  - Highlight/Hintergrundfarbe
- Schriftgrößendialog.
- Eigene Schriftgröße.
- Schriftgrößenmodell aus `fontsize.vb`:
  - leerer Zwischenzustand erlaubt
  - nichtnumerische Eingaben fallen auf letzten gültigen Wert zurück
  - Werte über `99` werden auf `99` begrenzt
- Schriftfamilienliste.
- Fontplan-Modell aus `font_set(...)`.
- Fokusentscheidung Editor vs. Baum.
- Desktop-Haftnotiz-Reload-Markierung bei Fontänderung.
- Wichtig: Android-`EditText` selbst bleibt Plaintext; Formatierung wird in RTF-Daten gespeichert und über Vorschau/Export sichtbar.

---

## 11. Bilder

- Bild über Android-Dateiauswahl einfügen.
- Bild wird als RTF-`pict` in aktuelle Notiz eingebettet.
- PNG/JPEG/BMP-Erkennung.
- Bildausgabe in HTML-Vorschau.
- Bildausgabe in HTML-Export.
- Bildausgabe in RTF-Gesamtexport.
- BMP/DIB-Erhalt im RTF-Export.
- Bildgruppen in RTF werden als `[Bild]` bzw. HTML-Bild dargestellt.

---

## 12. Objekt-/OLE-Unterstützung

- OLE-/Objektgruppen werden erkannt.
- `objclass` wird erkannt.
- Objektgruppen werden als Platzhalter dargestellt.
- Objektgruppen werden im Export besser erhalten.
- Objektplatzhalter erscheinen in HTML.
- Objektplatzhalter erscheinen in kombiniertem RTF-Export.
- Keine echte OLE-Aktivierung auf Android.

---

## 13. Suche

- Suche über aktuelle Notiz.
- Suche über alle Knoten.
- Suchlabels mit Pfad.
- Sichtbare Trefferposition.
- Kompakte Snippets:
  - Zeilenumbrüche ersetzt
  - Tabs ersetzt
  - Mehrfachräume verdichtet
- Suchdialogmodell:
  - Suchbegriffnormalisierung
  - leere Suche wird nicht still ignoriert
  - lokalisierte Statusmeldungen
- Suchsession:
  - Suchbegriff wird gecacht
  - Optionen werden gecacht
  - gleicher Suchlauf springt zyklisch weiter
  - geänderte Suche baut Trefferliste neu
- Suchergebnisnavigator:
  - Trefferliste
  - aktueller Trefferindex
  - weiter/zurück
  - Status `Treffer X von N`

---

## 14. Export

- TXT-Export.
- HTML-Export.
- RTF-Export.
- Aktuellen Knoten als TXT exportieren.
- Aktuellen Knoten als RTF exportieren.
- Gesamten Baum exportieren.
- Aktuellen Teilbaum exportieren/zusammenfassen.
- Gesamten Baum zusammenfassen.
- PyQt-naher HTML-Export:
  - `lang="de"`
  - CSS-Struktur
  - `<section class="notizen-node">`
  - nummerierte Unterüberschriften
  - Bilder
  - Objekt-Platzhalter
  - Tabellen-CSS
- TXT-Exportmodi:
  - UTF-8
  - ANSI / Windows-1252
  - Unicode / UTF-16LE mit BOM
- CRLF-Zeilenendennormalisierung.
- Sichere Dateinamen aus Knotentiteln.
- Dialogspezifikationen für Exportpfade.
- Aktionen:
  - Export TXT
  - Export ANSI
  - Export Unicode
  - Export HTML
  - Export RTF
  - Knoten TXT
  - Knoten RTF

---

## 15. Import

- TXT-Import.
- RTF-Import.
- HTML-Import.
- Bildimport.
- Config-Import.
- TXT-/RTF-Importpfade mit:
  - BOM-Erkennung
  - UTF-16LE
  - UTF-16BE
  - UTF-8
  - CP1252-/ANSI-Fallback
  - Charset-Metaerkennung
  - PyQt-nahem `preferAnsi`-Pfad für RTF
- RTF-Dateien werden als echte RTF-Dateien erhalten.
- Nicht-RTF kann als Plaintext→RTF importiert werden.
- HTML-Dateien werden als neuer Unterknoten importiert.

---

## 16. Drucken

- Android-natives Drucken über `PrintManager` / `WebView`.
- Druck aktueller Notiz.
- Druck aktueller Teilbaum.
- Druck gesamter Baum.
- Druckjob-Modell im Java-Kern.

---

## 17. Vorschau

- WebView-Vorschau.
- RTF-/HTML-Vorschau.
- Zentrale HTML-Dokumentbrücke.
- Vorschau nutzt RTF→HTML-Konvertierung.
- Bilder, Links, Tabellen, Formatierungen und Objektplatzhalter werden dargestellt.

---

## 18. Statistik und Diagnose

- Dokumentstatistik:
  - Knoten
  - Blätter
  - maximale Tiefe
  - Desktop-Haftnotizen
  - Zeilen
  - Wörter
  - Zeichen
  - Zeichen ohne Leerraum
  - Bilder
  - RTF-Bytes
- RTF-Metriken.
- Lokale Diagnose ohne rohe Notiztexte:
  - Version
  - Speicherziel
  - Änderungsstatus
  - aktuelle Notiz
  - Baumstatistik
  - RTF-Metriken
  - Backup-Settings
  - Autosave-Settings
  - Sprache
  - Recent-Anzahl
  - FTP-Konfigurationsstatus
- Config-Snapshot-Anzeige.
- Haftnotiz-Layoutdiagnose.
- Layoutdiagnose.
- Launcher-/Runtime-Diagnose.
- Buildplan-Anzeige.
- ALX-Pipeline-Anzeige.

---

## 19. Konfiguration / `notizen.config.xml`

- Legacy-Config-Datei `notizen.config.xml`.
- UTF-16-XML lesen.
- UTF-16-XML schreiben.
- Import aus Android-App.
- Export aus Android-App.
- Recent-Files:
  - `a`
  - `b`
  - `c`
  - `d`
- Backup-Anzahl.
- Autosave-Normalisierung.
- Window-State-Normalisierung.
- FTP-Felder.
- Sprache.
- Scrollleistenwahl.
- Haftnotiz-Ränder.
- Minimiert-in-Taskbar-Regel.
- ToolStrip-Positionen.
- Letzter Ordner.
- Letzte Datei.
- `openOnceFile`.
- `openOnceTimestamp`.
- Unbekannte Rootattribute bleiben erhalten.
- Extra-Attribute bekannter Elemente bleiben erhalten.
- Unbekannte Top-Level-Elemente bleiben erhalten.
- Config-OnLoad-/OnExit-Modell:
  - Offscreen-Schutz
  - WindowState-Normalisierung
  - Workarea-Grenzen
  - Toolstrip-Status
  - Scrollleistenstatus
  - Recent-Verhalten

---

## 20. Einstellungen

- Einstellungsdialogmodell.
- Unterstützte Einstellungen:
  - Sprache
  - Backup-Anzahl
  - Autosave an/aus
  - Autosave-Sekunden
  - Autostart
  - Autostart minimiert
  - minimiert in Taskbar anzeigen
  - Haftnotiz-Ränder anzeigen
- Autosave-Default:

```text
60 Sekunden
```

- Autosave-Mindestwert:

```text
5 Sekunden
```

- Deaktivierter Autostart deaktiviert auch „Autostart minimiert“.
- Ungültige Werte werden als Warnung behandelt.
- Android-Einstellungen normalisieren Sprache über Legacy-Sprachkatalog.

---

## 21. Sprache / Lokalisierung

- Legacy-Sprachkatalog.
- 118 historische Sprachschlüssel.
- Sprachen:
  - Deutsch
  - Englisch
  - Chinesisch
  - Französisch
  - Spanisch
  - Russisch
- Alias-/Auto-Erkennung:
  - `french`
  - `es`
  - `ru_RU`
  - weitere Varianten
- Alte Feedback-Labels in Deutsch und Englisch.
- Alte Suchstatusmeldungen in Deutsch und Englisch.
- About-/Help-Texte in Deutsch und Englisch.

---

## 22. Passwort

- Legacy-DES-Passwortpfad.
- Passwortdialogmodell aus WinForms.
- Drei-Felder-Dialog:
  - altes Passwort
  - neues Passwort
  - neues Passwort wiederholen
- 24-Zeichen-Grenze.
- Legacy-Padding/Truncation auf 24 Zeichen.
- Altes Passwortfeld nur aktiv, wenn Passwort gesetzt ist.
- Fehlerfälle:
  - falsches altes Passwort
  - neue Passwörter unterschiedlich
  - Passwort zu lang
- Passwortstatus im Dokument-Lifecycle.

---

## 23. FTP

- Passives FTP-Öffnen von `.alx`.
- Passives FTP-Speichern von `.alx`.
- FTP-URL-Parsing.
- URL-Dekodierung.
- Beispiele wie:

```text
ftp://user:pass@example.org:2121/pfad/datei.alx
```

- Host-Normalisierung.
- Pfad-Normalisierung.
- Benutzername-Normalisierung.
- Passwort-Normalisierung.
- Maskierte Passwortanzeige mit `***`.
- Hintergrundthread für FTP, damit Android-UI nicht blockiert.
- `INTERNET`-Permission.
- FTP-Dialogmodell.
- FTP-Konfig speichern.
- FTP öffnen.
- FTP hochladen/speichern.
- FTP-Retry-Regel für alten Fehlerstatus `7`.
- Begrenzte Wiederholungen.
- FTP als aktuelles Speicherziel nach FTP-Öffnen.

---

## 24. Autosave

- Autosave-Kernmodell.
- Autosave nur, wenn:
  - Dokument geändert ist
  - Autosave aktiv ist
  - Speicherziel vorhanden ist
- Unterstützte Speicherziele:
  - Android-URI/SAF
  - lokale Datei
  - FTP
- Periodischer Android-Autosave.
- Autosave-Timer-Tick-Modell.
- Bugfix v96:
  - kein nicht vorhandenes `settings.autosaveEnabled`
  - stattdessen `settings.autosaveSeconds > 0`

---

## 25. Backup

- Legacy-Backup-Hilfen.
- Backupordner-Schema.
- Backup-Dateinamensschema.
- Timestamp-Parser.
- Sortierung.
- Pruning-Regeln.
- Backup-Anzahl aus Config.
- Backup-Vorentscheidung in Save-Pipeline.

---

## 26. Recent-Files

- Vier alte Recent-Slots.
- Recent-Menümodell.
- Text/Tag-Trennung.
- Shift-/Rotationsregel.
- Missing-File-Wächter.
- Android-Aktion **Letzte**.
- Öffnen von Recent-Einträgen:
  - `content://`
  - `file://`
  - rohe Dateipfade
  - `ftp://`
- Auswahl rotiert Liste nach Legacy-Regel.

---

## 27. Start / Autostart / Systemintegration

- Legacy-Startargumente:
  - `/min`
  - `-min`
  - Help-Flags
  - `.alx`-Startziel
  - `ftp://`-Startziel
- Windows-kompatibles Commandline-Quoting.
- Recent-File-Auswahl für Autostart-Ziele.
- PyQt-Autostartscript `Notizen PyQt.cmd`.
- Unveränderte Autostartscripts werden nicht unnötig überschrieben.
- Deaktivierter Autostart entfernt Script.
- Windows-`.alx`-Dateizuordnung als Registry-Vorschau.
- Windows-Open-Command-Erzeugung.
- Linux/GNOME-Desktop-`Exec=`-Zeile.
- Linux-Desktop-Dateimodell.
- Desktop-ID:

```text
notizen-py-qt
```

- GNOME-Tray-Safe-Start-Entscheidung.
- Qt/GNOME/Wayland/X11-Display-Environment-Normalisierung.
- Smoke-Test-Environment-Regeln.
- Runtime-Identitätsmodell.
- Unix-/ZIP-Dateirechte-Modell für:
  - Desktop-Dateien
  - Shell-Skripte
  - Python-Skripte
  - normale Quelldateien

---

## 28. Fenster-, Toolbar- und Layoutmodelle

- Fensterstatusmodell:
  - sichtbar
  - versteckt
  - minimiert
  - maximiert
  - minimierter Start
  - `Deactivate`
  - `VisibleChanged`
- Schutz gegen alte unsichtbare/offscreen Fensterdaten.
- Force-visible-Umgebungslogik.
- Hauptfenster-Chrome-Modell.
- Move-/Resize-/Minimize-Mauslogik:
  - Move
  - Resize unten rechts
  - Minimize
  - Blockierung durch sichtbare Dialoge
  - ToolStrip-Sichtbarkeitswechsel während Resize
- ToolStrip-Breitenlogik.
- ToolStrip-Anfügepositionen.
- Toolbar-Präsentationsmodell:
  - Legacy-Actionnamen
  - Tooltips
  - Shortcuts
  - Toolstrip-Gruppen
  - Glyph-/Icon-Texte
- Toolbar-Toggle-Modell.
- Enablement-Modell für Aktionen:
  - Datei
  - Baum
  - Editor
  - Export
  - Legacy-Aktionen
- Android-Layoutmodell:
  - Hochformat
  - Querformat
  - Baum-/Editor-Gewichte
  - versteckte Header-/Label-Regeln

---

## 29. Scrollleisten

- Legacy-Scrollleisten-Zyklus:

```text
None -> Horizontal -> Vertical -> Both -> None
```

- Android speichert Auswahl persistent.
- Toolbar-Aktion **Scroll**.
- Horizontaler Editor-Scrollmodus.
- Zeilenumbruchmodus.

---

## 30. Tastaturkürzel

- Hardware-Tastaturkürzel aus alter App, unter anderem:
  - `Ctrl+S`
  - `Ctrl+O`
  - `Ctrl+F`
  - `Ctrl+Space`
  - Baum-`Insert`
  - Baum-`Delete`
  - Baum-`Enter`
  - `Shift+Insert`
  - `Shift+Delete`
  - `Ctrl+Plus`
  - `Ctrl+Minus`
- Legacy-Shortcut-Modell.

---

## 31. Menüs und Kontextmenüs

- Editor-Kontextmenü.
- Baum-Kontextmenü.
- Desktop-Haftnotiz-Kontextmenü.
- Desktop-Haftnotiz-Opacity-Kontextmenü.
- Startmenümodell.
- Tray-Menümodell.
- Menügruppen:
  - Datei
  - Bearbeiten
  - Baum
  - Einheit/Zusammenfassung
  - Export
  - Recent Files
  - Tray-Menü
- Alte Actionnamen bleiben als Modell erhalten.
- Alte Tooltips / Glyphs teilweise modelliert.

---

## 32. Desktop-Haftnotizen

- Alte Desktop-Haftnotiz-Metadaten werden erhalten.
- Desktop-Haftnotizen werden gezählt.
- Desktop-Haftnotiz-Metadaten können aus Teilbaum entfernt werden.
- Geometriehilfen:
  - Hover-Geometrie
  - versteckte Ränder
  - Rückrechnung versteckter Ränder
  - Resize-Hit-Zone
  - Titelleisten-Hit-Zonen
  - Verstecken
  - Schließen
- Opacity-/Transparenz-Menü-Mapping.
- Transparenzmenü von `90 %` bis `0 %`.
- Umrechnung alter Transparenzwerte auf gespeicherte Deckkraft.
- Desktop-Haftnotiz-Autosize:
  - Idle-Guard
  - manuelle Resize-/Scroll-Sperre
  - Schrumpfen
  - Wachsen
  - Workarea-Grenzen
- Desktop-Haftnotiz-Paint-/Layoutmodell:
  - Ecke-Ressource `aa`
  - linke/rechte Header-Ecken
  - Trennlinien
  - Titelbereich
  - `_`-Button
  - `x`-Button
  - Editor-Rechteck
- MouseLeave-Verhalten für Rand-Verbergen.
- Desktop-Haftnotiz-Trayregistry.
- Haftnotizliste.
- Tray-Auswahlmodell:
  - Hauptfenster zeigen/verstecken
  - Haftnotiz anzeigen
  - kleine/zu kleine Haftnotiz neu laden
  - Beenden
- Wichtig: Keine echten frei schwebenden Windows-Desktop-Haftnotizfenster auf Android.

---

## 33. Wecker / Erinnerungen

- Wecker-Legacy-Mappings.
- Alte Checkboxnamen:
  - `CheckBox15`
  - `CheckBox12`
  - `CheckBox9`
  - `CheckBox14`
  - `CheckBox11`
  - `CheckBox10`
  - `CheckBox13`
- Wiederholungsmodi:
  - einmalig
  - täglich
  - wöchentlich
  - monatlich
  - jährlich
- Intervall-Einheiten:
  - Tage
  - Wochen
  - Monate
  - Jahre
- Datumsformat:

```text
yyyy-MM-dd HH:mm
```

- Android-Wecker-Dialog nutzt Legacy-Modell.
- Intervallfeld und Wochentags-Checkboxen werden dynamisch ein-/ausgeblendet.
- Validierung für:
  - Datum
  - Wiederholung
  - Intervall

---

## 34. Feedback / Hilfe / About

- Feedback-Legacy-System.
- Mindestlänge wie im alten Code.
- Tagesdrosselung.
- Tageszähler.
- `.NET Date.Ticks`-kompatible Speicherung als `long`.
- Lokales UTF-16LE/GZip-Feedbackarchiv.
- Kein ungefragter Netzversand.
- Android-Aktion **Feedback**.
- About-/Help-Texte aus alten Quellen.
- Deutsche und englische Beschreibung.
- Alte Feedback-Typen:
  - persönliche Meinung
  - Fehlermeldung
  - Feature-Vorschlag
  - opinion
  - bug report
  - feature request
- Alte Mindestlängenmeldung.
- Legacy-Webadresse bleibt erhalten.

---

## 35. Farben

- WinForms-ColorDialog-Modell.
- Knoten-Hintergrundfarbe.
- Knoten-Textfarbe.
- RTF-Textfarbe.
- RTF-Hintergrund/Hervorhebung.
- Desktop-Haftnotiz-Hintergrundfarbe.
- Alte Legacy-Farbpalette.
- Historischer `Random.Next(0,14)`-Bereich.
- RTF-Textfarbe über `\cf1`.
- RTF-Hervorhebung über `\highlight1`.
- CSS-Farbparser für:
  - `#rgb`
  - `#rrggbb`
  - `#AARRGGBB`
  - `rgb(...)`
  - `rgba(...)`
  - Prozentwerte
  - Named Colors wie `rebeccapurple`, `chocolate`, `lightgray`, `teal`
- Ungültige Farben werden abgelehnt.

---

## 36. Dateidialogmodelle

- OpenFileDialog-/SaveFileDialog-Modell.
- Zentrale Dialogspezifikation für:
  - ALX öffnen
  - ALX speichern
  - TXT exportieren
  - ANSI-TXT exportieren
  - UTF-8-TXT exportieren
  - Unicode-TXT exportieren
  - RTF exportieren
  - HTML exportieren
  - TXT importieren
  - RTF importieren
  - HTML importieren
  - Config importieren
  - Config exportieren
  - Bild einfügen
- Legacy-Filtertexte.
- Standarddateinamen.
- Dateiendungen.
- Android-MIME-Brücken.
- Android-SAF-Intents zentralisiert über dieses Modell.

---

## 37. Clipboard

- Clipboard-Formatpriorität:
  - Knoten-XML
  - RTF
  - HTML
  - Text
- Clipboard-Fokusmodell:
  - Baum
  - Inhalt/Editor
- Blockieren unpassender Paste-Operationen.
- Knoten-XML vor RTF vor HTML vor Text.
- RichTextBox-nahe Clipboard-Semantik.

---

## 38. RichTextBox-Semantik

- WinForms-RichTextBox-nahe Standardwerte.
- Standardfont:

```text
Microsoft Sans Serif
```

- Desktop-Haftnotiz-CSS mit:

```text
line-height:100%
```

- WebView-kompatible HTML-Dokumentbrücke.
- RTF-Metriken.
- Weiche RichTextBox-Zeilenumbrüche bleiben beim Escaping als `\line` erhalten.
- Tabs, Absätze, Felder, Links, Bilder und Objekte werden gezählt.

---

## 39. Dokumenttitel / Dateistatus

- Dateistatusmodell:
  - Startordner
  - Dateiname
  - Verzeichnis
  - `fileExists`
  - `saved`
  - `.alx`-Anzeigename
- Standarddatei:

```text
unbenannt.alx
```

- Dokumenttitelmodell für:
  - ungespeicherte Dateien
  - lokale Dateien
  - Android-`content://`-Ziele
  - FTP-Ziele
- Sternchen/Geändert-Status.
- Android-Titel wird aus diesem Modell erzeugt.

---

## 40. Zusammenfassung / Einheit

- Aktuellen Teilbaum zusammenfassen.
- Gesamten Baum zusammenfassen.
- Rich-RTF-Zusammenfassung.
- Formatierungen bleiben besser erhalten.
- Hyperlinks bleiben besser erhalten.
- Bilder bleiben besser erhalten.
- Felder bleiben besser erhalten.
- Objektplatzhalter bleiben besser erhalten.
- RTF-Reihenfolge bleibt besser erhalten.

---

## 41. Konkrete Android-Toolbar-Aktionen, die behauptet wurden

Unter anderem:

- Neu
- Öffnen
- Speichern
- Speichern unter
- Einstellungen
- FTP öffnen
- FTP speichern
- Feedback
- Validieren
- Bild
- Vorschau
- HTML Import
- TXT Import
- RTF Import
- Punkt
- Kind
- Daneben
- Löschen
- Ausschneiden
- Kopieren
- Einfügen
- Einrücken
- Ausrücken
- Rauf
- Runter
- Vor Ziel
- Alle auf
- Alle zu
- Suche
- Letzte
- Passwort
- Wecker
- RTF Format
- Schriftgröße
- Schriftart
- Drucken
- Export TXT
- Export ANSI
- Export Unicode
- Export HTML
- Export RTF
- Knoten TXT
- Knoten RTF
- Teilbaum
- Gesamt
- Haft weg
- Haftliste
- Haft Layout
- RTF Info
- Diagnose
- Config
- Status
- Schließen
- Layout
- Launcher
- Dialoge
- Toolbars
- Fenster
- Autosave
- Buildplan
- Fontplan
- Maus
- ALX Pipe
- Scroll

---

## 42. Build-/Patch-/Regressionsthemen

- Termux-Buildscript.
- Root-Script `notizen-build-apk`.
- Syntaxprüfungen für Shellscripts.
- Java-Core-Tests.
- Patch-Tests auf frisch entpackten Vorgängerversionen.
- Regression gegen:
  - `EditText.isHorizontallyScrolling()`
  - doppelte `pendingExportHtml`-Deklaration
  - doppeltes `case KeyEvent.KEYCODE_O`
  - doppelte `Uri uri = intent.getData();`
  - falsches `settings.autosaveEnabled`
- Javac-Optionen für robustere Tests:
  - `-proc:none`
  - `-XDcompilePolicy=simple`
  - begrenzter Heap
  - ein aktiver Javac-Prozessor
  - leerer `-sourcepath` im Core-Test
- Termux-Buildscript mit explizitem:

```text
-sourcepath "$JAVA_SRC:$BUILD_DIR/gen"
```

---

## 43. Bekannte Grenzen, trotz aller behaupteten Features

- In der ChatGPT-/Container-Umgebung wurde kein APK erzeugt, weil dort kein Android SDK / Termux / `aapt2` / `d8` / `apksigner` / `zipalign` vorhanden ist.
- Der echte APK-Build muss auf dem Zielsystem, zum Beispiel in Termux, laufen.
- Windows-Tray ist nicht 1:1 Android-nativ vorhanden.
- Frei schwebende Windows-Desktop-Haftnotizfenster sind auf Android nicht 1:1 vorhanden.
- OLE-Aktivierung ist auf Android nicht vorhanden.
- Der Android-Editor ist kein vollständiger RichText-WYSIWYG-Editor wie WinForms `RichTextBox`.
- Android `EditText` zeigt Formatierungen nicht direkt fett/kursiv/farbig an.
- RTF-Formatierungen werden in den RTF-Daten gespeichert und in Vorschau/Export sichtbar.
- Viele Desktop-/WinForms-/PyQt-Funktionen sind als **Kernmodell**, **Diagnose**, **Konvertierung**, **Datenbrücke** oder **Android-Ersatzaktion** portiert, nicht als identische Desktop-UI.
