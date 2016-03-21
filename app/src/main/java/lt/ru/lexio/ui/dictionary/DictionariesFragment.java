package lt.ru.lexio.ui.dictionary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import java.util.Date;
import java.util.Set;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.DictionaryDAO;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.DialogHandler;
import lt.ru.lexio.ui.MainActivity;

/**
 * Created by User on 15.03.2016.
 */
public class DictionariesFragment extends ContentFragment {

    ListView lDictionaries;
    MainActivity activity;

    DictionaryDAO dictionaryDAO;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        dictionaryDAO = new DictionaryDAO(view.getContext());

        SimpleCursorAdapter sca = initAdapter(view.getContext());

        lDictionaries = (ListView) view.findViewById(R.id.lDictionaries);
        lDictionaries.setAdapter(sca);

        return view;
    }

    public SimpleCursorAdapter initAdapter(Context context) {
        return new DictionariesListAdapter(context, R.layout.content_dictionaries_item,
                dictionaryDAO.getAll().execute(), new String[]{Db.Common.TITLE, Db.Dictionary.WORDS_CNT},
                new int[]{R.id.tvTitle, R.id.tvWordCnt});
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_dictionaries_add) {
            createDictionary();
            return true;
        }
        else if (id == R.id.action_dictionaries_del) {
            deleteDictionaries();
            return true;
        }
        else if (id == R.id.action_dictionary_active) {
            makeCurrent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeCurrent() {
        Set<Integer> selectedItems = ((DictionariesListAdapter) lDictionaries.getAdapter()).selectedItems;
        if (!selectedItems.isEmpty()) {
            Dictionary cd = dictionaryDAO.setActive(lDictionaries
                    .getItemIdAtPosition(selectedItems.iterator().next()));
            if (activity != null) {
                activity.setCurrentDictionary(cd);
            }
            refreshDictionaryList();
        }
    }

    private void refreshDictionaryList() {
        getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        lDictionaries.setAdapter(initAdapter(((View) lDictionaries.getParent()).getContext()));
                    }
                });
    }

    private void deleteDictionaries() {
        Log.d("", "on delete dict " + lDictionaries.getAdapter());
        final Set<Integer> checkedPos = ((DictionariesListAdapter) lDictionaries.getAdapter()).getSelectedItems();
        if (!checkedPos.isEmpty()) {
            DialogHandler.Confirm(getActivity(),
                    getResources().getString(R.string.dictionaries_Deletion),
                    getResources().getString(R.string.dictionaries_Delete_Alert),
                    getResources().getString(R.string.dialog_Cancel),
                    getResources().getString(R.string.dialog_Delete),
                    new Runnable() {
                        @Override
                        public void run() {
                            for (int pos : checkedPos) {
                                dictionaryDAO.delete(lDictionaries.getItemIdAtPosition(pos));
                                refreshDictionaryList();
                            }
                        }
                    }, null);
        }
    }

    private void createDictionary() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View promptView = layoutInflater.inflate(R.layout.dialog_add_dictionary, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.dialog_Create, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText edTitle = (EditText) promptView.findViewById(R.id.edDictionaryTitle);
                        Spinner spLang = (Spinner) promptView.findViewById(R.id.spLanguages);
                        createDictionaryObject(edTitle.getText().toString(), "",
                                spLang.getSelectedItem().toString());
                        refreshDictionaryList();
                    }
                })
                .setNegativeButton(R.string.dialog_Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alert.show();
    }

    private void createDictionaryObject(String title, String desc, String lang) {
        Dictionary dictionary = new Dictionary();
        dictionary.setWords(0);
        dictionary.setTitle(title);
        dictionary.setLastModified(new Date());
        dictionary.setDesc(desc);
        dictionary.setLanguage(lang);
        dictionaryDAO.create(dictionary);
    }

}
