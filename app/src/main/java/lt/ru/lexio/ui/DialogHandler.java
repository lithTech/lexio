package lt.ru.lexio.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Created by lithTech on 19.03.2016.
 */
public class DialogHandler {

    public static boolean Confirm(Activity act, String title, String confirmText,
                           String cancelBtn, String okBtn,
                           final Runnable done,
                           final Runnable cancel) {
        AlertDialog dialog = new AlertDialog.Builder(act).create();
        dialog.setTitle(title);
        dialog.setMessage(confirmText);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, okBtn,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonId) {
                        if (done != null)
                            done.run();
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancelBtn,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonId) {
                        if (cancel != null)
                            cancel.run();
                        dialog.cancel();
                    }
                });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
        return true;
    }
}