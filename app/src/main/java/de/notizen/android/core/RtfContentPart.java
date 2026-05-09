package de.notizen.android.core;

/** Base class for parsed RTF content parts that need more than plain text. */
public class RtfContentPart {
    public final String text;
    public final String rtf;
    public final RtfTextStyle style;

    public RtfContentPart(String text, String rtf, RtfTextStyle style) {
        this.text = text == null ? "" : text;
        this.rtf = rtf == null ? "" : rtf;
        this.style = style == null ? new RtfTextStyle() : style;
    }
}
