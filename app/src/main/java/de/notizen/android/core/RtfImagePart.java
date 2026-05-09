package de.notizen.android.core;

/** Embedded RTF picture group as a structured content part. */
public final class RtfImagePart extends RtfContentPart {
    public final String mimeType;
    public final int byteCount;
    public final byte[] imageData;
    public final int widthTwips;
    public final int heightTwips;

    public RtfImagePart(String text, String rtf, RtfTextStyle style, String mimeType, int byteCount) {
        super(text, rtf, style);
        this.mimeType = mimeType == null ? "" : mimeType;
        this.byteCount = Math.max(0, byteCount);
        this.imageData = new byte[0];
        this.widthTwips = 0;
        this.heightTwips = 0;
    }

    public RtfImagePart(String text, String rtf, RtfTextStyle style, String mimeType, byte[] imageData, int widthTwips, int heightTwips) {
        super(text, rtf, style);
        this.mimeType = mimeType == null ? "" : mimeType;
        this.imageData = imageData == null ? new byte[0] : imageData;
        this.byteCount = this.imageData.length;
        this.widthTwips = Math.max(0, widthTwips);
        this.heightTwips = Math.max(0, heightTwips);
    }
}
