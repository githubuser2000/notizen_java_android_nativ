package de.notizen.android.core;

/**
 * Central safety limits for the Android port.  The desktop versions could rely
 * on a larger process and Windows RichTextBox behaviour; Android must avoid
 * very deep recursion, unbounded byte reads and full hashing of multi-MB RTF
 * picture payloads on UI-triggered paths.
 */
public final class LegacyCrashHardening {
    public static final int MAX_TREE_DEPTH = 512;
    public static final int MAX_TREE_NODES = 200_000;
    public static final int MAX_VALIDATION_FILE_BYTES = 96 * 1024 * 1024;
    public static final int LARGE_TEXT_HASH_THRESHOLD_CHARS = 2 * 1024 * 1024;
    public static final int HASH_SAMPLE_CHARS = 8192;

    private LegacyCrashHardening() {}

    public static void checkTreeDepth(int depth) {
        if (depth > MAX_TREE_DEPTH) {
            throw new IllegalStateException("Baum ist zu tief verschachtelt für stabiles Android-Laden/Speichern (Tiefe " + depth + ").");
        }
    }

    public static void checkTreeNodeCount(int count) {
        if (count > MAX_TREE_NODES) {
            throw new IllegalStateException("Baum enthält zu viele Knoten für stabiles Android-Laden/Speichern (" + count + ").");
        }
    }

    public static int saturatedAdd(int a, int b) {
        long sum = (long) a + (long) b;
        if (sum > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (sum < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) sum;
    }

    public static int utf8LengthEstimate(String text) {
        if (text == null || text.isEmpty()) return 0;
        long total = 0L;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch < 0x80) total += 1;
            else if (ch < 0x800) total += 2;
            else if (Character.isHighSurrogate(ch) && i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1))) {
                total += 4;
                i++;
            } else total += 3;
            if (total > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) total;
    }

    public static String digestInput(String text) {
        String value = text == null ? "" : text;
        if (value.length() <= LARGE_TEXT_HASH_THRESHOLD_CHARS) return value;
        int sample = Math.max(256, HASH_SAMPLE_CHARS);
        int len = value.length();
        int headEnd = Math.min(sample, len);
        int midStart = Math.max(0, len / 2 - sample / 2);
        int midEnd = Math.min(len, midStart + sample);
        int tailStart = Math.max(0, len - sample);
        return "len=" + len + "\n--head--\n" + value.substring(0, headEnd)
                + "\n--middle@" + midStart + "--\n" + value.substring(midStart, midEnd)
                + "\n--tail@" + tailStart + "--\n" + value.substring(tailStart);
    }
}
