package de.notizen.android.core;

import java.util.Objects;

/** Snapshot of the RichTextBox/QTextEdit style state while parsing RTF. */
public final class RtfTextStyle {
    public boolean bold;
    public boolean italic;
    public boolean underline;
    public boolean strike;
    public boolean hidden;
    public boolean allCaps;
    public boolean smallCaps;
    public String fgColor;
    public String bgColor;
    public String fontFamily;
    public Integer fontSizeHalfPoints;
    public String align;
    public int leftIndentTwips;
    public int rightIndentTwips;
    public int firstIndentTwips;
    public int spaceBeforeTwips;
    public int spaceAfterTwips;
    public int lineSpacingTwips;
    public boolean lineSpacingMultiple;
    public String vertical;
    public String direction;
    public int letterSpacingTwips;

    public RtfTextStyle copy() {
        RtfTextStyle s = new RtfTextStyle();
        s.bold = bold;
        s.italic = italic;
        s.underline = underline;
        s.strike = strike;
        s.hidden = hidden;
        s.allCaps = allCaps;
        s.smallCaps = smallCaps;
        s.fgColor = fgColor;
        s.bgColor = bgColor;
        s.fontFamily = fontFamily;
        s.fontSizeHalfPoints = fontSizeHalfPoints;
        s.align = align;
        s.leftIndentTwips = leftIndentTwips;
        s.rightIndentTwips = rightIndentTwips;
        s.firstIndentTwips = firstIndentTwips;
        s.spaceBeforeTwips = spaceBeforeTwips;
        s.spaceAfterTwips = spaceAfterTwips;
        s.lineSpacingTwips = lineSpacingTwips;
        s.lineSpacingMultiple = lineSpacingMultiple;
        s.vertical = vertical;
        s.direction = direction;
        s.letterSpacingTwips = letterSpacingTwips;
        return s;
    }

    public void resetCharacterStyle() {
        bold = false;
        italic = false;
        underline = false;
        strike = false;
        hidden = false;
        allCaps = false;
        smallCaps = false;
        fgColor = null;
        bgColor = null;
        fontFamily = null;
        fontSizeHalfPoints = null;
        vertical = null;
        direction = null;
        letterSpacingTwips = 0;
    }

    public void resetParagraphStyle() {
        align = null;
        leftIndentTwips = 0;
        rightIndentTwips = 0;
        firstIndentTwips = 0;
        spaceBeforeTwips = 0;
        spaceAfterTwips = 0;
        lineSpacingTwips = 0;
        lineSpacingMultiple = false;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RtfTextStyle)) return false;
        RtfTextStyle s = (RtfTextStyle) other;
        return bold == s.bold && italic == s.italic && underline == s.underline && strike == s.strike
                && hidden == s.hidden && allCaps == s.allCaps && smallCaps == s.smallCaps
                && leftIndentTwips == s.leftIndentTwips && rightIndentTwips == s.rightIndentTwips
                && firstIndentTwips == s.firstIndentTwips && spaceBeforeTwips == s.spaceBeforeTwips
                && spaceAfterTwips == s.spaceAfterTwips && lineSpacingTwips == s.lineSpacingTwips
                && lineSpacingMultiple == s.lineSpacingMultiple && letterSpacingTwips == s.letterSpacingTwips
                && Objects.equals(fgColor, s.fgColor) && Objects.equals(bgColor, s.bgColor)
                && Objects.equals(fontFamily, s.fontFamily) && Objects.equals(fontSizeHalfPoints, s.fontSizeHalfPoints)
                && Objects.equals(align, s.align) && Objects.equals(vertical, s.vertical) && Objects.equals(direction, s.direction);
    }

    @Override public int hashCode() {
        return Objects.hash(bold, italic, underline, strike, hidden, allCaps, smallCaps, fgColor, bgColor,
                fontFamily, fontSizeHalfPoints, align, leftIndentTwips, rightIndentTwips, firstIndentTwips,
                spaceBeforeTwips, spaceAfterTwips, lineSpacingTwips, lineSpacingMultiple, vertical, direction, letterSpacingTwips);
    }
}
