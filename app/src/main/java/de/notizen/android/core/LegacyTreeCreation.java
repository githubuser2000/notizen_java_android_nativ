package de.notizen.android.core;

/** WinForms TreeView creation rules for root, child and "new next" nodes. */
public final class LegacyTreeCreation {
    public enum Kind { ROOT, CHILD, NEXT }

    public static final class CreationResult {
        public final NoteNode node;
        public final NoteNode parent;
        public final int index;
        public final Kind kind;
        public final String status;
        public CreationResult(NoteNode node, NoteNode parent, int index, Kind kind, String status) {
            this.node = node;
            this.parent = parent;
            this.index = index;
            this.kind = kind;
            this.status = status;
        }
    }

    private LegacyTreeCreation() {}

    public static String normalizeTitle(String title) {
        return title == null || title.trim().isEmpty() ? "..." : title.trim();
    }

    public static CreationResult ensureRoot(NoteDocument document, String title) {
        if (document == null) throw new IllegalArgumentException("document == null");
        if (document.root == null) {
            document.root = new NoteNode(normalizeTitle(title), "");
            document.markChanged();
            return new CreationResult(document.root, null, 0, Kind.ROOT, "Root erzeugt");
        }
        return new CreationResult(document.root, null, 0, Kind.ROOT, "Root vorhanden");
    }

    public static CreationResult newChild(NoteDocument document, NoteNode current, String title) {
        if (document == null) throw new IllegalArgumentException("document == null");
        if (current == null) current = ensureRoot(document, "start").node;
        current.expanded = true;
        NoteNode child = current.addChild(new NoteNode(normalizeTitle(title), ""));
        document.markChanged();
        return new CreationResult(child, current, current.children.size() - 1, Kind.CHILD, "Neuer Unterknoten");
    }

    public static CreationResult newNext(NoteDocument document, NoteNode current, String title) {
        if (document == null) throw new IllegalArgumentException("document == null");
        if (current == null || current.parent == null) return newChild(document, ensureRoot(document, "start").node, title);
        NoteNode parent = current.parent;
        int index = current.indexInParent() + 1;
        NoteNode next = parent.insertChild(index, new NoteNode(normalizeTitle(title), ""));
        document.markChanged();
        return new CreationResult(next, parent, index, Kind.NEXT, "Neuer Knoten daneben");
    }
}
