package de.notizen.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.notizen.android.core.LegacyRichTextToolbar;
import de.notizen.android.core.NoteNode;

public final class TreeListAdapter extends BaseAdapter {
    public static final class FlatNode {
        public final NoteNode node;
        public final int depth;
        public FlatNode(NoteNode node, int depth) { this.node = node; this.depth = depth; }
    }

    private static final String NODE_TITLE_STYLE_ATTR = "androidTitleStyle";
    private static final String NODE_TITLE_FONT_ATTR = "androidTitleFont";
    private static final String NODE_TITLE_SIZE_ATTR = "androidTitleSizeSp";

    private final Context context;
    private final ArrayList<FlatNode> rows = new ArrayList<>();
    private NoteNode selected;

    public TreeListAdapter(Context context) { this.context = context; }

    public void setRows(List<FlatNode> newRows) {
        rows.clear();
        if (newRows != null) rows.addAll(newRows);
        notifyDataSetChanged();
    }

    public List<FlatNode> getRows() { return rows; }

    public void setSelected(NoteNode selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }

    @Override public int getCount() { return rows.size(); }
    @Override public FlatNode getItem(int position) { return rows.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = convertView instanceof TextView ? (TextView) convertView : new TextView(context);
        FlatNode row = rows.get(position);
        NoteNode node = row.node;
        boolean hasChildren = !node.children.isEmpty();
        String marker = hasChildren ? (node.expanded ? "▾ " : "▸ ") : "  ";
        view.setText(marker + (node.title == null || node.title.isEmpty() ? "..." : node.title));
        view.setTextSize(treeTextSize(node));
        view.setGravity(android.view.Gravity.CENTER_VERTICAL);
        view.setSingleLine(false);
        view.setMinHeight(dp(44));
        view.setTypeface(treeTypeface(node));
        applyPaintFlags(view, node);
        view.setPadding(dp(12 + row.depth * 22), dp(7), dp(10), dp(7));
        if (node == selected) {
            view.setBackground(rowBackground(Color.rgb(218, 233, 255), Color.rgb(130, 170, 230)));
        } else if (node.bgArgb != 0) {
            view.setBackground(rowBackground(0xff000000 | (node.bgArgb & 0x00ffffff), Color.rgb(225, 228, 234)));
        } else if ((position & 1) == 0) {
            view.setBackground(rowBackground(Color.rgb(250, 251, 253), Color.TRANSPARENT));
        } else {
            view.setBackground(rowBackground(Color.WHITE, Color.TRANSPARENT));
        }
        if (node.fgArgb != 0) view.setTextColor(0xff000000 | (node.fgArgb & 0x00ffffff));
        else view.setTextColor(node == selected ? Color.rgb(20, 38, 64) : Color.rgb(32, 38, 46));
        return view;
    }

    private Typeface treeTypeface(NoteNode node) {
        int style = node != null && node.parent == null ? Typeface.BOLD : Typeface.NORMAL;
        String rawStyle = node == null ? "" : String.valueOf(node.extraAttrs.get(NODE_TITLE_STYLE_ATTR)).toLowerCase(Locale.ROOT);
        if (rawStyle.contains("bold")) style |= Typeface.BOLD;
        if (rawStyle.contains("italic")) style |= Typeface.ITALIC;
        String family = node == null ? "" : node.extraAttrs.get(NODE_TITLE_FONT_ATTR);
        return Typeface.create(androidTypefaceFamily(family), style);
    }

    private void applyPaintFlags(TextView view, NoteNode node) {
        int flags = view.getPaintFlags();
        flags &= ~Paint.UNDERLINE_TEXT_FLAG;
        flags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
        String rawStyle = node == null ? "" : String.valueOf(node.extraAttrs.get(NODE_TITLE_STYLE_ATTR)).toLowerCase(Locale.ROOT);
        if (rawStyle.contains("underline")) flags |= Paint.UNDERLINE_TEXT_FLAG;
        if (rawStyle.contains("strike")) flags |= Paint.STRIKE_THRU_TEXT_FLAG;
        view.setPaintFlags(flags);
    }

    private float treeTextSize(NoteNode node) {
        if (node == null) return 16f;
        try {
            int size = Integer.parseInt(String.valueOf(node.extraAttrs.get(NODE_TITLE_SIZE_ATTR)).trim());
            return Math.max(8, Math.min(42, LegacyRichTextToolbar.normalizeFontSize(size)));
        } catch (Exception ignored) {
            return 16f;
        }
    }

    private String androidTypefaceFamily(String family) {
        String clean = LegacyRichTextToolbar.normalizeFontFamily(family).toLowerCase(Locale.ROOT);
        if (clean.contains("courier") || clean.contains("consolas") || clean.contains("mono")) return "monospace";
        if (clean.contains("times") || clean.contains("georgia") || clean.contains("serif")) return "serif";
        return "sans-serif";
    }

    private GradientDrawable rowBackground(int fillColor, int strokeColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(fillColor);
        bg.setCornerRadius(dp(7));
        if (strokeColor != Color.TRANSPARENT) bg.setStroke(dp(1), strokeColor);
        return bg;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
