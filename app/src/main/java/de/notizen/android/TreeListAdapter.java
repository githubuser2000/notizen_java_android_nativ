package de.notizen.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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
        view.setMinHeight(dp(42));
        view.setTypeface(node.parent == null ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        view.setPadding(dp(10 + row.depth * 22), dp(6), dp(8), dp(6));
        if (node == selected) {
            view.setBackgroundColor(Color.rgb(220, 235, 255));
        } else if (node.bgArgb != 0) {
            view.setBackgroundColor(0xff000000 | (node.bgArgb & 0x00ffffff));
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
        }
        if (node.fgArgb != 0) view.setTextColor(0xff000000 | (node.fgArgb & 0x00ffffff));
        else view.setTextColor(Color.rgb(30, 30, 30));
        return view;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
