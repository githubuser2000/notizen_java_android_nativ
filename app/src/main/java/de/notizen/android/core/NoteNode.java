package de.notizen.android.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NoteNode {
    public String title = "...";
    public String rtf = "";
    public boolean expanded = true;
    public int bgArgb = 0;
    public int fgArgb = 0;
    public DesktopNoteState desktopNote = null;
    public final Map<String, String> extraAttrs = new LinkedHashMap<>();
    public final List<String> extraChildXml = new ArrayList<>();
    public final List<NoteNode> children = new ArrayList<>();
    public NoteNode parent = null;

    public NoteNode() {}

    public NoteNode(String title, String rtf) {
        this.title = title == null ? "..." : title;
        this.rtf = rtf == null ? "" : rtf;
    }

    public NoteNode addChild(NoteNode child) {
        if (child == null) throw new IllegalArgumentException("child == null");
        child.removeFromParent();
        child.parent = this;
        children.add(child);
        return child;
    }

    public NoteNode insertChild(int index, NoteNode child) {
        if (child == null) throw new IllegalArgumentException("child == null");
        child.removeFromParent();
        child.parent = this;
        if (index < 0) index = 0;
        if (index > children.size()) index = children.size();
        children.add(index, child);
        return child;
    }

    public void removeFromParent() {
        if (parent != null) {
            parent.children.remove(this);
            parent = null;
        }
    }

    public int indexInParent() {
        return parent == null ? 0 : parent.children.indexOf(this);
    }

    public boolean isAncestorOf(NoteNode other) {
        NoteNode n = other == null ? null : other.parent;
        while (n != null) {
            if (n == this) return true;
            n = n.parent;
        }
        return false;
    }

    public NoteNode root() {
        NoteNode n = this;
        while (n.parent != null) n = n.parent;
        return n;
    }

    public List<NoteNode> walk() {
        ArrayList<NoteNode> out = new ArrayList<>();
        walkInto(out);
        return out;
    }

    private void walkInto(List<NoteNode> out) {
        out.add(this);
        for (NoteNode child : children) child.walkInto(out);
    }

    public NoteNode cloneDeep(boolean includeDesktopNote) {
        NoteNode c = new NoteNode(title, rtf);
        c.expanded = expanded;
        c.bgArgb = bgArgb;
        c.fgArgb = fgArgb;
        c.extraAttrs.putAll(extraAttrs);
        c.extraChildXml.addAll(extraChildXml);
        if (includeDesktopNote && desktopNote != null) c.desktopNote = desktopNote.copy();
        for (NoteNode child : children) c.addChild(child.cloneDeep(includeDesktopNote));
        return c;
    }
}
