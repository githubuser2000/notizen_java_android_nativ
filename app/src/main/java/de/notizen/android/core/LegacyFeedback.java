package de.notizen.android.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

public final class LegacyFeedback {
    public static final int LEGACY_FEEDBACK_MIN_CHARS = 10;
    public static final int LEGACY_FEEDBACK_DAILY_LIMIT = 30;
    public static final int LEGACY_FEEDBACK_DAILY_INCREMENT = 10;
    public static final String LEGACY_FEEDBACK_EMAIL = "notizen@notiza.de";
    public static final String LEGACY_FEEDBACK_WEB_URL = "http://www.notiza.de";
    private static final long TICKS_PER_DAY = 24L * 60L * 60L * 10_000_000L;
    private static final Charset UTF_16_LE = Charset.forName("UTF-16LE");
    private LegacyFeedback() {}
    public static final class FeedbackDecision { public final boolean allowed; public final String reason; public FeedbackDecision(boolean allowed, String reason){this.allowed=allowed;this.reason=reason==null?"":reason;} }
    public static final class FeedbackThrottleState { public final long dayTicks; public final int count; public FeedbackThrottleState(long dayTicks,int count){this.dayTicks=dayTicks;this.count=count;} }
    public static boolean textIsLongEnough(String text){ return (text==null?0:text.length()) >= LEGACY_FEEDBACK_MIN_CHARS; }
    public static long dotnetDateTicks(int year,int month,int day){ int y=Math.max(1,year); int m=Math.max(1,Math.min(12,month)); int d=Math.max(1,day); long yb=y-1L; long days=365L*yb+yb/4L-yb/100L+yb/400L+dayOfYear(y,m,d)-1L; return days*TICKS_PER_DAY; }
    public static long dotnetTodayTicks(){ return dotnetTodayTicks(TimeZone.getDefault()); }
    public static long dotnetTodayTicks(TimeZone zone){ Calendar c=Calendar.getInstance(zone==null?TimeZone.getDefault():zone, Locale.ROOT); return dotnetDateTicks(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH)); }
    public static FeedbackDecision decision(String text,long previousDayTicks,int previousCount,long todayTicks){ if(!textIsLongEnough(text)) return new FeedbackDecision(false,"char10minimum"); long when=Math.max(0L,previousDayTicks); int count=Math.max(0,previousCount); long today=Math.max(0L,todayTicks); if(when<today) return new FeedbackDecision(true,"new-day"); if(when==today && count<LEGACY_FEEDBACK_DAILY_LIMIT) return new FeedbackDecision(true,"same-day"); return new FeedbackDecision(false,"no-send"); }
    public static FeedbackDecision decision(String text,long previousDayTicks,int previousCount){ return decision(text,previousDayTicks,previousCount,dotnetTodayTicks()); }
    public static FeedbackThrottleState nextState(long previousDayTicks,int previousCount,long todayTicks){ long when=Math.max(0L,previousDayTicks); int count=Math.max(0,previousCount); long today=Math.max(0L,todayTicks); if(when<today) return new FeedbackThrottleState(today,0); return new FeedbackThrottleState(today,count+LEGACY_FEEDBACK_DAILY_INCREMENT); }
    public static FeedbackThrottleState nextState(long previousDayTicks,int previousCount){ return nextState(previousDayTicks,previousCount,dotnetTodayTicks()); }
    public static String feedbackFilename(Date now){ Date when=now==null?new Date():now; return "feedback."+new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",Locale.ROOT).format(when)+".txt.gz"; }
    public static File writeLocalFeedbackArchive(String text,File directory,Date now) throws IOException { File dir=directory==null?new File("."):directory; if(!dir.exists() && !dir.mkdirs()) throw new IOException("Feedback-Ordner konnte nicht angelegt werden: "+dir); File out=new File(dir,feedbackFilename(now)); GZIPOutputStream gz=new GZIPOutputStream(new FileOutputStream(out)); try{ gz.write((text==null?"":text).getBytes(UTF_16_LE)); } finally { gz.close(); } return out; }
    public static String reasonText(String reason){ if("char10minimum".equals(reason)) return "Geben Sie mindestens 10 Zeichen ein."; if("no-send".equals(reason)) return "Nach 3 Feedbacks kann man erst wieder am nächsten Tag senden."; if("new-day".equals(reason)) return "Feedback für neuen Tag erlaubt."; if("same-day".equals(reason)) return "Feedback für heute erlaubt."; return reason==null||reason.isEmpty()?"":reason; }
    private static int dayOfYear(int year,int month,int day){ int[] starts=isLeapYear(year)?new int[]{0,0,31,60,91,121,152,182,213,244,274,305,335}:new int[]{0,0,31,59,90,120,151,181,212,243,273,304,334}; return starts[month]+Math.max(1,Math.min(daysInMonth(year,month),day)); }
    private static int daysInMonth(int year,int month){ switch(month){case 2:return isLeapYear(year)?29:28;case 4:case 6:case 9:case 11:return 30;default:return 31;} }
    private static boolean isLeapYear(int year){ return year%4==0 && (year%100!=0 || year%400==0); }
}
