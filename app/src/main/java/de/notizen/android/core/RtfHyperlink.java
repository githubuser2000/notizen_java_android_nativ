package de.notizen.android.core;

/** HYPERLINK field with URL and visible result text. */
public final class RtfHyperlink extends RtfContentPart {
    public final String url;

    public RtfHyperlink(String text, String rtf, RtfTextStyle style, String url) {
        super(text, rtf, style);
        this.url = url == null ? "" : url;
    }
}
