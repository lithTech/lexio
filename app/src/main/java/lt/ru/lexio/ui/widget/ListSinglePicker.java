package lt.ru.lexio.ui.widget;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import lt.ru.lexio.R;
import lt.ru.lexio.ui.GeneralCallback;
import lt.ru.lexio.ui.training.TrainingWordOrder;

/**
 * Created by lithTech on 26.12.2016.
 */

public class ListSinglePicker {

    public static void show(Activity activity,
                            final String[] items,
                            final int title,
                            final GeneralCallback callback) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String item = items[which];
                        callback.done(item);
                    }
                })
                .setTitle(title)
                //.setMessage(message)
                .setCancelable(true)
                .show();
    }

}
