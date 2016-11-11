package lt.ru.lexio.ui.training;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.persist.sql.stmt.Where;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.DictionaryDAO;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatistic;
import lt.ru.lexio.db.WordStatisticDAO;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.GeneralCallback;
import lt.ru.lexio.ui.MainActivity;
import lt.ru.lexio.ui.dictionary.DictionariesListAdapter;

/**
 * Created by lithTech on 27.03.2016.
 */
public abstract class TrainingFragmentBase extends ContentFragment {

    protected class QuestionExpireTimer extends CountDownTimer {

        TrainingFragmentBase training = null;
        boolean paused = false;

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

        public TrainingFragmentBase getTraining() {
            return training;
        }

        public void setTraining(TrainingFragmentBase training) {
            this.training = training;
        }

        public QuestionExpireTimer(long millisInFuture, TrainingFragmentBase training) {
            super(millisInFuture, 1000);
            if (millisInFuture > 0)         //use training == null flag to obtain, is there a timer at all
                this.training = training;
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (training != null && !paused)
                training.onQuestionTimeExpire();
        }

        public void run() {
            if (training != null) {                 //timer is not set by user, so do not do anything
                start();
            }
        }
    }

    protected QuestionExpireTimer onQuestionExpireTimer;
    private Word wordDummy = new Word();
    protected WordStatisticDAO wordStatisticDAO = null;
    protected WordDAO wordDAO = null;
    protected MainActivity activity = null;
    protected TrainingWordBuilder trainingWordBuilder = null;
    protected List<Word> sessionWords = null;
    protected Random random = new Random(System.nanoTime());
    protected int currentPage = 0;
    protected Date sessionDate = new Date();

    protected int wordCount;
    protected TrainingWordOrder wordOrder;

    Word currentWord = null;
    long currentSessionId = 0;
    protected int currentQuestionNum = 0;

    private View trainingPageContainer = null;
    private View endPageContainer = null;
    private ProgressBar progressBar;

    Animation aniNextCloseLastQuestion;
    Animation aniNextStartNewQuestion;

    Animation aniPrevCloseLastQuestion;
    Animation aniPrevStartNewQuestion;
    private int answerTime;

    public MainActivity getMainActivity() {

        return activity;
    }

    protected abstract int getTrainingPageContainerId();

    protected abstract int getEndPageContainerId();

    protected abstract int getProgressBarId();

    public Dictionary getCurrentDictionary() {
        Dictionary dictionary = null;
        if (activity != null) {
            return activity.getCurrentDictionary();
        }
        else {
            dictionary = new Dictionary();
            dictionary.id = 0;
        }
        return dictionary;
    }

    protected abstract void onQuestionTimeExpire();

    protected abstract void startTraining();

    protected abstract void onNextQuestion();

    private void exitTraining() {
        trainingPageContainer.setVisibility(View.GONE);
        trainingPageContainer.invalidate();

        Cursor statCur = wordStatisticDAO.getTrainStatisticsBySession(currentSessionId);
        int correct = 0;
        int total = 0;
        List<WordStatistic> wordStatistics = new ArrayList<>();
        while (statCur.moveToNext()) {
            WordStatistic wordStatistic = wordStatisticDAO.readRow(statCur);
            wordStatisticDAO.fillForeignKeys(wordStatistic, Db.WordStatistic.WORD_ID);
            total++;
            if (wordStatistic.getTrainingResult() == 1)
                correct++;
            wordStatistics.add(wordStatistic);
        }

        setEndPageStatistics(wordStatistics, correct, total - correct);
        endPageContainer.setVisibility(View.VISIBLE);

        //replace trainig menu with global action menu
        getActivity().invalidateOptionsMenu();
    }

    protected abstract void setEndPageStatistics(List<WordStatistic> wordStatistics, int correct,
                                        int incorrect);

    protected void nextQuestion(Boolean isLastQuestionCorrect) {
        if (currentWord != null && isLastQuestionCorrect != null)
            currentSessionId = storeStatistic(currentWord.id, isLastQuestionCorrect, currentSessionId);
        nextQuestionInternal(true);
    }

    protected void prevQuestion(Boolean isLastQuestionCorrect) {
        if (currentWord != null && isLastQuestionCorrect != null)
            currentSessionId = storeStatistic(currentWord.id, isLastQuestionCorrect, currentSessionId);
        prevQuestionInternal(true);
    }

    private void nextQuestionInternal(boolean animateQuestionExit) {
        //exit from old question is not yet animated
        if (animateQuestionExit) {
            trainingPageContainer.startAnimation(aniNextCloseLastQuestion);
        }
        //animation on end question is ended, present new question
        else {
            if (currentQuestionNum + 1 == sessionWords.size()) {
                exitTraining();
                return;
            }
            currentQuestionNum++;
            currentWord = sessionWords.get(currentQuestionNum);
            onNextQuestion();
            progressBar.setProgress(currentQuestionNum);
            trainingPageContainer.startAnimation(aniNextStartNewQuestion);
        }
    }

    private void prevQuestionInternal(boolean animateQuestionExit) {
        //exit from old question is not yet animated
        if (animateQuestionExit && currentQuestionNum > 0) {
            trainingPageContainer.startAnimation(aniPrevCloseLastQuestion);
        }
        //animation on end question is ended, present new question
        else {
            if (currentQuestionNum == 0) {
                return;
            }
            currentQuestionNum--;
            currentWord = sessionWords.get(currentQuestionNum);
            onNextQuestion();
            progressBar.setProgress(currentQuestionNum);
            trainingPageContainer.startAnimation(aniPrevStartNewQuestion);
        }
    }

    protected abstract List<Word> buildWords(Random random,
                                             Date sessionDate,
                                             int currentPage,
                                             WordDAO wordDAO,
                                             WordStatisticDAO wordStatisticDAO);

    @Override
    public void onStart() {
        super.onStart();
        currentQuestionNum = -1;
        trainingWordBuilder.dictId = getCurrentDictionary().id;
        currentPage++;

        long[] startWordList = getArguments().getLongArray(ContentFragment.ARG_TRAINING_START_LIST);
        if (startWordList == null) {
            sessionWords = buildWords(random, sessionDate, currentPage, wordDAO, wordStatisticDAO);
        } else {
            sessionWords = new ArrayList<>(startWordList.length);
            for (long l : startWordList) {
                sessionWords.add(wordDAO.read(l));
            }
        }

        if (sessionWords.isEmpty()) {
            Toast.makeText(getView().getContext(), getResources().getString(R.string.training_Word_Empty),
                    Toast.LENGTH_SHORT).show();
            currentPage--;
            return;
        }
        endPageContainer.setVisibility(View.GONE);
        trainingPageContainer.setVisibility(View.VISIBLE);
        progressBar.setMax(sessionWords.size()-1);
        progressBar.setProgress(0);

        currentSessionId = 0;

        startTraining();
        nextQuestionInternal(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            this.activity = (MainActivity) activity;
        }
    }

    protected long storeStatistic(long wordId, boolean isSuccess, long sessionId) {
        if (getTrainingType() == null)
            return 0;
        WordStatistic statistic = new WordStatistic();
        wordDummy.id = wordId;
        statistic.setWord(wordDummy);
        if (sessionId > 0)
            statistic.setSessionId(sessionId);
        statistic.setTrainedOn(new Date());
        statistic.setTrainingResult(isSuccess ? 1 : 0);
        statistic.setTrainingType(getTrainingType().ordinal());

        wordStatisticDAO.create(statistic);
        if (sessionId > 0)
            return sessionId;
        return statistic.id;
    }

    protected abstract TrainingType getTrainingType();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        trainingPageContainer = view.findViewById(getTrainingPageContainerId());
        endPageContainer = view.findViewById(getEndPageContainerId());

        wordStatisticDAO = new WordStatisticDAO(view.getContext());
        wordDAO = new WordDAO(view.getContext());
        trainingWordBuilder = new TrainingWordBuilder(wordDAO, 0);

        //we are using 2 animations: the first for the old question gone, the second is for the new question arrive
        aniNextCloseLastQuestion = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.anim_word_translation_nextcloseold);
        aniNextCloseLastQuestion.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                nextQuestionInternal(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        aniNextStartNewQuestion = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.anim_word_translation_nextopennew);
        aniNextStartNewQuestion.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onQuestionExpireTimer.run();
                onQuestionShow();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        aniPrevCloseLastQuestion = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.anim_word_translation_prevcloseold);
        aniPrevCloseLastQuestion.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                prevQuestionInternal(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        aniPrevStartNewQuestion = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.anim_word_translation_prevopennew);
        aniPrevStartNewQuestion.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onQuestionExpireTimer.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.trainingProgress);

        wordCount = getArguments().getInt(ContentFragment.ARG_TRAINING_WORD_COUNT);
        wordOrder = TrainingWordOrder.values()[getArguments().getInt(ContentFragment.ARG_TRAINING_WORD_ORDER)];
        answerTime = getArguments().getInt(ContentFragment.ARG_TRAINING_ANSWER_TIMER);

        onQuestionExpireTimer = new QuestionExpireTimer(answerTime * 1000, this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int r = R.menu.menu_content_training;
        if (trainingPageContainer != null && trainingPageContainer.getVisibility() == View.GONE)
            r = R.menu.menu_activity_main_actionbar;
        inflater.inflate(r, menu);
    }

    protected void onQuestionShow() {

    }

    private void chooseDictionary(final GeneralCallback callback, int resIdActionButton) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View promptView = layoutInflater.inflate(R.layout.dialog_choose_dict, null);

        final DictionaryDAO dDAO = new DictionaryDAO(getView().getContext());

        Word word = wordDAO.read(currentWord.id);
        String tag = word.getDictionary().getLanguage();
        Where where = new Where(Db.Dictionary.LANG, Is.EQUAL, tag).and(Db.Common.ID, Is.NOT_EQUAL, word.getDictionary().id);
        final List<Dictionary> dictionaries = dDAO.readAll(dDAO.getAll().where(where));

        if (dictionaries.isEmpty()) {
            Toast.makeText(getView().getContext(),
                    getResources().getString(R.string.msg_action_word_nodict),
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

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);

        // setup a dialog window
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton(resIdActionButton, new DialogInterface.OnClickListener() {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_word_copy) {
            chooseDictionary(new GeneralCallback() {
                @Override
                public void done(Object data) {
                    //TODO implement word copying sometime
                }
            }, R.string.action_word_copy);
        }
        else if (item.getItemId() == R.id.action_word_move)
        {
            chooseDictionary(new GeneralCallback() {
                @Override
                public void done(Object data) {
                    Dictionary dictionary = (Dictionary) data;
                    wordDAO.moveWord(dictionary.id, currentWord.id);
                    Toast.makeText(getView().getContext(),
                            currentWord.getTitle() + " " +
                                    getResources().getString(R.string.msg_action_word_move_success) +
                                    " " + dictionary.getTitle(),
                            Toast.LENGTH_LONG).show();
                }
            }, R.string.action_word_move);
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        onQuestionExpireTimer.setPaused(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        onQuestionExpireTimer.setPaused(false);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
