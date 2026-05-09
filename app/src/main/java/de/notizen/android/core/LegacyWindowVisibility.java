package de.notizen.android.core;

import java.util.Locale;
import java.util.Map;

public final class LegacyWindowVisibility {
    private LegacyWindowVisibility() {}
    public static final class VisibleWindowGeometry { public final int x,y,width,height; public final boolean reset; public VisibleWindowGeometry(int x,int y,int width,int height,boolean reset){this.x=x;this.y=y;this.width=width;this.height=height;this.reset=reset;} }
    public static boolean envRequestsWindowReset(Map<String,String> env){return envFlag(env,"NOTIZEN_RESET_WINDOW")||envFlag(env,"NOTIZEN_FORCE_VISIBLE");}
    public static VisibleWindowGeometry sanitizeLegacyWindowGeometry(Object x,Object y,Object width,Object height,int screenLeft,int screenTop,int screenWidth,int screenHeight,boolean forceReset){ int left=asInt(screenLeft,0),top=asInt(screenTop,0),sw=Math.max(320,asInt(screenWidth,1280)),sh=Math.max(240,asInt(screenHeight,800)); int right=left+sw,bottom=top+sh,minW=640,minH=420; int dw=Math.min(Math.max(minW,1000),sw),dh=Math.min(Math.max(minH,700),sh); int defX=Math.min(Math.max(left,left+60),Math.max(left,right-dw)),defY=Math.min(Math.max(top,top+60),Math.max(top,bottom-dh)); if(forceReset)return new VisibleWindowGeometry(defX,defY,dw,dh,true); int w=Math.min(Math.max(minW,asInt(width,dw)),sw),h=Math.min(Math.max(minH,asInt(height,dh)),sh); int px=asInt(x,defX),py=asInt(y,defY),margin=50; boolean off=px<left-(w-margin)||py<top-(h-margin)||px>right-margin||py>bottom-margin; if(off)return new VisibleWindowGeometry(defX,defY,w,h,true); int cx=Math.min(Math.max(px,left),Math.max(left,right-margin)),cy=Math.min(Math.max(py,top),Math.max(top,bottom-margin)); if(cx+w>right)cx=Math.max(left,right-w); if(cy+h>bottom)cy=Math.max(top,bottom-h); return new VisibleWindowGeometry(cx,cy,w,h,cx!=px||cy!=py); }
    public static boolean legacyWindowStateIsRestorable(Object x,Object y){return asInt(x,0)!=0&&asInt(y,0)!=0;}
    public static boolean shouldStartMinimized(boolean explicitMinimized,boolean legacyMinimized,String storedState,boolean forceVisible,boolean resetWindow,boolean restorable){ if(forceVisible||resetWindow)return false; boolean stored=restorable&&"minimized".equals((storedState==null?"":storedState).toLowerCase(Locale.ROOT)); return explicitMinimized||legacyMinimized||stored; }
    private static boolean envFlag(Map<String,String> env,String key){ if(env==null)return false; String v=env.get(key); if(v==null)return false; String s=v.trim().toLowerCase(Locale.ROOT); return s.equals("1")||s.equals("true")||s.equals("yes")||s.equals("on")||s.equals("ja"); }
    private static int asInt(Object value,int fallback){ try{return Integer.parseInt(String.valueOf(value));}catch(Exception e){return fallback;} }
}
