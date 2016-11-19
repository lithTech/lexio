package lt.ru.lexio.ui.words;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuAdapter;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.ui.EventListenerManager;
import lt.ru.lexio.ui.GeneralCallback;
import lt.ru.lexio.fetcher.IPAEngFetcher;
import lt.ru.lexio.fetcher.MSTranslator;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.DialogHelper;
import lt.ru.lexio.util.AbbyyLingvoURL;
import lt.ru.lexio.util.ClipboardHelper;

/**
 * Created by lithTech on 21.03.2016.
 */
public class WordFragment extends ContentFragment implements TextWatcher, View.OnClickListener, SwipeMenuListView.OnMenuItemClickListener {

    WordDAO wordDAO = null;
    SwipeMenuListView lWords = null;
    EditText edFilter = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        wordDAO = new WordDAO(view.getContext());

        lWords = (SwipeMenuListView) view.findViewById(R.id.lvWords);
        lWords.setAdapter(initAdapter(view.getContext()));

        edFilter = (EditText) view.findViewById(R.id.edWordsFilter);
        edFilter.addTextChangedListener(this);

        ImageButton bCancelFilter = (ImageButton) view.findViewById(R.id.bWordsFilterClear);
        bCancelFilter.setOnClickListener(this);

        lWords.setLongClickable(true);
        registerForContextMenu(lWords);

        SwipeMenuCreator swypeMenuCreator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem add = new SwipeMenuItem(getView().getContext());
                add.setIcon(R.drawable.ic_menu_context_word_edit);
                add.setTitle(R.string.word_Add);
                add.setId(R.id.action_word_edit);
                add.setTitleColor(R.color.colorPrimaryDark);
                add.setWidth(dp2px(64));


                SwipeMenuItem del = new SwipeMenuItem(getView().getContext());
                del.setTitle(R.string.words_Deletion);
                del.setWidth(dp2px(64));
                del.setId(R.id.action_word_del);
                del.setTitleColor(R.color.colorPrimaryDark);
                del.setIcon(R.drawable.ic_menu_context_word_delete);

                menu.addMenuItem(add);
                menu.addMenuItem(del);
            }
        };

        lWords.setMenuCreator(swypeMenuCreator);
        lWords.setOnMenuItemClickListener(this);

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_content_words, menu);
    }

    private SimpleCursorAdapter initAdapter(Context context) {
        final long dictId;
        if (mainActivity != null && mainActivity.getCurrentDictionary() != null)
            dictId = mainActivity.getCurrentDictionary().id;
        else dictId = 0;

        WordListAdapter adapter = new WordListAdapter(context, R.layout.content_word_item,
                wordDAO.getAll(dictId),
                new String[]{Db.Common.TITLE, Db.Word.TRANSLATION, Db.Word.TRANSCRIPTION},
                new int[]{R.id.tvWord, R.id.tvTranslation, R.id.tvTranscription});

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return wordDAO.getAllFiltered(dictId, constraint.toString().toUpperCase() + "%");
            }
        });

        return adapter;
    }

    //called from context menu items
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        getAdapter().getSelectedWords().clear();
        getAdapter().getSelectedWords().add(info.position);
        return onOptionsItemSelected(item);
    }

    //called from main menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        chooseAction(item.getItemId(), getAdapter().getSelectedWords());

        return super.onOptionsItemSelected(item);
    }

    private void chooseAction(int id, Collection<Integer> positions) {
        if (id == R.id.action_word_add) {
            createWord(getActivity(), mainActivity.getCurrentDictionary());
        }
        else if (id == R.id.action_word_del) {
            deleteWords(getActivity(), positions);
        }
        else if (id == R.id.action_word_edit) {
            editWord(getActivity(), mainActivity.getCurrentDictionary(), positions.iterator().next());
        }
    }

    private void editWord(Activity activity, Dictionary currentDictionary, Integer position) {
        Word word = wordDAO.read(lWords.getItemIdAtPosition(position));
        saveWord(activity, currentDictionary, word);
    }

    private void deleteWords(final Activity activity, final Collection<Integer> positions) {
        if (!positions.isEmpty()) {
            DialogHelper.confirm(activity,
                    getResources().getString(R.string.words_Deletion),
                    getResources().getString(R.string.words_Delete_Alert),
                    getResources().getString(R.string.dialog_Cancel),
                    getResources().getString(R.string.dialog_Delete),
                    new Runnable() {
                        @Override
                        public void run() {
                            for (int pos : positions) {
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

    public void createWord(final Context creationWindowContext, final Dictionary dictionary) {
        saveWord(creationWindowContext, dictionary, new Word());
    }

    private void saveWord(final Context creationWindowContext, final Dictionary dictionary,
                          final Word word) {
        final boolean needRefresh = this.getView() != null;

        String clipBoardText = "";
        final ClipboardManager clipMgr = (ClipboardManager) creationWindowContext.getSystemService(Context.CLIPBOARD_SERVICE);
        /*
        if (ClipboardHelper.hasText(clipMgr)) {
            clipBoardText = clipMgr.getPrimaryClip().getItemAt(0).getText().toString();
        }*/

        LayoutInflater layoutInflater = LayoutInflater.from(creationWindowContext);
        final View promptView = layoutInflater.inflate(R.layout.dialog_add_word, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(creationWindowContext);
        alertDialogBuilder.setView(promptView);

        //fill the form data
        final EditText edTranslation = (EditText) promptView.findViewById(R.id.edTranslation);
        edTranslation.setText(word.getTranslation());
        final EditText edWord = (EditText) promptView.findViewById(R.id.edWord);
        if (word.getTitle() != null && !word.getTitle().isEmpty())
            edWord.setText(word.getTitle());
        else
            edWord.setText(clipBoardText);
        final EditText edContext = (EditText) promptView.findViewById(R.id.edContext);
        edContext.setText(word.getContext());

        //translate button event
        Button bTranslate = (Button) promptView.findViewById(R.id.bTranslate);
        final Button bTranslateLingvo = (Button) promptView.findViewById(R.id.bTranslateLingvo);
        bTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edWord.getText().toString().isEmpty()) {
                    final ProgressDialog progressDialog = ProgressDialog.show(promptView.getContext(),
                            creationWindowContext.getString(R.string.words_AddWord_Translation_ProgressTitle),
                            creationWindowContext.getString(R.string.words_AddWord_Translation_ProgressMessage));
                    MSTranslator translator = new MSTranslator(new GeneralCallback() {
                        @Override
                        public void done(Object data) {
                            progressDialog.dismiss();
                            if (data instanceof String && data != null && !((String)data).isEmpty() ) {
                                edTranslation.setText(data.toString());
                                edTranslation.requestFocus();
                            }
                            else
                                Toast.makeText(creationWindowContext,
                                        R.string.words_AddWord_CantloadTranslation,
                                        Toast.LENGTH_SHORT).show();
                        }
                    });
                    translator.execute(dictionary.getLanguageTag(), Locale.getDefault().getLanguage(),
                            edWord.getText().toString());
                }
            }
        })
        ;
        //open in lingvo button event
        bTranslateLingvo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tell that lingvo button was pressed
                bTranslateLingvo.setTag(Boolean.TRUE);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(AbbyyLingvoURL.getUrl(dictionary.getLanguageTag(),
                                edWord.getText().toString())));
                creationWindowContext.startActivity(browserIntent);
            }
        });

        //event when user returns to our application. If user returns from lingvo, trying to paste from clipboard into the translation field
        mainActivity.getEventListenerManager().register(EventListenerManager.EVENT_TYPE_RESUME, "CreateWordDialog",
                new GeneralCallback() {
                    @Override
                    public void done(Object data) {
                        String word = edWord.getText().toString();
                        if (bTranslateLingvo.getTag() != null &&
                                (Boolean) bTranslateLingvo.getTag() &&
                                !word.isEmpty()) {
                            String ct = "";
                            bTranslateLingvo.setTag(Boolean.FALSE);
                            if (ClipboardHelper.hasText(clipMgr)) {
                                ct = clipMgr.getPrimaryClip().getItemAt(0).getText().toString();
                                if (ct != null && !ct.isEmpty() && !ct.equalsIgnoreCase(word))
                                {
                                    edTranslation.setText(ct);
                                }
                            }
                        }
                    }
                });

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.dialog_Save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveWordObject(creationWindowContext, word.id, edWord.getText().toString(),
                                edTranslation.getText().toString(),
                                edContext.getText().toString(), dictionary);

                        IPAEngFetcher ipaEngFetcher = new IPAEngFetcher(wordDAO);
                        ipaEngFetcher.execute(wordDAO.getWordsWithoutIPA(dictionary.id));

                        if (needRefresh)
                            refreshList();

                        Toast.makeText(creationWindowContext, edWord.getText() + " " +
                                creationWindowContext.getString(R.string.Word_WordSavedMessage),
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

        //Apply button on keyboard when translation field is active
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

        //we need to destroy event, registered previously
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mainActivity.getEventListenerManager().unregister(EventListenerManager.EVENT_TYPE_RESUME,
                        "CreateWordDialog");
                mainActivity.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );
            }
        });

        alert.show();
        //we have some pasted text into word. Activate translation field now
        if (edWord.getText().length() > 0)
            edTranslation.requestFocus();
    }

    private Word saveWordObject(Context daoContext,
                                long id,
                                String word, String translation,
                                String context, Dictionary dict) {
        if (word == null || word.isEmpty() || translation == null || translation.isEmpty())
            return null;

        word = word.toLowerCase().trim();
        translation = translation.toLowerCase().trim();

        if (context != null && !context.isEmpty())
            context = context.trim();

        Word w = new Word();
        boolean isUpdate = id > 0;

        w.setTitle(word);
        w.setContext(context);
        w.setTranslation(translation);
        w.setDictionary(dict);
        w.setCreated(new Date());

        if (wordDAO == null)
            wordDAO = new WordDAO(daoContext);

        if (!isUpdate)
            wordDAO.create(w);
        else {
            w.id = id;
            w.setLastModified(new Date());
            wordDAO.update(w);
        }
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
        getAdapter().getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public WordListAdapter getAdapter() {
        SwipeMenuAdapter wrAdapter = (SwipeMenuAdapter) lWords.getAdapter();
        return (WordListAdapter) wrAdapter.getWrappedAdapter();
    }   

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bWordsFilterClear) {
            getAdapter().getFilter().filter("");
            ((EditText) ((View) v.getParent()).findViewById(R.id.edWordsFilter)).setText("");
        }
    }

    /**
     * For the swype menu items
     * @param position
     * @param menu
     * @param index
     * @return
     */
    @Override
    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
        List<Integer> pos = new ArrayList<>(1);
        pos.add(position);

        chooseAction(menu.getMenuItem(index).getId(), pos);

        return true;
    }
}
