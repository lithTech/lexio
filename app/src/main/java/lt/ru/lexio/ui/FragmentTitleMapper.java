package lt.ru.lexio.ui;

import android.content.res.Resources;

/**
 * Created by lithTech on 12.11.2016.
 */

public class FragmentTitleMapper
{
    public static int getTitleResId(Resources res, int contentResId) {
        String resName = res.getResourceName(contentResId);
        resName = resName.substring(resName.indexOf("/") + 1);
        int stringResId = res.getIdentifier(resName, "string", "lt.ru.lexio");
        String titleResName = res.getString(stringResId);

        return res.getIdentifier(titleResName, "string", "lt.ru.lexio");
    }

    public static String getTitle(Resources res, int contentResId) {
        return res.getString(getTitleResId(res, contentResId));
    }
}
