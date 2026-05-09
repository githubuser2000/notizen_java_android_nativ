package de.notizen.android.core;

/** Portable representation of the old wanna_save.vb and wanna_restart.vb dialogs. */
public final class LegacyDialogModels {
    public enum DialogKind { WANNA_SAVE, WANNA_RESTART }
    public enum SavePromptChoice { SAVE, DISCARD, CANCEL }

    public static final class DialogSpec {
        public final DialogKind kind;
        public final String title;
        public final String message;
        public final String positive;
        public final String negative;
        public final String neutral;

        public DialogSpec(DialogKind kind, String title, String message, String positive, String negative, String neutral) {
            this.kind = kind;
            this.title = clean(title);
            this.message = clean(message);
            this.positive = clean(positive);
            this.negative = clean(negative);
            this.neutral = clean(neutral);
        }
    }

    private LegacyDialogModels() {}

    public static DialogSpec wannaSave(String language) {
        return new DialogSpec(
                DialogKind.WANNA_SAVE,
                stripAccelerators(LegacyI18n.tr(language, "Strip1_4", "Speichern")),
                LegacyI18n.tr(language, "saveA", "Möchten Sie vorher speichern?"),
                stripAccelerators(LegacyI18n.tr(language, "ja", "Ja")),
                stripAccelerators(LegacyI18n.tr(language, "abbrechen", "Abbrechen")),
                stripAccelerators(LegacyI18n.tr(language, "nein", "Nein"))
        );
    }

    public static DialogSpec wannaRestart(String language) {
        String first = LegacyI18n.tr(language, "eeoff2", "Vorhandene Daten werden gelöscht.");
        String second = LegacyI18n.tr(language, "eeoff3", "Sind Sie sicher?");
        return new DialogSpec(
                DialogKind.WANNA_RESTART,
                stripAccelerators(LegacyI18n.tr(language, "eeoff1", "von vorn beginnen")),
                first + "\n" + second,
                stripAccelerators(LegacyI18n.tr(language, "OK", "OK")),
                stripAccelerators(LegacyI18n.tr(language, "abbrechen", "Abbrechen")),
                ""
        );
    }

    public static boolean shouldShowWannaSave(boolean changed) { return changed; }

    public static SavePromptChoice choiceForWannaSaveButton(String which) {
        String s = clean(which).toLowerCase(java.util.Locale.ROOT);
        if ("positive".equals(s) || "yes".equals(s) || "ja".equals(s) || "save".equals(s)) return SavePromptChoice.SAVE;
        if ("neutral".equals(s) || "no".equals(s) || "nein".equals(s) || "discard".equals(s)) return SavePromptChoice.DISCARD;
        return SavePromptChoice.CANCEL;
    }

    public static String describeWannaSaveFlow(String language, SavePromptChoice choice) {
        DialogSpec spec = wannaSave(language);
        if (choice == SavePromptChoice.SAVE) return spec.title + ": " + spec.positive + " -> speichern";
        if (choice == SavePromptChoice.DISCARD) return spec.title + ": " + spec.neutral + " -> ohne Speichern fortfahren";
        return spec.title + ": " + spec.negative + " -> abbrechen";
    }

    private static String stripAccelerators(String s) {
        return clean(s).replace("&", "").replaceAll("\\s+", " ").trim();
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }
}
