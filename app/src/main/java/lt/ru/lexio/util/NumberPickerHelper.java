package lt.ru.lexio.util;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.widget.NumberPicker;

/**
 * Created by lithTech on 19.11.2016.
 */

public class NumberPickerHelper {

    /**
     * Set number picker divider color (from inintial hardcoded blue). Uses reflexion
     * @param picker - numberpicker widget
     * @param color - color. E.g. Color.GRAY
     */
    public static void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

}
