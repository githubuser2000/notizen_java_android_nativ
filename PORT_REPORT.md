# Portierungsbericht: Notizen.NET / Notizen PyQt → Native Java Android

## Quellen

Analysierte Archive:

- `notizen.net.tar.bz2`
- `notizen_py_qt.tar.bz2`

Die PyQt-Version enthielt den weit fortgeschrittenen Fachkern aus der .NET-Portierung. Darum wurde die Android-Portierung nicht als 1:1-UI-Kopie der WinForms-Oberfläche gebaut, sondern aus dem stabilisierten PyQt-Kern plus den Legacy-Regeln der .NET-Version abgeleitet.

## Android-Projekt

```text
NotizenJavaAndroidNativ/
  app/src/main/java/de/notizen/android/MainActivity.java
  app/src/main/java/de/notizen/android/TreeListAdapter.java
  app/src/main/java/de/notizen/android/AndroidBackupStore.java
  app/src/main/java/de/notizen/android/AndroidAlarmScheduler.java
  app/src/main/java/de/notizen/android/AlarmReceiver.java
  app/src/main/java/de/notizen/android/core/*.java
```

Verwendet wird ausschließlich Java und die Android-Plattform-API. Es gibt keine Drittbibliotheken.

## Übernommene Kerndatenstrukturen

| Legacy/PyQt | Android-Java |
|---|---|
| `NoteNode` | `core.NoteNode` |
| `NoteDocument` | `core.NoteDocument` |
| `DesktopNoteState` | `core.DesktopNoteState` |
| `legacy_new_next_node` | `core.NoteTreeOps.legacyNewNextNode` |
| `legacy_delete_fallback_node` | `core.NoteTreeOps.legacyDeleteFallbackNode` |
| `legacy_paste_clone` | `core.NoteTreeOps.legacyPasteClone` |
| `node_to_clipboard_xml` / `node_from_clipboard_xml` | `core.NodeClipboard` |
| `legacy_light_color_argb` | `core.LegacyColors` |
| `AlarmSpec`, `next_occurrence`, `describe_recurrence` | `core.AlarmSpec`, `core.AlarmUtils` |
| `search_nodes`, Suchergebnislabels/Snippets | `core.Search.searchNodes`, `core.SearchResult` |
| `tree_to_plain_text` | `core.Exporters.treeToPlainText` |
| `tree_to_rtf` | `core.Exporters.treeToRtf` |
| `tree_to_html` / `html_export.py` | `core.Exporters.treeToHtml` |
| `legacy_clipboard_bullet_text` / Shortcuts | `core.LegacyEditorActions`, `core.LegacyShortcuts` |
| `plain_text_to_rtf` / `rtf_to_plain_text` | `core.RtfUtils` |
| `load_alx` / `save_alx` | `core.AlxIo` |
| `AppSettings`, Recent-Files, Autosave | `core.LegacySettings` |
| `FtpTarget` / FTP open-save | `core.FtpTarget`, `core.SimpleFtpClient` |
| `i18n.py` / `languages.vb` | `core.LegacyI18n` |
| Feedback-Drosselung/Archiv | `core.LegacyFeedback`, `AndroidFeedbackStore` |
| Startargumente/Autostart-Regeln | `core.LegacyStartup` |
| Legacy-Pfade | `core.LegacyPaths` |
| Desktop-Haftnotiz-Fensterlogik | `core.LegacyDesktopNote` |
| Fensterzustand/Wiederherstellung | `core.LegacyWindowVisibility` |
| Legacy-Backup-Dateischema | `core.LegacyBackup` |
| `legacy_validation.py` | `core.LegacyValidation` |

## ALX-Kompatibilität

Portiert:

- GZip-Erkennung und Dekompression.
- Roh-XML-Erkennung für UTF-16, UTF-8 und Windows-1252-Fallback.
- `notizen-alx2` mit `Notiz`-Knoten.
- Älteres `notes_doc`-Format mit `node`/`leaf`/`leaf_text`.
- Attribute `name`, `title`, `isexpanded`, `bgcolor`, `fgcolor`.
- Desktop-Haftnotiz-Attribute `visible`, `x`, `y`, `width`, `height`, `opacity`, `argb`.
- zusätzliche mobile Wecker-Attribute unter den `notizen_alarm_*`-Namen.
- unbekannte Rootattribute, unbekannte Root-Kindelemente sowie unbekannte Attribute und Kindelemente unter `Notiz`.
- Schreiben als UTF-16-XML in GZip.

## Verschlüsselung

Die Legacy-ALX-Verschlüsselung wurde in Java nachgebildet:

- Passwort wird auf 24 Zeichen normalisiert.
- DES-Schlüssel: Zeichen `0..7`, `7..14`, `15..22`.
- DES/CBC/PKCS5Padding.
- IV entspricht jeweils dem DES-Schlüssel.
- Beim Speichern wird in umgekehrter Schlüsselreihenfolge verschlüsselt.
- Ein leeres Passwort bleibt unverschlüsselt.

## Zusätzlich weiterportiert in dieser Iteration

- Knoten-Zwischenablage als XML-kompatibler Teilbaum.
- Kopieren, Ausschneiden, Einfügen, Rauf/Runter sowie Einrücken/Ausrücken im Android-Baum.
- Anzeige und Wiederöffnung app-interner Sicherungen.
- Legacy-Farbpalette aus `get_lightcolor()`.
- Haftnotiz-Metadaten-Dialog, damit alte Desktop-Sticky-Attribute nicht nur erhalten, sondern mobil geändert werden können.
- Wecker-Regeln aus `wecker.vb` und `alarms.py`, inklusive wiederholter Android-Alarmplanung.
- RTF-Gesamtexport und Gesamtnotiz-Erzeugung.
- Legacy-Konfigurationsparser/-Writer und Android-Import/-Export für `notizen.config.xml` mit Recent-Files, Backup-Anzahl, Autosave-Regeln, Window-State und FTP-Feldern.
- Optionaler passiver FTP-Download/-Upload für ALX-Dateien, inklusive URL-Dekodierung und Android-Hintergrundthread.
- RTF-Codepage-Erkennung (`\ansicpgNNNN`) und Legacy-Platzhalter für eingebettete OLE-Objekte/Bilder.
- RTF-`pict`-Extraktion für PNG/JPEG/BMP/DIB sowie HTML-Data-URI-Ausgabe.
- Erhalt von Hyperlink-Feldern und OLE-/Objektgruppen in Vorschau und kombiniertem RTF-Export.
- Android-Aktionen `Bild`, `Vorschau`, `HTML Import` und `Punkt`.
- Dependency-freie formatierte RTF→HTML-Brücke und HTML→RTF-Konverter für typische RichTextBox-/QTextEdit-Formatierungen, inklusive Data-URI-Bildern, Hyperlinks, Spezialfeld-/Objekt-Roundtrip, verstecktem Text und besserer Stil-Koaleszierung.
- PyQt-ähnlicher HTML-Gesamtexport mit `<section class="notizen-node">`, CSS, Nummerierung und Bild-/Objekt-Styling.
- Suchergebnis-Pfade, kompakte Snippets und sichtbare Trefferpositionen wie im PyQt-Hilfsmodul `search_results.py`.
- Legacy-Aufzählungspunkt und Hardware-Tastaturkürzel aus `editor_legacy.py`/`keyboard_legacy.py`.
- Passthrough unbekannter ALX-Rootattribute/Root-Elemente und unbekannter `notizen.config.xml`-Rootattribute, bekannter Extra-Attribute und unbekannter Top-Level-Elemente.
- Erweiterte Statistik nach PyQt-/Desktop-Nutzen: Blätter, Tiefe, Desktop-Notizen, Bilder und RTF-Größe.
- Erweiterte Kern-Regressionstests.
- Vollständiger Legacy-Sprachkatalog aus `i18n.py`/`languages.vb`, inklusive 118 historischer Schlüssel, sechs Sprachspalten, Aliasnamen und Auto-Spracherkennung aus der Locale.
- Feedback-Logik der Desktop-/PyQt-Version: Mindestlänge, Tageslimit, Tageszähler, `Date.Ticks`-kompatible Speicherung und lokales UTF-16LE/GZip-Archiv auf Android.
- Korrektur des Config-Datentyps für Feedback-Ticks von `int` auf `long`, damit aktuelle .NET-Ticks wie `638819712000000000` verlustfrei gelesen und geschrieben werden.
- Legacy-Startargumente und Autostart-Regeln: `/min`/`-min`, Help-Flags, `.alx`-/`ftp://`-Ziele, Windows-kompatibles Quoting und Zielauswahl aus Recent-Files.
- Legacy-Pfadlogik mit Standardordner `Documents/Notizen`, Standarddatei `unbenannt.alx` und Dateiname/Ordner-Auftrennung.
- WinForms-Haftnotiz-Geometrie, inklusive Hover-Vergrößerung, Hidden-Border-Rückrechnung, Opacity/Transparency-Konvertierung, Titelleisten-Aktionen und Resize-Hit-Zone.
- Fensterstatus-Sanitizing für alte Desktop-Geometrien, minimierten Start und Force-visible-Umgebungsvariablen.
- Legacy-Backup-Dateischema mit Ordnername, Timestamp-Dateiname, Parser, Sortierung und Pruning.
- Privacy-light ALX-Validierung aus `legacy_validation.py`: Zusammenfassungen ohne rohe Notiztexte, stabile Baum-/Inhalts-Hashes und Load→Dump→Load-Vergleich für echte Altdateien.
- Android-Feedback-Dialog als sichere mobile Umsetzung: lokales Archiv plus gespeicherter Legacy-Zähler statt direktem Mailversand.
- RTF-Content-Part-Parser: Textsegmente mit Stil-Snapshot, Hyperlink-Felder, generische Felder, Bilder und OLE-/Objektgruppen werden jetzt getrennt ansprechbar.
- RTF-Fidelity weiter an PyQt 0.10.17–0.10.26 angeglichen: `\*\pntext`/`\*\listtext`, `\line` als sichtbarer Zeilenbruch, `\fi`, `\super`/`\sub`, `\slmult`, `\cbpat`, Font-Aliasgruppen `\*\falt` und Farbtabelle ohne automatische Startfarbe.
- HTML→RTF erweitert um Listen-/Tabellen-Brücke, CSS-Text-Indent, CSS-Zeilenhöhe, RTL und Super/Subscript.
- Kompakte Desktop-Haftnotiz-HTML-Ausgabe für alte Sticky-Note-Inhalte ohne Absatzrahmen.
- PyQt-Systemintegration als Java-Kernhilfen: Windows-`.alx`-Registry-Einträge, Linux/GNOME-Desktop-Exec, GNOME-Tray-Safe-Start und Qt/GNOME-Display-Environment-Normalisierung.

## RTF

Der Android-Port hat keinen vollwertigen Microsoft-RTF-Renderer. Implementiert ist eine mobile Brücke:

- RTF→Plaintext für Anzeige, Suche, Statistik und Export, inklusive verborgenem Text (`\v`) als nicht sichtbarem Inhalt.
- RTF→HTML für Vorschau/HTML-Export mit Bold, Italic, Underline, Strike, Farben, Highlights, Font-Family, Fontsize, Ausrichtung, Einzügen, First-Line-Indent, Absatzabständen, Zeilenabstand, Hoch-/Tiefstellung, Super/Subscript, Caps/Small-Caps, RTL/LTR, Zeichenabstand, `\line`-Brüchen und Hidden-Text-CSS.
- HTML→RTF für Import und Roundtrip mit Basis-Tags, Inline-CSS, Blöcken, Listen-/Tabellen-Plaintext-Brücke, Hyperlinks, Data-URI-Bildern sowie erhaltenen Notizen-Feld-/Objektgruppen.
- `\par`, `\line`, `\tab`, Legacy-Listentextgruppen `\*\pntext`/`\*\listtext` und Tabellenzellen/-zeilen.
- Unicode `\uN` inklusive Fallback-Skip.
- Hex-Escapes `\'hh` mit `\ansicpgNNNN`-Codepage-Erkennung und CP1252-Fallback.
- typische typografische RTF-Symbole.
- Auslassen von Metadaten-/Font-/Farbgruppen; Bild-/Objektgruppen werden als lesbare Platzhalter markiert.
- `pict`-Bilder werden extrahiert und in HTML als `data:`-Bilder angezeigt.
- Feldgruppen zeigen `fldrslt`, während `fldinst` unterdrückt wird; Hyperlink-Felder werden als HTML-Links dargestellt, generische Felder bleiben als kodiertes Roh-RTF erhalten.
- Objektgruppen werden als sichtbare Platzhalter mit erhaltenem Roh-RTF und erkannter `objclass` abgebildet.
- Plaintext→RTF für bearbeitete Notizen.
- RTF-Gesamtexport als portable RTF-Zusammenfassung mit Überschriften und Erhalt von Bild-/Feld-/Objektgruppen.

Wichtig: Nicht bearbeitete Notizen behalten ihr ursprüngliches RTF-Rohfeld. Erst Bearbeitung eines Knotens ersetzt dessen RTF durch neu erzeugtes Plain-RTF.

## UI-Portierung

Die Android-Oberfläche nutzt:

- horizontale Toolbar mit Legacy-Aktionen.
- Baumliste mit Einrückung, Auf-/Zu-Indikator, Long-Press zum Klappen und Toolbar-Ersatz für Desktop-Drag-and-drop.
- gelbes Root-Feld und gelbes Titel-Feld nach dem WinForms/PyQt-Vorbild.
- großen nativen Editor.
- Android-Dateidialoge über Storage Access Framework.
- Android-Notification für Wecker.
- Android-Dateiauswahl zum Einfügen von Bildern als RTF-`pict`.
- WebView-basierte RTF/HTML-Vorschau.
- HTML-Import über Android-Dateiauswahl; der importierte Inhalt wird als neuer Unterknoten gespeichert.
- Hardware-Shortcut-Auswertung für alte Notizen.NET-Aktionen, soweit Android sinnvoll dazu passt.
- Einstellungen-Dialog mit Legacy-Sprachliste und Alias-Normalisierung.
- Feedback-Toolbar-Aktion mit Legacy-Drosselung und lokalem Archiv.
- Validieren-Toolbar-Aktion mit ALX-Roundtripprüfung und privacy-light Hash-Zusammenfassung.

## Nicht 1:1 portiert

- Windows-Tray und GNOME-Tray.
- frei schwebende Desktop-Haftnotizen.
- SFTP/FTPS und komplexe FTP-Serververwaltung.
- vollständiger RichText-WYSIWYG-Editor mit RTF-Formatleiste. Die HTML/RTF-Brücke erhält inzwischen deutlich mehr Formatierung, aber der mobile Editor selbst bleibt ein Plaintext-Editor.
- Druckdialoge der Desktop-Version.

Diese Punkte sind nicht gelöscht: Desktop-Haftnotizdaten, unbekannte XML-Fragmente und alte RTF-Rohdaten werden im Dateiformat soweit möglich erhalten.

## Lokale Validierung

Ausgeführt:

```bash
./tools/run_core_tests.sh
```

Ergebnis:

```text
Core tests OK
```

Kein APK wurde in dieser Umgebung gebaut, weil weder Android SDK noch Gradle installiert waren.


## v8 Weitertranspilierung

- Version erhöht auf `versionCode 8` / `versionName 1.0.8-java-native`.
- Der RTF-Gesamtexport wurde von einer weitgehend flachen Text-/Spezialgruppen-Brücke auf einen strukturierten Content-Part-Export erweitert. Er sammelt Font-Familien und Farben, erzeugt eigene RTF-Font-/Farbtabellen und emittiert Textläufe mit Bold, Italic, Underline, Strike, Ausrichtung, Einzügen, Absatzabständen, Zeilenhöhe, RTL/LTR, Hoch-/Tiefstellung, Caps/Small-Caps, Zeichenabstand, Fontsize, Vordergrundfarbe und Highlight.
- `RtfContentParser.contentParts(...)` liefert jetzt auch normale Textteile in Reihenfolge, nicht nur Bilder/Felder/Objekte. Dadurch können Exporte die Reihenfolge zwischen Text, Bildern, Hyperlinks, generischen Feldern und OLE-Objekten besser rekonstruieren.
- Legacy-BMP/DIB-Pfade wurden im kombinierten RTF-Export getestet: alte `\dibitmap0`-Payloads bleiben als DIB erhalten.
- Neue Kernklassen `LegacyRichTextToolbar`, `LegacyRtfSelectionFormatter`, `LegacyPrintLayout` und `LegacyDesktopNoteRendering`.
- Android-UI erweitert: neue Aktionen `RTF Format`, `Drucken`, `TXT Import` und `RTF Import`. `Drucken` nutzt Android `PrintManager`/`WebView` für aktuelle Notiz, Teilbaum oder gesamten Baum. `RTF Format` speichert markierten Plaintext als RTF-Formatierung und macht sie über Vorschau/Export sichtbar.
- Neue Kernklasse `LegacyImporters` für TXT-/RTF-Import mit BOM-Erkennung, Charset-Metaerkennung, UTF-8/CP1252-Fallback, PyQt-nahem `preferAnsi`-Pfad für RTF und Plaintext→RTF-Fallback.
- Kompakte Haftnotiz-Darstellung weiter modelliert: `line-height:100%`, RGBA-Farb-/Opacity-Brücke und minimierte Legacy-Semantik als Java-Hilfe.
- Tests ergänzt für Rich-RTF-Gesamtexport, BMP/DIB-Erhalt, Toolbar-Modell, Druckjob-Modell, Haftnotiz-Rendering und RTF-Auswahlformatierung.


## v9 Buildfix / Termux-Kompatibilität

- Version erhöht auf `versionCode 9` / `versionName 1.0.9-java-native`.
- `compileSdk` und `targetSdk` auf 34 normalisiert, damit der manuelle Termux-Build mit API-34-`android.jar` ohne SDK-36-Abhängigkeit laufen kann.
- Buildfehler aus `MainActivity.java` behoben: Android `EditText` bietet keinen Getter `isHorizontallyScrolling()`. Die App nutzt jetzt ein eigenes Feld `editorHorizontallyScrolling` und ruft nur den vorhandenen Setter `setHorizontallyScrolling(boolean)` auf.
- Reiner Java-Kern unverändert grün mit `./tools/run_core_tests.sh`.


## v10 Weitertranspilierung

- Version erhöht auf `versionCode 10` / `versionName 1.0.10-java-native`.
- Neue Kernklasse `LegacyAutosave` mit Entscheidungsobjekt, Zieltypen (`file`, `android-uri`, `ftp`) und normalisiertem Intervall. Damit ist die alte `Autosavetimer_Tick`-Bedingung im Java-Kern testbar und Android kann sie ohne Dateisystem-Annahmen auf SAF/FTP/Rohdateipfade anwenden.
- Android `MainActivity` führt Autosave jetzt periodisch aus, wenn ein geändertes Dokument bereits ein URI-, Rohdatei- oder FTP-Ziel besitzt. Speichern markiert das Dokument wieder sauber als gespeichert und plant den nächsten Tick.
- Toolbar-Aktion `Letzte` portiert die Recent-File-Wiederöffnung: Auswahl rotiert die Liste über `LegacySettings.activateRecentFile(...)`; `content://`, `file://`, rohe Dateipfade und `ftp://` werden getrennt behandelt.
- Toolbar-Aktion `Vor Ziel` portiert die alte WinForms-TreeView-Dragregel als mobile Liste: der aktuelle Knoten wird vor einem gültigen sichtbaren Ziel eingefügt; Root, Self-Drop und Descendant-Drops bleiben gesperrt.
- `openOnceFile`/`openOnceTimestamp` aus `notizen.config.xml` werden beim Android-Start einmal verbraucht.
- Wecker-Port erweitert um `LEGACY_WECKER_WEEKDAY_CHECKBOXES`, `legacyWeckerWeekdayForCheckbox`, `legacyWeckerWeekdayLabels` und `legacyWeckerIntervalUnit`.
- Neuer manueller Termux-Buildhelfer `notizen-build-apk` / `tools/notizen-build-apk-termux.sh`, passend zu API 34 und den gemeldeten Tools `aapt2`, `d8`, `apksigner`, `zipalign`.
- Tests erweitert für Autosave-Entscheidungen, Recent-File-Rotation, Legacy-Dragregel, Wecker-Checkbox-/Intervall-Mappings und Regression gegen `EditText.isHorizontallyScrolling()`.


## v11 Weitertranspilierung

- Version erhöht auf `versionCode 11` / `versionName 1.0.11-java-native`.
- Neue Kernklasse `LegacyOpenTarget`: Recent-Files, `openOnceFile` und Autosave-Ziele werden jetzt einheitlich als Android-URI, Rohdatei, `file://`-URI oder FTP-Ziel erkannt und normalisiert. Prozentkodierte alte Dateiziele wie `file:///C:/Users/me/Notizen%20A.alx` werden wieder zu lesbaren Pfaden.
- Android `MainActivity.openStoredPath(...)` nutzt diese Normalisierung jetzt direkt. Dadurch bleibt die Wiederöffnung aus Legacy-Config/Recent-Liste robuster und vermeidet doppelte Sonderlogik.
- Neue Autostart-Script-Brücke in `LegacyStartup`: Die alte Windows-/PyQt-Autostartlogik kann jetzt ein `Notizen PyQt.cmd`-Script erzeugen, unveränderte Scripts nicht unnötig überschreiben und bei deaktiviertem Autostart entfernen. Die Windows-Argumentquotierung bleibt testbar.
- RTF-Tabellen werden nicht mehr nur als Tab-/Zeilentext durchgereicht: RTF-`\trowd`, `\cell` und `\row` werden in der HTML-/WebView-Brücke zu semantischen `<table>`-/`<td>`-Strukturen. Export-HTML enthält passende Tabellen-CSS-Regeln.
- Neue Kernklasse `LegacyDesktopNoteAutoResize`: Die alte `desknote.vb`-Autosize-Entscheidung (`set_clientsizes`) ist als testbares Java-Modell portiert, inklusive Idle-Guard, manueller Resize-/Scroll-Sperre, Schrumpf-/Wachs-Entscheidung und Workarea-Grenzen.
- Neue Kernklasse `LegacyToolbarPresentation`: Die späte PyQt-Toolbar-Anpassung mit icon-only-Darstellung, Legacy-Actionnamen, Tooltips, Shortcuts und Toolstrip-Gruppen ist jetzt im Java-Kern abgebildet. Android nutzt diese Spezifikation für kompaktere Toolbar-Buttons mit Content-Description/Tooltip.
- Android-Manifest weiter an ALX-Dateizuordnung angepasst: getrennte `VIEW`-Filter für `content://` mit ALX-/XML-MIME-Typen und `file://` mit `.alx`/`.ALX`-Pfadmustern.
- Tests erweitert für Autostart-Script-Erzeugung/-Entfernung, OpenTarget-Normalisierung, semantische RTF-Tabellen, Desktop-Haftnotiz-Autosize-Entscheidung, Toolbar-Präsentationskatalog und Android-Manifest-ALX-Filter.

## v13 Weitertranspilierung

- Version erhöht auf `versionCode 13` / `versionName 1.0.13-java-native`.
- `kontext_inhalt.vb` und `Baum_Kontext_.vb` sind als `LegacyContextMenus` in den Java-Kern übertragen, einschließlich Indexreihenfolge, doppeltem `Neu`, alten Labels `Einfuegen`/`Loechen` und dem Baum-Kontextpunkt `Speichern`.
- Das alte Datumseinfügen aus `kontext_inhalt.vb` ist formatgetreu portiert: führendes/abschließendes Leerzeichen, keine führenden Nullen, Tages-/Monats-/Stunden-/Minutenwerte direkt aus der Legacy-Logik.
- Der alte Current-Node-RTF-Speicherpfad ist als `LegacyNodeExport` und Android-Toolbar-Aktionen `Knoten TXT`/`Knoten RTF` umgesetzt.
- `wanna_save.vb` und `wanna_restart.vb` sind als `LegacyDialogModels` portiert; Android verwendet die Speichern/Nein/Abbrechen-Semantik jetzt wieder beim Verlassen geänderter Dokumente.
- `desknote_kontext.vb` und `desknote_kontext_opacy.vb` sind als `LegacyDesktopNoteContextMenu` portiert, inklusive Opacity-Umrechnung von alter Transparenzanzeige auf gespeicherte Deckkraft.
- Die RichText-Toolbar wurde um `ToolStrip_fontsizenumber` und `ToolStrip_fonts` erweitert. Android stellt dafür Schriftgrößen- und Schriftartdialoge bereit, die RTF-Daten für die Auswahl erzeugen.
- Tests erweitert; `./tools/run_core_tests.sh` bleibt grün.

## v15

Diese Runde portiert weitere Dialogentscheidungen aus den WinForms-Quellen in reine Java-Kernmodelle und bindet sie in `MainActivity` ein. Neu sind Passwortwechselmodell, FTP-Dialogmodell, Einstellungsdialogmodell, About-/Help-Texte und ein Suchdialogmodell. Zusätzlich wurden Android-Java-Buildregressionen aus der UI-Zusammenführung abgesichert: doppelte Feld-/Variablendeklarationen und doppelte Shortcut-`case`-Labels dürfen nicht zurückkehren. Kernvalidierung: `Core tests OK` über die Java-Kernkompilierung und `TestCore`.

## v17

Diese Runde fasst die v16-Dialogportierung und weitere alte Editor-/Datei-/Suchzustände zusammen. `LegacyAlarmDialogModel` portiert die verhaltensrelevante `wecker.vb`-Dialoglogik inklusive RadioButton-Mapping, Intervall-Sichtbarkeit, Wochentags-Checkboxen und Datumsvalidierung. `LegacyFontSizeEntry` portiert das alte `fontsize.vb`-Eingabeverhalten. Neu dazugekommen sind `LegacyEditorNodeSync` für `inhalt.vb`/`CText.vb`, `LegacyFileState` für `Datei.vb` und `LegacySearchSession` für die gecachte Trefferfolge aus `suche.vb`. Android nutzt diese Modelle für Wecker, eigene Schriftgröße, Editor→Knoten-Speicherung und zyklische Suche. Kernvalidierung: `Core tests OK`.

## v23

Diese Runde enthält die v21-Portierung direkt und ergänzt weitere Baum-/Menü-/Haftnotizlogik. `LegacyScrollbars` bildet den alten Scrollleisten-Zyklus aus WinForms/PyQt ab und wird in Android persistent angewendet. `LegacyTreeExpansion` überträgt globales Auf-/Zuklappen. `LegacyUnifiedNote` trennt Zusammenfassung für aktuellen Teilbaum und ganzen Baum; der Export nutzt jetzt Rich-RTF statt Plaintext-RTF. Dazu kommen `LegacyTreeTraversal`, `LegacyTreeDragDrop`, `LegacyActionState`, `LegacyDesktopNoteTreeOps`, `LegacyTreeDelete` und `LegacyStartMenuModel`. Android nutzt diese Modelle für Toolbar-Aktionen, Scrollleisten, Baumverschiebung, Teilbaum-/Gesamtzusammenfassung und Haftnotiz-Teilbaum-Aufräumen. Kernvalidierung: `Core tests OK`.

## v29

Diese Runde konzentriert sich auf die verbleibende RichTextBox-/QTextEdit-Parität aus den späteren PyQt-Stufen und auf alte Fokus-/Clipboard-/TreeView-Details. Die RTF-Brücke erkennt zusätzliche Steuerwörter wie `\chcbpat`, `\expnd`, `\super0` und `\sub0`; HTML→RTF versteht `visibility:hidden`, `padding-left`, `dir`, Body-Farbattribute, `<kbd>` und `<samp>`. CSS-Farben wurden auf eine breite Named-Color-Tabelle plus `rgb`/`rgba`/8-stellige Hexwerte erweitert. `RtfUtils.rtfEscapeMultiline` bewahrt weiche RichTextBox-Zeilenumbrüche als `\line`.

Neu im Java-Kern sind `LegacyRichTextBoxSemantics`, `LegacyClipboardFocus` und `LegacyTreeLabelEditing`. Android nutzt die RichTextBox-Semantik für Vorschau und die neue Aktion `RTF Info`; die übrigen Modelle bleiben bewusst als testbare Kernbrücken erhalten, weil sie Windows-/WinForms-Verhalten abbilden, das auf Android nur indirekt sinnvoll ist. Kernvalidierung: `Core tests OK`.

## v35

Diese Runde portiert weitere WinForms-Verhaltensmodelle, die bisher nur indirekt in Android vorhanden waren. `LegacyRecentMenu` bildet die vier alten Recent-File-Menüpunkte mit File1..File4, Text/Tag-Trennung, Shift-Regel und Klickrotation ab. `LegacyDocumentTitle` ersetzt die harte Android-Titelzeile durch ein testbares Datei-/Quellenmodell. `LegacyWindowToggle` modelliert die alten Entscheidungen zum Wiederherstellen, Minimieren, Verstecken und minimierten Start. `LegacyToolstripLayout` überträgt `toolstrip_breite`, während `LegacyFtpRetryPolicy` die alte FTP-Retry-Regel für Status `7` kapselt.

Android nutzt diese Schicht für bessere Recent-Labels, Dokumenttitel und die neue Aktion `Diagnose`. `LegacyDiagnosticReport` erstellt eine lokale, privacy-light Zusammenfassung aus Version, Quelle, Änderungsstatus, Baumstatistik, RTF-Metriken und Einstellungen, ohne rohe Notiztexte auszugeben. Kernvalidierung: `Core tests OK`.

## v47

Diese Runde ergänzt die Dialog- und Export-Parität aus den WinForms-/PyQt-Stufen. `LegacyColorDialogModel` überträgt die alten ColorDialog-Entscheidungen in den Java-Kern; Android kann markierte RTF-Auswahl dadurch nicht nur fett/kursiv/unterstrichen, sondern auch mit Textfarbe und Hervorhebung speichern. Die erzeugten RTF-Daten verwenden Farbtabellen mit `\cf1` bzw. `\highlight1` und bleiben über Vorschau/HTML/Export sichtbar.

Zusätzlich bündelt `LegacyFileDialogModel` die alten OpenFileDialog-/SaveFileDialog-Filter, Standardnamen, Dateiendungen und Android-MIME-Brücken. `MainActivity` erzeugt Dateiauswahl-Intents nun zentral aus diesen Spezifikationen. `LegacyTextExportModel` portiert die drei TXT-Exportpfade weiter: ANSI/Windows-1252, UTF-8 und Unicode/UTF-16LE mit BOM, jeweils mit alter CRLF-Normalisierung. Kernvalidierung: `Core tests OK`.

## v59

Diese Runde portiert weitere WinForms-Hilfslogik, die bisher nur implizit im Android-Port steckte:

- `xml_kram.vb`: direkte Config-Zugriffe, ToolStrip-Koordinaten, Fensterzustands-Normalisierung, Offscreen-Schutz beim Laden und Speichern von Fenster-/Toolstrip-Daten wurden in `LegacyConfigSnapshot` nachgebildet.
- `desknote.vb`: Paint- und Layoutmodell der Desktop-Haftnotizen wurde in `LegacyDesktopNotePaint` abstrahiert. Android rendert keine freien Desktop-Fenster, kann die Geometrie und Zeichenbefehle aber nun prüfen und anzeigen.
- `Datei.vb` / `Notizen.vb`: Speicherentscheidung für vorhandenes Ziel, FTP, Rohdatei, Android-SAF-URI oder Save-As-Fallback wurde in `LegacySaveWorkflow` zentralisiert und in `MainActivity.saveDocument()` verwendet.
- `suchergebnisse.vb`: Suchergebnis-Navigation und zyklische Trefferanzeige wurden in `LegacySearchResultNavigator` portiert.
- Android-UI: neue Diagnoseaktionen `Config` und `Haft Layout` für alte Config-/Haftnotizdaten.

Geprüft mit `./tools/run_core_tests.sh` sowie `bash -n` für die Buildskripte.
## v71

Diese Runde führt die v59-Basis mit weiteren WinForms-/PyQt-Lifecycle- und Launcher-Modellen zusammen. Datei-Schließen, Reset nach Schließen, Config-OnLoad/OnExit, AppData-/Config-Pfade, Clipboard-Formatpriorität, neue Knotenanlage, Haftnotiz-Trayliste, Launcher-Identität, Layoutdiagnose und ZIP-/Unix-Permissions sind nun als reine Java-Kernmodelle testbar und in der Android-Oberfläche sichtbar.


## v95

Diese Runde führt die v83-Schicht direkt mit weiterer WinForms-Randlogik zusammen. `LegacyActivationDialogFocus` bildet `Notizen_Activated` und die alte Dialog-Reaktivierungsreihenfolge ab. `LegacyToolbarToggleModel` portiert die Schrift-/Bearbeiten-/Neu-ToolStrip-Toggles. `LegacyMainWindowChrome`, `LegacyAutosaveTimerTick`, `LegacyCTextState` und `LegacyApkBuildPipeline` machen Hauptfenster-Chrome, Autosave-Tick, CText-Markierung und den Termux-APK-Buildpfad testbar.

Neu hinzugekommen sind `LegacyFontSetModel`, `LegacyWindowMoveResizeModel` und `LegacyAlxStreamPipeline`. Damit sind weitere Entscheidungen aus `Notizen.vb` portiert: `font_set(...)`, Maus-Move/Resize/Minimize und die Stream-Schichten beim ALX-Öffnen/Speichern mit GZip, UTF-16-XML, optionaler DES-Kette und Backup-Vorentscheidung. Android zeigt diese Modelle über die Aktionen `Fontplan`, `Maus` und `ALX Pipe`. Kernvalidierung: `Core tests OK`.


## v96

- Buildfix: `MainActivity.showAutosaveTickModel()` nutzt wieder `LegacySettings.autosaveSeconds > 0` statt des nicht existierenden Felds `autosaveEnabled`.
- Regressionstest gegen `settings.autosaveEnabled` ergänzt.
- Version erhöht auf `versionCode 96` / `versionName 1.0.96-java-native`.

## v97

- Projektname und sichtbarer App-Name auf `Notizen Java Android Nativ` umgestellt.
- Version erhöht auf `versionCode 97` / `versionName 1.0.97-java-android-nativ`.
- RTF-Editor von reiner Plaintext-Anzeige auf native Android-`Spannable`-Darstellung erweitert: Fett/Kursiv/Unterstrichen/Durchgestrichen, Schriftgröße, Schriftart, Textfarbe, Hervorhebung, Ausrichtung und Einzüge werden im Editor sichtbar und wieder als RTF gespeichert.
- RTF-Bilder werden aus vorhandenen `\pict`-Blöcken dekodiert, in der Editorbox angezeigt und beim Speichern erhalten. Neu eingefügte Bilder erscheinen sofort an der Cursorposition.
- Obere Toolbar ergänzt um direkte Formatbuttons `Fett`, `Kursiv`, `Größer`, `Kleiner`, `Schriftart` und `Größe`, damit die wichtigsten RTF-Aktionen ohne Unterdialog erreichbar sind.
- Tablet-Drehung verliert Baum und RTF-Inhalt nicht mehr: Dokument, aktueller Knoten, Auswahl, Dateiziel, Settings und Status werden über `onRetainNonConfigurationInstance()` gehalten und nach dem Neuaufbau der Oberfläche wiederhergestellt.
- Kernvalidierung: `./tools/run_core_tests.sh` meldet `Core tests OK`.

## v98

- Version erhöht auf `versionCode 98` / `versionName 1.0.98-java-android-nativ`.
- ALX-Baumzustand wird beim Laden sichtbar übernommen: offene und geschlossene Knoten aus `isexpanded` sowie Aliasformen wie `isExpanded`, `IsExpanded` und `expanded` werden gelesen und beim Speichern wieder als kanonisches `isexpanded` geschrieben.
- Obere Toolbar neu geordnet in drei horizontale Listen: `Datei`, `Baum` und `Text`. Alle Toolbar-Schaltflächen sind quadratisch und nutzen kompaktere Icon-/Glyph-Darstellung mit Tooltip/Accessibility-Text.
- Baum- und Editorbereich in breiter Ansicht haben einen ziehbaren Trenner. Die Baumbreite kann mit dem Finger stark verkleinert oder vergrößert werden und wird in der Android-Config gespeichert.
- Baumdarstellung optisch geglättet: hellere Zeilenflächen, abgerundete Auswahl und klarere Auf-/Zu-Markierungen.
- Kernvalidierung: `./tools/run_core_tests.sh` meldet `Core tests OK`; Buildskripte wurden per `bash -n` geprüft.

## v99

- Version erhöht auf `versionCode 99` / `versionName 1.0.99-java-android-nativ`.
- Die sichtbare Programmnamens-Überschrift wurde aus dem Hauptlayout entfernt, damit Baum und RTF-Editor höher stehen.
- Die Dateiquellenzeile zwischen Toolbar und Baum/RTF-Box wurde entfernt; der Fenstertitel bleibt intern für Android/Diagnose erhalten.
- Die untere dauerhafte Statusleiste wurde entfernt. Meldungen wie `Ansicht wiederhergestellt` belegen damit keinen Bildschirmplatz mehr.
- Die drei oberen Buttonleisten bleiben erhalten, sind aber deutlich kompakter: Toolbar-Buttons und Leisten-Badges sind jetzt 24 dp breit und 24 dp hoch statt 48 dp.
- Die Icon-Buttons wurden optisch geglättet: kleinere abgerundete Kacheln, weniger Außenabstand, kompaktere Glyph-Größen, Ripple-Maske und kleine Erhebung.
- Die Mindestbreite des Baumfensters wurde von 56 dp auf 24 dp reduziert, damit der Finger-Trenner den Bereich noch freier verkleinern kann.
- Kernvalidierung: `./tools/run_core_tests.sh` meldet `Core tests OK`; Buildskripte wurden per `bash -n` geprüft.

## v100

- Version erhöht auf `versionCode 100` / `versionName 1.0.100-java-android-nativ`.
- Die beiden hellgelben Kopfleisten über Baum und RTF-Editor haben jetzt eine gemeinsame feste Höhe und sind in der breiten Ansicht sauber auf gleicher Linie; der Trenner füllt die Kopfhöhe passend, damit die Leisten optisch direkt nebeneinander laufen.
- Der Farbpaletten-Button in der Textleiste öffnet jetzt direkt die RTF-Textfarbpalette. Die gewählte Farbe wird als `ForegroundColorSpan` sichtbar auf die aktuelle Auswahl angewendet und beim Speichern wieder als RTF-Farbtabelle/`\cf` geschrieben; Hintergrundfarbe und Knotenfarben bleiben über Zusatzoptionen erreichbar.
- Ein app-interner Laufzeit-Schnappschuss schützt Baum und RTF-Inhalt bei Abstürzen oder Prozessverlust: Änderungen werden verzögert, bei Pause/Stop/Destroy und im Crash-Handler als private ALX-XML-Sicherung abgelegt und beim Neustart wieder ausgewählt geladen.
- Die Speichernachfrage vor dem Laden/Neuanlegen wird für einen leeren Ein-Knoten-Startbaum ohne sichtbaren RTF-Text unterdrückt.
- Kernvalidierung: `./tools/run_core_tests.sh` meldet `Core tests OK`; Buildskripte wurden per `bash -n` geprüft.


## v101

- Version erhöht auf `versionCode 101` / `versionName 1.0.101-java-android-nativ`.
- Android-AppWidgetProvider, Widget-Layout und Manifest-Metadaten ergänzt, damit die Haftnotiz-Aktion den aktuellen Knoten als natives Android-Widget pinnen oder vorhandene Widgets aktualisieren kann.
- Gemeinsame Textformat-Leiste für Baum und RTF-Box: Fokus-/Touch-Erkennung entscheidet, ob Formatierung den ausgewählten Baumknoten oder die RTF-Auswahl betrifft.
- Baumknoten können nun mobil fett, kursiv, unterstrichen, durchgestrichen, mit Fontfamilie, Fontgröße sowie Text-/Hintergrundfarbe dargestellt und als ALX-Zusatzattribute erhalten werden.
- Erster Leistenbutton `Leer` stellt nach Speicherabfrage einen leeren Ein-Knoten-Startzustand her.
- Snapshot- und Editor-Sicherungen wurden gegen versehentliches Überschreiben mit transient leeren Zuständen gehärtet.
## v102

- Version erhöht auf `versionCode 102` / `versionName 1.0.102-java-android-nativ`.
- Die mittlere der drei oberen Leisten heißt jetzt `Baum/Text` und unterscheidet aktiv zwischen Baum, RTF-Editor und Titelzeile.
- `Kopieren`, `Ausschneiden`, `Einfügen` und `Löschen` wirken nun auf den aktiven Bereich: Baumknoten, markierten RTF-Text oder markierten Titeltext.
- RTF-Kopieren/Ausschneiden hält intern eine formatierte `Spannable`-Zwischenablage, schreibt zusätzlich Plaintext ins Android-Clipboard und fügt eigene RTF-Auswahl wieder mit Formatierung ein.
- `Einrücken` und `Ausrücken` wirken im RTF-Editor als Absatzeinzug und im Baum weiterhin als Knotenein-/ausrücken.
- Rein baumbezogene Aktionen der mittleren Leiste sind geschützt: Ist der RTF-Editor oder die Titelzeile aktiv, verändern sie den Baum nicht versehentlich, sondern fordern zum Antippen des Baums auf.
- Zwischenablagen werden sauber getrennt, damit eine später kopierte Textauswahl keinen alten Knoten mehr aus der internen Knoten-Zwischenablage einfügt.
- Kernvalidierung: `./tools/run_core_tests.sh` meldet `Core tests OK`; Buildskripte wurden per `bash -n` geprüft.


## v103

- Version erhöht auf `versionCode 103` / `versionName 1.0.103-java-android-nativ`.
- Der erste `Leer`/Neue-Datei-Start baut den Baumadapter nach dem Ersetzen des Dokuments neu auf, löscht sichtbare alte Baumzeilen und setzt RTF-/Titel-Felder leer zurück.
- Speichern-vorher läuft bei SAF-`Speichern unter` und FTP-Speichern nach erfolgreichem Speichern automatisch mit der geplanten Aktion weiter.
- ALX-Baumzustand bleibt weiterhin über `isexpanded="True/False"` je Knoten gespeichert und wird beim Laden rekursiv wiederhergestellt.

## v104

- Version erhöht auf `versionCode 104` / `versionName 1.0.104-java-android-nativ`.
- Weitertranspilation der Desktop-Schriftgrößenlogik in mobile Gesten: Pinch-Zoom skaliert RTF-Auswahl/ganzen RTF-Inhalt oder den selektierten Baumknoten.
- Android-spezifische UI-Zoomwerte für Toolbar-Buttons und hellgelbe Kopffelder werden in `<android-ui toolbar-button-dp="…" header-text-sp="…">` gesichert.
- Große Bilder werden vor RTF-Einbettung gemäß Android-Speichergrenzen heruntergerechnet; Anzeige vorhandener RTF-Bilder nutzt Downsampling statt Vollbitmap-Dekodierung.


## v105

- Version erhöht auf `versionCode 105` / `versionName 1.0.105-java-android-nativ`.
- Der Zwei-Finger-Zoom der drei oberen Icon-Leisten wurde stabilisiert: Die gesamte Fläche der drei Leisten ist nun ein gemeinsames Gestenziel.
- Die einzelnen Toolbar-Buttons bekommen keine konkurrierenden Toolbar-Zoom-Touchlistener mehr; dadurch können Gesten über mehrere Buttons/Reihen hinweg erkannt werden.
- Toolbar-Größenänderungen werden gebündelt und Config-Speichern wird verzögert ausgeführt, damit beim Spreizen/Kneifen kein UI-Stau/ANR entsteht.
- Schutz gegen fehlerhafte Android-Touchereignisse ergänzt: ScaleGestureDetector-Ausnahmen werden abgefangen, statt die App zu beenden.


## v106

- Version erhöht auf `versionCode 106` / `versionName 1.0.106-java-android-nativ`.
- Die dritte Textleiste wurde weiter an Notizen .NET/PyQt angenähert: direkte Buttons für `Rückgängig`, `Wiederholen`, `Durchgestrichen`, `Textfarbe`, `Hintergrund`, `Links`, `Mitte`, `Rechts` und `Blocksatz` sind ergänzt.
- Formatbuttons erkennen den aktiven Zielbereich und markieren ihren aktuellen Zustand: Baumformatierung wirkt auf den selektierten Knoten, RTF-Formatierung auf Cursor/Auswahl im Editor.
- RTF-Undo/Redo speichert Text, Auswahl und wichtige Android-Spans, damit reine Formatänderungen rückgängig gemacht und wiederholt werden können.
- Eine einklappbare PyQt-artige Schnellsuchleiste wurde ergänzt: `Weiter`, `Alle Treffer`, Suche im aktuellen Teilbaum oder ganzen Baum, optional Titel, ganze Wörter und Groß-/Kleinschreibung.
- Treffer springen den passenden Baumknoten an, öffnen dessen Vorfahren und markieren die Fundstelle im Titel oder in der RTF-Box.
- Die Suchleisten-Einstellung wird bei Drehung im Aktivitätszustand erhalten.
- Kernvalidierung: `./tools/run_core_tests.sh` meldet `Core tests OK`; Buildskripte wurden per `bash -n` geprüft.

## v107

- Version erhöht auf `versionCode 107` / `versionName 1.0.107-java-android-nativ`.
- Android-Langdruckmenüs ergänzt: Baum und RTF-Box haben jetzt eigene Kontextmenüs mit Desktop-/PyQt-nahen Aktionen.
- Baum-Kontextmenü enthält unter anderem Neuer Unterknoten, Neuer Knoten daneben, Direkt umbenennen, Kopieren, Ausschneiden, Einfügen, Einfügen als Unterknoten, Löschen, Auf/Zu, Alle auf/zu, Rauf/Runter, Einrücken/Ausrücken, Vor Ziel, Haftnotiz/Widget, Wecker, Knotenfarben und Exportaktionen.
- RTF-Kontextmenü enthält Rückgängig/Wiederholen, Ausschneiden/Kopieren/Einfügen, Alles markieren, Bild/Datum/Punkt einfügen, Normal/Fett/Kursiv/Unterstrichen/Durchgestrichen, Farben, Schriftart/-größe und Absatz-Ausrichtung.
- Baumknoten können per Langdruck im Baum direkt umbenannt werden; die alte Notizen-.NET-Fallback-Regel setzt leere Titel weiter auf `...`.
- Finger-Drag/Drop für Baumknoten ergänzt: langer Druck im linken Griffbereich startet Ziehen; Drop oben verschiebt vor Ziel, Mitte als Unterknoten, unten nach Ziel.
- Aktiver Bereich wird sichtbarer markiert: Baum, Titelzeile und RTF-Box bekommen klarere aktive Rahmen; der Baumadapter kennt aktive Auswahl und Drop-Vorschau.
- Kernvalidierung: `./tools/run_core_tests.sh` meldet `Core tests OK`; Buildskripte wurden per `bash -n` geprüft.


## v108

- Version erhöht auf `versionCode 108` / `versionName 1.0.108-java-android-nativ`.
- Haftnotizen/Widgets wurden Android-gerechter weitergeführt: Widgets merken jetzt den ALX-Knotenpfad, Dokumentnamen, Farben und Textgröße und können beim Speichern anhand des aktuellen Dokuments aktualisiert werden. Tippen auf ein Widget öffnet die App wieder beim passenden Knoten.
- Die Widget-Liste wurde ausgebaut: vorhandene Startbildschirm-Widgets können angezeigt werden; die Aktion `Haftliste` nutzt diese Übersicht.
- Export, Teilen und Drucken wurden in einen gemeinsamen `Export`-Dialog zusammengeführt: aktueller Knoten, aktueller Teilbaum oder ganzer Baum können als TXT, ANSI-TXT, Unicode-TXT, HTML oder RTF gespeichert oder über Android geteilt werden.
- Für das Teilen wurde ein privater `ExportFileProvider` ergänzt, damit Exportdateien sauber als `content://`-URI mit Leserechten statt als unsicherer Dateipfad an andere Android-Apps gehen.
- Drucken/PDF wurde um eine HTML-Vorschau erweitert; aus der Vorschau kann direkt gedruckt/PDF erzeugt oder HTML geteilt werden.
- Die Einstellungen wurden erweitert und scrollbar gemacht: Standardordner, Toolbar-Größe, Kopfzeilen-Schriftgröße, Baum-/Editor-/Widget-Schriftgrößen, Bild-Verkleinerungsgrenze, automatische Bildverkleinerung, Crash-Wiederherstellung und Diagnosebuttons sind nun direkt konfigurierbar.
- Große Bilder verwenden die neue einstellbare Maximal-Kantenlänge; die automatische Verkleinerung kann über die Android-Einstellungen an- und ausgeschaltet werden.
- Kernvalidierung: `./tools/run_core_tests.sh` meldet `Core tests OK`; Buildskripte wurden per `bash -n` geprüft.

## v109

- Version erhöht auf `versionCode 109` / `versionName 1.0.109-java-android-nativ`.
- Android-native Übersetzung eines Desktop-/PyQt-nahen Zeichenbild-Workflows: Stiftfenster öffnen, schreiben/zeichnen, als RTF-`\pict` speichern.
- Der neue `Stift`-Button nutzt eine native Canvas-View mit Stylus-/Fingerereignissen und schreibt nach `JPEG`-Kompression in den bestehenden `RtfImageSpan`/RTF-Speicherpfad.
- Haftnotiz-Widgets übernehmen nun RTF-Bilder als Bildvorschau; bis zu vier Bilder werden als Widget-Collage dargestellt.
- Widget-Previewdaten werden speicherschonend als interne Dateien gehalten und bei Widget-Löschung entfernt.
- Große RTF-Bilder werden für Widgets nur begrenzt extrahiert, damit Speicherdruck durch mehr-MB-Bilder reduziert wird.


## v110

- Version erhöht auf `versionCode 110` / `versionName 1.0.110-java-android-nativ`.
- Neuer Button `Markdown`/`MD` in der RTF-Textleiste: erkennt Markdown im aus RTF gewonnenen Rohtext und zeigt es schreibgeschützt gerendert direkt in der RTF-Box.
- Der Markdown-Modus ignoriert RTF-Textformatierung und RTF-Bilder für die Erkennung/Vorschau, indem zuerst in Rohtext umgewandelt und Bild-/Objekt-Platzhalter entfernt werden.
- Nochmaliges Drücken des Markdown-Buttons stellt den ursprünglichen RTF-Editorinhalt wieder her.
- Speichern während aktiver Markdown-Vorschau überschreibt den Knoten nicht mit Vorschautext; vorhandenes RTF, Formatierungen und Bilder bleiben erhalten.
- Neue Kernklasse `LegacyMarkdownPreviewModel` mit Rohtextbereinigung, Markdown-Erkennung und Statusmodell.

## v111

- Version erhöht auf `versionCode 112` / `versionName 1.0.112-java-android-nativ`.
- Baum-Undo/Redo mit eigener Snapshot-Historie ergänzt und an aktive Baumselektion, Kontextmenü und `Ctrl+Z`/`Ctrl+Y` angebunden.
- Baumaktionen wie Erzeugen, Löschen, Einfügen, Verschieben, Drag/Drop, Ein-/Ausrücken, Expansion, Titeländerung, Baumformatierung, Wecker- und Haftnotiz-Metadaten werden vor der Änderung gesichert.
- Die Baum-Historie ist von der bestehenden RTF-Undo-Historie getrennt.
- Samsung-Stift-Eingabe verarbeitet nun Druckwerte pro Stiftpunkt; die Strichbreite ändert sich innerhalb eines Strichs anhand der Druckstärke.
- Neue Kernklasse `LegacyTreeUndoModel`; `LegacyInkPictureModel` erweitert um `pressureWidthFactor`.

## v112

- Markdown-Preview-Bridge erweitert: WebView-HTML-Vorschau im RTF-Bereich statt reiner Spannable-Vorschau.
- Unterstützt jetzt insbesondere Markdown-Tabellen, Ausrichtung, Aufgabenlisten, Referenzlinks, Blockquotes, Codeblöcke und Fußnoten-Vorschau.
- Cursor-/Fokus-Wiederherstellung nach dem Ausschalten der Markdown-Vorschau gehärtet.

### v113
- Langdruck-Verhalten gezielt geändert: In der RTF-Box wird beim normalen Gedrückthalten wieder nur das Wort markiert; das eigene Android-Kontextmenü erschien erst nach 6 Sekunden dauerhaftem Halten.
- Baum-Langdruck klappt Knoten wieder wie früher auf/zu; das Baum-Kontextmenü erschien erst nach 6 Sekunden dauerhaftem Halten.
- Die verzögerten Kontextmenüs werden bei Loslassen, Scrollbewegung, Mehrfinger-Geste oder Abbruch wieder verworfen.
- Version erhöht auf `versionCode 113` / `versionName 1.0.113-java-android-nativ`.

### v114
- Die Verzögerung für beide verzögerten Kontextmenüs wurde von 6 Sekunden auf 4 Sekunden reduziert.
- Rückkehr aus der Markdown-WebView-Vorschau härtet den echten RTF-Editor: KeyListener/InputType, Fokus, sichtbarer Cursor, Auswahlfarbe und Cursor-Zeichnung werden explizit wiederhergestellt.
- Die RTF-Auswahl bleibt nach dem Ausschalten der Markdown-Vorschau wieder sichtbar, damit man erkennt, wie viel Text markiert ist.
- Version erhöht auf `versionCode 114` / `versionName 1.0.114-java-android-nativ`.
