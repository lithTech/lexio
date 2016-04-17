package lt.ru.lexio.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import lt.ru.lexio.R;

/**
 * Created by lithTech on 17.04.2016.
 */
public class TrainingPositionBar extends android.widget.ProgressBar {

    final Paint bg = new Paint();
    final Paint segment = new Paint();

    private void setColors() {
        bg.setColor(getResources().getColor(R.color.colorPrimary));
        segment.setColor(getResources().getColor(R.color.colorAccent));
    }

    public TrainingPositionBar(Context context) {
        super(context);
        setColors();
    }

    public TrainingPositionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColors();
    }

    public TrainingPositionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setColors();
    }

    public TrainingPositionBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setColors();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), bg);
        int progress = getProgress();
        if (progress >= 0) {
            float pSize = getWidth() / (getMax() + 1);
            float right = progress * pSize + pSize;
            if (getProgress() == getMax())
                right = getWidth();

            canvas.drawRect(progress * pSize, 0, right, getHeight(), segment);
        }
    }
}
