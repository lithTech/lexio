package lt.ru.lexio.ui.dictionary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.droidparts.persist.sql.stmt.Is;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.DictionaryDAO;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.DialogHelper;
import lt.ru.lexio.ui.GeneralCallback;
import lt.ru.lexio.ui.MainActivity;
import lt.ru.lexio.ui.words.WordListAdapter;

/**
 * Created by User on 15.03.2016.
 */
public class DictionariesFragment extends ContentFragment {

    ListView lDictionaries;

    DictionaryDAO dictionaryDAO;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    private class DictLoader implements Runnable {

        DictionaryDAO dao;
        InputStream sqlInsertsStream;
        long dictId;
        GeneralCallback callback = null;

        public DictLoader(DictionaryDAO dao, InputStream sqlInsertsStream, long dictId) {
            this.dao = dao;
            this.sqlInsertsStream = sqlInsertsStream;
            this.dictId = dictId;
        }

        public DictLoader(DictionaryDAO dao, InputStream sqlInsertsStream, long dictId, GeneralCallback callback) {
            this.dao = dao;
            this.sqlInsertsStream = sqlInsertsStream;
            this.dictId = dictId;
            this.callback = callback;
        }

        @Override
        public void run() {
            int c = 0;
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(sqlInsertsStream));
                String line = reader.readLine();
                while (line != null) {
                    Date date = new Date();
                    dao.importWord(dictId, line, date);
                    line = reader.readLine();
                    c++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Dictionary d = dao.read(dictId);
                d.setWords(c);
                dao.update(d);
                try {
                    sqlInsertsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (callback != null)
                    callback.done(d);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        dictionaryDAO = new DictionaryDAO(view.getContext());

        SimpleCursorAdapter sca = initAdapter(view.getContext());

        lDictionaries = (ListView) view.findViewById(R.id.lDictionaries);
        lDictionaries.setAdapter(sca);

        lDictionaries.setLongClickable(true);
        registerForContextMenu(lDictionaries);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ProgressDialog progressDialog = ProgressDialog.show(getView().getContext(),
                getString(R.string.defdict_loading_title),
                getString(R.string.defdict_loading_msg));

        GeneralCallback callback = new GeneralCallback() {
            @Override
            public void done(Object data) {
                progressDialog.dismiss();
                refreshDictionaryList();
            }
        };

        startLoadingIfDoesntExist(getResources().getString(R.string.defdict_eng_basic),
                R.raw.dict_eng_basic, "en-US", callback);
    }

    private void startLoadingIfDoesntExist(String dictTitle, int rId, String lang,
                                                GeneralCallback callback) {
        Cursor cursor = dictionaryDAO.select().where(Db.Common.TITLE, Is.EQUAL, dictTitle).execute();
        boolean hasDict = cursor.moveToNext();
        cursor.close();
        if (!hasDict) {
            long dictId = createDictionaryObject(dictTitle, getResources().getString(R.string.defaultDictDescription),
                    lang);
            InputStream in = getResources().openRawResource(rId);
            executor.submit(
                    new DictLoader(new DictionaryDAO(getView().getContext()), in, dictId, callback));
        } else {
            if (callback != null)
                callback.done(null);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_content_dictionaries, menu);
    }

    public SimpleCursorAdapter initAdapter(Context context) {
        return new DictionariesListAdapter(context, R.layout.content_dictionaries_item,
                dictionaryDAO.getAll().execute(), new String[]{Db.Common.TITLE, Db.Dictionary.WORDS_CNT},
                new int[]{R.id.tvTitle, R.id.tvWordCnt});
    }

    //called from context menu items
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ((DictionariesListAdapter) lDictionaries.getAdapter()).getSelectedItems().clear();
        ((DictionariesListAdapter) lDictionaries.getAdapter()).getSelectedItems().add(info.position);
        return onOptionsItemSelected(item);
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
            long dictId = lDictionaries
                    .getItemIdAtPosition(selectedItems.iterator().next());
            setActiveDictionary(dictId);
            refreshDictionaryList();
        }
    }

    private void setActiveDictionary(long dictId) {
        Dictionary cd = dictionaryDAO.setActive(dictId);
        if (mainActivity != null) {
            mainActivity.setCurrentDictionary(cd);
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
        final Set<Integer> checkedPos = ((DictionariesListAdapter) lDictionaries.getAdapter()).getSelectedItems();
        if (!checkedPos.isEmpty()) {
            DialogHelper.confirm(getActivity(),
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
        EditText edTitle = (EditText) promptView.findViewById(R.id.edDictionaryTitle);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
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
        final AlertDialog alert = alertDialogBuilder.create();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        edTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    alert.getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();
                    return true;
                }
                return false;
            }
        });
        alert.show();
    }

    private long createDictionaryObject(String title, String desc, String lang) {
        int dictCnt = dictionaryDAO.select().count();
        Dictionary dictionary = new Dictionary();
        dictionary.setWords(0);
        dictionary.setTitle(title);
        dictionary.setDesc(desc);
        dictionary.setLanguage(lang);
        if (dictCnt == 0)
            dictionary.setActive(1);
        dictionaryDAO.create(dictionary);

        if (dictCnt == 0)
            setActiveDictionary(dictionary.id);

        return dictionary.id;
    }

}
