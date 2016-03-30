package lt.ru.lexio.ui.training;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
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

    Button bAns1 = null;
    Button bAns2 = null;
    Button bAns3 = null;
    Button bAns4 = null;
    View parentLayout = null;
    private Drawable initialButtonBkg;

    @Override
    protected TrainingType getTrainingType() {
        return TrainingType.WORD_TRANS;
    }

    protected void onAnswer(boolean isCorrect, final Button clickedButton) {
        currentSessionId = storeStatistic(currentWord.id, isCorrect, currentSessionId);
        if (!isCorrect)
            animateBetweenColors(clickedButton,
                    android.R.drawable.btn_default,
                    getResources().getColor(R.color.colorMandatory), 1500, null);
        final Button correct = findCorrectAnswerButton();
        animateBetweenColors(correct,
                android.R.drawable.btn_default,
                getResources().getColor(R.color.correctAnswer), 1500, new Animator.AnimatorListener() {
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
                });
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

            setQuestion(currentWord.getTitle(), currentWord.getContext(), answers.get(0),
                    answers.get(1),
                    answers.get(2),
                    answers.get(3),
                    correctNum);

            questionNum++;

            parentLayout.startAnimation(aniStartNewQuestion);
        }
    }

    private void exitTraining() {

    }

    protected Button findCorrectAnswerButton() {
        if (((boolean) bAns1.getTag())) {
            return bAns1;
        }
        else if (((boolean) bAns2.getTag())) {
            return bAns2;
        }
        else if (((boolean) bAns3.getTag())) {
            return bAns3;
        }
        else {
            return bAns4;
        }
    }

    protected void setQuestion(String word, String context, String ans1, String ans2, String ans3, String ans4,
                               int correctNum) {
        EditText edWord = (EditText) getView().findViewById(R.id.edTrainingWord);
        TextView tvContext = (TextView) getView().findViewById(R.id.tvTrainingContext);

        edWord.setText(word);
        tvContext.setText(context);
        if (context == null || context.isEmpty())
            tvContext.setVisibility(View.INVISIBLE);
        else tvContext.setVisibility(View.VISIBLE);

        bAns1.setText(ans1);
        bAns1.setTag(correctNum == 1);
        bAns2.setText(ans2);
        bAns2.setTag(correctNum == 2);
        bAns3.setText(ans3);
        bAns3.setTag(correctNum == 3);
        bAns4.setText(ans4);
        bAns4.setTag(correctNum == 4);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        parentLayout = view.findViewById(R.id.layout_word_train);

        bAns1 = (Button) view.findViewById(R.id.bWordTransAnswer1);
        bAns2 = (Button) view.findViewById(R.id.bWordTransAnswer2);
        bAns3 = (Button) view.findViewById(R.id.bWordTransAnswer3);
        bAns4 = (Button) view.findViewById(R.id.bWordTransAnswer4);

        initialButtonBkg = bAns1.getBackground();

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

        bAns1.setOnClickListener(this);
        bAns2.setOnClickListener(this);
        bAns3.setOnClickListener(this);
        bAns4.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Button bAns = (Button) v;
        boolean correct = (boolean) bAns.getTag();
        onAnswer(correct, bAns);
    }

    public static void animateBetweenColors(final View viewToAnimateItBackground, final int colorFrom, final int colorTo,
                                            final int durationInMs, Animator.AnimatorListener animationListener) {
        final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            ColorDrawable colorDrawable = new ColorDrawable(colorFrom);

            @Override
            public void onAnimationUpdate(final ValueAnimator animator) {
                colorDrawable.setColor((Integer) animator.getAnimatedValue());
                viewToAnimateItBackground.setBackground(colorDrawable);
            }
        });
        if (animationListener != null)
            colorAnimation.addListener(animationListener);
        if (durationInMs >= 0)
            colorAnimation.setDuration(durationInMs);
        colorAnimation.setInterpolator(new DecelerateInterpolator(3.0f));
        colorAnimation.start();
    }
}
