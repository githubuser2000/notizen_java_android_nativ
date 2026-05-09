package de.notizen.android.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Termux/no-Gradle APK build plan used by tools/notizen-build-apk-termux.sh. */
public final class LegacyApkBuildPipeline {
    public static final class Params {
        public final int compileApi;
        public final int targetSdk;
        public final int minSdk;
        public final String packageName;
        public final int versionCode;
        public final String versionName;
        public Params(int compileApi, int targetSdk, int minSdk, String packageName, int versionCode, String versionName) {
            this.compileApi = compileApi;
            this.targetSdk = targetSdk;
            this.minSdk = minSdk;
            this.packageName = packageName == null ? "de.notizen.android" : packageName;
            this.versionCode = versionCode;
            this.versionName = versionName == null ? "" : versionName;
        }
    }

    private LegacyApkBuildPipeline() {}

    public static Params defaultParams() { return new Params(34, 34, 23, "de.notizen.android", 105, "1.0.105-java-android-nativ"); }

    public static List<String> steps(Params p) {
        Params x = p == null ? defaultParams() : p;
        return Collections.unmodifiableList(Arrays.asList(
                "aapt2 compile --dir res",
                "aapt2 link -I android.jar --manifest AndroidManifest.xml --min-sdk-version " + x.minSdk + " --target-sdk-version " + x.targetSdk,
                "javac -source 17 -target 17 -classpath android.jar -sourcepath java:gen",
                "d8 --lib android.jar --min-api " + x.minSdk + " --output dex classes",
                "zip classes.dex into APK",
                "zipalign -f 4",
                "apksigner sign",
                "apksigner verify"));
    }

    public static String summary(Params p) {
        Params x = p == null ? defaultParams() : p;
        StringBuilder b = new StringBuilder();
        b.append("package: ").append(x.packageName).append('\n');
        b.append("compile/android.jar API: ").append(x.compileApi).append('\n');
        b.append("targetSdk: ").append(x.targetSdk).append('\n');
        b.append("minSdk: ").append(x.minSdk).append('\n');
        b.append("version: ").append(x.versionCode).append(" / ").append(x.versionName).append('\n');
        for (String step : steps(x)) b.append("- ").append(step).append('\n');
        b.append("Output: build/termux-apk/out/NotizenJavaAndroidNativ-v").append(x.versionCode).append("-debug.apk");
        return b.toString();
    }
}
