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



    protected List<String> sessionAnswers = null;

    Button[] bAnsArray;
    private Button bDontKnow;

    private View trainingPageContainer = null;
    private View endPageContainer = null;

    private Drawable initialButtonBkg;


    protected abstract int getTrainingPageContainerId();

    protected abstract int getEndPageContainerId();

    protected abstract int[] getButtonAnswersId();

    protected abstract int getDontKnowButtonAnswerId();

    @Override
    protected void startTraining() {
        sessionAnswers = buildAnswers(random, wordDAO, wordStatisticDAO);
    }

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
                nextQuestion();
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

    @Override
    protected void onNextQuestion() {
        String correctAnswer = getCorrectAnswer(currentWord);
        List<String> answers = buildQuestionAnswers(correctAnswer);
        int correctNum = answers.indexOf(correctAnswer);

        setQuestionToUI(currentWord, answers, correctNum);

        toggleEnabledControls(true);
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

        int[] buttonAns = getButtonAnswersId();
        bAnsArray = new Button[buttonAns.length];
        for (int i = 0; i < buttonAns.length; i++) {
            int bAnsId = buttonAns[i];
            bAnsArray[i] = (Button) view.findViewById(bAnsId);
        }

        bDontKnow = (Button) view.findViewById(getDontKnowButtonAnswerId());


        initialButtonBkg = bAnsArray[0].getBackground();

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
