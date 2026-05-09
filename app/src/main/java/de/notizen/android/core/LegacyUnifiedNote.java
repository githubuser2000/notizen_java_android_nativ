package de.notizen.android.core;

/** Legacy Einheit/fasse_zusammen helper for current subtree or whole tree. */
public final class LegacyUnifiedNote {
    public enum Scope { CURRENT_SUBTREE, ROOT }

    public static final class UnifiedResult {
        public final NoteNode parent;
        public final NoteNode created;
        public final Scope scope;
        public final int sourceNodes;

        public UnifiedResult(NoteNode parent, NoteNode created, Scope scope, int sourceNodes) {
            this.parent = parent;
            this.created = created;
            this.scope = scope == null ? Scope.ROOT : scope;
            this.sourceNodes = sourceNodes;
        }
    }

    private LegacyUnifiedNote() {}

    public static NoteNode create(NoteNode selected, NoteDocument document, Scope scope) {
        NoteNode source = sourceNode(selected, document, scope);
        String title = scope == Scope.CURRENT_SUBTREE ? "Teilbaum" : "Gesamt";
        return Exporters.unifiedNote(source, title);
    }

    public static UnifiedResult attach(NoteDocument document, NoteNode selected, Scope scope) {
        if (document == null) return new UnifiedResult(null, null, scope, 0);
        NoteNode source = sourceNode(selected, document, scope);
        int sourceNodes = source == null ? 0 : source.walk().size();
        NoteNode parent = scope == Scope.CURRENT_SUBTREE && selected != null ? selected : document.ensureRoot();
        NoteNode created = create(selected, document, scope);
        parent.addChild(created);
        parent.expanded = true;
        document.markChanged();
        return new UnifiedResult(parent, created, scope, sourceNodes);
    }

    private static NoteNode sourceNode(NoteNode selected, NoteDocument document, Scope scope) {
        if (scope == Scope.CURRENT_SUBTREE && selected != null) return selected;
        return document == null ? selected : document.ensureRoot();
    }
}
