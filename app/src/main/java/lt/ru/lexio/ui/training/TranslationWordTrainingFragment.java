package lt.ru.lexio.ui.training;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Db;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatistic;
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
    protected int getProgressBarId() {
        return R.id.trainingProgress;
    }

    @Override
    protected void onQuestionTimeExpire() {
        onAnswer(false, (Button) getView().findViewById(getDontKnowButtonAnswerId()));
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
    protected void setEndPageStatistics(List<WordStatistic> wordStatistics, int correct, int incorrect) {
        super.setEndPageStatistics(wordStatistics, correct, incorrect);

        View fragment = getView().findViewById(R.id.layout_train_end_page);

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
    protected List<String> buildAnswers(Random random, WordDAO wordDAO, WordStatisticDAO wordStatisticDAO) {
        List<String> answers = trainingWordBuilder.buildRandomAnswers(wordCount, bAnsArray.length, Db.Common.TITLE);

        if (answers.size() < 4) {
            String tag = getCurrentDictionary().getLanguageTag().substring(0, 2).toLowerCase();
            int extraAnsId = getResources().getIdentifier("training_ExtraAnswers_" + tag, "array",
                    "lt.ru.lexio");
            String[] exrtaAnswers = getResources().getStringArray(extraAnsId);
            answers.addAll(Arrays.asList(exrtaAnswers));
        }

        return answers;
    }

    @Override
    protected List<Word> buildWords(Random random,
                                    Date sessionDate,
                                    int currentPage,
                                    WordDAO wordDAO,
                                    WordStatisticDAO wordStatisticDAO) {
        return trainingWordBuilder.build(wordCount, currentPage, sessionDate, wordOrder,
                getTrainingType());
    }

    @Override
    protected String getCorrectAnswer(Word word) {
        return word.getTitle();
    }

    @Override
    protected void setQuestion(Word word, List<String> answers, int correctNumIndex) {
        ((TextView) getView().findViewById(R.id.edTrainingTranslation)).setText(word.getTranslation());
    }

    @Override
    protected TrainingType getTrainingType() {
        return TrainingType.TRANS_WORD;
    }
}
