package de.notizen.android.core;

/** Delete planning from Baum.vb: close subtree desktop notes, remove node, select previous visible node. */
public final class LegacyTreeDelete {
    public static final class DeletePlan {
        public final boolean deletesRoot;
        public final NoteNode fallback;
        public final int affectedNodes;
        public final int desktopNotesToClose;

        public DeletePlan(boolean deletesRoot, NoteNode fallback, int affectedNodes, int desktopNotesToClose) {
            this.deletesRoot = deletesRoot;
            this.fallback = fallback;
            this.affectedNodes = affectedNodes;
            this.desktopNotesToClose = desktopNotesToClose;
        }
    }

    private LegacyTreeDelete() {}

    public static DeletePlan plan(NoteNode selected) {
        if (selected == null) return new DeletePlan(false, null, 0, 0);
        boolean root = selected.parent == null;
        return new DeletePlan(root,
                root ? null : NoteTreeOps.legacyDeleteFallbackNode(selected),
                selected.walk().size(),
                LegacyDesktopNoteTreeOps.countDesktopNotes(selected));
    }

    public static NoteNode delete(NoteNode selected) {
        DeletePlan p = plan(selected);
        if (selected == null || p.deletesRoot) return null;
        LegacyDesktopNoteTreeOps.clearDesktopNotes(selected);
        selected.removeFromParent();
        return p.fallback;
    }

    public static String confirmationText(DeletePlan p) {
        if (p == null) return "";
        if (p.deletesRoot) return "Die Wurzel wird nicht entfernt; die Datei wird neu gestartet.";
        StringBuilder b = new StringBuilder("Diesen Knoten inklusive Unterknoten löschen?");
        if (p.affectedNodes > 1) b.append("\n").append(p.affectedNodes).append(" Knoten betroffen.");
        if (p.desktopNotesToClose > 0) b.append("\n").append(p.desktopNotesToClose).append(" Haftnotiz(en) werden geschlossen.");
        return b.toString();
    }
}
