package lt.ru.lexio.ui.training;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordStatistic;
import lt.ru.lexio.util.ColorAnimateHelper;

/**
 * Created by lithTech on 27.03.2016.
 */
public abstract class TrainingEnterTextFragment extends TrainingFragmentBase implements View.OnClickListener {

    private EditText edInputText;

    private TextView tvTranslationQuestion;

    private Button bAnswer;

    private TextView tvCorrectAnswer;

    private Drawable bgnAnswer;

    private Drawable bgnCorrectAnswer;

    protected abstract int getTrainingQuestionId();

    protected abstract int getInputTextId();

    protected abstract int getCorrectAnswerHolderId();

    protected abstract String getCorrectAnswer();

    @Override
    protected void startTraining() {

    }

    protected void onAnswer(final boolean isCorrect) {
        int color = getResources().getColor(R.color.colorMandatory);
        if (isCorrect)
            color = getResources().getColor(R.color.correctAnswer);

        Animator.AnimatorListener onEndAnimation = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                nextQuestion(isCorrect);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        ColorAnimateHelper.animateBetweenColors(edInputText, android.R.drawable.editbox_background,
                color, getResources().getInteger(R.integer.animation_incorrect_answer), onEndAnimation);
        if (!isCorrect) {
            tvCorrectAnswer.setText(getResources()
                    .getString(R.string.Training_TransVoice_CorrectAnswerPrefix) + " " +getCorrectAnswer());
            ColorAnimateHelper.animateBetweenColors(tvCorrectAnswer, android.R.drawable.editbox_background,
                    getResources().getColor(R.color.correctAnswer),
                    getResources().getInteger(R.integer.animation_correct_answer), null);
        }
        //nextQuestion(isCorrect);
    }

    @Override
    protected void onNextQuestion() {
        setQuestionToUI(currentWord);
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

    protected boolean isCorrect(String answer) {
        return getCorrectAnswer().trim().equalsIgnoreCase(answer.trim());
    }

    private void setQuestionToUI(Word word)
    {
        tvTranslationQuestion.setText(word.getTranslation());
        tvCorrectAnswer.setText("");
        edInputText.setText("");

        tvCorrectAnswer.setBackground(bgnCorrectAnswer);
        edInputText.setBackground(bgnAnswer);

        edInputText.setShowSoftInputOnFocus(true);
    }

    @Override
    protected void onQuestionShow() {
        if (edInputText.requestFocus())
        {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edInputText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    protected void onQuestionTimeExpire() {
        onAnswer(false);
    }

    protected int getDontKnowButtonAnswerId() {
        return R.id.bTransDontKnow;
    }

    @Override
    protected int getTrainingPageContainerId() {
        return R.id.layout_trans_word_train;
    }

    @Override
    protected int getEndPageContainerId() {
        return R.id.layout_train_end_page;
    }

    @Override
    protected int getProgressBarId() {
        return R.id.trainingProgress;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        tvTranslationQuestion = (TextView) view.findViewById(getTrainingQuestionId());

        edInputText = (EditText) view.findViewById(getInputTextId());

        edInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onAnswer(isCorrect(edInputText.getText().toString()));
                    return true;
                }
                return false;
            }
        });


        bAnswer = (Button) view.findViewById(getDontKnowButtonAnswerId());
        bAnswer.setOnClickListener(this);

        tvCorrectAnswer = (TextView) view.findViewById(getCorrectAnswerHolderId());

        bgnAnswer = edInputText.getBackground();
        bgnCorrectAnswer = tvCorrectAnswer.getBackground();

        return view;
    }



    @Override
    public void onClick(View v) {
        onAnswer(isCorrect(edInputText.getText().toString()));
    }

}
