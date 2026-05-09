package de.notizen.android.core;

/** Embedded RTF picture group as a structured content part. */
public final class RtfImagePart extends RtfContentPart {
    public final String mimeType;
    public final int byteCount;

    public RtfImagePart(String text, String rtf, RtfTextStyle style, String mimeType, int byteCount) {
        super(text, rtf, style);
        this.mimeType = mimeType == null ? "" : mimeType;
        this.byteCount = Math.max(0, byteCount);
    }
}
