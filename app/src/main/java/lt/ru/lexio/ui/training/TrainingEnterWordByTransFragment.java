package lt.ru.lexio.ui.training;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatistic;
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
    protected void setEndPageStatistics(List<WordStatistic> wordStatistics, int correct, int incorrect) {
        super.setEndPageStatistics(wordStatistics, correct, incorrect);

        View fragment = getView().findViewById(getEndPageContainerId());

        List<EndPageStatistic> statistics = new ArrayList<>(wordStatistics.size());
        for (WordStatistic wordStatistic : wordStatistics) {
            EndPageStatistic e = new EndPageStatistic(wordStatistic.getWord().getTranslation(),
                    wordStatistic.getWord().getTitle(), wordStatistic.getTrainingResult() == 1);
            statistics.add(e);
        }

        ListView lWordStatistic = (ListView) fragment.findViewById(R.id.lvTrainingEndPageWordStat);
        lWordStatistic.setAdapter(TrainingEndPageFragment.initAdapter(fragment.getContext(), statistics));
    }

    @Override
    protected TrainingType getTrainingType() {
        return TrainingType.TRANS_WRITE;
    }

    @Override
    protected int getInputTextId() {
        return R.id.edInputWord;
    }

    @Override
    protected int getCorrectAnswerHolderId() {
        return R.id.tvCorrectAnswer;
    }

    @Override
    protected String getCorrectAnswer() {
        return currentWord.getTitle();
    }
}
