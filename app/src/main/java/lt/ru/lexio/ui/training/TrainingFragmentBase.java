package lt.ru.lexio.ui.training;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.persist.sql.stmt.Where;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatistic;
import lt.ru.lexio.db.WordStatisticDAO;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.GeneralCallback;
import lt.ru.lexio.ui.MainActivity;
import lt.ru.lexio.ui.dictionary.DictionaryChooser;
import lt.ru.lexio.util.AdvertiseHelper;
import lt.ru.lexio.util.TutorialHelper;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.target.Target;
import uk.co.deanwild.materialshowcaseview.target.ViewTarget;

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
    protected WordStatisticDAO wordStatisticDAO = null;
    protected WordDAO wordDAO = null;
    protected TrainingWordBuilder trainingWordBuilder = null;
    protected List<Word> sessionWords = null;
    protected Map<Long, WordStatistic> sessionStatistics = null;
    protected Random random = new Random(System.nanoTime());
    protected int currentPage = 0;
    protected Date sessionDate = new Date();
    protected static volatile TextToSpeech tts = null;

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

    protected abstract int getTrainingPageContainerId();

    protected abstract int getEndPageContainerId();

    protected abstract int getProgressBarId();

    public Dictionary getCurrentDictionary() {
        Dictionary dictionary = null;
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getCurrentDictionary();
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

        int correct = 0;
        for (WordStatistic statistic : sessionStatistics.values()) {
            if (statistic.getTrainingResult() == 1)
                correct++;
        }

        setEndPageStatistics(correct, sessionStatistics.size() - correct);
        endPageContainer.setVisibility(View.VISIBLE);

        presentTutorial();

        //replace trainig menu with global action menu
        getActivity().invalidateOptionsMenu();
    }

    protected abstract void setEndPageStatistics(int correct,
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
        if (animateQuestionExit && trainingPageContainer.getVisibility() == View.VISIBLE) {
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

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    protected void textToSpeech(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(text);
        } else {
            ttsUnder20(text);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem speech = menu.findItem(R.id.action_word_speech);
        if (speech != null)
            speech.setVisible(isSpeechEnabled());
    }

    protected abstract boolean isSpeechEnabled();

    @Override
    public void onStart() {
        super.onStart();

        ViewGroup adView = (ViewGroup) getView().findViewById(R.id.adView);
        if (adView != null) {
            adView.setVisibility(View.GONE);
            AdvertiseHelper.cancelShow();
        }

        currentQuestionNum = -1;
        trainingWordBuilder.dictId = getCurrentDictionary().id;
        currentPage++;

        if (getArguments().containsKey(ContentFragment.ARG_TRAINING_TO_RUN_PAGE))
            currentPage = getArguments().getInt(ContentFragment.ARG_TRAINING_TO_RUN_PAGE);

        long[] startWordList = getArguments().getLongArray(ContentFragment.ARG_TRAINING_START_LIST);
        getArguments().remove(ContentFragment.ARG_TRAINING_START_LIST);
        getArguments().remove(ContentFragment.ARG_TRAINING_TO_RUN_PAGE);
        if (startWordList == null) {
            sessionWords = buildWords(random, sessionDate, currentPage, wordDAO, wordStatisticDAO);
        } else {
            sessionWords = new ArrayList<>(startWordList.length);
            for (long l : startWordList) {
                sessionWords.add(wordDAO.read(l));
            }
        }
        sessionStatistics = new HashMap<>(sessionWords.size());

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

        registerForContextMenu(getView());
        getActivity().invalidateOptionsMenu();

        startTraining();
        nextQuestionInternal(false);
    }

    protected List<EndPageStatistic> getEndPageStatistic(EndPageStatisticFiller filler) {
        List<EndPageStatistic> statistics = new ArrayList<>(sessionWords.size());
        for (Word w : sessionWords) {
            WordStatistic storedStat = sessionStatistics.get(w.id);
            Boolean result = null;
            if (storedStat != null)
                result = storedStat.getTrainingResult() == 1;
            EndPageStatistic s = new EndPageStatistic(w.id, null, null, result);

            filler.fill(w, storedStat, s);
            statistics.add(s);
        }
        return statistics;
    }

    protected long storeStatistic(long wordId, boolean isSuccess, long sessionId) {
        if (getTrainingType() == null)
            return 0;
        WordStatistic statistic = sessionStatistics.get(wordId);
        int result = isSuccess ? 1 : 0;
        if (statistic == null) {
            statistic = new WordStatistic();
            sessionStatistics.put(wordId, statistic);
            Word wordDummy = new Word();
            wordDummy.id = wordId;
            statistic.setWord(wordDummy);
            if (sessionId > 0)
                statistic.setSessionId(sessionId);
            statistic.setTrainedOn(new Date());
            statistic.setTrainingResult(result);
            statistic.setTrainingType(getTrainingType().ordinal());
        }
        if (statistic.id > 0) {
            if (statistic.getTrainingResult() != result) {
                statistic.setTrainingResult(result);
                wordStatisticDAO.update(statistic);
            }
        }
        else {
            wordStatisticDAO.create(statistic);
        }
        return sessionId > 0 ? sessionId : statistic.id;
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

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(new Locale(getCurrentDictionary().getLanguageTag()));
                tts.setPitch(1.0f);
                tts.setSpeechRate(0.8f);
            }
        });

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_word_speech) {
            if (currentWord != null)
                textToSpeech(currentWord.getTitle());
        }
        else if (item.getItemId() == R.id.action_word_move)
        {
            Word word = wordDAO.read(currentWord.id);
            String tag = word.getDictionary().getLanguage();
            Where where = new Where(Db.Dictionary.LANG, Is.EQUAL, tag)
                    .and(Db.Common.ID, Is.NOT_EQUAL, word.getDictionary().id);

            DictionaryChooser.showChooser(getActivity(), where, R.string.action_word_move,
                    new GeneralCallback() {
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
                    });
        }
        return true;
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
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

    private void presentTutorial() {
        View v = getView();
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);

        final View vEndPageNext = v.findViewById(R.id.bTrainingEndPageNext);
        final View vReloadMenu = v.findViewById(R.id.amTrainingMenu);
        final View vWordStatList = v.findViewById(R.id.lvTrainingEndPageWordStat);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "endPageStatTut");

        sequence.setConfig(config);

        sequence.addSequenceItem(TutorialHelper.defElem(getActivity(),
                R.string.tutorial_ep_summary, false, v.findViewById(R.id.cTrainingEndPageSummary))
                .setDelay(getResources().getInteger(R.integer.tutorial_delay))
                .build()
        );


        MaterialShowcaseView tmpView = TutorialHelper.defElem(getActivity(),
                R.string.tutorial_ep_list, true, vWordStatList)
                .build();
        sequence.addSequenceItem(tmpView);


        tmpView = TutorialHelper.defElem(getActivity(),
                R.string.tutorial_ep_reload_menu, false, vReloadMenu)
                .build();

        Target wordItemTarget = new ViewTarget(vEndPageNext) {
            @Override
            public Point getPoint() {
                int[] loc = new int[2];
                vEndPageNext.getLocationInWindow(loc);
                int[] cloc = new int[2];
                vReloadMenu.getLocationInWindow(cloc);
                int w = vEndPageNext.getMeasuredWidth();
                int h = vEndPageNext.getMeasuredHeight();

                return new Point(cloc[0] + w / 2, loc[1] + h / 2);
            }
        };
        tmpView.setTarget(wordItemTarget);
        sequence.addSequenceItem(tmpView);

        tmpView = TutorialHelper.defElem(getActivity(),
                R.string.tutorial_ep_next, false, vEndPageNext)
                .build();
        sequence.addSequenceItem(tmpView);

        sequence.start();
    }
}

interface EndPageStatisticFiller {
    void fill(final Word word, @Nullable WordStatistic storedStatistic, EndPageStatistic out);
}