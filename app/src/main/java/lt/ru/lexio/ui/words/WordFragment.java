package lt.ru.lexio.ui.words;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Set;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.DialogHelper;
import lt.ru.lexio.ui.MainActivity;

/**
 * Created by lithTech on 21.03.2016.
 */
public class WordFragment extends ContentFragment implements TextWatcher, View.OnClickListener{

    WordDAO wordDAO = null;
    ListView lWords = null;
    EditText edFilter = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        wordDAO = new WordDAO(view.getContext());

        lWords = (ListView) view.findViewById(R.id.lvWords);
        lWords.setAdapter(initAdapter(view.getContext()));

        edFilter = (EditText) view.findViewById(R.id.edWordsFilter);
        edFilter.addTextChangedListener(this);

        ImageButton bCancelFilter = (ImageButton) view.findViewById(R.id.bWordsFilterClear);
        bCancelFilter.setOnClickListener(this);

        return view;
    }

    private WordListAdapter initAdapter(Context context) {
        final long dictId;
        if (mainActivity != null && mainActivity.getCurrentDictionary() != null)
            dictId = mainActivity.getCurrentDictionary().id;
        else dictId = 0;

        WordListAdapter adapter = new WordListAdapter(context, R.layout.content_word_item,
                wordDAO.getAll(dictId),
                new String[]{Db.Common.TITLE, Db.Word.TRANSLATION},
                new int[]{R.id.tvWord, R.id.tvTranslation});

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return wordDAO.getAllFiltered(dictId, constraint.toString().toUpperCase() + "%");
            }
        });

        return adapter;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_word_add) {
            createWord(getActivity(), mainActivity.getCurrentDictionary());
        }
        else if (item.getItemId() == R.id.action_word_del) {
            deleteWords();
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteWords() {
        final Set<Integer> checkedPos = ((WordListAdapter) lWords.getAdapter()).getSelectedWords();
        if (!checkedPos.isEmpty()) {
            DialogHelper.confirm(getActivity(),
                    getResources().getString(R.string.words_Deletion),
                    getResources().getString(R.string.words_Delete_Alert),
                    getResources().getString(R.string.dialog_Cancel),
                    getResources().getString(R.string.dialog_Delete),
                    new Runnable() {
                        @Override
                        public void run() {
                            for (int pos : checkedPos) {
                                wordDAO.delete(lWords.getItemIdAtPosition(pos));
                                refreshList();
                            }
                        }
                    }, null);
        }
    }

    private void refreshList() {
        Activity act = getActivity();
        if (getActivity() == null)
            act = mainActivity;
        if (act == null)
            return;

        lWords.setFilterText("");
        edFilter.setText("");

        act.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        lWords.setAdapter(initAdapter(((View) lWords.getParent()).getContext()));
                    }
                });
    }

    public void createWord(final Context context, final Dictionary dictionary) {
        final boolean needRefresh = this.getView() != null;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View promptView = layoutInflater.inflate(R.layout.dialog_add_word, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        EditText edTranslation = (EditText) promptView.findViewById(R.id.edTranslation);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.dialog_Create, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText edWord = (EditText) promptView.findViewById(R.id.edWord);
                        EditText edTranslation = (EditText) promptView.findViewById(R.id.edTranslation);
                        EditText edContext = (EditText) promptView.findViewById(R.id.edContext);
                        saveWordObject(context, edWord.getText().toString(),
                                edTranslation.getText().toString(),
                                edContext.getText().toString(), dictionary);
                        if (needRefresh)
                            refreshList();

                        Toast.makeText(context, edWord.getText() + " " +
                                context.getResources().getString(R.string.Word_WordAddedMessage),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        final AlertDialog alert = alertDialogBuilder.create();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        edTranslation.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

    private Word saveWordObject(Context daoContext,
                                String word, String translation,
                                String context, Dictionary dict) {
        Word w = new Word();
        w.setTitle(word);
        w.setContext(context);
        w.setTranslation(translation);
        w.setDictionary(dict);
        w.setCreated(new Date());

        if (wordDAO == null)
            wordDAO = new WordDAO(daoContext);

        wordDAO.create(w);
        return w;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments().getBoolean(ContentFragment.ARG_NEED_REFRESH)) {
            getArguments().remove(ContentFragment.ARG_NEED_REFRESH);
            refreshList();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        ((WordListAdapter) lWords.getAdapter()).getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bWordsFilterClear) {
            ((WordListAdapter) lWords.getAdapter()).getFilter().filter("");
            ((EditText) ((View) v.getParent()).findViewById(R.id.edWordsFilter)).setText("");
        }
    }
}
