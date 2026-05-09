package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Portable model of Notizen_Activated: when the main window receives focus,
 * visible legacy modal/tool dialogs are re-activated in a stable order and the
 * custom mouse-move logic is blocked while any of them is visible.
 */
public final class LegacyActivationDialogFocus {
    public enum Dialog {
        FTP("ftpkram", "FTP"),
        PASSWORD("passwort_dialog", "Passwort"),
        PASSWORD_CONFIRM("passwort_dialog2", "Passwort bestätigen"),
        SETTINGS("einstellungen", "Einstellungen"),
        SEARCH("suche", "Suchen"),
        ALARM("wecker", "Wecker"),
        FEEDBACK("info_help_and_feedback", "Feedback"),
        ABOUT("aboutbox1", "Info");

        public final String legacyName;
        public final String displayName;
        Dialog(String legacyName, String displayName) {
            this.legacyName = legacyName;
            this.displayName = displayName;
        }
    }

    public static final class DialogState {
        public final Set<Dialog> visibleDialogs;
        public DialogState(Set<Dialog> visibleDialogs) {
            EnumSet<Dialog> s = EnumSet.noneOf(Dialog.class);
            if (visibleDialogs != null) s.addAll(visibleDialogs);
            this.visibleDialogs = Collections.unmodifiableSet(s);
        }
        public boolean isVisible(Dialog d) { return visibleDialogs.contains(d); }
    }

    public static final class Plan {
        public final boolean blockMainWindowMove;
        public final List<Dialog> activationOrder;
        public final Dialog finalActiveDialog;
        public final String reason;
        public Plan(boolean blockMainWindowMove, List<Dialog> activationOrder, Dialog finalActiveDialog, String reason) {
            this.blockMainWindowMove = blockMainWindowMove;
            this.activationOrder = Collections.unmodifiableList(new ArrayList<>(activationOrder == null ? Collections.<Dialog>emptyList() : activationOrder));
            this.finalActiveDialog = finalActiveDialog;
            this.reason = reason == null ? "" : reason;
        }
        public String summary() {
            StringBuilder b = new StringBuilder();
            b.append(blockMainWindowMove ? "Dialog-Fokus aktiv" : "kein Dialog aktiv");
            if (!activationOrder.isEmpty()) {
                b.append("\nReaktivierung:");
                for (Dialog d : activationOrder) b.append("\n- ").append(d.displayName).append(" (").append(d.legacyName).append(")");
                b.append("\nFinal aktiv: ").append(finalActiveDialog == null ? "-" : finalActiveDialog.displayName);
            }
            if (!reason.isEmpty()) b.append("\n").append(reason);
            return b.toString();
        }
    }

    private static final Dialog[] ACTIVATION_ORDER = new Dialog[] {
            Dialog.FTP,
            Dialog.PASSWORD_CONFIRM,
            Dialog.PASSWORD,
            Dialog.SETTINGS,
            Dialog.ALARM,
            Dialog.FEEDBACK,
            Dialog.ABOUT,
            Dialog.SEARCH
    };

    private LegacyActivationDialogFocus() {}

    public static DialogState stateFromVisibleNames(String... names) {
        LinkedHashSet<Dialog> out = new LinkedHashSet<>();
        if (names != null) {
            for (String n : names) {
                Dialog d = parse(n);
                if (d != null) out.add(d);
            }
        }
        return new DialogState(out);
    }

    public static Plan onMainActivated(DialogState state) {
        ArrayList<Dialog> order = new ArrayList<>();
        if (state != null) {
            for (Dialog d : ACTIVATION_ORDER) if (state.isVisible(d)) order.add(d);
        }
        Dialog finalDialog = order.isEmpty() ? null : order.get(order.size() - 1);
        return new Plan(!order.isEmpty(), order, finalDialog, order.isEmpty() ? "mouseclickmovetogether_block=false" : "mouseclickmovetogether_block=true");
    }

    public static Dialog parse(String legacyName) {
        if (legacyName == null) return null;
        String key = legacyName.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        for (Dialog d : Dialog.values()) {
            if (d.legacyName.equals(key) || d.name().toLowerCase(Locale.ROOT).equals(key)) return d;
        }
        if (key.contains("ftp")) return Dialog.FTP;
        if (key.contains("passwort2") || key.contains("password2") || key.contains("confirm")) return Dialog.PASSWORD_CONFIRM;
        if (key.contains("passwort") || key.contains("password")) return Dialog.PASSWORD;
        if (key.contains("such") || key.contains("search")) return Dialog.SEARCH;
        if (key.contains("wecker") || key.contains("alarm")) return Dialog.ALARM;
        if (key.contains("feedback")) return Dialog.FEEDBACK;
        if (key.contains("about") || key.contains("info")) return Dialog.ABOUT;
        if (key.contains("setting") || key.contains("einstellung")) return Dialog.SETTINGS;
        return null;
    }
}
