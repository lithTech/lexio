package lt.ru.lexio.util;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by lithTech on 01.04.2016.
 */
public class ColorAnimateHelper {
    public static void animateBetweenColors(final View viewToAnimateItBackground, final int colorFrom, final int colorTo,
                                            final int durationInMs, Animator.AnimatorListener animationListener) {
        final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            ColorDrawable colorDrawable = new ColorDrawable(colorFrom);

            @Override
            public void onAnimationUpdate(final ValueAnimator animator) {
                colorDrawable.setColor((Integer) animator.getAnimatedValue());
                viewToAnimateItBackground.setBackground(colorDrawable);
            }
        });
        if (animationListener != null)
            colorAnimation.addListener(animationListener);
        if (durationInMs >= 0)
            colorAnimation.setDuration(durationInMs);
        colorAnimation.setInterpolator(new DecelerateInterpolator(3.0f));
        colorAnimation.start();
    }
}
