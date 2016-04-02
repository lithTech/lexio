package lt.ru.lexio.ui.training;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatisticDAO;

/**
 * Created by lithTech on 27.03.2016.
 */
public class WordTranslationTrainingFragment extends TrainingAnswerOptionsFragment implements View.OnClickListener {

    @Override
    protected TrainingType getTrainingType() {
        return TrainingType.WORD_TRANS;
    }


    @Override
    protected int getTrainingPageContainerId() {
        return R.id.layout_word_train;
    }

    @Override
    protected int getEndPageContainerId() {
        return R.id.layout_train_end_page;
    }

    @Override
    protected int[] getButtonAnswersId() {
        return new int[]{
                R.id.bWordTransAnswer1,
                R.id.bWordTransAnswer2,
                R.id.bWordTransAnswer3,
                R.id.bWordTransAnswer4
        };
    }

    @Override
    protected int getDontKnowButtonAnswerId() {
        return R.id.bWordTransDontKnow;
    }

    @Override
    protected List<String> buildAnswers(Random random, WordDAO wordDAO, WordStatisticDAO wordStatisticDAO) {
        return trainingWordBuilder.buildRandomAnswers(20, 4, Db.Word.TRANSLATION);
    }

    @Override
    protected List<Word> buildWords(Random random, WordDAO wordDAO, WordStatisticDAO wordStatisticDAO) {
        return trainingWordBuilder.build(20, TrainingWordMethod.UNTRAINING_WORDS, TrainingType.WORD_TRANS);
    }

    @Override
    protected String getCorrectAnswer(Word word) {
        return word.getTranslation();
    }

    @Override
    protected void setQuestion(Word word, List<String> answers, int correctNumIndex) {
        ((EditText) getView().findViewById(R.id.edTrainingWord)).setText(word.getTitle());
        ((TextView) getView().findViewById(R.id.tvTrainingContext)).setText(word.getContext());
    }
}
