package lt.ru.lexio.ui.training;

import android.widget.Button;
import android.widget.EditText;

import java.util.List;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatisticDAO;

/**
 * Created by lithTech on 01.04.2016.
 */
public class TranslationWordTrainingFragment extends TrainingAnswerOptionsFragment {

    @Override
    protected int getTrainingPageContainerId() {
        return R.id.layout_trans_word_train;
    }

    @Override
    protected int getEndPageContainerId() {
        return R.id.layout_train_end_page;
    }

    @Override
    protected int[] getButtonAnswersId() {
        return new int[]{R.id.bTransAnswer1, R.id.bTransAnswer2, R.id.bTransAnswer3,
                R.id.bTransAnswer4};
    }

    @Override
    protected int getDontKnowButtonAnswerId() {
        return R.id.bTransDontKnow;
    }

    @Override
    protected List<String> buildAnswers(Random random, WordDAO wordDAO, WordStatisticDAO wordStatisticDAO) {
        return trainingWordBuilder.buildRandomAnswers(20, bAnsArray.length, Db.Common.TITLE);
    }

    @Override
    protected List<Word> buildWords(Random random, WordDAO wordDAO, WordStatisticDAO wordStatisticDAO) {
        return trainingWordBuilder.build(20, TrainingWordMethod.UNTRAINING_WORDS, TrainingType.TRANS_WORD);
    }

    @Override
    protected String getCorrectAnswer(Word word) {
        return word.getTitle();
    }

    @Override
    protected void setQuestion(Word word, List<String> answers, int correctNumIndex) {
        ((EditText) getView().findViewById(R.id.edTrainingTranslation)).setText(word.getTranslation());
    }

    @Override
    protected TrainingType getTrainingType() {
        return TrainingType.TRANS_WORD;
    }
}