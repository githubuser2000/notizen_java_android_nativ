package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LegacyI18n {
    public static final String[] LEGACY_LANGUAGE_KEYS = new String[]{
            "Strip1_1",
            "Strip1_2",
            "Strip1_3",
            "Strip1_4",
            "Strip1_5",
            "Strip1_6",
            "Strip1_7",
            "Strip1_8",
            "Strip1_9",
            "Strip1_10",
            "Strip1_11",
            "Strip1_12",
            "Strip1_13",
            "Strip1_14",
            "Strip1_15",
            "Strip1_16",
            "Strip1_17",
            "Strip1_18",
            "Strip1_19",
            "Strip2_1",
            "Strip3_1",
            "Strip3_2",
            "Strip4_1",
            "Strip4_2",
            "Strip4_3",
            "e1",
            "e2",
            "e3",
            "e4",
            "kontext1",
            "kontext2",
            "kontext3",
            "kontext4",
            "kontext5",
            "kontext2_1",
            "kontext2_2",
            "kontext2_3",
            "kontext2_4",
            "kontext2_5",
            "etwa_loeschen",
            "ja",
            "nein",
            "info1",
            "info2",
            "info3",
            "suche1",
            "suche2",
            "suche3",
            "suche4",
            "suche5",
            "neu1",
            "neu2",
            "eeoff1",
            "eeoff2",
            "eeoff3",
            "saveA",
            "OK",
            "abbrechen",
            "kontext2_6",
            "kontext2_7",
            "kontext2_8",
            "font_regular",
            "font_bold",
            "font_italic",
            "font_underline",
            "font_strikeout",
            "font_bigger",
            "font_smaller",
            "unity_note",
            "Strip1_20",
            "Strip1_1_1",
            "Strip1_1_2",
            "fehler1",
            "fehler2",
            "strip1_21",
            "pass1",
            "pass2",
            "pass3",
            "passerror1",
            "passerror2",
            "password",
            "passwort_falsch",
            "passerror3",
            "pw_unten_info",
            "kontext6",
            "kontext7",
            "kontext8",
            "kontext9",
            "kontext10",
            "e5",
            "kontext2_9",
            "kontext2_10",
            "sicherungen",
            "autostart",
            "color",
            "passwort",
            "pfaddatei",
            "alxerror",
            "export",
            "exportrtf",
            "exporttxt",
            "exporttxt2",
            "nexxt",
            "under",
            "kontext11",
            "suche6",
            "suche7",
            "aboutinfotext",
            "feedback",
            "close",
            "send",
            "no_send",
            "char10minimum",
            "no_feedback_sent",
            "minautostart",
            "autosave",
            "seconds",
            "scroll"
    };
    private static final Map<String,Integer> LANGUAGE_INDEX = new LinkedHashMap<>();
    private static final Map<String,Map<String,String>> TRANSLATIONS = new LinkedHashMap<>();
    private static final Map<String,String> LANGUAGE_ALIASES = new LinkedHashMap<>();
    private static final String[][] DISPLAY_LANGUAGES = new String[][]{
            {"Auto","Auto"},
            {"Deutsch","Deutsch"},
            {"English","English"},
            {"Français","français"},
            {"Español","spanish"},
            {"Русский","russian"},
            {"中文","Chinese"}
    };
    static {
        for(int i=0;i<LEGACY_LANGUAGE_KEYS.length;i++) LANGUAGE_INDEX.put(LEGACY_LANGUAGE_KEYS[i],i);
        LANGUAGE_ALIASES.put("de","Deutsch");
        LANGUAGE_ALIASES.put("deutsch","Deutsch");
        LANGUAGE_ALIASES.put("german","Deutsch");
        LANGUAGE_ALIASES.put("en","English");
        LANGUAGE_ALIASES.put("english","English");
        LANGUAGE_ALIASES.put("englisch","English");
        LANGUAGE_ALIASES.put("fr","français");
        LANGUAGE_ALIASES.put("français","français");
        LANGUAGE_ALIASES.put("francais","français");
        LANGUAGE_ALIASES.put("french","français");
        LANGUAGE_ALIASES.put("es","spanish");
        LANGUAGE_ALIASES.put("spanish","spanish");
        LANGUAGE_ALIASES.put("español","spanish");
        LANGUAGE_ALIASES.put("espanol","spanish");
        LANGUAGE_ALIASES.put("ru","russian");
        LANGUAGE_ALIASES.put("russian","russian");
        LANGUAGE_ALIASES.put("русский","russian");
        LANGUAGE_ALIASES.put("zh","Chinese");
        LANGUAGE_ALIASES.put("chinese","Chinese");
        LANGUAGE_ALIASES.put("中文","Chinese");
        TRANSLATIONS.put("Deutsch",loadDeutsch());
        TRANSLATIONS.put("English",loadEnglish());
        TRANSLATIONS.put("Chinese",loadChinese());
        TRANSLATIONS.put("français",loadfrançais());
        TRANSLATIONS.put("spanish",loadspanish());
        TRANSLATIONS.put("russian",loadrussian());
    }
    private LegacyI18n() {}
    private static Map<String,String> loadDeutsch(){ LinkedHashMap<String,String> m=new LinkedHashMap<>();
        m.put("Strip1_1","&Menü");
        m.put("Strip1_2","&Neue Datei STRG+N");
        m.put("Strip1_3","&Öffnen        STRG+O");
        m.put("Strip1_4","Speichern   STRG+S");
        m.put("Strip1_5","Speichern &unter");
        m.put("Strip1_6","Schließen");
        m.put("Strip1_7","&Einstellungen");
        m.put("Strip1_8","&Beenden    STRG+Q");
        m.put("Strip1_9","&Hilfe");
        m.put("Strip1_10","fuer den Programmierer hoffentlich");
        m.put("Strip1_11","Neue Datei");
        m.put("Strip1_12","Öffnen");
        m.put("Strip1_13","Speichern ");
        m.put("Strip1_14","Schließen");
        m.put("Strip1_15","Drucken");
        m.put("Strip1_16","Schrift");
        m.put("Strip1_17","Bearbeiten");
        m.put("Strip1_18","Neu/Entf.");
        m.put("Strip1_19","Werkzeug-Streifen");
        m.put("Strip2_1","Schrift");
        m.put("Strip3_1","Neues Element");
        m.put("Strip3_2","Element löschen");
        m.put("Strip4_1","Aussschneiden STRG+X");
        m.put("Strip4_2","Kopieren STRG+C");
        m.put("Strip4_3","Einfügen STRG+V");
        m.put("e1","Einstellungen");
        m.put("e2","In Deskbar zeigen");
        m.put("e3","Sprache");
        m.put("e4","ok");
        m.put("kontext1","Kopieren");
        m.put("kontext2","Ausschneiden");
        m.put("kontext3","Einfuegen");
        m.put("kontext4","Löschen");
        m.put("kontext5","Suchen");
        m.put("kontext2_1","Neu darunter [einfg]");
        m.put("kontext2_2","Umbenennen ");
        m.put("kontext2_3","Löschen [entf]");
        m.put("kontext2_4","Speichern");
        m.put("kontext2_5","Desktop-Notiz");
        m.put("etwa_loeschen","Möchten Sie den Knoten wirklich löschen?");
        m.put("ja","Ja");
        m.put("nein","Nein");
        m.put("info1","Baum in dem die Notizen geordnet sind");
        m.put("info2","Anzeige der jeweiligen Notiz");
        m.put("info3","Zeigen/Ausblenden");
        m.put("suche1","Suche");
        m.put("suche2","Suchen");
        m.put("suche3","Fertig");
        m.put("suche4","Alle Knoten durchsuchen?");
        m.put("suche5","Ergebnisse:");
        m.put("neu1","Wollen Sie vorher speichern?");
        m.put("neu2","von vorn beginnen");
        m.put("eeoff1","von vorn beginnen");
        m.put("eeoff2","Vorhandene Daten werden gelöscht.");
        m.put("eeoff3","Sind sie sicher, dass Sie vorn beginnen wollen?");
        m.put("saveA","Möchten Sie vorher speichern?");
        m.put("OK","OK");
        m.put("abbrechen","abbrechen");
        m.put("kontext2_6","Kopieren");
        m.put("kontext2_7","Ausschneiden");
        m.put("kontext2_8","Einfügen");
        m.put("font_regular","normale Schrift");
        m.put("font_bold","Fett-Schrift");
        m.put("font_italic","Kursiv-Schrift");
        m.put("font_underline","unterstrichen");
        m.put("font_strikeout","durchgestrichen");
        m.put("font_bigger","grössere Schrift");
        m.put("font_smaller","kleinere Schrift");
        m.put("unity_note","Zusammenfassen");
        m.put("Strip1_20","Info + Hilfe + Feedback");
        m.put("Strip1_1_1","alles");
        m.put("Strip1_1_2","unterhalb des markierten Knotens");
        m.put("fehler1","Keine Datei wurde geladen.");
        m.put("fehler2","Fehler beim Laden von XML Code:");
        m.put("strip1_21","Passwort setzen");
        m.put("pass1","altes Passwort");
        m.put("pass2","neues Passwort");
        m.put("pass3","wiederholen");
        m.put("passerror1","Das Passwort darf nicht mehr als 24 Zeichen haben.");
        m.put("passerror2","Das alte Passwort ist falsch.");
        m.put("password","Passwort");
        m.put("passwort_falsch","Passwort falsch oder Datei fehlerhaft");
        m.put("passerror3","Die letzten beiden Eingaben sind nicht gleich.");
        m.put("pw_unten_info","Leeres Feld bedeutet kein Passwort.");
        m.put("kontext6","Bild einfügen");
        m.put("kontext7","Datum einfügen");
        m.put("kontext8","Hintergrundfarbe");
        m.put("kontext9","Minimieren");
        m.put("kontext10","Schließen");
        m.put("e5","Desktop-Notitz-Fensterrand einblenden");
        m.put("kontext2_9","Hintergrundfarbe");
        m.put("kontext2_10","Schriftfarbe");
        m.put("sicherungen","Sicherungen");
        m.put("autostart","Autostart");
        m.put("color","Farbe");
        m.put("passwort","Passwort");
        m.put("pfaddatei","Pfad+Datei");
        m.put("alxerror","Die Datei muss auf .alx enden!");
        m.put("export","Export");
        m.put("exportrtf","in rtf");
        m.put("exporttxt","in ansi txt");
        m.put("exporttxt2","in unicode txt");
        m.put("nexxt","daneben [Enter]");
        m.put("under","darunter [einfg]");
        m.put("kontext11","Neu daneben [Enter]");
        m.put("suche6","ganze Wörter");
        m.put("suche7","Groß-/Klein-Schreibung beachten");
        m.put("aboutinfotext","• Eine Desktop-Notitz erstellt man mit dem Menü eines Knotens und erreicht man dann über das Menü des Trayicons.\r\n\r\n• Einzelne Knoten lassen sich als rtf Datei zusammenfassen, die z.B. mit Wordpad lesbar sind.\r\n\r\n• Erstellt man eine Verknüpfung mit dem Ziel \"C:\\Ort1\\notizen.exe\" -min \"C:\\Dokumente und Einstellungen\\Benutzer\\Eigene Dateien\\Notizen\\datei.alx\" also erst notizen.exe und die alx-Datei, dann öffnet sich das Programm zusammen mit der Notizendatei. Beide Ortsangaben müssen am Besten in Anführungszeichen stehen. Stellt man diese Verknüpfung in den Autostartordner, so hat man immer seine gut sortierten Notizen zur Hand.\r\n\r\n• Die Anhabe /min sorgt als Programmstartangabe z.B. in einer Verknüpfung für ein minimiertes Hauptfenster bei Beginn. (notizen.exe /min Zieldatei)\" \r\n\r\n• Startet man Notizen .Net mit Adminrechten kann danach eine alx-Datei durch den Explorerer erkannt werden.");
        m.put("feedback","persönliche Meinung / Fehlermeldung / Feature-Vorschlag");
        m.put("close","schließen");
        m.put("send","senden");
        m.put("no_send","Nach 3 Feebacks kann man erst wieder am nächsten Tag senden.");
        m.put("char10minimum","Geben sie mindestens 10 Zeichen ein!");
        m.put("no_feedback_sent","Feedback konnte nicht gesendet werden.");
        m.put("minautostart","minimierter autostart");
        m.put("autosave","automatisches speichern jede");
        m.put("seconds","Sekunde");
        m.put("scroll","scroll");
        return Collections.unmodifiableMap(m); }
    private static Map<String,String> loadEnglish(){ LinkedHashMap<String,String> m=new LinkedHashMap<>();
        m.put("Strip1_1","&Menu");
        m.put("Strip1_2","&New File   CTRL+N");
        m.put("Strip1_3","&Open        CTRL+O");
        m.put("Strip1_4","Save         CTRL+S");
        m.put("Strip1_5","save &as");
        m.put("Strip1_6","&Close");
        m.put("Strip1_7","&Settings");
        m.put("Strip1_8","&Exit       CTRL+Q");
        m.put("Strip1_9","&Help");
        m.put("Strip1_10","for the programmer, I hope so");
        m.put("Strip1_11","new file");
        m.put("Strip1_12","open");
        m.put("Strip1_13","save");
        m.put("Strip1_14","close");
        m.put("Strip1_15","print");
        m.put("Strip1_16","font");
        m.put("Strip1_17","change");
        m.put("Strip1_18","new/remove");
        m.put("Strip1_19","toolstrips");
        m.put("Strip2_1","font");
        m.put("Strip3_1","new element");
        m.put("Strip3_2","remove element");
        m.put("Strip4_1","cut CTRL+X");
        m.put("Strip4_2","copy CTRL+C");
        m.put("Strip4_3","paste CTRL+V");
        m.put("e1","settings");
        m.put("e2","show in Deskbar");
        m.put("e3","language");
        m.put("e4","ok");
        m.put("kontext1","copy");
        m.put("kontext2","cut");
        m.put("kontext3","paste");
        m.put("kontext4","delete");
        m.put("kontext5","search");
        m.put("kontext2_1","new under [ins]");
        m.put("kontext2_2","rename");
        m.put("kontext2_3","remove [del]");
        m.put("kontext2_4","save");
        m.put("kontext2_5","desk note");
        m.put("etwa_loeschen","Do you want to remove this node?");
        m.put("ja","Yes");
        m.put("nein","No");
        m.put("info1","notes tree");
        m.put("info2","place where Notes are shown");
        m.put("info3","Show/Hide");
        m.put("suche1","search");
        m.put("suche2","search");
        m.put("suche3","back");
        m.put("suche4","search all nodes?");
        m.put("suche5","results:");
        m.put("neu1","Do you want to save before?");
        m.put("neu2","begin again");
        m.put("eeoff1","begin again");
        m.put("eeoff2","Data will be lost.");
        m.put("eeoff3","Are you sure to begin again?");
        m.put("saveA","Do you want to save before?");
        m.put("OK","OK");
        m.put("abbrechen","cancel");
        m.put("kontext2_6","copy");
        m.put("kontext2_7","cut");
        m.put("kontext2_8","paste");
        m.put("font_regular","regular font");
        m.put("font_bold","bold font");
        m.put("font_italic","italic font");
        m.put("font_underline","underline");
        m.put("font_strikeout","strikeout");
        m.put("font_bigger","bigger font");
        m.put("font_smaller","smaller font");
        m.put("unity_note","together in one node");
        m.put("Strip1_20","info + help + feedback");
        m.put("Strip1_1_1","everything");
        m.put("Strip1_1_2","inside the marked node");
        m.put("fehler1","No file is loaded.");
        m.put("fehler2","Error when loading xml code:");
        m.put("strip1_21","set password");
        m.put("pass1","old password");
        m.put("pass2","new password");
        m.put("pass3","again");
        m.put("passerror1","Not more than 24 characters are allowed.");
        m.put("passerror2","The old password is wrong.");
        m.put("password","password");
        m.put("passwort_falsch","wrong password or incorrect file");
        m.put("passerror3","The last 3 entrys are not equal.");
        m.put("pw_unten_info","Empty textbox means no password.");
        m.put("kontext6","insert picture");
        m.put("kontext7","insert date");
        m.put("kontext8","background color");
        m.put("kontext9","minimize");
        m.put("kontext10","close");
        m.put("e5","show desknote borders");
        m.put("kontext2_9","Background Color");
        m.put("kontext2_10","Font Color");
        m.put("sicherungen","backup copies");
        m.put("autostart","autorun");
        m.put("color","color");
        m.put("passwort","password");
        m.put("pfaddatei","Path+File");
        m.put("alxerror","File has to end with .alx !");
        m.put("export","export");
        m.put("exportrtf","in rtf");
        m.put("exporttxt","in ansi txt");
        m.put("exporttxt2","in unicode txt");
        m.put("nexxt","next [Enter]");
        m.put("under","under [insert]");
        m.put("kontext11","new next [Enter]");
        m.put("suche6","whole words");
        m.put("suche7","case sensitive");
        m.put("aboutinfotext","• A desknote can be created by using the menu of the node and can be reached by the trayicon menu.\r\n\r\n• Nodes can be summed up as rtf file, witch can be read with Wordpad, Word or OpenOffice.\r\n\r\n• Create a Link with with a destination: \"C:\\Ort1\\notizen.exe\" -min \"C:\\Dokumente und Einstellungen\\Benutzer\\Eigene Dateien\\Notizen\\datei.alx\" , first notizen.exe and the alx file, the program will be opend by opening the File. Both Locations should be better into double quotes. If you put the link into the autostart folder you have your notes by starting the Computer.\r\n\r\n• The argument /min after notizen.exe, maybe in a line of a link, makes the programm running minimized at startup. (notizen.exe /min destination file)\" \r\n\r\n•If you run Notizen .Net with Administrator permissions once, the explorer will know Notizen .NET alx Files.");
        m.put("feedback","opinion / bug report / feature request");
        m.put("close","close");
        m.put("send","send");
        m.put("no_send","After 3 Feedbacks sending feedback is only possible tomorrow.");
        m.put("char10minimum","A minimal input of 10 characters is requiered.");
        m.put("no_feedback_sent","Feedback was not transmitted.");
        m.put("minautostart","minimized autorun");
        m.put("autosave","automatic saves every");
        m.put("seconds","seconds");
        m.put("scroll","scroll");
        return Collections.unmodifiableMap(m); }
    private static Map<String,String> loadChinese(){ LinkedHashMap<String,String> m=new LinkedHashMap<>();
        m.put("Strip1_1","文件");
        m.put("Strip1_2","新          CTRL+N");
        m.put("Strip1_3","打开        CTRL+O");
        m.put("Strip1_4","保存        CTRL+S");
        m.put("Strip1_5","另存为");
        m.put("Strip1_6","关闭");
        m.put("Strip1_7","安装");
        m.put("Strip1_8","结束        CTRL+Q");
        m.put("Strip1_9","帮助");
        m.put("Strip1_10","希望如此");
        m.put("Strip1_11","开始");
        m.put("Strip1_12","打开");
        m.put("Strip1_13","保存");
        m.put("Strip1_14","关闭");
        m.put("Strip1_15","打印");
        m.put("Strip1_16","字体");
        m.put("Strip1_17","修改");
        m.put("Strip1_18","新的/移动");
        m.put("Strip1_19","工具条");
        m.put("Strip2_1","字体");
        m.put("Strip3_1","新要素");
        m.put("Strip3_2","移动新要素");
        m.put("Strip4_1","剪切 CTRL++X");
        m.put("Strip4_2","复制 CTRL+C");
        m.put("Strip4_3","粘贴 CTRL+V");
        m.put("e1","安装");
        m.put("e2","桌面");
        m.put("e3","语言");
        m.put("e4","ok");
        m.put("kontext1","复制");
        m.put("kontext2","剪切");
        m.put("kontext3","粘贴");
        m.put("kontext4","删除");
        m.put("kontext5","搜索");
        m.put("kontext2_1","新的");
        m.put("kontext2_2","重命名");
        m.put("kontext2_3","移动");
        m.put("kontext2_4","保存");
        m.put("kontext2_5","新窗口");
        m.put("etwa_loeschen","是否移动此节点？");
        m.put("ja","是");
        m.put("nein","否");
        m.put("info1","词条正确");
        m.put("info2","显示节点所在位置");
        m.put("info3","显示/隐藏");
        m.put("suche1","搜索");
        m.put("suche2","搜索");
        m.put("suche3","完成");
        m.put("suche4","搜索所有词条？");
        m.put("suche5","结果");
        m.put("neu1","是否将前面保存？");
        m.put("neu2","重新开始");
        m.put("eeoff1","重新开始");
        m.put("eeoff2","消除日期");
        m.put("eeoff3","是否决定重新开始?");
        m.put("saveA","是否将前面保存？");
        m.put("OK","OK");
        m.put("abbrechen","中止");
        m.put("kontext2_6","复制");
        m.put("kontext2_7","剪切");
        m.put("kontext2_8","粘贴");
        m.put("font_regular","常规字体");
        m.put("font_bold","黑体");
        m.put("font_italic","斜体");
        m.put("font_underline","强调");
        m.put("font_strikeout","划去");
        m.put("font_bigger","大字体");
        m.put("font_smaller","小字体");
        m.put("unity_note","一起，在一个节点");
        m.put("Strip1_20","信息+帮助+反馈");
        m.put("Strip1_1_1","一切");
        m.put("Strip1_1_2","内的标记节点");
        m.put("fehler1","没有文件被加载。");
        m.put("fehler2","错误时加载XML代码:");
        m.put("strip1_21","设置密码");
        m.put("pass1","旧密码");
        m.put("pass2","新密码");
        m.put("pass3","再次");
        m.put("passerror1","不超过24个字符是允许的。");
        m.put("passerror2","旧密码是错误的。");
        m.put("password","密码");
        m.put("passwort_falsch","错误的密码或不正确的文件");
        m.put("passerror3","最后的3项不相等。");
        m.put("pw_unten_info","空文本就没有密码。");
        m.put("kontext6","插入图片");
        m.put("kontext7","插入日期");
        m.put("kontext8","背景颜色");
        m.put("kontext9","最小化");
        m.put("kontext10","关闭");
        m.put("e5","显示办公桌边界");
        m.put("kontext2_9","背景颜色");
        m.put("kontext2_10","字体颜色");
        m.put("sicherungen","备份");
        m.put("autostart","自动运行");
        m.put("color","颜色");
        m.put("passwort","密码");
        m.put("pfaddatei","路径+文件");
        m.put("alxerror","文件已经结束了。");
        m.put("export","出口");
        m.put("exportrtf","以RTF");
        m.put("exporttxt","在ANSI txt 的");
        m.put("exporttxt2","在Unicode txt的");
        m.put("nexxt","下一页[进入");
        m.put("under","在[插入]");
        m.put("kontext11","新的未来[进入] ");
        m.put("suche6","全字");
        m.put("suche7","区分大小写");
        m.put("aboutinfotext","• A desknote can be created by using the menu of the node and can be reached by the trayicon menu.\r\n\r\n• Nodes can be summed up as rtf file, witch can be read with Wordpad, Word or OpenOffice.\r\n\r\n• Create a Link with with a destination: \"C:\\Ort1\\notizen.exe\" -min \"C:\\Dokumente und Einstellungen\\Benutzer\\Eigene Dateien\\Notizen\\datei.alx\" , first notizen.exe and the alx file, the program will be opend by opening the File. Both Locations should be better into double quotes. If you put the link into the autostart folder you have your notes by starting the Computer.\r\n\r\n• The argument /min after notizen.exe, maybe in a line of a link, makes the programm running minimized at startup. (notizen.exe /min destination file)\" \r\n\r\n•If you run Notizen .Net with Administrator permissions once, the explorer will know Notizen .NET alx Files.");
        m.put("feedback","意见/错误报告/功能要求");
        m.put("close","关闭");
        m.put("send","发送");
        m.put("no_send","3反馈后，发送反馈是唯一可能的明天。");
        m.put("char10minimum","10个字符最小输入需求列表。");
        m.put("no_feedback_sent","反馈不传染。");
        m.put("minautostart","最小化自动运行");
        m.put("autosave","自动保存每个");
        m.put("seconds","秒");
        m.put("scroll","滚动");
        return Collections.unmodifiableMap(m); }
    private static Map<String,String> loadfrançais(){ LinkedHashMap<String,String> m=new LinkedHashMap<>();
        m.put("Strip1_1","Menu");
        m.put("Strip1_2","Nouveau fichier Ctrl + N");
        m.put("Strip1_3","Ouvrir Ctrl + O");
        m.put("Strip1_4","Enregistrer CTRL + S");
        m.put("Strip1_5","Enregistrer sous");
        m.put("Strip1_6","Fermer");
        m.put("Strip1_7","Paramètres");
        m.put("Strip1_8","Quitter Ctrl + Q");
        m.put("Strip1_9","Aide");
        m.put("Strip1_10","pour le programmeur, je l'espère» ");
        m.put("Strip1_11","nouveau fichier");
        m.put("Strip1_12","ouvert");
        m.put("Strip1_13","sauver");
        m.put("Strip1_14","close");
        m.put("Strip1_15","print");
        m.put("Strip1_16","font");
        m.put("Strip1_17","changement");
        m.put("Strip1_18","new / Supprimer");
        m.put("Strip1_19","toolstrips");
        m.put("Strip2_1","font");
        m.put("Strip3_1","nouvel élément");
        m.put("Strip3_2","Retirer l'élément");
        m.put("Strip4_1","Couper Ctrl + X");
        m.put("Strip4_2","Copier Ctrl + C");
        m.put("Strip4_3","Coller Ctrl + V");
        m.put("e1","Paramètres");
        m.put("e2","Afficher dans la barre de Bureau");
        m.put("e3","language");
        m.put("e4","ok");
        m.put("kontext1","Copier");
        m.put("kontext2","Couper");
        m.put("kontext3","coller");
        m.put("kontext4","supprimer");
        m.put("kontext5","Recherche");
        m.put("kontext2_1","Recherche");
        m.put("kontext2_2","Nouveau sous [Ins]");
        m.put("kontext2_3","Renommer");
        m.put("kontext2_4","supprimer [Suppr]");
        m.put("kontext2_5","save");
        m.put("etwa_loeschen","note desk");
        m.put("ja","Voulez-vous supprimer ce noeud?");
        m.put("nein","Oui");
        m.put("info1","Non");
        m.put("info2","Notes arbre");
        m.put("info3","lieu où les notes sont affichées");
        m.put("suche1","Afficher / Masquer");
        m.put("suche2","Recherche");
        m.put("suche3","Retour");
        m.put("suche4","rechercher tous les nœuds? ");
        m.put("suche5","Results:");
        m.put("neu1","Voulez-vous enregistrer avant?");
        m.put("neu2","recommencer");
        m.put("eeoff1","recommencer");
        m.put("eeoff2","Les données seront perdues.");
        m.put("eeoff3","Etes-vous sûr de vouloir recommencer?");
        m.put("saveA","Voulez-vous enregistrer avant?");
        m.put("OK","OK");
        m.put("abbrechen","Annuler");
        m.put("kontext2_6","Copier");
        m.put("kontext2_7","Couper");
        m.put("kontext2_8","coller");
        m.put("font_regular","font régulièrement");
        m.put("font_bold","font bold");
        m.put("font_italic","italique");
        m.put("font_underline","underline");
        m.put("font_strikeout","barrées");
        m.put("font_bigger","font plus grands");
        m.put("font_smaller","petits caractères");
        m.put("unity_note","ensemble dans un noeud");
        m.put("Strip1_20","info + aide + feedback» ");
        m.put("Strip1_1_1","tout");
        m.put("Strip1_1_2","à l'intérieur du nœud marqué");
        m.put("fehler1","Aucun fichier n'est chargé.");
        m.put("fehler2","Erreur lors du chargement de code XML:");
        m.put("strip1_21","Mot de code");
        m.put("pass1","ancien mot de passe");
        m.put("pass2","nouveau mot de passe");
        m.put("pass3","à nouveau");
        m.put("passerror1","caractères Pas plus de 24 ans sont autorisées.");
        m.put("passerror2","L'ancien mot de passe est erroné.");
        m.put("password","password");
        m.put("passwort_falsch","mauvais mot de passe ou un fichier incorrect");
        m.put("passerror3","Les 3 dernières Entrys ne sont pas égaux.");
        m.put("pw_unten_info","zone de texte vide signifie qu'aucun mot de passe.");
        m.put("kontext6","Insérer image");
        m.put("kontext7","insérer la date");
        m.put("kontext8","couleur de fond");
        m.put("kontext9","Minimize");
        m.put("kontext10","close");
        m.put("e5","Afficher les frontières desknote");
        m.put("kontext2_9","Couleur de fond");
        m.put("kontext2_10","Font Color");
        m.put("sicherungen","copies de sauvegarde");
        m.put("autostart","autorun");
        m.put("color","color");
        m.put("passwort","password");
        m.put("pfaddatei","Path + File");
        m.put("alxerror","Le fichier a pour terminer. alx! ");
        m.put("export","export");
        m.put("exportrtf","dans le rtf");
        m.put("exporttxt","dans la norme ANSI txt");
        m.put("exporttxt2","IN TXT unicode");
        m.put("nexxt","next [Entrée]");
        m.put("under","en vertu de [insérer]");
        m.put("kontext11","prochain nouveau sur [Entrée]");
        m.put("suche6","Mot entier");
        m.put("suche7","case sensitive");
        m.put("aboutinfotext","• A desknote can be created by using the menu of the node and can be reached by the trayicon menu.\r\n\r\n• Nodes can be summed up as rtf file, witch can be read with Wordpad, Word or OpenOffice.\r\n\r\n• Create a Link with with a destination: \"C:\\Ort1\\notizen.exe\" -min \"C:\\Dokumente und Einstellungen\\Benutzer\\Eigene Dateien\\Notizen\\datei.alx\" , first notizen.exe and the alx file, the program will be opend by opening the File. Both Locations should be better into double quotes. If you put the link into the autostart folder you have your notes by starting the Computer.\r\n\r\n• The argument /min after notizen.exe, maybe in a line of a link, makes the programm running minimized at startup. (notizen.exe /min destination file)\" \r\n\r\n•If you run Notizen .Net with Administrator permissions once, the explorer will know Notizen .NET alx Files.");
        m.put("feedback","opinion / rapport de bug / demande de fonctionnalité");
        m.put("close","close");
        m.put("send","Envoyer");
        m.put("no_send","Après 3 Feedbacks rétroaction d'envoi est seulement possible demain.");
        m.put("char10minimum","Un apport minimal de 10 caractères est requiered. ");
        m.put("no_feedback_sent","évaluation n'a pas été transmise");
        m.put("minautostart","minimisées démarrage automatique");
        m.put("autosave","Sauvegarde automatique");
        m.put("seconds","Seconds");
        m.put("scroll","défilement");
        return Collections.unmodifiableMap(m); }
    private static Map<String,String> loadspanish(){ LinkedHashMap<String,String> m=new LinkedHashMap<>();
        m.put("Strip1_1","Menú");
        m.put("Strip1_2","Nuevo archivo Ctrl + N");
        m.put("Strip1_3","Abrir Ctrl + O");
        m.put("Strip1_4","Guardar Ctrl + S");
        m.put("Strip1_5","guardar como");
        m.put("Strip1_6","Cerrar");
        m.put("Strip1_7","Configuración");
        m.put("Strip1_8","Salir Ctrl + Q");
        m.put("Strip1_9","Ayuda");
        m.put("Strip1_10","para el programador, así lo espero");
        m.put("Strip1_11","archivo");
        m.put("Strip1_12","abierta");
        m.put("Strip1_13","guardar");
        m.put("Strip1_14","cerca");
        m.put("Strip1_15","print");
        m.put("Strip1_16","font");
        m.put("Strip1_17","cambio");
        m.put("Strip1_18","nuevo / eliminar");
        m.put("Strip1_19","toolstrips");
        m.put("Strip2_1","font");
        m.put("Strip3_1","nuevo elemento");
        m.put("Strip3_2","eliminar elemento");
        m.put("Strip4_1","Cortar Ctrl + X");
        m.put("Strip4_2","Copiar Ctrl + C");
        m.put("Strip4_3","Pegar Ctrl + V");
        m.put("e1","Configuración");
        m.put("e2","Mostrar en la barra del escritorio");
        m.put("e3","idioma");
        m.put("e4","ok");
        m.put("kontext1","copia");
        m.put("kontext2","corte");
        m.put("kontext3","Pegar");
        m.put("kontext4","borrar");
        m.put("kontext5","Buscar");
        m.put("kontext2_1","Buscar");
        m.put("kontext2_2","nuevo bajo [INS]");
        m.put("kontext2_3","Renombrar");
        m.put("kontext2_4","eliminar [del]");
        m.put("kontext2_5","guardar");
        m.put("etwa_loeschen","nota de escritorio");
        m.put("ja","¿Desea eliminar este nodo?");
        m.put("nein","Sí");
        m.put("info1","No");
        m.put("info2","toma nota de árbol");
        m.put("info3","lugar donde se muestran Notas");
        m.put("suche1","Mostrar / Ocultar");
        m.put("suche2","Buscar");
        m.put("suche3","Volver");
        m.put("suche4","buscar todos los nodos?");
        m.put("suche5","resultados");
        m.put("neu1","¿Desea guardar antes?");
        m.put("neu2","empezar de nuevo");
        m.put("eeoff1","empezar de nuevo");
        m.put("eeoff2","Los datos se perderán.");
        m.put("eeoff3","¿Estás seguro de volver a empezar?");
        m.put("saveA","¿Desea guardar antes?");
        m.put("OK","OK");
        m.put("abbrechen","cancelar");
        m.put("kontext2_6","copia");
        m.put("kontext2_7","corte");
        m.put("kontext2_8","Pegar");
        m.put("font_regular","fuente regular");
        m.put("font_bold","negrita");
        m.put("font_italic","cursiva");
        m.put("font_underline","subrayado");
        m.put("font_strikeout","tachado");
        m.put("font_bigger","fuente mayor");
        m.put("font_smaller","fuente más pequeña");
        m.put("unity_note","juntos en un nodo");
        m.put("Strip1_20","info + ayuda + de votos");
        m.put("Strip1_1_1","todo");
        m.put("Strip1_1_2","dentro del nodo marcado");
        m.put("fehler1","No existe el fichero está cargado.");
        m.put("fehler2","Error al cargar el código XML:");
        m.put("strip1_21","Set Password");
        m.put("pass1","contraseña antigua");
        m.put("pass2","nueva contraseña");
        m.put("pass3","nuevo");
        m.put("passerror1","(personajes No más de 24 están permitidas.)");
        m.put("passerror2","La contraseña antigua es incorrecta.");
        m.put("password","contraseña");
        m.put("passwort_falsch","contraseña equivocada o incorrecta de archivo");
        m.put("passerror3","Los 3 últimos Juegos PSP no son iguales.");
        m.put("pw_unten_info","texto vacío significa que no hay contraseña.");
        m.put("kontext6","Insertar imagen");
        m.put("kontext7","insertar fecha");
        m.put("kontext8","color de fondo");
        m.put("kontext9","minimizar");
        m.put("kontext10","cerca");
        m.put("e5","Mostrar las fronteras Desknote");
        m.put("kontext2_9","Color de fondo");
        m.put("kontext2_10","Color de fuente");
        m.put("sicherungen","copias de seguridad");
        m.put("autostart","autorun");
        m.put("color","color");
        m.put("passwort","contraseña");
        m.put("pfaddatei","Ruta + Archivo");
        m.put("alxerror","El archivo tiene que terminar con .alx");
        m.put("export","exportación");
        m.put("exportrtf","en rtf");
        m.put("exporttxt","en ANSI txt");
        m.put("exporttxt2","en TXT Unicode");
        m.put("nexxt","Siguiente [Enter]");
        m.put("under","en [insertar]");
        m.put("kontext11","nuevo siguiente [Enter]");
        m.put("suche6","palabras completas");
        m.put("suche7","asunto sensible");
        m.put("aboutinfotext","• A desknote can be created by using the menu of the node and can be reached by the trayicon menu.\r\n\r\n• Nodes can be summed up as rtf file, witch can be read with Wordpad, Word or OpenOffice.\r\n\r\n• Create a Link with with a destination: \"C:\\Ort1\\notizen.exe\" -min \"C:\\Dokumente und Einstellungen\\Benutzer\\Eigene Dateien\\Notizen\\datei.alx\" , first notizen.exe and the alx file, the program will be opend by opening the File. Both Locations should be better into double quotes. If you put the link into the autostart folder you have your notes by starting the Computer.\r\n\r\n• The argument /min after notizen.exe, maybe in a line of a link, makes the programm running minimized at startup. (notizen.exe /min destination file)\" \r\n\r\n•If you run Notizen .Net with Administrator permissions once, the explorer will know Notizen .NET alx Files.");
        m.put("feedback","opinión / informe de fallo / feature request");
        m.put("close","cerca");
        m.put("send","Enviar");
        m.put("no_send","Después de 3 Opiniones envío de comentarios sólo es posible mañana.");
        m.put("char10minimum","Una aportación mínima de 10 caracteres es Requiere.");
        m.put("no_feedback_sent","votos no se transmite.");
        m.put("minautostart","reducir al mínimo el arranque automático");
        m.put("autosave","Automatic Save");
        m.put("seconds","Segundos");
        m.put("scroll","desplazamiento");
        return Collections.unmodifiableMap(m); }
    private static Map<String,String> loadrussian(){ LinkedHashMap<String,String> m=new LinkedHashMap<>();
        m.put("Strip1_1","Меню");
        m.put("Strip1_2","Новый файл Ctrl + N");
        m.put("Strip1_3","Открыть CTRL + O");
        m.put("Strip1_4","Сохранить CTRL + S");
        m.put("Strip1_5","Сохранить как");
        m.put("Strip1_6","Закрыть");
        m.put("Strip1_7","Настройки");
        m.put("Strip1_8","Выйти Ctrl + Q");
        m.put("Strip1_9","Помощь");
        m.put("Strip1_10","для программиста, я надеюсь на это");
        m.put("Strip1_11","новый файл");
        m.put("Strip1_12","открыть");
        m.put("Strip1_13","Сохранить");
        m.put("Strip1_14","Закрыть");
        m.put("Strip1_15","Печать");
        m.put("Strip1_16","Шрифт");
        m.put("Strip1_17","Смена");
        m.put("Strip1_18","новый / удалить");
        m.put("Strip1_19","toolstrips");
        m.put("Strip2_1","Шрифт");
        m.put("Strip3_1","новых элементов");
        m.put("Strip3_2","Удалить элемент");
        m.put("Strip4_1","Вырезать Ctrl + X");
        m.put("Strip4_2","Копировать Ctrl + C");
        m.put("Strip4_3","Вставить Ctrl + V");
        m.put("e1","Настройки");
        m.put("e2","сделали видимыми на панели задач");
        m.put("e3","язык");
        m.put("e4","OK");
        m.put("kontext1","Копировать");
        m.put("kontext2","Вырезать");
        m.put("kontext3","Вставить");
        m.put("kontext4","Удалить");
        m.put("kontext5","Поиск");
        m.put("kontext2_1","Поиск");
        m.put("kontext2_2","под новым [Ins]");
        m.put("kontext2_3","Переименовать");
        m.put("kontext2_4","Удалить [деле]");
        m.put("kontext2_5","Сохранить");
        m.put("etwa_loeschen","столе записки");
        m.put("ja","Вы хотите удалить этот узел?");
        m.put("nein","Да");
        m.put("info1","Нет");
        m.put("info2","отмечает, дерево");
        m.put("info3","место, где показаны Заметки");
        m.put("suche1","Показать / Скрыть");
        m.put("suche2","Поиск");
        m.put("suche3","Назад");
        m.put("suche4","Поиск Всех узлов");
        m.put("suche5","Результат:");
        m.put("neu1","Вы хотите сначала сохранить? ");
        m.put("neu2","начинать");
        m.put("eeoff1","начинать");
        m.put("eeoff2","Данные будут потеряны.");
        m.put("eeoff3","Вы уверены, что хотите начать заново?");
        m.put("saveA","Вы хотите сначала сохранить? ");
        m.put("OK","OK");
        m.put("abbrechen","Отмена");
        m.put("kontext2_6","Копировать");
        m.put("kontext2_7","Вырезать");
        m.put("kontext2_8","Вставить");
        m.put("font_regular","Обычный шрифт");
        m.put("font_bold","Жирный шрифт");
        m.put("font_italic","Курсив");
        m.put("font_underline","Подчеркнутый");
        m.put("font_strikeout","Зачеркнутый");
        m.put("font_bigger","Большой шрифт");
        m.put("font_smaller","Мелкий шрифт");
        m.put("unity_note","вместе в один узел");
        m.put("Strip1_20","Инфо + + Помощь Обратная связь");
        m.put("Strip1_1_1","все");
        m.put("Strip1_1_2","ниже отмечены узлы");
        m.put("fehler1","Не был загружен файл");
        m.put("fehler2","Ошибка при загрузке XML-код:");
        m.put("strip1_21","Установить пароль");
        m.put("pass1","Старый пароль");
        m.put("pass2","Новый пароль");
        m.put("pass3","еще раз");
        m.put("passerror1","Допускается Не более 24 символов.");
        m.put("passerror2","старый пароль неправильный.");
        m.put("password","пароль");
        m.put("passwort_falsch","неправильный пароль или неправильный файл");
        m.put("passerror3","последние 3 символа не одинаковы.");
        m.put("pw_unten_info","Пусто текстовое средствами без пароля.");
        m.put("kontext6","вставить картинку");
        m.put("kontext7","Дата");
        m.put("kontext8","Цвет фона");
        m.put("kontext9","Свернуть");
        m.put("kontext10","Закрыть");
        m.put("e5","Показать границы Desknote");
        m.put("kontext2_9","Цвет фона");
        m.put("kontext2_10","Цвет шрифта");
        m.put("sicherungen","резервные копии");
        m.put("autostart","автозапуск");
        m.put("color","Цвет");
        m.put("passwort","пароль");
        m.put("pfaddatei","путь + Файл ");
        m.put("alxerror","Файл должен заканчиваться на. ALX!");
        m.put("export","экспорт");
        m.put("exportrtf","в RTF");
        m.put("exporttxt","В ANSI TXT");
        m.put("exporttxt2","в Unicode TXT");
        m.put("nexxt","Следующий [Enter]");
        m.put("under","под [вставить]");
        m.put("kontext11","новый следующий [Enter]");
        m.put("suche6","Всего слов");
        m.put("suche7","случае чувствительной");
        m.put("aboutinfotext","• A desknote can be created by using the menu of the node and can be reached by the trayicon menu.\r\n\r\n• Nodes can be summed up as rtf file, witch can be read with Wordpad, Word or OpenOffice.\r\n\r\n• Create a Link with with a destination: \"C:\\Ort1\\notizen.exe\" -min \"C:\\Dokumente und Einstellungen\\Benutzer\\Eigene Dateien\\Notizen\\datei.alx\" , first notizen.exe and the alx file, the program will be opend by opening the File. Both Locations should be better into double quotes. If you put the link into the autostart folder you have your notes by starting the Computer.\r\n\r\n• The argument /min after notizen.exe, maybe in a line of a link, makes the programm running minimized at startup. (notizen.exe /min destination file)\" \r\n\r\n•If you run Notizen .Net with Administrator permissions once, the explorer will know Notizen .NET alx Files.");
        m.put("feedback","Мнение / сообщение об ошибке / Запрос функции");
        m.put("close","Закрыть");
        m.put("send","Отправить");
        m.put("no_send","После 3 Отзывы отправка отзыва возможна только завтра.");
        m.put("char10minimum","является минимальный ввод 10 символов.");
        m.put("no_feedback_sent","обратной связи не было передано.");
        m.put("minautostart","свести к минимуму автоматического запуска");
        m.put("autosave","автоматически сохранит каждый");
        m.put("seconds","секунда");
        m.put("scroll","прокрутки");
        return Collections.unmodifiableMap(m); }
    public static int keyCount(){return LEGACY_LANGUAGE_KEYS.length;}
    public static String keyForIndex(int index){return index>=0&&index<LEGACY_LANGUAGE_KEYS.length?LEGACY_LANGUAGE_KEYS[index]:null;}
    public static Integer indexForKey(String key){return LANGUAGE_INDEX.get(key);}
    public static Map<String,String> translations(String language){return TRANSLATIONS.get(resolveLanguage(language));}
    public static String[] languageValues(String language){ Map<String,String> t=translations(language),fb=TRANSLATIONS.get("Deutsch"); String[] out=new String[LEGACY_LANGUAGE_KEYS.length]; for(int i=0;i<LEGACY_LANGUAGE_KEYS.length;i++){String k=LEGACY_LANGUAGE_KEYS[i],v=t==null?null:t.get(k); if(v==null||v.isEmpty())v=fb==null?null:fb.get(k); out[i]=v==null?k:v;} return out;}
    public static String resolveLanguage(String choice){return resolveLanguage(choice,Locale.getDefault().toString());}
    public static String resolveLanguage(String choice,String localeName){ String raw=choice==null?"Auto":choice.trim(); String lower=raw.toLowerCase(Locale.ROOT); if(!raw.isEmpty()&&!lower.equals("auto")&&!lower.equals("automatic")&&!lower.equals("system")){String aliased=LANGUAGE_ALIASES.get(lower); if(aliased!=null)return aliased; return TRANSLATIONS.containsKey(raw)?raw:"English";} String loc=localeName==null?"":localeName.replace('-','_').toLowerCase(Locale.ROOT); if(loc.startsWith("de"))return "Deutsch"; if(loc.startsWith("fr"))return "français"; if(loc.startsWith("es"))return "spanish"; if(loc.startsWith("ru"))return "russian"; if(loc.startsWith("zh"))return "Chinese"; return "English";}
    public static String tr(String language,String key){return tr(language,key,null);}
    public static String tr(String language,String key,String defaultValue){ String v=lookup(TRANSLATIONS.get(resolveLanguage(language)),key); if(v==null)v=lookup(TRANSLATIONS.get("Deutsch"),key); if(v!=null)return v; return defaultValue==null?key:defaultValue;}
    public static List<String> availableLanguageLabels(){ ArrayList<String> out=new ArrayList<>(); for(String[] item:DISPLAY_LANGUAGES)out.add(item[0]); return Collections.unmodifiableList(out);}
    public static String[][] availableLanguages(){ String[][] copy=new String[DISPLAY_LANGUAGES.length][2]; for(int i=0;i<DISPLAY_LANGUAGES.length;i++){copy[i][0]=DISPLAY_LANGUAGES[i][0]; copy[i][1]=DISPLAY_LANGUAGES[i][1];} return copy;}
    private static String lookup(Map<String,String> table,String key){ if(table==null||key==null)return null; String d=table.get(key); if(d!=null&&!d.isEmpty())return d; String folded=key.toLowerCase(Locale.ROOT); for(Map.Entry<String,String> e:table.entrySet()) if(e.getKey().toLowerCase(Locale.ROOT).equals(folded)&&e.getValue()!=null&&!e.getValue().isEmpty())return e.getValue(); return null;}
}
