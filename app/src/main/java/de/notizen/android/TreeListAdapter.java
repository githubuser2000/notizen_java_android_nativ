package de.notizen.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.notizen.android.core.LegacyRichTextToolbar;
import de.notizen.android.core.NoteNode;

public final class TreeListAdapter extends BaseAdapter {
    public interface InlineEditListener {
        void onCommit(NoteNode node, String candidateTitle);
        void onCancel(NoteNode node);
    }

    public enum DropPreview { NONE, BEFORE, AS_CHILD, AFTER }

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
    private boolean treeActive;
    private NoteNode editingNode;
    private InlineEditListener inlineEditListener;
    private NoteNode dropPreviewNode;
    private DropPreview dropPreview = DropPreview.NONE;
    private float defaultTreeTextSizeSp = 16f;

    public TreeListAdapter(Context context) { this.context = context; }

    public void setDefaultTreeTextSizeSp(float sizeSp) {
        float next = Math.max(8f, Math.min(42f, sizeSp <= 0f ? 16f : sizeSp));
        if (Math.abs(defaultTreeTextSizeSp - next) < 0.01f) return;
        defaultTreeTextSizeSp = next;
        notifyDataSetChanged();
    }

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

    public void setTreeActive(boolean active) {
        if (treeActive == active) return;
        treeActive = active;
        notifyDataSetChanged();
    }

    public boolean isInlineEditing() { return editingNode != null; }

    public void beginInlineEdit(NoteNode node, InlineEditListener listener) {
        editingNode = node;
        inlineEditListener = listener;
        notifyDataSetChanged();
    }

    public void cancelInlineEdit() {
        NoteNode node = editingNode;
        InlineEditListener listener = inlineEditListener;
        editingNode = null;
        inlineEditListener = null;
        notifyDataSetChanged();
        if (listener != null) listener.onCancel(node);
    }

    public void clearInlineEdit() {
        editingNode = null;
        inlineEditListener = null;
        notifyDataSetChanged();
    }

    public void setDropPreview(NoteNode node, DropPreview preview) {
        DropPreview safe = preview == null ? DropPreview.NONE : preview;
        if (dropPreviewNode == node && dropPreview == safe) return;
        dropPreviewNode = node;
        dropPreview = safe;
        notifyDataSetChanged();
    }

    public void clearDropPreview() {
        if (dropPreviewNode == null && dropPreview == DropPreview.NONE) return;
        dropPreviewNode = null;
        dropPreview = DropPreview.NONE;
        notifyDataSetChanged();
    }

    @Override public int getCount() { return rows.size(); }
    @Override public FlatNode getItem(int position) { return rows.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        FlatNode row = rows.get(position);
        NoteNode node = row.node;
        if (node == editingNode) return editableView(convertView, row, node);

        TextView view = convertView instanceof TextView && !(convertView instanceof EditText) ? (TextView) convertView : new TextView(context);
        boolean hasChildren = !node.children.isEmpty();
        String marker = hasChildren ? (node.expanded ? "▾ " : "▸ ") : "  ";
        view.setText(marker + (node.title == null || node.title.isEmpty() ? "..." : node.title));
        view.setTextSize(treeTextSize(node));
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setSingleLine(false);
        view.setMinHeight(dp(44));
        view.setTypeface(treeTypeface(node));
        applyPaintFlags(view, node);
        view.setPadding(dp(12 + row.depth * 22), dp(7), dp(10), dp(7));
        view.setBackground(backgroundFor(position, node));
        if (node.fgArgb != 0) view.setTextColor(0xff000000 | (node.fgArgb & 0x00ffffff));
        else view.setTextColor(node == selected ? Color.rgb(20, 38, 64) : Color.rgb(32, 38, 46));
        return view;
    }

    private View editableView(View convertView, FlatNode row, NoteNode node) {
        EditText edit = convertView instanceof EditText ? (EditText) convertView : new EditText(context);
        String title = node.title == null || node.title.isEmpty() ? "..." : node.title;
        if (edit.getTag() != node || !edit.hasFocus()) {
            edit.setText(title);
            edit.setSelection(0, edit.getText().length());
        }
        edit.setTag(node);
        edit.setSingleLine(true);
        edit.setSelectAllOnFocus(true);
        edit.setTextSize(treeTextSize(node));
        edit.setTypeface(treeTypeface(node));
        edit.setTextColor(node.fgArgb != 0 ? 0xff000000 | (node.fgArgb & 0x00ffffff) : Color.rgb(20, 38, 64));
        edit.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        edit.setMinHeight(dp(44));
        edit.setPadding(dp(12 + row.depth * 22), dp(4), dp(10), dp(4));
        edit.setBackground(rowBackground(Color.rgb(255, 253, 214), Color.rgb(74, 133, 216), dp(2)));
        edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edit.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_DONE || enter) {
                commitInlineEdit(node, v.getText().toString());
                return true;
            }
            return false;
        });
        edit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && editingNode == node) commitInlineEdit(node, ((EditText) v).getText().toString());
        });
        edit.post(() -> {
            if (editingNode == node && !edit.hasFocus()) {
                edit.requestFocus();
                edit.selectAll();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        return edit;
    }

    private void commitInlineEdit(NoteNode node, String candidate) {
        if (editingNode != node) return;
        InlineEditListener listener = inlineEditListener;
        editingNode = null;
        inlineEditListener = null;
        if (listener != null) listener.onCommit(node, candidate);
        notifyDataSetChanged();
    }

    private GradientDrawable backgroundFor(int position, NoteNode node) {
        if (node == dropPreviewNode && dropPreview != DropPreview.NONE) {
            int stroke = dropPreview == DropPreview.AS_CHILD ? Color.rgb(45, 145, 80) : Color.rgb(210, 150, 32);
            int fill = dropPreview == DropPreview.AS_CHILD ? Color.rgb(224, 247, 231) : Color.rgb(255, 244, 214);
            return rowBackground(fill, stroke, dp(2));
        }
        if (node == selected) {
            return treeActive
                    ? rowBackground(Color.rgb(210, 229, 255), Color.rgb(44, 112, 205), dp(2))
                    : rowBackground(Color.rgb(226, 236, 250), Color.rgb(146, 174, 218), dp(1));
        }
        if (node.bgArgb != 0) {
            return rowBackground(0xff000000 | (node.bgArgb & 0x00ffffff), Color.rgb(225, 228, 234), dp(1));
        }
        if ((position & 1) == 0) return rowBackground(Color.rgb(250, 251, 253), Color.TRANSPARENT, 0);
        return rowBackground(Color.WHITE, Color.TRANSPARENT, 0);
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
        if (node == null) return defaultTreeTextSizeSp;
        try {
            int size = Integer.parseInt(String.valueOf(node.extraAttrs.get(NODE_TITLE_SIZE_ATTR)).trim());
            return Math.max(8, Math.min(42, LegacyRichTextToolbar.normalizeFontSize(size)));
        } catch (Exception ignored) {
            return defaultTreeTextSizeSp;
        }
    }

    private String androidTypefaceFamily(String family) {
        String clean = LegacyRichTextToolbar.normalizeFontFamily(family).toLowerCase(Locale.ROOT);
        if (clean.contains("courier") || clean.contains("consolas") || clean.contains("mono")) return "monospace";
        if (clean.contains("times") || clean.contains("georgia") || clean.contains("serif")) return "serif";
        return "sans-serif";
    }

    private GradientDrawable rowBackground(int fillColor, int strokeColor) {
        return rowBackground(fillColor, strokeColor, strokeColor == Color.TRANSPARENT ? 0 : dp(1));
    }

    private GradientDrawable rowBackground(int fillColor, int strokeColor, int strokePx) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(fillColor);
        bg.setCornerRadius(dp(7));
        if (strokeColor != Color.TRANSPARENT && strokePx > 0) bg.setStroke(strokePx, strokeColor);
        return bg;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
