package de.notizen.android.core;

/** Generic RTF field: visible result text plus the original field group. */
public final class RtfField extends RtfContentPart {
    public RtfField(String text, String rtf, RtfTextStyle style) {
        super(text, rtf, style);
    }
}
