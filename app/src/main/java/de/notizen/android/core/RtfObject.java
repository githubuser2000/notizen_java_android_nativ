package de.notizen.android.core;

/** OLE/object RTF group with its object class when available. */
public final class RtfObject extends RtfContentPart {
    public final String className;

    public RtfObject(String text, String rtf, RtfTextStyle style, String className) {
        super(text, rtf, style);
        this.className = className == null ? "" : className;
    }
}
