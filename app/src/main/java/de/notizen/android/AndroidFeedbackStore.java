package de.notizen.android;

import android.content.Context;
import java.io.File;
import java.util.Date;
import de.notizen.android.core.LegacyFeedback;

public final class AndroidFeedbackStore {
    private AndroidFeedbackStore() {}
    public static File feedbackDirectory(Context context) { return new File(context.getFilesDir(), "feedback"); }
    public static File write(Context context, String text) throws java.io.IOException { return LegacyFeedback.writeLocalFeedbackArchive(text, feedbackDirectory(context), new Date()); }
}
