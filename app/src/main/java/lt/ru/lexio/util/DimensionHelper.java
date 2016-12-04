package lt.ru.lexio.util;

import android.app.Activity;
import android.content.res.TypedArray;
import android.util.TypedValue;

/**
 * Created by lithTech on 04.12.2016.
 */

public class DimensionHelper {

    public static int getActionBarSize(Activity act) {
        int[] actionBarSizeArr = new int[] { android.R.attr.actionBarSize };
        int indexOfAttrTextSize = 0;
        TypedValue typedValue = new TypedValue();
        TypedArray a = act.obtainStyledAttributes(typedValue.data, actionBarSizeArr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();

        return actionBarSize;
    }

}
