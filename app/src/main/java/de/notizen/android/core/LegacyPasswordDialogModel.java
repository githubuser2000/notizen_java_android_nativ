package de.notizen.android.core;

/**
 * Portable model of the two old WinForms password dialogs.
 *
 * passwort_dialog.vb used a 24 character DES password slot. The old code
 * padded short values with spaces, truncated the old password field before
 * comparing, disabled the old-password field for an empty legacy password,
 * and refused new passwords longer than 24 characters.
 */
public final class LegacyPasswordDialogModel {
    public static final int LEGACY_PASSWORD_LENGTH = 24;
    public static final String BLANK_PASSWORD_24 = "                        ";

    public enum ErrorKind { NONE, TOO_LONG, OLD_PASSWORD_WRONG, NEW_PASSWORDS_DIFFER }

    public static final class PromptSpec {
        public final String title;
        public final String oldPasswordLabel;
        public final String newPasswordLabel;
        public final String repeatPasswordLabel;
        public final String bottomInfo;
        public final boolean oldPasswordEnabled;
        public final int maxChars;

        public PromptSpec(String title, String oldPasswordLabel, String newPasswordLabel,
                          String repeatPasswordLabel, String bottomInfo,
                          boolean oldPasswordEnabled, int maxChars) {
            this.title = title == null ? "" : title;
            this.oldPasswordLabel = oldPasswordLabel == null ? "" : oldPasswordLabel;
            this.newPasswordLabel = newPasswordLabel == null ? "" : newPasswordLabel;
            this.repeatPasswordLabel = repeatPasswordLabel == null ? "" : repeatPasswordLabel;
            this.bottomInfo = bottomInfo == null ? "" : bottomInfo;
            this.oldPasswordEnabled = oldPasswordEnabled;
            this.maxChars = maxChars;
        }
    }

    public static final class PasswordChangeDecision {
        public final boolean accepted;
        public final ErrorKind error;
        public final String messageKey;
        public final String message;
        public final String newPassword;
        public final String newPasswordLegacy24;

        public PasswordChangeDecision(boolean accepted, ErrorKind error, String messageKey,
                                      String message, String newPassword) {
            this.accepted = accepted;
            this.error = error == null ? ErrorKind.NONE : error;
            this.messageKey = messageKey == null ? "" : messageKey;
            this.message = message == null ? "" : message;
            this.newPassword = newPassword == null ? "" : newPassword;
            this.newPasswordLegacy24 = toLegacy24(this.newPassword);
        }
    }

    private LegacyPasswordDialogModel() {}

    public static String toLegacy24(String value) {
        String s = value == null ? "" : value;
        if (s.length() > LEGACY_PASSWORD_LENGTH) return s.substring(0, LEGACY_PASSWORD_LENGTH);
        StringBuilder out = new StringBuilder(s);
        while (out.length() < LEGACY_PASSWORD_LENGTH) out.append(' ');
        return out.toString();
    }

    public static boolean isBlankLegacyPassword(String currentPassword) {
        String s = currentPassword == null ? "" : currentPassword;
        return s.isEmpty() || BLANK_PASSWORD_24.equals(toLegacy24(s));
    }

    public static boolean oldPasswordFieldEnabled(String currentPassword) {
        return !isBlankLegacyPassword(currentPassword);
    }

    public static PromptSpec changePrompt(String language, String currentPassword) {
        String title = LegacyI18n.tr(language, "password", "Passwort");
        return new PromptSpec(
                title,
                LegacyI18n.tr(language, "pass1", "altes Passwort"),
                LegacyI18n.tr(language, "pass2", "neues Passwort"),
                LegacyI18n.tr(language, "pass3", "wiederholen"),
                LegacyI18n.tr(language, "pw_unten_info", "Leeres Feld bedeutet kein Passwort."),
                oldPasswordFieldEnabled(currentPassword),
                LEGACY_PASSWORD_LENGTH
        );
    }

    public static PasswordChangeDecision validateChange(String currentPassword, String oldPasswordInput,
                                                        String newPassword1, String newPassword2,
                                                        String language) {
        String n1 = newPassword1 == null ? "" : newPassword1;
        String n2 = newPassword2 == null ? "" : newPassword2;
        if (n1.length() > LEGACY_PASSWORD_LENGTH || n2.length() > LEGACY_PASSWORD_LENGTH) {
            return rejected(ErrorKind.TOO_LONG, "passerror1", language);
        }
        String currentLegacy = toLegacy24(currentPassword);
        if (!isBlankLegacyPassword(currentPassword) && !toLegacy24(oldPasswordInput).equals(currentLegacy)) {
            return rejected(ErrorKind.OLD_PASSWORD_WRONG, "passerror2", language);
        }
        if (!n1.equals(n2)) {
            return rejected(ErrorKind.NEW_PASSWORDS_DIFFER, "passerror3", language);
        }
        return new PasswordChangeDecision(true, ErrorKind.NONE, "", "", n1);
    }

    public static String wrongPasswordMessage(String language, String errorMessage) {
        String base = LegacyI18n.tr(language, "passwort_falsch", "Passwort falsch oder Datei fehlerhaft");
        String err = errorMessage == null || errorMessage.isEmpty() ? "" : "\nError: " + errorMessage;
        return base + err;
    }

    private static PasswordChangeDecision rejected(ErrorKind kind, String key, String language) {
        return new PasswordChangeDecision(false, kind, key, LegacyI18n.tr(language, key, key), "");
    }
}
