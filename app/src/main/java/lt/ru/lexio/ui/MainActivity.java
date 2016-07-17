package lt.ru.lexio.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.DictionaryDAO;
import lt.ru.lexio.ui.charts.HardWordsFragment;
import lt.ru.lexio.ui.dictionary.DictionariesFragment;
import lt.ru.lexio.ui.training.TrainingCards;
import lt.ru.lexio.ui.training.TrainingEnterWordByTransFragment;
import lt.ru.lexio.ui.training.TrainingManager;
import lt.ru.lexio.ui.training.TrainingTranslationVoice;
import lt.ru.lexio.ui.training.TranslationWordTrainingFragment;
import lt.ru.lexio.ui.training.WordTranslationTrainingFragment;
import lt.ru.lexio.ui.words.WordFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    Dictionary currentDictionary = null;
    Menu navMenu = null;
    ContentFragment currentFragment = null;
    WordFragment wordFragment = null;

    public Dictionary getCurrentDictionary() {
        return currentDictionary;
    }

    public void setCurrentDictionary(Dictionary currentDictionary) {
        this.currentDictionary = currentDictionary;
        if (navMenu != null) {
            MenuItem menuItem = navMenu.findItem(R.id.dict_dictionary);
            menuItem.setTitle(currentDictionary.getTitle());
        }
    }

    private void initUI(Menu navMenu) {
        this.navMenu = navMenu;
        selectContent(R.id.dict_dictionaries);

        //init current dictionary
        DictionaryDAO dictionaryDAO = new DictionaryDAO(this);
        Cursor cursor = dictionaryDAO.getActive().execute();
        if (cursor.moveToNext()) {
            setCurrentDictionary(dictionaryDAO.read(cursor.getLong(cursor.getColumnIndex(Db.Common.ID))));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initUI(navigationView.getMenu());

        FloatingActionButton btnWordAdd = (FloatingActionButton) findViewById(R.id.word_add_global);
        btnWordAdd.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_activity_main_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        return selectContent(id);
    }

    private boolean selectContent(int id) {
        ContentFragment fragment = null;
        Bundle args = new Bundle();
        String title = null;
        //select the proper content layout to load into the fragment
        if (id == R.id.dict_dictionaries) {
            args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, R.layout.content_dictionaries);
            args.putInt(ContentFragment.ARG_ACTION_MENU_ID, R.menu.menu_content_dictionaries);
            title = getResources().getString(R.string.nav_MyDictionaries);
            fragment = new DictionariesFragment();
        }
        else if (id == R.id.nav_settings) {
            args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, R.layout.content_settings);
            title = getResources().getString(R.string.nav_Settings);
            fragment = new SettingsFragment();
        }
        else if (id == R.id.stat_hard_words) {
            args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, R.layout.content_graph_hard);
            title = getResources().getString(R.string.stat_HardWords);
            fragment = new HardWordsFragment();
        }
        else if (id == R.id.stat_train_words_by_day) {
            //TODO implement chart
        }
        else if (id == R.id.training_cards) {
            args.putInt(ContentFragment.ARG_TRAINING_TO_RUN, R.layout.content_training_cards);
            args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, R.layout.content_training_start);
            args.putInt(ContentFragment.ARG_TRAINING_TO_RUN_TITLE, R.string.train_Cards);
            title = getResources().getString(R.string.training_Start);
            fragment = new TrainingManager();
        }
        else if (id == R.id.training_trans_audio) {
            args.putInt(ContentFragment.ARG_TRAINING_TO_RUN, R.layout.content_training_trans_voice);
            args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, R.layout.content_training_start);
            args.putInt(ContentFragment.ARG_TRAINING_TO_RUN_TITLE, R.string.train_TransVoice);
            title = getResources().getString(R.string.training_Start);
            fragment = new TrainingManager();
        }
        else if (id == R.id.training_trans_word) {
            args.putInt(ContentFragment.ARG_TRAINING_TO_RUN, R.layout.content_training_trans_word);
            args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, R.layout.content_training_start);
            args.putInt(ContentFragment.ARG_TRAINING_TO_RUN_TITLE, R.string.train_TransWord);
            title = getResources().getString(R.string.training_Start);
            fragment = new TrainingManager();
        }
        else if (id == R.id.training_word_audio) {
            //TODO Implament traingnin word audio
        }
        else if (id == R.id.training_word_trans) {
            args.putInt(ContentFragment.ARG_TRAINING_TO_RUN, R.layout.content_training_word_trans);
            args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, R.layout.content_training_start);
            args.putInt(ContentFragment.ARG_TRAINING_TO_RUN_TITLE, R.string.train_WordTrans);
            title = getResources().getString(R.string.training_Start);
            fragment = new TrainingManager();
        } else if (id == R.id.training_trans_write) {
            args.putInt(ContentFragment.ARG_TRAINING_TO_RUN, R.layout.content_training_enter_word);
            args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, R.layout.content_training_start);
            args.putInt(ContentFragment.ARG_TRAINING_TO_RUN_TITLE, R.string.train_TransWrite);
            title = getResources().getString(R.string.training_Start);
            fragment = new TrainingManager();
        } else if (id == R.id.dict_dictionary) {
            title = getResources().getString(R.string.nav_MyDictionary);
            if (currentDictionary != null)
                title = currentDictionary.getTitle();

            fragment = createWordFragment(args);
        }

        //if we selected the content, load it into fragment
        if (args.size() > 0 && currentFragment != fragment) {
            //need to remove all old views from parent container
            changeFragment(fragment, args, title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void changeFragment(ContentFragment fragment, Bundle args, String title) {
        FragmentManager fragmentManager = getFragmentManager();
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.content_fragment_parent);
        viewGroup.removeAllViews();
        if (fragment == null)
            fragment = new ContentFragment();
        fragment.setArguments(args);
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.content_fragment_parent, fragment)
                .commit();
        currentFragment = fragment;
        if (title != null)
            setTitle(getResources().getString(R.string.app_name) + ": " + title);
    }

    @NonNull
    private ContentFragment createWordFragment(Bundle outArgs) {
        ContentFragment fragment;
        outArgs.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, R.layout.content_words);
        outArgs.putInt(ContentFragment.ARG_ACTION_MENU_ID, R.menu.menu_content_words);

        fragment = wordFragment;
        if (wordFragment == null) {
            fragment = new WordFragment();
            wordFragment = (WordFragment) fragment;
        }
        else
            outArgs.putBoolean(ContentFragment.ARG_NEED_REFRESH, true);
        return fragment;
    }

    /**
     * When word_add_global clicked
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (wordFragment == null) {
            createWordFragment(new Bundle(3));
        }

        wordFragment.createWord(this, currentDictionary);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getApplicationContext(), "RC: " + resultCode,
                Toast.LENGTH_LONG);
    }
}
