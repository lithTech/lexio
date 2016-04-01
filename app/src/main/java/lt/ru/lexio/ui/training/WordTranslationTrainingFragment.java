package lt.ru.lexio.ui.training;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.util.ColorAnimateHelper;

/**
 * Created by lithTech on 27.03.2016.
 */
public class WordTranslationTrainingFragment extends TrainingFragmentBase implements View.OnClickListener {

    Word currentWord = null;
    long currentSessionId = 0;
    int questionNum = 0;
    List<Word> sessionWords = null;
    List<String> sessionAnswers = null;
    Random random = new Random(System.nanoTime());

    Animation aniCloseLastQuestion;
    Animation aniStartNewQuestion;

    Button[] bAnsArray;

    View parentLayout = null;
    private Drawable initialButtonBkg;

    @Override
    protected TrainingType getTrainingType() {
        return TrainingType.WORD_TRANS;
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
                    getResources().getColor(R.color.colorMandatory), 2000, onEndAnimation);
            onEndAnimation = null;      //animation of wrong answer is longer, so put onEndAnimation event there
        }
        ColorAnimateHelper.animateBetweenColors(correct,
                android.R.drawable.btn_default,
                getResources().getColor(R.color.correctAnswer), 400, onEndAnimation);
    }

    protected void startTraining() {
        questionNum = 0;
        trainingWordBuilder.dictId = getCurrentDictionary().id;
        sessionWords = trainingWordBuilder.build(15, TrainingWordMethod.UNTRAINING_WORDS,
                TrainingType.WORD_TRANS);
        sessionAnswers = trainingWordBuilder.buildRandomAnswers(15, 4);

        nextQuestion(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        startTraining();
    }

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
        if (questionNum >= sessionWords.size()) {
            exitTraining();
            return;
        }
        //exit from old question is not yet animated
        if (animateQuestionExit) {
            parentLayout.startAnimation(aniCloseLastQuestion);
        }
        //animation on end question is ended, present new question
        else {
            currentWord = sessionWords.get(questionNum);
            String correctAnswer = currentWord.getTranslation();
            List<String> answers = buildQuestionAnswers(correctAnswer);
            int correctNum = answers.indexOf(correctAnswer) + 1;

            setQuestion(currentWord.getTitle(), currentWord.getContext(), answers,
                    correctNum);

            questionNum++;

            parentLayout.startAnimation(aniStartNewQuestion);
        }
    }

    private void exitTraining() {

    }

    protected Button findCorrectAnswerButton() {
        for (Button ans : bAnsArray) {
            if (((boolean) ans.getTag())) {
                return ans;
            }
        }
        return null;
    }

    protected void setQuestion(String word, String context, List<String> answers, int correctNumIndex) {
        EditText edWord = (EditText) getView().findViewById(R.id.edTrainingWord);
        TextView tvContext = (TextView) getView().findViewById(R.id.tvTrainingContext);

        edWord.setText(word);
        tvContext.setText(context);
        if (context == null || context.isEmpty())
            tvContext.setVisibility(View.INVISIBLE);
        else tvContext.setVisibility(View.VISIBLE);

        for (int i = 0; i < bAnsArray.length; i++) {
            Button ans = bAnsArray[i];
            ans.setTag(correctNumIndex == i);
            ans.setTag(answers.get(i));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        parentLayout = view.findViewById(R.id.layout_word_train);

        bAnsArray = new Button[4];

        bAnsArray[0] = (Button) view.findViewById(R.id.bWordTransAnswer1);
        bAnsArray[1] = (Button) view.findViewById(R.id.bWordTransAnswer2);
        bAnsArray[2] = (Button) view.findViewById(R.id.bWordTransAnswer3);
        bAnsArray[3] = (Button) view.findViewById(R.id.bWordTransAnswer4);

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


        return view;
    }

    @Override
    public void onClick(View v) {
        Button bAns = (Button) v;
        boolean correct = (boolean) bAns.getTag();
        onAnswer(correct, bAns);
    }

}
