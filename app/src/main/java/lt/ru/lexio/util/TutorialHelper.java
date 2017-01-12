package lt.ru.lexio.util;

import android.app.Activity;
import android.view.View;

import lt.ru.lexio.R;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Created by lithTech on 03.12.2016.
 */

public class TutorialHelper {

    public static MaterialShowcaseView.Builder defElem(Activity act, int contentStrResId, boolean rectShape,
                                                View target) {
        return new MaterialShowcaseView.Builder(act)
                .setTarget(target)
                //.setDismissText(act.getResources().getString(R.string.dialog_GotIt))
                .setContentText(act.getString(contentStrResId))
                .setDismissOnTouch(true)
                .setMaskColour(act.getResources().getColor(R.color.tutorialBackColor))
                .withRectangleShape(rectShape);
    }

}
