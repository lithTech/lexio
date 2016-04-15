package lt.ru.lexio.ui.training;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatistic;
import lt.ru.lexio.db.WordStatisticDAO;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.MainActivity;

/**
 * Created by lithTech on 27.03.2016.
 */
public abstract class TrainingFragmentBase extends ContentFragment {

    private Word wordDummy = new Word();
    protected WordStatisticDAO wordStatisticDAO = null;
    protected WordDAO wordDAO = null;
    protected MainActivity activity = null;
    protected TrainingWordBuilder trainingWordBuilder = null;
    protected List<Word> sessionWords = null;
    protected Random random = new Random(System.nanoTime());

    Word currentWord = null;
    long currentSessionId = 0;
    protected int currentQuestionNum = 0;

    private View trainingPageContainer = null;
    private View endPageContainer = null;

    Animation aniCloseLastQuestion;
    Animation aniStartNewQuestion;

    protected abstract int getTrainingPageContainerId();

    protected abstract int getEndPageContainerId();

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
            total++;
            if (wordStatistic.getTrainingResult() == 1)
                correct++;
            wordStatistics.add(wordStatistic);
        }

        setEndPageStatistics(wordStatistics, correct, total - correct);
        endPageContainer.setVisibility(View.VISIBLE);
    }

    protected abstract void setEndPageStatistics(List<WordStatistic> wordStatistics, int correct,
                                        int incorrect);

    protected void nextQuestion(boolean isLastQuestionCorrect) {
        if (currentWord != null)
            currentSessionId = storeStatistic(currentWord.id, isLastQuestionCorrect, currentSessionId);
        nextQuestionInternal(true);
    }

    private void nextQuestionInternal(boolean animateQuestionExit) {
        //exit from old question is not yet animated
        if (animateQuestionExit) {
            trainingPageContainer.startAnimation(aniCloseLastQuestion);
        }
        //animation on end question is ended, present new question
        else {
            if (currentQuestionNum >= sessionWords.size()) {
                exitTraining();
                return;
            }
            currentWord = sessionWords.get(currentQuestionNum);
            onNextQuestion();
            currentQuestionNum++;
            trainingPageContainer.startAnimation(aniStartNewQuestion);
        }
    }

    protected abstract List<Word> buildWords(Random random, WordDAO wordDAO, WordStatisticDAO wordStatisticDAO);

        @Override
    public void onStart() {
        super.onStart();
        trainingWordBuilder.dictId = getCurrentDictionary().id;
        currentQuestionNum = 0;
        trainingWordBuilder.dictId = getCurrentDictionary().id;
        sessionWords = buildWords(random, wordDAO, wordStatisticDAO);
        endPageContainer.setVisibility(View.GONE);
        trainingPageContainer.setVisibility(View.VISIBLE);
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

        aniCloseLastQuestion = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.anim_word_translation_closeold);
        aniCloseLastQuestion.setAnimationListener(new Animation.AnimationListener() {
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
        aniStartNewQuestion = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.anim_word_translation_startnew);

        return view;
    }
}
