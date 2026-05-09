# Notizen Java Android Nativ

Native Android-/Java-Portierung aus den bereitgestellten Projekten **Notizen.NET** und **Notizen PyQt**.

## Status

Dieses Paket ist ein Android-Studio-Projekt mit einer nativen Java-App. Es enthält keine Python-, Qt-, .NET-, Xamarin- oder Compose-Abhängigkeiten. Die UI wird direkt mit Android-Views erzeugt; der portable Kern liegt als reines Java unter `de.notizen.android.core`.

In dieser Ausführungsumgebung sind kein Android SDK und kein Gradle installiert. Deshalb wurde hier kein APK erzeugt. Der portable Java-Kern wurde mit `javac` getestet. Lokal lässt sich das Projekt in Android Studio öffnen oder per Gradle bauen.

## Enthaltene Funktionen

- ALX-Dateien öffnen und speichern über Android Storage Access Framework.
- Notizen.NET-/PyQt-kompatibles `notizen-alx2`-XML mit GZip und UTF-16.
- Legacy-ALX-Passwortverschlüsselung über die alten dreifachen DES-CBC-Schritte.
- Älteres `notes_doc`-Importformat mit `node`/`leaf`/`leaf_text`.
- Baum mit Knoten, Unterknoten, Legacy-Expansion, Löschen und der alten „Neu daneben“-Regel.
- Knoten-Zwischenablage als `application/x-notizen-pyqt-node+xml`; Kopieren, Ausschneiden und Einfügen von Teilbäumen.
- Verschieben von Knoten innerhalb derselben Ebene über `Rauf`/`Runter`, `Einrücken`/`Ausrücken` sowie `Vor Ziel` als mobile Dialog-Umsetzung der alten WinForms-Dragregel.
- Datumseinfügung nach altem Editor-Nutzen.
- Legacy-Aufzählungspunkt aus der alten `ToolStrip_dot`-Aktion (`CR + • + drei Leerzeichen`), Android-normalisiert als Zeilenumbruch.
- Legacy-Tastaturkürzel aus Notizen.NET/PyQt für Hardware-Tastaturen, z. B. `Ctrl+S`, `Ctrl+O`, `Ctrl+F`, `Ctrl+Space`, Baum-`Insert`/`Delete`/`Enter` und Editor-Schriftgröße per `Ctrl+Plus/Minus`.
- Legacy-Farbpalette aus Notizen.NET inklusive der historischen `Random.Next(0,14)`-Reichweite.
- Haftnotiz-Metadaten (`visible`, `x`, `y`, `width`, `height`, `opacity`, `argb`) anzeigen, ändern und entfernen.
- Wecker-Metadaten und native Android-Weckerplanung mit Notification-Ausgabe. Wiederholungen: einmalig, täglich, wöchentlich, monatlich, jährlich; die alten `wecker.vb`-Checkboxnamen und Intervall-Einheiten liegen als Java-Mapping vor.
- RTF wird im Editor nativ als Android-`Spannable` angezeigt: Textformatierungen, Schriftgrößen, Schriftarten, Farben, Hervorhebungen, Ausrichtung, Einzüge, Hyperlinks, Objekt-/Feldplatzhalter und Bilder aus `\pict`-Blöcken bleiben sichtbar bzw. erhalten. Zusätzlich gibt es eine HTML-Vorschau für weitere RichTextBox-Details.
- HTML-Import in den aktuellen Knoten: HTML wird dependency-frei in RTF übertragen, inklusive Basis-Tags/CSS, Data-URI-Bildern, Hyperlinks, Listen-/Tabellen-Plaintext-Brücke und erhaltenen Notizen-Spezialfeldern.
- TXT-/RTF-Import in die aktuelle Notiz, inklusive BOM-Erkennung, UTF-8/CP1252-Fallback und RTF-Erhalt bei echten `.rtf`-Quellen.
- Suche in Baumtexten und optional Titeln; „ganze Wörter“ benutzt die historische Trennregel Space/CR/LF.
- TXT-, HTML- und RTF-Export des Gesamtbaums; HTML nutzt jetzt den PyQt-ähnlichen Abschnitts-/Nummerierungsaufbau mit CSS und erhält RTF-Formatierungen, Bilder, Hyperlink-Felder und Objekt-Platzhalter besser als die reine Textbrücke.
- „Gesamt“-Funktion erzeugt eine zusammengefasste Notiz aus dem aktuellen Baum.
- Erweiterte Statistik über Knoten, Blätter, Baumtiefe, Desktop-Notizen, Zeilen, Wörter, Zeichen, Bilder und RTF-Größe.
- App-interne Sicherungskopien vor dem Überschreiben gespeicherter Dateien, plus Sicherungsauswahl zum Wiederöffnen. Autosave nutzt die alte Guard-Regel: nur geänderte Dokumente mit bereits verbundenem Android-URI, Rohdateipfad oder FTP-Ziel werden automatisch gesichert.
- Legacy-Konfigurationsdatei `notizen.config.xml`: Import, Export/Schreiben als UTF-16-XML, Recent-Files mit Wiederöffnen/Rotation, Backup-Anzahl, Autosave-Normalisierung und FTP-Felder.
- Passives FTP-Öffnen und FTP-Speichern für `.alx`-Dateien mit URL-Dekodierung wie im PyQt-Port.
- Erhalt von unbekannten ALX-Rootattributen, Root-XML-Elementen sowie unbekannten Attributen/XML-Kindelementen unter `Notiz`.
- Erhalt unbekannter Root-/Elementattribute und unbekannter Top-Level-Elemente in `notizen.config.xml`.
- Vollständiger Legacy-Sprachkatalog aus der Desktop-/PyQt-Portierung mit Deutsch, English, Chinese, français, spanish und russian, inklusive Alias-/Auto-Erkennung.
- Feedback-Legacy-Regeln: Mindestlänge, Tagesdrosselung, `Date.Ticks`-kompatible Speicherung als `long` und lokales UTF-16LE/GZip-Feedbackarchiv statt ungefragtem Netzversand.
- Legacy-Start-/Autostart-Hilfen: `/min`/`-min`, Help-Flags, `.alx`-/`ftp://`-Startziel, Windows-kompatibles Commandline-Quoting und Recent-File-Zielauswahl.
- Legacy-Pfade für `Documents/Notizen`, Standarddatei `unbenannt.alx`, Dateiname/Ordner-Auftrennung und mobile Normalisierung.
- WinForms-Haftnotiz-Geometriehilfen für Hover-Rahmen, versteckte Ränder, Opacity-/Transparenz-Menü, Titelleisten-Hit-Zonen und Resize-Zone.
- Legacy-Fensterstatus-Hilfen für minimierten Start, wiederherstellbare Geometrie und Schutz gegen unsichtbare/offscreen Desktop-Fensterdaten.
- Legacy-Backup-Hilfen mit altem Backupordner-/Dateinamensschema, Timestamp-Parser und Pruning-Regeln.
- Privacy-light ALX-Validierung nach PyQt-`legacy_validation.py`: strukturelle Zusammenfassung ohne Notiztexte, SHA-256-Baum-/Inhalts-Hashes und Load→Dump→Load-Roundtripcheck.
- Bild-Einfügen aus Android-Dateiauswahl: PNG/JPEG/BMP werden als RTF-`pict` in den aktuellen Knoten eingebettet und sofort in der RTF-Box sichtbar angezeigt.
- RTF-Content-Parts im reinen Java-Kern: formatierte Textsegmente, Hyperlink-Felder, generische Felder, Bilder und OLE-/Objektgruppen können getrennt ausgewertet werden.
- Kombinierter RTF-Gesamtexport erhält jetzt auch Textformatierungen über eigene Font-/Farbtabellen, nicht nur Spezialgruppen.
- Native RTF-Auswahlformatierung für Android: markierter Text kann über Toolbar oder Dialog direkt fett/kursiv/unterstrichen/durchgestrichen, größer/kleiner, mit Schriftart/Schriftgröße, Farben und Ausrichtung formatiert werden; die Änderungen werden wieder als RTF gespeichert.
- Legacy-`ToolStrip_fontstyle` liegt als Java-Modell mit alten ObjectNames, Shortcuts, Icon-only-Beschreibungen und Fontsize-Grenzen vor.
- Android-Drucken über WebView/PrintManager für aktuelle Notiz, aktuellen Teilbaum oder gesamten Baum.
- Kompakte Desktop-Haftnotiz-HTML-Brücke: `\line` wird als `<br/>` statt Absatzlayout ausgegeben, passend für alte Sticky-Note-Inhalte.
- Testbare Legacy-Systemintegration aus dem PyQt-Port: Windows-`.alx`-Dateizuordnung, GNOME-Desktop-Exec-Zeile, GNOME-Tray-Sicherheitsentscheidung und Qt/GNOME-Display-Environment-Normalisierung liegen jetzt als Java-Hilfen vor.

## Bewusst mobil angepasst

Android hat keinen Windows-Tray, keine WinForms-Desktop-Haftnotizen und kein frei schwebendes Desktop-Fenstersystem wie die alte Anwendung. Diese Zustände werden deshalb in Dateien erhalten und können mobil editiert werden, aber sie erscheinen nicht als separate Desktop-Fenster.

Die RTF-Bearbeitung ist mobil-pragmatisch: RTF wird in Text konvertiert und im nativen `EditText` bearbeitet. Wird eine Notiz nicht editiert, bleibt ihr ursprüngliches RTF erhalten. Wird sie editiert, wird der neue Text als sauberes Plain-RTF gespeichert. Die Vorschau, der HTML-Export und der HTML-Import nutzen aber eine erweiterte RTF/HTML-Brücke für typische RichTextBox-Formatierung, Bilder, Hyperlinks, Felder und OLE-/Objektgruppen.

Klassisches FTP wurde als optionaler Legacy-Pfad ergänzt. Es nutzt passives FTP und läuft im Hintergrundthread, bleibt aber bewusst schlicht: keine SFTP-/FTPS-Aufwertung und keine Serververwaltung. Für normale lokale Dateien nutzt die App weiterhin das Android Storage Access Framework.

## Bauen

Voraussetzungen lokal:

- JDK 17
- Android Studio oder Gradle mit Android-Gradle-Plugin-Umgebung
- Android SDK Platform 34

Mit installiertem Gradle:

```bash
gradle :app:assembleDebug
```

Manuell in Termux ohne Gradle, wenn `aapt2`, `d8`, `apksigner`, `zipalign`, JDK und `android.jar` API 34 vorhanden sind:

```bash
./notizen-build-apk
# oder
./tools/notizen-build-apk-termux.sh
```

Mit Android Studio:

1. Projektordner `NotizenJavaAndroidNativ` öffnen.
2. SDK installieren lassen, falls Android Studio danach fragt.
3. Build Variant `debug` wählen.
4. `Build > Make Project` oder `Run` ausführen.

Der Debug-APK-Ausgabepfad ist normalerweise:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Reine Kern-Tests ohne Android SDK

```bash
./tools/run_core_tests.sh
```

Getestet werden:

- RTF-Plaintext-Roundtrip inklusive Unicode, ANSI-Codepage, Feldresultaten und Objekt-Platzhaltern.
- RTF-Bildextraktion, HTML-Bilddaten, Hyperlink-Felder, Objektgruppen und kombinierter RTF-Export mit Spezialgruppen.
- Formatierte RTF→HTML-Darstellung und HTML→RTF-Roundtrips für Bold/Italic/Underline/Strike, Farben, Highlights, Schriftarten, Größen, Ausrichtung, Einzüge, First-Line-Indent, Absatzabstände, Zeilenabstände, Super/Subscript, versteckten Text, Listen/Tabellen, Data-URI-Bilder, Felder und Objektgruppen.
- ALX-GZip/XML-Roundtrip inklusive unbekannter Root-/Notiz-Fragmente.
- verschlüsseltes ALX mit Passwortpflicht.
- Suche, Legacy-Ganzwortmodus, kompakte Such-Snippets und Pfad-/Positionslabels.
- Baumoperationen inklusive Einrücken/Ausrücken und Legacy-Dragregel `move before target`.
- nummerierter Text-Export.
- Knoten-Zwischenablage inklusive Teilbaum-Roundtrip.
- Legacy-Farbpalette.
- Wecker-Wiederholungsregeln inklusive Legacy-Checkbox-/Intervall-Mappings.
- RTF-Gesamtexport als portable Plain-RTF-Brücke mit Bild-/Feld-/Objekt-Erhalt.
- RTF-Content-Part-Parser, Font-/Color-Table-Extraktion, Legacy-Listentext aus `\*\pntext`/`\*\listtext` und kompakte Desktop-HTML-Ausgabe.
- Legacy-Konfigurationsparser, Passthrough unbekannter Config-Elemente und FTP-Zielparser.
- Erweiterte Statistik.
- Legacy-Editor-Aktionen und Hardware-Tastaturkürzel.
- PyQt-ähnlicher HTML-Export mit CSS, Abschnittsstruktur und Nummerierung.
- Legacy-I18n-Sprachkatalog, Alias-/Auto-Erkennung und Übersetzungswerte.
- Feedback-Drosselung, UTF-16LE/GZip-Archiv und `Date.Ticks`-Roundtrip in `notizen.config.xml`.
- Legacy-Startargumente, Autostart-Zielwahl, Pfadregeln, Recent-File-Rotation, Autosave-Entscheidungen und Desktop-Haftnotiz-Geometrie.
- Legacy-Systemintegration: Windows-Registry-Vorschau für `.alx`, Linux/GNOME-Desktop-Exec, GNOME-Tray-Regeln und Display-/Wayland-/Qt-Environment-Normalisierung.
- Fensterstatus-Sanitizing und Backup-Dateinamens-/Pruning-Regeln.
- Privacy-light ALX-Summary und Roundtrip-Validierung mit Baum-/Inhalts-Hashes.

Aktuelles Testergebnis in dieser Umgebung:

```text
Core tests OK
```

## v9 Buildfix / Termux-Kompatibilität

- Version erhöht auf `versionCode 9` / `versionName 1.0.9-java-native`.
- Gradle-Parameter auf `compileSdk 34` und `targetSdk 34` gesetzt, passend zum gemeldeten Termux-Build mit `android.jar` API 34.
- Android-Java-Buildfehler behoben: `EditText.isHorizontallyScrolling()` existiert in der Android-API nicht. Der horizontale Scrollmodus wird jetzt intern über `editorHorizontallyScrolling` gespeichert und mit `setHorizontallyScrolling(...)` angewendet.
- Der Fix betrifft die Aktion `RTF Format -> Scrollbars/Zeilenumbruch wechseln`.


## v10 Weitertranspilierung

- Version erhöht auf `versionCode 10` / `versionName 1.0.10-java-native`.
- Neue Kernklasse `LegacyAutosave`: portiert die alte WinForms-/PyQt-Autosave-Guardregel sauber als testbare Java-Entscheidung. Android nutzt sie jetzt für periodisches Speichern über SAF-URI, Rohdateipfad oder FTP, ohne neue Ziele stillschweigend anzulegen.
- Android-UI erweitert um `Letzte`: gespeicherte Recent-Files aus `notizen.config.xml`/App-Settings können geöffnet und nach Legacy-Regel rotiert werden; `ftp://`, `content://`, `file://` und rohe Dateipfade werden getrennt behandelt.
- Android-UI erweitert um `Vor Ziel`: mobile Dialog-Umsetzung der alten TreeView-Dragregel `move before target`.
- `openOnceFile` aus der Legacy-Config wird beim Start einmalig übernommen und danach gelöscht, damit alte „einmal öffnen“-Semantik nicht dauerhaft wiederholt wird.
- Wecker-Port erweitert um die historischen `wecker.vb`-Checkboxnamen (`CheckBox15` bis `CheckBox13`), Wochentagslabels und Intervall-Einheiten.
- Neuer Termux-/No-Gradle-Buildhelfer `notizen-build-apk` und `tools/notizen-build-apk-termux.sh` für API-34-Builds mit `aapt2`, `d8`, `apksigner` und `zipalign`.
- Regressionstest ergänzt, der den v9-Buildfix absichert: `EditText.isHorizontallyScrolling()` darf nicht zurückkehren.

## Paketname

```text
de.notizen.android
```

## Lizenz

GPLv3, passend zu den Ausgangsarchiven.

## v11 Weitertranspilierung

- Version erhöht auf `versionCode 11` / `versionName 1.0.11-java-native`.
- Recent-/openOnce-/Autosave-Ziele werden über `LegacyOpenTarget` einheitlich als `content://`, `file://`, Rohdatei oder `ftp://` normalisiert.
- Windows-/PyQt-Autostart wurde weiter als Java-Kernmodell portiert: `Notizen PyQt.cmd` kann erzeugt, unverändert belassen oder entfernt werden.
- RTF-Tabellen werden in der HTML-/WebView-Brücke als echte HTML-Tabellen statt nur als Tabtext dargestellt.
- `desknote.vb`-Autosize-Entscheidungen liegen als `LegacyDesktopNoteAutoResize` vor.
- Haupttoolbar nutzt jetzt `LegacyToolbarPresentation`: kompakte Icon-only-Buttons mit Tooltips/Content-Description und stabilen Legacy-Actionnamen.
- Android-Manifest enthält getrennte ALX-Öffnungsfilter für `content://`-MIME-Typen und `.alx`/`.ALX`-Dateipfade.
- Tests erweitert für diese v11-Brücken; `./tools/run_core_tests.sh` bleibt grün.

## v13 Weitertranspilierung

- Version erhöht auf `versionCode 13` / `versionName 1.0.13-java-native`.
- Die v12-Kontextportierung wurde in diese Stufe integriert: `LegacyContextMenus`, formatgetreues Legacy-Datumseinfügen und Current-Node-Export für `Knoten TXT`/`Knoten RTF`.
- Neue Kernklasse `LegacyDialogModels`: `wanna_save.vb` und `wanna_restart.vb` liegen als testbare Java-Dialogmodelle vor. Android nutzt beim Öffnen/Neu/Beenden jetzt wieder die alte Drei-Wege-Frage: speichern, ohne Speichern fortfahren oder abbrechen.
- Neue Kernklasse `LegacyDesktopNoteContextMenu`: `desknote_kontext.vb` und `desknote_kontext_opacy.vb` sind als Java-Modell portiert, inklusive Sprachschlüsseln `kontext8` bis `kontext10` und Transparenzmenü `90 %` bis `0 %`.
- `LegacyRichTextToolbar` bildet jetzt zusätzlich die alten Designer-Controls `ToolStrip_fontsizenumber` und `ToolStrip_fonts` ab. Android kann markierten Text als RTF mit Schriftgröße oder Schriftart speichern.
- `LegacyRtfSelectionFormatter` nutzt die neue Schriftfamilien-Normalisierung und bleibt dadurch robuster gegen alte/unsaubere Fontnamen.
- Tests erweitert für Dialogmodelle, Haftnotiz-Kontextmenü, Opacity-Menü, Fontfamilien-/Fontgrößen-Toolbar, Current-Node-Export und Android-Brücken.

## v15 Weiterportierung

- `LegacyPasswordDialogModel` portiert die alten `passwort_dialog.vb`-/`passwort_dialog2.vb`-Regeln: 24-Zeichen-Grenze, Legacy-Padding, altes Passwort nur bei gesetztem Passwort und die drei alten Fehlerfälle.
- `LegacyFtpDialogModel` portiert `ftpkram.vb` als Java-Dialogmodell: FTP-Daten speichern, FTP öffnen, FTP hochladen, URL-Normalisierung und sichere Passwortmaskierung.
- `LegacySettingsDialogModel` ergänzt die alten Einstellungsabhängigkeiten für Autosave, Autostart, minimierten Start, Taskbar-Anzeige, Haftnotiz-Ränder und Backup-Anzahl.
- `LegacyAboutHelp` portiert die About-/Help-/Feedback-Texte aus `AboutBox1.vb` und `info_help_and_feedback.vb`.
- `LegacySearchDialogModel` normalisiert den Suchdialog und Ergebnisstatus für Android, ohne die bestehende Suchlogik zu ersetzen.
- Android-Build-Härtung: doppelte `pendingExportHtml`-Deklaration, doppeltes `KEYCODE_O`-Case und doppelte `Uri uri`-Deklaration werden per Regressionstest abgesichert.
- Version erhöht auf `versionCode 15` / `versionName 1.0.15-java-native`.

## v17 Weitertranspilierung

- Version erhöht auf `versionCode 17` / `versionName 1.0.17-java-native`.
- Die v16-Dialogportierung ist in dieser Stufe enthalten: `LegacyAlarmDialogModel` bildet den alten `wecker.vb`-Dialog mit RadioButton-Namen, Intervallfeldern, Wochentags-Checkboxen, Einheiten und Datumsformat `yyyy-MM-dd HH:mm` als Java-Kernmodell ab.
- `LegacyFontSizeEntry` portiert das alte `fontsize.vb`-Textfeld: leerer Zwischenzustand, Zahlenbereinigung, Rückfall auf den letzten gültigen Wert, Obergrenze `99` und Enter-Anwendung.
- Neue Kernklasse `LegacyEditorNodeSync`: überträgt die alte `inhalt.vb`/`CText.vb`-Bindung zwischen Baumknoten, Titelzeile, Editorinhalt und Desktop-Haftnotiz-Refresh in ein testbares Java-Modell.
- Neue Kernklasse `LegacyFileState`: modelliert die `Datei.vb`-Statusfelder für Startordner, Dateiname, Verzeichnis, gespeicherter Zustand, Dateiexistenz und `.alx`-Anzeigenamen.
- Neue Kernklasse `LegacySearchSession`: bildet die gecachte `suche.vb`-Suchsitzung ab. Gleiche Suchoptionen werden weitergeschaltet, geänderte Optionen bauen die Trefferliste neu auf.
- Android-Suche erweitert: zusätzlicher Schalter `Alle Knoten durchsuchen`; Suchbegriff und Optionen werden über `LegacySearchSession` wiederverwendet und der nächste Treffer wird zyklisch ausgewählt.
- Android-Wecker- und Schriftgrößendialog nutzen jetzt die neuen Kernmodelle direkt.
- Test-/Buildhärtung: `run_core_tests.sh` und das manuelle Termux-Buildscript nutzen explizite `-sourcepath`-Werte, damit Core-Tests und Android-Java-Kompilierung sich nicht gegenseitig Quellen einziehen.

## v23 Weitertranspilierung

- Version erhöht auf `versionCode 23` / `versionName 1.0.23-java-native`.
- Enthält die zuvor geplante v21-Schicht direkt: Legacy-Scrollleisten, Alle-Auf/Alle-Zu, Teilbaum-/Gesamt-Zusammenfassung und Rich-RTF-Zusammenfassung.
- Neue Kernklasse `LegacyScrollbars`: portiert den alten `ToolStrip_whatscroll`-/`scrollbars_choice`-Zyklus `None -> Horizontal -> Vertical -> Both` mit Persistenz über `notizen.config.xml` / Android-Settings.
- Neue Kernklasse `LegacyTreeExpansion`: portiert globales `ExpandAll` / `CollapseAll` für den Baum. Android-Aktionen: `Alle auf`, `Alle zu`.
- Neue Kernklasse `LegacyUnifiedNote`: trennt die alte `Einheit`-/`fasse_zusammen`-Logik in aktuellen Teilbaum und ganzen Baum. Android-Aktionen: `Teilbaum`, `Gesamt`.
- `Exporters.unifiedNote(...)` nutzt jetzt den Rich-RTF-Gesamtexport statt Plaintext-Konvertierung, damit Formatierungen, Hyperlinks, Bilder, Felder und Objektplatzhalter besser erhalten bleiben.
- Neue Kernklasse `LegacyTreeTraversal`: portiert die alte rekursive `Baum.allnodes`-Traversal inklusive Tiefe und Legacy-Pfadnummern.
- Neue Kernklasse `LegacyTreeDragDrop`: modelliert die alte TreeView-Drag/drop-Entscheidung für `move before target`, inklusive Root-, Self- und Descendant-Sperren.
- Neue Kernklasse `LegacyActionState`: testbares Enablement-Modell für Datei-, Baum-, Editor-, Export- und Legacy-Aktionen.
- Neue Kernklasse `LegacyDesktopNoteTreeOps`: portiert `mach_haft_weg` / `loesche_haftnotiz_aus_baum` als Teilbaum-Aufräumen alter Desktop-Haftnotizdaten. Android-Aktion: `Haft weg`.
- Neue Kernklasse `LegacyTreeDelete`: plant Löschen nach alter Baumlogik mit vorherigem sichtbarem Fallback-Knoten und Haftnotiz-Schließhinweis.
- Neue Kernklasse `LegacyStartMenuModel`: modelliert `Startmenue`, `Einheit`, `Export`, Recent-File-Einträge und Tray-Menü aus `Notizen.Designer.vb` in Java.
- Tests erweitert für Scrollleisten-Zyklus, Settings-Roundtrip, Alles-Auf/Alles-Zu, Rich-Unified-Export, Baum-Traversal, Drag/drop, Menümodell, Haftnotiz-Teilbaum-Aufräumen, Delete-Planung, Toolbar-Mappings und Action-Enablement.

## v29 Weitertranspilierung

- Version erhöht auf `versionCode 29` / `versionName 1.0.29-java-native`.
- RTF/CSS-Brücke weiter an die späten PyQt-/WinForms-Stufen angepasst: `\chcbpat`, `\expnd`, `\expndtw`, `\super0`/`\sub0`, `visibility:hidden`, `dir=rtl/ltr`, `padding-left`, `body text`, `body bgcolor`, `<kbd>` und `<samp>` werden jetzt modelliert.
- CSS-Farben wurden deutlich erweitert: CSS-Named-Colors, `rgb(...)`, `rgba(...)`, Prozentwerte und Qt-/CSS-artige 8-stellige Hexwerte wie `#AARRGGBB` werden normalisiert.
- Weiche RichTextBox-Zeilenumbrüche werden beim RTF-Escaping als `\line` erhalten, statt zu harten Absätzen `\par` zu werden.
- Neue Kernklasse `LegacyRichTextBoxSemantics`: WinForms-RichTextBox-nahe Dokument-CSS, Desktop-Haftnotiz-CSS, RTF-Metriken und Clipboard-Formatpriorität als testbares Java-Modell.
- Neue Kernklasse `LegacyClipboardFocus`: portiert die alte Fokus-Entscheidung zwischen Baum und Inhalt für Kopieren, Ausschneiden, Einfügen und Löschen.
- Neue Kernklasse `LegacyTreeLabelEditing`: portiert die alte TreeView-Label-Normalisierung mit `...` als Fallback für leere Knotentitel.
- Android-UI erweitert um `RTF Info`; Vorschau und RTF-Info nutzen die zentrale RichTextBox-WebView-Brücke.
- Tests erweitert für die neuen RTF-/CSS-Regeln, RichTextBox-Semantik, Clipboard-Fokus, TreeView-Label-Editing, Toolbar-Mapping und Termux-v29-Builddaten.

## v35 Weitertranspilierung

- Version erhöht auf `versionCode 35` / `versionName 1.0.35-java-native`.
- Neue Kernklasse `LegacyRecentMenu`: portiert das alte vierteilige Recent-File-Menü aus `merke_4dateien` und `changefiletoolstripitems`, inklusive Text/Tag-Trennung, Shift-Regel, Klickrotation und Missing-File-Wächter.
- Neue Kernklasse `LegacyDocumentTitle`: modelliert alte Datei-/Fenstertitel und Quellenzeile für ungespeicherte, lokale, Android-URI- und FTP-Ziele.
- Neue Kernklasse `LegacyWindowToggle`: portiert die sichtbare/minimierte/maximierte Hauptfenster-Entscheidung aus `change_win_state`, `VisibleChanged` und `Deactivate`.
- Neue Kernklasse `LegacyToolstripLayout`: portiert `toolstrip_breite` und die alte sichtbare ToolStrip-Breiten-/Anfügeposition als Java-Modell.
- Neue Kernklasse `LegacyFtpRetryPolicy`: portiert die alte `ftpversuche`-Regel für FTP-Verbindungsfehlerstatus `7`.
- Neue Kernklasse `LegacyDiagnosticReport`: privacy-light Diagnose ohne rohe Notiztexte, mit Version, Quelle, Änderungsstatus, Baumstatistik, RTF-Metriken und Einstellungsübersicht.
- Android-UI erweitert um `Diagnose`; Recent-Files werden jetzt mit Legacy-Dateiname/Ordner-Labels angezeigt und Titel/Dateizeile laufen über `LegacyDocumentTitle`.
- Tests erweitert für Recent-Menü, Fenstertoggle, Toolstrip-Breite, FTP-Retry, Diagnosebericht, Android-Brücken und Termux-v35-Builddaten.

## v47 Weitertranspilierung

- Version erhöht auf `versionCode 47` / `versionName 1.0.47-java-native`.
- Die v41-Farb-/Dateidialog-Schicht ist direkt enthalten: `LegacyColorDialogModel` portiert alte WinForms-ColorDialog-Entscheidungen für Knotenfarben, RTF-Textfarbe, RTF-Hervorhebung und Desktop-Haftnotiz-Farben.
- Markierter Text kann über Android `RTF Format` jetzt auch als RTF-Textfarbe (`\\cf1`) oder RTF-Hervorhebung (`\\highlight1`) gespeichert werden; sichtbar wird das in Vorschau, HTML-Brücke und Export.
- Neue Kernklasse `LegacyFileDialogModel`: zentrale OpenFileDialog-/SaveFileDialog-Spezifikation für ALX, TXT, RTF, HTML, Config und Bildauswahl mit Legacy-Filtern, Standardnamen, Dateiendungen und Android-MIME-Brücken.
- Android-Dateiauswahl läuft jetzt zentral über diese Spezifikation; rohe verteilte `ACTION_OPEN_DOCUMENT`-/`ACTION_CREATE_DOCUMENT`-Blöcke wurden entfernt.
- Neue Kernklasse `LegacyTextExportModel`: trennt TXT-Exportpfade in ANSI/Windows-1252, UTF-8 und Unicode/UTF-16LE mit BOM und normalisiert Zeilenenden nach alter CRLF-Regel.
- Android-Toolbar ergänzt um `Export ANSI` und `Export Unicode`; `Export TXT` bleibt als UTF-8-Pfad erhalten.
- Tests erweitert für ColorDialog-Modell, RTF-Farbformatierung, FileDialog-Spezifikationen, ANSI-/UTF-8-/Unicode-TXT-Bytes, Android-Brücken und Termux-v47-Builddaten.

## v59 Weitertranspilierung

- Version erhöht auf `versionCode 59` / `versionName 1.0.59-java-native`.
- `LegacyConfigSnapshot` ergänzt die alten `xml_kram.vb`-Direktzugriffe und `on_load`/`on_exit`-Regeln als Java-Modell: letzter Ordner, letzte Datei, ToolStrip-Positionen, Scrollleistenwahl, Fensterstatus, Offscreen-Schutz und minimierter Taskbar-Pulse.
- `LegacyDesktopNotePaint` modelliert die alte `desknote.vb`-Paint-/Layoutlogik: Ecke-Ressource `aa`, Titelbereich, Trennlinien, `_`-/`x`-Schaltflächen, Editorrechteck und MouseLeave-Verbergen.
- `LegacySaveWorkflow` bündelt die alten Save-Entscheidungen aus `Datei.vb`/`Notizen.vb` für Android-URI, lokalen Dateipfad, FTP-Ziel und Save-As-Fallback.
- `LegacySearchResultNavigator` portiert die Suchergebnisfenster-Navigation aus `suchergebnisse.vb` als testbaren Java-Kern.
- Android ergänzt die Aktionen `Config` und `Haft Layout`; `Speichern` nutzt jetzt das zentrale Legacy-Save-Workflow-Modell.
- Toolbar-Präsentation ergänzt Mapping für `Config`, `Haft Layout`, `Export ANSI` und `Export Unicode`.
- Tests erweitert für Legacy-Config-Snapshot, Haftnotiz-Paintmodell, Save-Workflow, Suchergebnisnavigation, Android-Brücken und Termux-v59-Builddaten.
## v71 Weitertranspilierung

- Version erhöht auf `versionCode 71` / `versionName 1.0.71-java-native`.
- Die v59-Schicht bleibt vollständig erhalten und wurde mit der neuen v65/v71-Schicht sauber zusammengeführt.
- Neue Lifecycle-/Schließen-Schicht: `LegacyDocumentLifecycle`, `LegacyCloseResetModel`, `LegacyConfigLifecycle` und `LegacyAppDataConfigPath`.
- Neue Baum-Erzeugungsbrücke: `LegacyTreeCreation`; Android-Aktionen `Kind` und `Daneben` nutzen jetzt das Kernmodell.
- Neue Haftnotiz-Tray-Brücke: `LegacyDesktopNoteTrayRegistry` und `LegacyTraySelectionModel`; Android-Aktion `Haftliste`.
- Neue Runtime-/Launcher-Schicht: `LegacyRuntimeIdentity`, `LegacyMainLayoutModel` und `LegacyPackagePermissionModel`; Android-Aktionen `Layout` und `Launcher`.
- Test-Härtung: `tools/run_core_tests.sh` nutzt `-proc:none`, `-XDcompilePolicy=simple`, begrenzten Heap und einen Prozessor für stabilere Termux-/Container-Läufe.


## v95 Weitertranspilierung

- Version erhöht auf `versionCode 95` / `versionName 1.0.95-java-native`.
- Enthält die v83-Schicht direkt: `LegacyActivationDialogFocus`, `LegacyToolbarToggleModel`, `LegacyMainWindowChrome`, `LegacyAutosaveTimerTick`, `LegacyCTextState` und `LegacyApkBuildPipeline`.
- Neue Kernklasse `LegacyFontSetModel`: portiert die verhaltensrelevanten Entscheidungen aus `Notizen.vb font_set(...)`, inklusive Baum-vs-Editor-Fokus, Auswahl-auf-Alles-Fallback, Fett/Kursiv/Unterstrichen/Durchgestrichen, Schriftfamilie, Schriftgröße und Desktop-Haftnotiz-Reload-Markierung.
- Neue Kernklasse `LegacyWindowMoveResizeModel`: modelliert die alte linke-Maustaste-Move/Resize/Minimize-Logik des benutzerdefinierten Hauptfenster-/Haftnotiz-Chromes inklusive `mouseclickmovetogether_block`.
- Neue Kernklasse `LegacyAlxStreamPipeline`: dokumentiert die alte ALX-Speicher-/Öffnen-Schichtreihenfolge für lokale Datei, FTP, Android-SAF-URI und Speicherziel, inklusive Backup-Entscheidung, GZip, UTF-16-XML und alter dreistufiger DES-Passwortkette.
- Android-UI erweitert um `Dialoge`, `Toolbars`, `Fenster`, `Autosave`, `Fontplan`, `Maus`, `ALX Pipe` und `Buildplan`.
- Termux-No-Gradle-Buildpfad gehärtet: `d8` wird jetzt mit `--lib "$ANDROID_JAR"` aufgerufen.


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
- ALX-Baumzustand wird beim Laden jetzt sichtbar übernommen: offene und geschlossene Knoten aus `isexpanded` sowie Aliasformen wie `isExpanded`, `IsExpanded` und `expanded` werden gelesen und beim Speichern wieder als kanonisches `isexpanded` geschrieben.
- Nach dem Öffnen zeigt die Statuszeile eine kurze Baumzustands-Zusammenfassung, damit sofort klar ist, dass der ALX-Auf-/Zuklappzustand übernommen wurde.
- Obere Toolbar neu geordnet in drei horizontale Listen: `Datei`, `Baum` und `Text`. Alle Toolbar-Schaltflächen sind quadratisch und nutzen kompaktere Icon-/Glyph-Darstellung mit Tooltip/Accessibility-Text.
- Textformat-Buttons `Fett`, `Kursiv`, `Größer`, `Kleiner`, `Schriftart` und `Größe` haben eigene Icon-Mappings bekommen.
- Baum- und Editorbereich in breiter Ansicht haben einen ziehbaren Trenner. Die Baumbreite kann mit dem Finger stark verkleinert oder vergrößert werden und wird in der Android-Config gespeichert.
- Baumdarstellung optisch geglättet: hellere Zeilenflächen, abgerundete Auswahl und klarere Auf-/Zu-Markierungen.
- Kernvalidierung: `./tools/run_core_tests.sh` meldet `Core tests OK`; Buildskripte wurden per `bash -n` geprüft.

