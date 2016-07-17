package lt.ru.lexio.ui.training;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordStatistic;

/**
 * Created by lithTech on 27.03.2016.
 */
public abstract class TrainingEnterTextFragment extends TrainingFragmentBase implements View.OnClickListener {

    private EditText edInputText;

    private TextView tvTranslationQuestion;

    private Button bDontKnow;

    protected abstract int getTrainingQuestionId();

    protected abstract int getInputTextId();

    @Override
    protected void startTraining() {

    }

    protected void onAnswer(final boolean isCorrect) {
        nextQuestion(isCorrect);
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
        return currentWord.getTitle().trim().equalsIgnoreCase(answer.trim());
    }

    private void setQuestionToUI(Word word)
    {
        tvTranslationQuestion.setText(word.getTranslation());
        edInputText.setText("");

        edInputText.setShowSoftInputOnFocus(true);
        if (edInputText.requestFocus())
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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

        bDontKnow = (Button) view.findViewById(getDontKnowButtonAnswerId());

        bDontKnow.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        onAnswer(isCorrect(edInputText.getText().toString()));
    }

}
