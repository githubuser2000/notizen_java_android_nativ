package de.notizen.android.core;

import java.io.File;

public final class LegacyPaths {
    public static final String LEGACY_DEFAULT_FILENAME = "unbenannt.alx";
    private LegacyPaths() {}
    public static String documentsNotizenDir(String homeDirectory){ String base=homeDirectory==null||homeDirectory.isEmpty()?System.getProperty("user.home",""):homeDirectory; return join(join(base,"Documents"),"Notizen"); }
    public static String[] splitLegacyFileLocation(String path,String defaultDirectory){ String fallback=defaultDirectory==null||defaultDirectory.isEmpty()?documentsNotizenDir(null):defaultDirectory; String value=path==null?"":path.trim(); if(value.isEmpty()) return new String[]{fallback,LEGACY_DEFAULT_FILENAME}; int slash=Math.max(value.lastIndexOf('/'),value.lastIndexOf('\\')); if(slash>=0){ String dir=value.substring(0,slash); String file=value.substring(slash+1); if(file.trim().isEmpty()) file=LEGACY_DEFAULT_FILENAME; if(dir.trim().isEmpty()) dir=fallback; return new String[]{dir,file}; } return new String[]{fallback,value}; }
    public static String join(String left,String right){ String l=left==null?"":left; String r=right==null?"":right; if(l.isEmpty())return r; if(r.isEmpty())return l; if(l.endsWith("/")||l.endsWith("\\"))return l+r; return l+ File.separator+r; }
}
