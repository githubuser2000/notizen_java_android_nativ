package de.notizen.android.core;

/** Net/PyQt-like main layout model for Android diagnostics and regression tests. */
public final class LegacyMainLayoutModel {
    public static final int ANDROID_ORIENTATION_PORTRAIT = 1;
    public static final int ANDROID_ORIENTATION_LANDSCAPE = 2;

    public static final class Layout {
        public final boolean wide;
        public final int screenWidthDp;
        public final int screenHeightDp;
        public final int treeWeight;
        public final int editorWeight;
        public final boolean treeHeaderHidden;
        public final boolean titleLabelsHidden;
        public final boolean toolbarIconOnly;
        public final String orientation;

        public Layout(boolean wide, int screenWidthDp, int screenHeightDp, int treeWeight, int editorWeight,
                      boolean treeHeaderHidden, boolean titleLabelsHidden, boolean toolbarIconOnly, String orientation) {
            this.wide = wide;
            this.screenWidthDp = Math.max(0, screenWidthDp);
            this.screenHeightDp = Math.max(0, screenHeightDp);
            this.treeWeight = treeWeight;
            this.editorWeight = editorWeight;
            this.treeHeaderHidden = treeHeaderHidden;
            this.titleLabelsHidden = titleLabelsHidden;
            this.toolbarIconOnly = toolbarIconOnly;
            this.orientation = orientation == null ? "" : orientation;
        }

        public String summary() {
            return "Ausrichtung: " + orientation
                    + "\nBreit: " + (wide ? "ja" : "nein")
                    + "\nScreen dp: " + screenWidthDp + "x" + screenHeightDp
                    + "\nBaum/Editor-Gewicht: " + treeWeight + ":" + editorWeight
                    + "\nTreeView-Header verborgen: " + (treeHeaderHidden ? "ja" : "nein")
                    + "\nZusatzlabels verborgen: " + (titleLabelsHidden ? "ja" : "nein")
                    + "\nToolbar icon-only: " + (toolbarIconOnly ? "ja" : "nein");
        }
    }

    private LegacyMainLayoutModel() {}

    public static Layout compute(int widthDp, int heightDp, int androidOrientation) {
        boolean landscape = androidOrientation == ANDROID_ORIENTATION_LANDSCAPE || widthDp > heightDp;
        boolean wide = widthDp >= 700 || landscape;
        return new Layout(wide, widthDp, heightDp,
                wide ? 1 : 0,
                wide ? 2 : 1,
                true,
                true,
                true,
                wide ? "horizontal" : "vertical");
    }
}
