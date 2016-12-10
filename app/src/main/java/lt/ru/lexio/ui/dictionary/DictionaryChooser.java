package lt.ru.lexio.ui.dictionary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.persist.sql.stmt.Where;

import java.util.List;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.DictionaryDAO;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.ui.GeneralCallback;
import lt.ru.lexio.util.NumberPickerHelper;

/**
 * Created by lithTech on 10.12.2016.
 */

public class DictionaryChooser {

    public static void showChooser(Context context, Where dictWhere, int positiveButtonTitleId,
                            final GeneralCallback callback) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View promptView = layoutInflater.inflate(R.layout.dialog_choose_dict, null);

        final DictionaryDAO dDAO = new DictionaryDAO(context);

        final List<Dictionary> dictionaries = dDAO.readAll(dDAO.getAll().where(dictWhere));

        if (dictionaries.isEmpty()) {
            Toast.makeText(context,
                    context.getResources().getString(R.string.msg_action_word_nodict),
                    Toast.LENGTH_LONG).show();
            return;
        }

        String[] dictDisplay = new String[dictionaries.size()];
        for (int i = 0; i < dictionaries.size(); i++) {
            Dictionary dictionary = dictionaries.get(i);
            dictDisplay[i] = dictionary.getTitle();
        }

        final NumberPicker npDictionaries = (NumberPicker) promptView.findViewById(R.id.npDictionaries);
        npDictionaries.setDisplayedValues(dictDisplay);
        npDictionaries.setMinValue(0);
        npDictionaries.setMaxValue(dictDisplay.length-1);
        NumberPickerHelper.setDividerColor(npDictionaries, Color.GRAY);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);

        // setup a dialog window
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton(positiveButtonTitleId, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Dictionary dictionary = dictionaries.get(npDictionaries.getValue());
                        callback.done(dictionary);
                    }
                })
                .setNegativeButton(R.string.dialog_Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        final AlertDialog alert = alertDialogBuilder.create();

        alert.show();
    }

}
