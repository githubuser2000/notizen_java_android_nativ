package de.notizen.android.core;

import java.util.ArrayDeque;
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
        ArrayDeque<NoteNode> stack = new ArrayDeque<>();
        stack.push(this);
        int count = 0;
        while (!stack.isEmpty()) {
            NoteNode node = stack.pop();
            out.add(node);
            LegacyCrashHardening.checkTreeNodeCount(++count);
            for (int i = node.children.size() - 1; i >= 0; i--) {
                NoteNode child = node.children.get(i);
                if (child != null) stack.push(child);
            }
        }
        return out;
    }

    public NoteNode cloneDeep(boolean includeDesktopNote) {
        NoteNode rootClone = cloneShallow(includeDesktopNote);
        ArrayDeque<ClonePair> stack = new ArrayDeque<>();
        stack.push(new ClonePair(this, rootClone, 1));
        int count = 0;
        while (!stack.isEmpty()) {
            ClonePair pair = stack.pop();
            LegacyCrashHardening.checkTreeDepth(pair.depth);
            LegacyCrashHardening.checkTreeNodeCount(++count);
            for (NoteNode child : pair.source.children) {
                if (child == null) continue;
                NoteNode childClone = child.cloneShallow(includeDesktopNote);
                pair.target.addChild(childClone);
                stack.push(new ClonePair(child, childClone, pair.depth + 1));
            }
        }
        return rootClone;
    }

    private NoteNode cloneShallow(boolean includeDesktopNote) {
        NoteNode c = new NoteNode(title, rtf);
        c.expanded = expanded;
        c.bgArgb = bgArgb;
        c.fgArgb = fgArgb;
        c.extraAttrs.putAll(extraAttrs);
        c.extraChildXml.addAll(extraChildXml);
        if (includeDesktopNote && desktopNote != null) c.desktopNote = desktopNote.copy();
        return c;
    }

    private static final class ClonePair {
        final NoteNode source;
        final NoteNode target;
        final int depth;
        ClonePair(NoteNode source, NoteNode target, int depth) {
            this.source = source;
            this.target = target;
            this.depth = depth;
        }
    }
}
