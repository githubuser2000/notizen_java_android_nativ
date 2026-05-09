package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NoteDocument {
    public NoteNode root;
    public String password = "";
    public boolean changed = false;
    public String displayName = null;
    public final Map<String, String> rootAttrs = new LinkedHashMap<>();
    public final List<String> extraRootXml = new ArrayList<>();

    public static NoteDocument newDocument() {
        NoteDocument d = new NoteDocument();
        d.root = new NoteNode("start", "");
        d.changed = true;
        return d;
    }

    public NoteNode ensureRoot() {
        if (root == null) root = new NoteNode("start", "");
        return root;
    }

    public List<NoteNode> walk() {
        return root == null ? Collections.emptyList() : root.walk();
    }

    public void markChanged() { changed = true; }
    public void markSaved() { changed = false; }
}
