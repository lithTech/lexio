package lt.ru.lexio.ui.training;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.List;

import lt.ru.lexio.db.Dictionary;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatistic;
import lt.ru.lexio.db.WordStatisticDAO;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.MainActivity;

/**
 * Created by lithTech on 27.03.2016.
 */
public abstract class TrainingFragmentBase extends ContentFragment {

    private Word wordDummy = new Word();
    protected WordStatisticDAO wordStatisticDAO = null;
    protected WordDAO wordDAO = null;
    protected MainActivity activity = null;
    protected TrainingWordBuilder trainingWordBuilder = null;


    public Dictionary getCurrentDictionary() {
        Dictionary dictionary = null;
        if (activity != null) {
            return activity.getCurrentDictionary();
        }
        else {
            dictionary = new Dictionary();
            dictionary.id = 0;
        }
        return dictionary;
    }

    @Override
    public void onStart() {
        super.onStart();
        trainingWordBuilder.dictId = getCurrentDictionary().id;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            this.activity = (MainActivity) activity;
        }
    }

    protected long storeStatistic(long wordId, boolean isSuccess, long sessionId) {
        WordStatistic statistic = new WordStatistic();
        wordDummy.id = wordId;
        statistic.setWord(wordDummy);
        if (sessionId > 0)
            statistic.setSessionId(sessionId);
        statistic.setTrainedOn(new Date());
        statistic.setTrainingResult(isSuccess ? 1 : 0);
        statistic.setTrainingType(getTrainingType().ordinal());

        wordStatisticDAO.create(statistic);
        if (sessionId > 0)
            return sessionId;
        return statistic.id;
    }

    protected abstract TrainingType getTrainingType();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        wordStatisticDAO = new WordStatisticDAO(view.getContext());
        wordDAO = new WordDAO(view.getContext());
        trainingWordBuilder = new TrainingWordBuilder(wordDAO, 0);

        return view;
    }
}
