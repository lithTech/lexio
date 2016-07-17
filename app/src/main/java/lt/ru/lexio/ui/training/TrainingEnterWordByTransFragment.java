package lt.ru.lexio.ui.training;

import java.util.Date;
import java.util.List;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatisticDAO;

/**
 * Created by lithTech on 17.07.2016.
 */
public class TrainingEnterWordByTransFragment extends TrainingEnterTextFragment {
    @Override
    protected int getTrainingQuestionId() {
        return R.id.edTrainingTranslation;
    }

    @Override
    protected List<Word> buildWords(Random random, Date sessionDate, int currentPage,
                                    WordDAO wordDAO, WordStatisticDAO wordStatisticDAO) {
        return trainingWordBuilder.build(wordCount, currentPage, sessionDate, wordOrder,
                getTrainingType());
    }

    @Override
    protected TrainingType getTrainingType() {
        return TrainingType.TRANS_WRITE;
    }

    @Override
    protected int getInputTextId() {
        return R.id.edInputWord;
    }
}
