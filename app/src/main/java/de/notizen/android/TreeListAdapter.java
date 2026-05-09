package de.notizen.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.notizen.android.core.NoteNode;

public final class TreeListAdapter extends BaseAdapter {
    public static final class FlatNode {
        public final NoteNode node;
        public final int depth;
        public FlatNode(NoteNode node, int depth) { this.node = node; this.depth = depth; }
    }

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
        view.setTextSize(16f);
        view.setGravity(android.view.Gravity.CENTER_VERTICAL);
        view.setSingleLine(false);
        view.setMinHeight(dp(44));
        view.setTypeface(node.parent == null ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
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
