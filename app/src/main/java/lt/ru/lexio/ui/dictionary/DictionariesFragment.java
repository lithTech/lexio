package lt.ru.lexio.ui.dictionary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuAdapter;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.droidparts.persist.sql.stmt.Is;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.DictionaryDAO;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.DialogHelper;
import lt.ru.lexio.ui.GeneralCallback;
import lt.ru.lexio.ui.MainActivity;
import lt.ru.lexio.ui.settings.SettingsFragment;
import lt.ru.lexio.util.AdvertiseHelper;
import lt.ru.lexio.util.TutorialHelper;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.target.Target;
import uk.co.deanwild.materialshowcaseview.target.ViewTarget;

/**
 * Created by User on 15.03.2016.
 */
public class DictionariesFragment extends ContentFragment implements SwipeMenuListView.OnMenuItemClickListener {

    SwipeMenuListView lDictionaries;

    DictionaryDAO dictionaryDAO;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
        List<Integer> positions = new ArrayList<>(1);
        positions.add(position);
        return chooseAction(menu.getMenuItem(index).getId(), positions);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        dictionaryDAO = new DictionaryDAO(view.getContext());

        SimpleCursorAdapter sca = initAdapter(view.getContext());

        lDictionaries = (SwipeMenuListView) view.findViewById(R.id.lDictionaries);
        lDictionaries.setAdapter(sca);

        SwipeMenuCreator swypeMenuCreator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem edit = new SwipeMenuItem(getView().getContext());
                edit.setIcon(R.drawable.ic_menu_context_dict_edit);
                edit.setTitle(R.string.action_word_edit);
                edit.setId(R.id.action_dictionaries_edit);
                edit.setTitleColor(R.color.colorPrimaryDark);
                edit.setWidth(dp2px(64));
                SwipeMenuItem del = new SwipeMenuItem(getView().getContext());
                del.setTitle(R.string.dictionaries_Delete);
                del.setWidth(dp2px(64));
                del.setId(R.id.action_dictionaries_del);
                del.setTitleColor(R.color.colorPrimaryDark);
                del.setIcon(R.drawable.ic_menu_context_object_delete);
                SwipeMenuItem act = new SwipeMenuItem(getView().getContext());
                act.setTitle(R.string.dictionaries_Active);
                act.setWidth(dp2px(64));
                act.setId(R.id.action_dictionaries_active);
                act.setTitleColor(R.color.colorPrimaryDark);
                act.setIcon(R.drawable.ic_menu_context_dict_flag);

                menu.addMenuItem(edit);
                menu.addMenuItem(act);
                menu.addMenuItem(del);
            }
        };

        lDictionaries.setMenuCreator(swypeMenuCreator);
        lDictionaries.setOnMenuItemClickListener(this);

        lDictionaries.setLongClickable(true);
        registerForContextMenu(lDictionaries);

        if (!AdvertiseHelper.isFlavorWithoutAds())
            AdvertiseHelper.loadAd(getActivity(),
                    (ViewGroup) view.findViewById(R.id.adView),
                    getString(R.string.content_dictionary_banner), null);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ProgressDialog progressDialog = ProgressDialog.show(getView().getContext(),
                getString(R.string.defdict_loading_title),
                getString(R.string.defdict_loading_msg));
        final int MAX_STD_DICT = 1;
        final AtomicInteger lc = new AtomicInteger(MAX_STD_DICT);

        GeneralCallback callback = new GeneralCallback() {
            @Override
            public void done(Object data) {
                if (data != null) {
                    Dictionary d = (Dictionary) data;
                    SharedPreferences pref = getActivity().getSharedPreferences(SettingsFragment.SETTINGS_FILE_NAME,
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor ed = pref.edit();
                    ed.putBoolean(d.getTitle(), true);
                    ed.commit();
                    refreshDictionaryList();
                }
                if (lc.decrementAndGet() == 0)
                    progressDialog.dismiss();
            }
        };

        startLoadingIfDoesntExist(getResources().getString(R.string.defdict_en_ru_medium),
                R.raw.dict_en_ru_medium, "English", callback);
    }

    private void startLoadingIfDoesntExist(String dictTitle, int rId, String lang,
                                                GeneralCallback callback) {
        SharedPreferences pref = getActivity().getSharedPreferences(SettingsFragment.SETTINGS_FILE_NAME,
                Context.MODE_PRIVATE);

        if (pref.contains(dictTitle)){
            if (callback != null)
                callback.done(null);
            return;
        }
        Cursor cursor = dictionaryDAO.select().columns(Db.Common.ID).where(Db.Common.TITLE,
                Is.EQUAL, dictTitle).execute();
        boolean hasDict = cursor.moveToNext();
        long dictId = 0;
        if (!hasDict) {
            dictId = saveDictionaryObject(0, dictTitle,
                    getResources().getString(R.string.defaultDictDescription),
                    lang);
            InputStream in = getResources().openRawResource(rId);
            executor.submit(
                    new DictLoader(new WordDAO(getActivity()),
                            new DictionaryDAO(getActivity()), in, dictId, callback));
        } else {
            dictId = cursor.getLong(cursor.getColumnIndex(Db.Common.ID));
            Dictionary d = dictionaryDAO.read(dictId);
            if (callback != null)
                callback.done(d);
        }
        cursor.close();
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
        (getAdapter()).getSelectedItems().clear();
        (getAdapter()).getSelectedItems().add(info.position);
        return onOptionsItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (chooseAction(id, getAdapter().getSelectedItems())) return true;

        return super.onOptionsItemSelected(item);
    }

    private boolean chooseAction(int id, Collection<Integer> positions) {
        if (id == R.id.action_dictionaries_add) {
            saveDictionary(0);
            return true;
        }
        else if (id == R.id.action_dictionaries_del) {
            deleteDictionaries(positions);
            return true;
        }
        else if (id == R.id.action_dictionaries_edit) {
            if (!positions.isEmpty()) {
                long dId = lDictionaries.getItemIdAtPosition(positions.iterator().next());
                saveDictionary(dId);
            }
            return true;
        }
        else if (id == R.id.action_dictionaries_active) {
            if (!positions.isEmpty()) {
                makeCurrent(positions.iterator().next());
                return true;
            }
        }
        return false;
    }

    private DictionariesListAdapter getAdapter() {
        SwipeMenuAdapter wrAdapter = ((SwipeMenuAdapter) lDictionaries.getAdapter());
        return (DictionariesListAdapter) wrAdapter.getWrappedAdapter();
    }

    private void makeCurrent(int position) {
        long dictId = lDictionaries.getItemIdAtPosition(position);
        setActiveDictionary(dictId);
        refreshDictionaryList();
    }

    private void setActiveDictionary(long dictId) {
        Dictionary cd = dictionaryDAO.setActive(dictId);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setCurrentDictionary(cd);
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

    private void deleteDictionaries(final Collection<Integer> positions) {
        if (!positions.isEmpty()) {
            DialogHelper.confirm(getActivity(),
                    getResources().getString(R.string.dictionaries_Deletion),
                    getResources().getString(R.string.dictionaries_Delete_Alert),
                    getResources().getString(R.string.dialog_Cancel),
                    getResources().getString(R.string.dialog_Delete),
                    new Runnable() {
                        @Override
                        public void run() {
                            for (int pos : positions) {
                                dictionaryDAO.delete(lDictionaries.getItemIdAtPosition(pos));
                                refreshDictionaryList();
                            }
                        }
                    }, null);
        }
    }

    private void saveDictionary(final long dictionaryId) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View promptView = layoutInflater.inflate(R.layout.dialog_add_dictionary, null);
        final EditText edTitle = (EditText) promptView.findViewById(R.id.edDictionaryTitle);
        final Spinner spLang = (Spinner) promptView.findViewById(R.id.spLanguages);

        int dialogOkTitle = R.string.dialog_Save;
        if (dictionaryId > 0)
        {
            List<String> tags = Arrays.asList(getResources().getStringArray(R.array.dictionary_languages));
            Dictionary d = dictionaryDAO.read(dictionaryId);
            edTitle.setText(d.getTitle());
            spLang.setSelection(tags.indexOf(d.getLanguage()));
        }

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(dialogOkTitle, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveDictionaryObject(dictionaryId, edTitle.getText().toString(), "",
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

    private long saveDictionaryObject(long dictId, String title, String desc, String lang) {
        int dictCnt = dictionaryDAO.select().count();
        Dictionary dictionary = new Dictionary();
        dictionary.setWords(0);
        dictionary.setTitle(title);
        dictionary.setDesc(desc);
        dictionary.setLanguage(lang);
        if (dictCnt == 0)
            dictionary.setActive(1);
        if (dictId > 0) {
            Dictionary old = dictionaryDAO.read(dictId);
            dictionary.id = dictId;
            dictionary.setWords(old.getWords());
            dictionary.setActive(old.getActive());
            dictionaryDAO.update(dictionary);
        }
        else {
            dictionaryDAO.create(dictionary);
        }

        if (dictCnt == 0)
            setActiveDictionary(dictionary.id);

        return dictionary.id;
    }

    @Override
    public void onStart() {
        super.onStart();

        presentTutorial();
    }


    private void presentTutorial() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "dictTut");

        sequence.setConfig(config);

        MaterialShowcaseView item = TutorialHelper.defElem(getActivity(),
                R.string.tutorial_dict_list, false, lDictionaries)
                .setDelay(getResources().getInteger(R.integer.tutorial_delay))
                .build();
        item.setTarget(new ViewTarget(lDictionaries){
            @Override
            public Point getPoint() {
                Point p = super.getPoint();
                p.offset(0, dp2px(40));
                return p;
            }

            @Override
            public Rect getBounds() {
                Rect r = super.getBounds();
                r.top += dp2px(80);
                return r;
            }
        });
        sequence.addSequenceItem(item);

        MaterialShowcaseView tmpView = TutorialHelper.defElem(getActivity(),
                R.string.tutorial_dict_list_item, true, lDictionaries)
                .build();
        Target wordItemTarget = new ViewTarget(lDictionaries) {
            @Override
            public Point getPoint() {
                int[] location = new int[2];
                lDictionaries.getLocationInWindow(location);
                return new Point(location[0] + lDictionaries.getMeasuredWidth() / 2,
                        location[1] + dp2px(40));
            }

            @Override
            public Rect getBounds() {
                Rect r = super.getBounds();
                r.bottom = r.top + dp2px(80);
                return r;
            }
        };
        tmpView.setTarget(wordItemTarget);
        sequence.addSequenceItem(tmpView);

        tmpView = TutorialHelper.defElem(getActivity(),
                R.string.tutorial_dict_list_item_2, false, lDictionaries)
                .build();
        tmpView.setTarget(wordItemTarget);
        sequence.addSequenceItem(tmpView);

        sequence.start();
    }
}
