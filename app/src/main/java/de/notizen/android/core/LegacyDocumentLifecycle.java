package de.notizen.android.core;

/**
 * Portable subset of the old WinForms document-state helpers: have_changed_file,
 * set_unchanged, wasimbaum and save_anyway. It deliberately contains no Android
 * classes so the behavior can be tested like the desktop code was.
 */
public final class LegacyDocumentLifecycle {
    public enum SaveDecision { SAVE, SAVE_AS, SAVE_FTP, EXPORT_ONLY, NOTHING }

    public static final class Status {
        public final boolean hasDocument;
        public final boolean hasRoot;
        public final boolean changed;
        public final boolean fileAssociated;
        public final boolean ftpAssociated;
        public final boolean canSave;
        public final boolean canSaveAs;
        public final boolean canFtpSave;
        public final boolean canExport;
        public final boolean canEditTree;
        public final boolean canEditContent;
        public final boolean passwordSet;
        public final String displayName;
        public final String titleSuffix;
        public final SaveDecision defaultSaveDecision;

        public Status(boolean hasDocument, boolean hasRoot, boolean changed, boolean fileAssociated,
                      boolean ftpAssociated, boolean canSave, boolean canSaveAs, boolean canFtpSave,
                      boolean canExport, boolean canEditTree, boolean canEditContent, boolean passwordSet,
                      String displayName, String titleSuffix, SaveDecision defaultSaveDecision) {
            this.hasDocument = hasDocument;
            this.hasRoot = hasRoot;
            this.changed = changed;
            this.fileAssociated = fileAssociated;
            this.ftpAssociated = ftpAssociated;
            this.canSave = canSave;
            this.canSaveAs = canSaveAs;
            this.canFtpSave = canFtpSave;
            this.canExport = canExport;
            this.canEditTree = canEditTree;
            this.canEditContent = canEditContent;
            this.passwordSet = passwordSet;
            this.displayName = displayName == null ? LegacyPaths.LEGACY_DEFAULT_FILENAME : displayName;
            this.titleSuffix = titleSuffix == null ? "" : titleSuffix;
            this.defaultSaveDecision = defaultSaveDecision == null ? SaveDecision.NOTHING : defaultSaveDecision;
        }

        public String windowTitle(String appName) {
            String prefix = appName == null || appName.trim().isEmpty() ? "Notizen" : appName.trim();
            return prefix + " - " + displayName + titleSuffix;
        }
    }

    private LegacyDocumentLifecycle() {}

    public static Status status(NoteDocument document, NoteNode currentNode, boolean localTarget, boolean ftpTarget) {
        boolean hasDoc = document != null;
        boolean hasRoot = hasDoc && document.root != null;
        boolean changed = hasDoc && document.changed;
        boolean passwordSet = hasDoc && document.password != null && !document.password.isEmpty();
        String name = hasDoc && document.displayName != null && !document.displayName.trim().isEmpty()
                ? document.displayName.trim() : LegacyPaths.LEGACY_DEFAULT_FILENAME;
        boolean associated = localTarget || ftpTarget;
        SaveDecision decision;
        if (!hasDoc) decision = SaveDecision.NOTHING;
        else if (ftpTarget) decision = SaveDecision.SAVE_FTP;
        else if (localTarget) decision = SaveDecision.SAVE;
        else decision = SaveDecision.SAVE_AS;
        return new Status(hasDoc, hasRoot, changed, localTarget, ftpTarget,
                hasRoot && (associated || changed), hasRoot, hasRoot, hasRoot,
                currentNode != null, currentNode != null, passwordSet, name,
                changed ? " *" : "", decision);
    }

    public static boolean haveChangedFile(NoteDocument document) {
        if (document != null) document.markChanged();
        return document != null && document.changed;
    }

    public static boolean setUnchanged(NoteDocument document) {
        if (document != null) document.markSaved();
        return document != null && !document.changed;
    }

    public static boolean wasInTree(NoteNode node) {
        return node != null && node.root() != null;
    }

    public static SaveDecision saveAnyway(NoteDocument document, boolean localTarget, boolean ftpTarget, boolean userRequestedExport) {
        if (userRequestedExport) return SaveDecision.EXPORT_ONLY;
        return status(document, document == null ? null : document.root, localTarget, ftpTarget).defaultSaveDecision;
    }
}
