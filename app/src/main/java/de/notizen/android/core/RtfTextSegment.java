package de.notizen.android.core;

/** One contiguous visible RTF text run with the style active for that run. */
public final class RtfTextSegment {
    public final String text;
    public final RtfTextStyle style;

    public RtfTextSegment(String text, RtfTextStyle style) {
        this.text = text == null ? "" : text;
        this.style = style == null ? new RtfTextStyle() : style;
    }
}
