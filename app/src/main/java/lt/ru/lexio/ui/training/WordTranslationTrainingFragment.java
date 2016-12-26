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
    protected int getProgressBarId() {
        return R.id.trainingProgress;
    }

    @Override
    protected void onQuestionTimeExpire() {
        onAnswer(false, (Button) getView().findViewById(getDontKnowButtonAnswerId()));
    }

    @Override
    protected void setEndPageStatistics(List<WordStatistic> wordStatistics, int correct, int incorrect) {
        super.setEndPageStatistics(wordStatistics, correct, incorrect);

        View fragment = getView().findViewById(R.id.layout_train_end_page);

        List<EndPageStatistic> statistics = new ArrayList<>(wordStatistics.size());
        for (WordStatistic ws : wordStatistics) {
            EndPageStatistic e = new EndPageStatistic(ws.getWord().id,
                    ws.getWord().getTitle(),
                    ws.getWord().getTranslation(), ws.getTrainingResult() == 1);
            statistics.add(e);
        }

        ListView lWordStatistic = (ListView) fragment.findViewById(R.id.lvTrainingEndPageWordStat);
        lWordStatistic.setAdapter(TrainingEndPageFragment.initAdapter(fragment.getContext(), statistics));
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
        List<String> answers = trainingWordBuilder.buildRandomAnswers(wordCount, 4, Db.Word.TRANSLATION);

        if (answers.size() < 4) {
            String tag = Locale.getDefault().getISO3Language().substring(0, 2).toLowerCase();
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
        return word.getTranslation();
    }

    @Override
    protected void setQuestion(Word word, List<String> answers, int correctNumIndex) {
        String wordTitle = word.getTitle();
        if (word.getTranscription() != null && !word.getTranscription().isEmpty())
            wordTitle += " [" + word.getTranscription() + "]";
        ((TextView) getView().findViewById(R.id.edTrainingWord)).setText(wordTitle);
        ((TextView) getView().findViewById(R.id.tvTrainingContext)).setText(word.getContext());

        textToSpeech(word.getTitle());
    }
}
