package lt.ru.lexio.ui.training;

import android.animation.Animator;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatistic;
import lt.ru.lexio.db.WordStatisticDAO;
import lt.ru.lexio.util.ColorAnimateHelper;

/**
 * Created by lithTech on 27.03.2016.
 */
public abstract class TrainingAnswerOptionsFragment extends TrainingFragmentBase implements View.OnClickListener {

    Word currentWord = null;
    long currentSessionId = 0;
    protected int currentQuestionNum = 0;
    protected List<Word> sessionWords = null;
    protected List<String> sessionAnswers = null;
    protected Random random = new Random(System.nanoTime());

    Animation aniCloseLastQuestion;
    Animation aniStartNewQuestion;

    Button[] bAnsArray;
    private Button bDontKnow;

    private View trainingPageContainer = null;
    private View endPageContainer = null;

    private Drawable initialButtonBkg;


    protected abstract int getTrainingPageContainerId();

    protected abstract int getEndPageContainerId();

    protected abstract int[] getButtonAnswersId();

    protected abstract int getDontKnowButtonAnswerId();

    protected void onAnswer(boolean isCorrect, final Button clickedButton) {
        currentSessionId = storeStatistic(currentWord.id, isCorrect, currentSessionId);
        final Button correct = findCorrectAnswerButton();
        Animator.AnimatorListener onEndAnimation = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                clickedButton.setBackground(initialButtonBkg);
                correct.setBackground(initialButtonBkg);
                nextQuestion(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        if (!isCorrect) {
            ColorAnimateHelper.animateBetweenColors(clickedButton,
                    android.R.drawable.btn_default,
                    getResources().getColor(R.color.colorMandatory),
                    getResources().getInteger(R.integer.animation_incorrect_answer), onEndAnimation);
            onEndAnimation = null;      //animation of wrong answer is longer, so put onEndAnimation event there
        }
        ColorAnimateHelper.animateBetweenColors(correct,
                android.R.drawable.btn_default,
                getResources().getColor(R.color.correctAnswer),
                getResources().getInteger(R.integer.animation_correct_answer), onEndAnimation);
    }

    protected abstract List<String> buildAnswers(Random random, WordDAO wordDAO, WordStatisticDAO wordStatisticDAO);

    protected abstract List<Word> buildWords(Random random, WordDAO wordDAO, WordStatisticDAO wordStatisticDAO);

    protected void startTraining() {
        currentQuestionNum = 0;
        trainingWordBuilder.dictId = getCurrentDictionary().id;
        sessionWords = buildWords(random, wordDAO, wordStatisticDAO);
        sessionAnswers = buildAnswers(random, wordDAO, wordStatisticDAO);

        endPageContainer.setVisibility(View.GONE);
        trainingPageContainer.setVisibility(View.VISIBLE);

        nextQuestion(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        startTraining();
    }

    protected abstract String getCorrectAnswer(Word word);

    private List<String> buildQuestionAnswers(String correctAnswer) {
        int s = sessionAnswers.size();
        List<String> answers = new ArrayList<>();
        int i = 0;
        answers.add(correctAnswer);
        while (i < 3) {
            String ans = sessionAnswers.get(random.nextInt(s));
            if (answers.contains(ans))
                continue;
            answers.add(ans);
            i++;
        }

        Collections.shuffle(answers, random);
        return answers;
    }


    private void nextQuestion(boolean animateQuestionExit) {
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
            String correctAnswer = getCorrectAnswer(currentWord);
            List<String> answers = buildQuestionAnswers(correctAnswer);
            int correctNum = answers.indexOf(correctAnswer);

            setQuestionToUI(currentWord, answers, correctNum);

            currentQuestionNum++;

            toggleEnabledControls(true);
            trainingPageContainer.startAnimation(aniStartNewQuestion);
        }
    }

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

    protected void setEndPageStatistics(List<WordStatistic> wordStatistics, int correct,
                                        int incorrect) {
        ViewGroup viewGroup = (ViewGroup) getView().findViewById(R.id.layout_train_end_page);
        View fragment = viewGroup.getChildAt(0);
        ((TextView) fragment.findViewById(R.id.tvTrainingEndPageCorrect))
                .setText(String.valueOf(correct));
        ((TextView) fragment.findViewById(R.id.tvTrainingEndPageInCorrect))
                .setText(String.valueOf(incorrect));
    }

    protected Button findCorrectAnswerButton() {
        for (Button ans : bAnsArray) {
            if (((boolean) ans.getTag())) {
                return ans;
            }
        }
        return null;
    }

    protected abstract void setQuestion(Word word, List<String> answers, int correctNumIndex);

    private void setQuestionToUI(Word word, List<String> answers, int correctNumIndex)
    {
        for (int i = 0; i < bAnsArray.length; i++) {
            Button ans = bAnsArray[i];
            ans.setText(answers.get(i));
            ans.setTag(correctNumIndex == i);
        }
        setQuestion(word, answers, correctNumIndex);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        trainingPageContainer = view.findViewById(getTrainingPageContainerId());
        endPageContainer = view.findViewById(getEndPageContainerId());

        int[] buttonAns = getButtonAnswersId();
        bAnsArray = new Button[buttonAns.length];
        for (int i = 0; i < buttonAns.length; i++) {
            int bAnsId = buttonAns[i];
            bAnsArray[i] = (Button) view.findViewById(bAnsId);
        }

        bDontKnow = (Button) view.findViewById(getDontKnowButtonAnswerId());


        initialButtonBkg = bAnsArray[0].getBackground();

        aniCloseLastQuestion = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.anim_word_translation_closeold);
        aniCloseLastQuestion.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                nextQuestion(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        aniStartNewQuestion = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.anim_word_translation_startnew);

        for (Button ans : bAnsArray) {
            ans.setOnClickListener(this);
        }
        bDontKnow.setTag(false);
        bDontKnow.setOnClickListener(this);

        return view;
    }

    protected void toggleEnabledControls(boolean enabled) {
        for (Button ans : bAnsArray) {
            ans.setEnabled(enabled);
        }
    }

    @Override
    public void onClick(View v) {
        toggleEnabledControls(false);
        Button bAns = (Button) v;
        boolean correct = (boolean) bAns.getTag();
        onAnswer(correct, bAns);
    }

}