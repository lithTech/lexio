package lt.ru.lexio.ui.words;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Date;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.MainActivity;

/**
 * Created by lithTech on 21.03.2016.
 */
public class WordFragment extends ContentFragment {

    MainActivity activity;
    WordDAO wordDAO = null;
    ListView lWords = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        wordDAO = new WordDAO(view.getContext());

        lWords = (ListView) view.findViewById(R.id.lvWords);
        lWords.setAdapter(initAdapter(view.getContext()));

        return view;
    }

    private WordListAdapter initAdapter(Context context) {
        long dictId = 0;
        if (activity != null && activity.getCurrentDictionary() != null)
            dictId = activity.getCurrentDictionary().id;

        WordListAdapter adapter = new WordListAdapter(context, R.layout.content_word_item,
                wordDAO.getAll(dictId),
                new String[]{Db.Common.TITLE, Db.Word.TRANSLATION},
                new int[]{R.id.tvWord, R.id.tvTranslation});

        return adapter;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_word_add) {
            createWord(getActivity());
        }
        else if (item.getItemId() == R.id.action_word_del){

        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshList() {
        Activity act = getActivity();
        if (getActivity() == null)
            act = activity;
        if (act == null)
            return;

        act.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        lWords.setAdapter(initAdapter(((View) lWords.getParent()).getContext()));
                    }
                });
    }

    public void createWord(Context context) {
        if (activity == null || activity.getCurrentDictionary() == null) {
            //TODO message to user
            return;
        }

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View promptView = layoutInflater.inflate(R.layout.dialog_add_word, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.dialog_Create, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText edWord = (EditText) promptView.findViewById(R.id.edWord);
                        EditText edTranslation = (EditText) promptView.findViewById(R.id.edTranslation);
                        EditText edContext = (EditText) promptView.findViewById(R.id.edContext);
                        saveWordObject(edWord.getText().toString(), edTranslation.getText().toString(),
                                edContext.getText().toString(), activity.getCurrentDictionary());
                        refreshList();
                    }
                })
                .setNegativeButton(R.string.dialog_Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alert.show();
    }

    private Word saveWordObject(String word, String translation, String context, Dictionary dict) {
        Word w = new Word();
        w.setTitle(word);
        w.setContext(context);
        w.setTranslation(translation);
        w.setDictionary(dict);
        w.setCreated(new Date());

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
}
