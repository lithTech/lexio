package lt.ru.lexio.util;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;

/**
 * Created by lithTech on 27.08.2016.
 */

public class ClipboardHelper {

    public static boolean hasText(ClipboardManager clipboardManager) {
        return clipboardManager.hasPrimaryClip() &&
                (clipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
                        clipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML));
    }

    public static void clearClipboard(ClipboardManager m) {
        ClipData clipData = ClipData.newPlainText("", "");
        m.setPrimaryClip(clipData);
    }

}
