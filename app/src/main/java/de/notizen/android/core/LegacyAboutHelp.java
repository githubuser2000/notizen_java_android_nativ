package de.notizen.android.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** About/help/feedback text bridge from AboutBox1.vb and info_help_and_feedback.vb. */
public final class LegacyAboutHelp {
    public static final String LEGACY_WEB_URL = "http://www.nik-west.de/notizen";

    public static final class AboutSpec {
        public final String title;
        public final String productName;
        public final String version;
        public final String description;
        public final List<String> feedbackLabels;
        public final String feedbackLabel;
        public final String closeLabel;
        public final String webUrl;

        public AboutSpec(String title, String productName, String version, String description,
                         List<String> feedbackLabels, String closeLabel, String webUrl) {
            this.title = title == null ? "" : title;
            this.productName = productName == null ? "" : productName;
            this.version = version == null ? "" : version;
            this.description = description == null ? "" : description;
            this.feedbackLabels = Collections.unmodifiableList(feedbackLabels);
            this.feedbackLabel = feedbackLabels.isEmpty() ? "" : feedbackLabels.get(0) + " / " + feedbackLabels.get(1) + " / " + feedbackLabels.get(2);
            this.closeLabel = closeLabel == null ? "" : closeLabel;
            this.webUrl = webUrl == null ? "" : webUrl;
        }
    }

    private LegacyAboutHelp() {}

    public static AboutSpec spec(String language, String versionName) {
        boolean de = LegacyI18n.resolveLanguage(language).equals("Deutsch");
        String version = versionName == null || versionName.isEmpty() ? "Android-Port" : versionName;
        String desc = de
                ? "Notizen ist ein Programm für geordnete Notizen und Haftnotiz-Metadaten. Diese Version ist eine native Java-Android-Portierung der alten Notizen.NET-/PyQt-Logik."
                : "Notizen is a program for ordered notes and desknote metadata. This version is a native Java Android port of the old Notizen.NET/PyQt logic.";
        List<String> labels = de
                ? Arrays.asList("persönliche Meinung", "Fehlermeldung", "Feature-Vorschlag")
                : Arrays.asList("opinion", "bug report", "feature request");
        String close = de ? "schließen" : "close";
        return new AboutSpec(de ? "Info über Notizen" : "About Notizen", "Notizen", version, desc, labels, close, LEGACY_WEB_URL);
    }

    public static String androidPortAppendix(String versionName) {
        return "Android-Port " + (versionName == null ? "" : versionName) + "\n" +
                "ALX-, RTF-, FTP-, Such-, Feedback- und Einstellungslogik wurden als Java-Kernmodelle portiert. " +
                "Windows-Tray und frei schwebende Desktop-Haftnotizfenster bleiben auf Android bewusst Datenmodelle.";
    }

    public static String feedbackTooShortMessage(String language) {
        return LegacyI18n.resolveLanguage(language).equals("Deutsch")
                ? "Geben Sie mindestens 10 Zeichen ein!"
                : "A minimal input of 10 characters is required.";
    }

    public static String feedbackStoredMessage(String language) {
        return LegacyI18n.resolveLanguage(language).equals("Deutsch")
                ? "Feedback lokal gespeichert."
                : "Feedback stored locally.";
    }
}
