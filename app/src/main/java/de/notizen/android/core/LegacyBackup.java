package de.notizen.android.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class LegacyBackup {
    private static final SimpleDateFormat BACKUP_STAMP = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.ROOT);
    private LegacyBackup() {}
    public static final class BackupEntry { public final File path; public final Date created; public final long size; public BackupEntry(File path,Date created,long size){this.path=path;this.created=created;this.size=size;} }
    public static File backupDirectoryFor(File path){ File original=path==null?new File(LegacyPaths.LEGACY_DEFAULT_FILENAME):path; String name=original.getName(); int dot=name.lastIndexOf('.'); String stem=dot>0?name.substring(0,dot):name; File parent=original.getParentFile(); return parent==null?new File(stem):new File(parent,stem); }
    public static String backupFilePattern(File path){ File original=path==null?new File(LegacyPaths.LEGACY_DEFAULT_FILENAME):path; String name=original.getName(); int dot=name.lastIndexOf('.'); String stem=dot>0?name.substring(0,dot):name; String suffix=dot>=0?name.substring(dot):".alx"; return stem+"-*"+suffix; }
    public static Date parseLegacyBackupTimestamp(File backupPath,File originalPath){ if(backupPath==null||originalPath==null)return null; String backupName=backupPath.getName(), originalName=originalPath.getName(); int dot=originalName.lastIndexOf('.'); String stem=dot>0?originalName.substring(0,dot):originalName; String suffix=dot>=0?originalName.substring(dot):extensionOrDefault(backupName); String prefix=stem+"-"; if(!backupName.startsWith(prefix)||!backupName.endsWith(suffix))return null; String stamp=backupName.substring(prefix.length(),backupName.length()-suffix.length()); synchronized(BACKUP_STAMP){ try{ BACKUP_STAMP.setLenient(false); return BACKUP_STAMP.parse(stamp); }catch(ParseException e){return null;} } }
    public static List<BackupEntry> listBackups(File path){ File original=path==null?new File(LegacyPaths.LEGACY_DEFAULT_FILENAME):path; File dir=backupDirectoryFor(original); File[] files=dir.listFiles(file -> file.isFile() && matchesBackupName(file,original)); if(files==null)return Collections.emptyList(); ArrayList<BackupEntry> out=new ArrayList<>(); for(File file:files)out.add(new BackupEntry(file,parseLegacyBackupTimestamp(file,original),file.length())); Collections.sort(out,new Comparator<BackupEntry>(){ @Override public int compare(BackupEntry a,BackupEntry b){ long at=sortTime(a),bt=sortTime(b); if(at<bt)return -1; if(at>bt)return 1; return a.path.getName().compareTo(b.path.getName()); }}); return out; }
    public static List<File> pruneBackups(File path,int keep){ int count=Math.max(0,keep); List<BackupEntry> entries=listBackups(path); int stale=count==0?entries.size():Math.max(0,entries.size()-count); ArrayList<File> removed=new ArrayList<>(); for(int i=0;i<stale;i++){ File f=entries.get(i).path; if(f.delete())removed.add(f); } return removed; }
    public static File createBackup(File path,int keep) throws IOException { if(path==null||!path.isFile())return null; int count=Math.max(0,keep); if(count<=0){pruneBackups(path,0);return null;} File dir=backupDirectoryFor(path); if(!dir.exists()&&!dir.mkdirs())throw new IOException("Backup-Ordner konnte nicht angelegt werden: "+dir); String name=path.getName(); int dot=name.lastIndexOf('.'); String stem=dot>0?name.substring(0,dot):name; String suffix=dot>=0?name.substring(dot):".alx"; String stamp; synchronized(BACKUP_STAMP){stamp=BACKUP_STAMP.format(new Date());} File target=new File(dir,stem+"-"+stamp+suffix); copy(path,target); pruneBackups(path,count); return target; }
    private static boolean matchesBackupName(File file,File original){ String pat=backupFilePattern(original), name=file.getName(); int star=pat.indexOf('*'); if(star<0)return name.equals(pat); return name.startsWith(pat.substring(0,star))&&name.endsWith(pat.substring(star+1)); }
    private static long sortTime(BackupEntry e){ return e.created!=null?e.created.getTime():e.path.lastModified(); }
    private static String extensionOrDefault(String name){ int dot=name==null?-1:name.lastIndexOf('.'); return dot>=0?name.substring(dot):".alx"; }
    private static void copy(File src,File dst) throws IOException { FileInputStream in=new FileInputStream(src); try{ FileOutputStream out=new FileOutputStream(dst); try{ byte[] buf=new byte[8192]; int n; while((n=in.read(buf))!=-1)out.write(buf,0,n); } finally { out.close(); } } finally { in.close(); } }
}
