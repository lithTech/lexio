package lt.ru.lexio.ui.training;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.settings.SettingsFragment;
import lt.ru.lexio.util.NumberPickerHelper;

/**
 * Created by lithTech on 06.05.2016.
 */
public class TrainingManager extends ContentFragment implements View.OnClickListener{

    private int loadTrainingId;
    private int titleId;
    private TrainingWordOrder[] wordOrders;
    NumberPicker nbTrainingWordCount;
    NumberPicker nbTrainingWordOrder1;
    NumberPicker nbTrainingAnswerTimer;
    String[] answerTimerOptions;

    private SharedPreferences pref;
    private long[] startWordList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        loadTrainingId = getArguments().getInt(ContentFragment.ARG_TRAINING_TO_RUN);

        startWordList = getArguments().getLongArray(ContentFragment.ARG_TRAINING_START_LIST);

        pref = getActivity().getSharedPreferences(SettingsFragment.SETTINGS_FILE_NAME,
                Context.MODE_PRIVATE);

        nbTrainingWordCount = (NumberPicker) view.findViewById(R.id.npCount);
        nbTrainingWordCount.setMinValue(getResources().getInteger(R.integer.training_min_words));
        nbTrainingWordCount.setMaxValue(getResources().getInteger(R.integer.training_max_words));

        titleId = getArguments().getInt(ContentFragment.ARG_TRAINING_TO_RUN_TITLE);

        nbTrainingWordOrder1 = (NumberPicker) view.findViewById(R.id.npWordOrder1);
        wordOrders = TrainingWordOrder.values();
        nbTrainingWordOrder1.setMinValue(0);
        nbTrainingWordOrder1.setMaxValue(wordOrders.length - 1);

        String[] orders = new String[wordOrders.length];
        for (int i = 0; i < wordOrders.length; i++)
            orders[i] = getResources().getString(wordOrders[i].getStringResTitleId());

        nbTrainingWordOrder1.setDisplayedValues(orders);

        int wOrder = pref.getInt(loadTrainingId + ".nbTrainingWordOrder1", 0);
        nbTrainingWordOrder1.setValue(wOrder);
        int wCnt = pref.getInt(loadTrainingId + ".nbTrainingWordCount", 5);
        nbTrainingWordCount.setValue(wCnt);

        nbTrainingWordOrder1.setWrapSelectorWheel(true);
        nbTrainingWordCount.setWrapSelectorWheel(true);

        nbTrainingAnswerTimer = (NumberPicker) view.findViewById(R.id.npAnswerTimer);
        answerTimerOptions = getResources().getStringArray(R.array.training_Start_AnswerTimerOptions);
        nbTrainingAnswerTimer.setMinValue(0);
        nbTrainingAnswerTimer.setMaxValue(answerTimerOptions.length - 1);
        nbTrainingAnswerTimer.setDisplayedValues(answerTimerOptions);
        nbTrainingAnswerTimer.setWrapSelectorWheel(true);

        int aTime = pref.getInt(loadTrainingId + ".nbTrainingAnswerTimer", 0);
        nbTrainingAnswerTimer.setValue(aTime);

        View bStart = view.findViewById(R.id.bTrainingStart);
        bStart.setOnClickListener(this);

        NumberPickerHelper.setDividerColor(nbTrainingAnswerTimer, Color.GRAY);
        NumberPickerHelper.setDividerColor(nbTrainingWordCount, Color.GRAY);
        NumberPickerHelper.setDividerColor(nbTrainingWordOrder1, Color.GRAY);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (startWordList != null)
            startTrainingWithList(startWordList);
    }

    private ContentFragment getTraining(int loadTrainingId) {
        ContentFragment trainingContent;
        switch (loadTrainingId) {
            case R.layout.content_training_cards:
                trainingContent = new TrainingCards();
                break;
            case R.layout.content_training_trans_voice:
                trainingContent = new TrainingTranslationVoice();
                break;
            case R.layout.content_training_trans_word:
                trainingContent = new TranslationWordTrainingFragment();
                break;
            case R.layout.content_training_word_trans:
                trainingContent = new WordTranslationTrainingFragment();
                break;
            case R.layout.content_training_enter_word:
                trainingContent = new TrainingEnterWordByTransFragment();
                break;
            default:
                return null;
        }
        return trainingContent;
    }

    public void startTrainingWithList(long[] startWordList) {
        TrainingWordOrder wordOrder1 = wordOrders[nbTrainingWordOrder1.getValue()];
        int count = nbTrainingWordCount.getValue();
        String title = "";
        int titleResId = getArguments().getInt(ContentFragment.ARG_TRAINING_TO_RUN_TITLE);
        if (titleResId != 0) {
            title = getResources().getString(titleResId);
        }

        Bundle args = new Bundle(5);
        args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, loadTrainingId);
        args.putInt(ContentFragment.ARG_TRAINING_WORD_COUNT, count);
        args.putInt(ContentFragment.ARG_TRAINING_WORD_ORDER, wordOrder1.ordinal());
        args.putInt(ContentFragment.ARG_TRAINING_ANSWER_TIMER, 0);
        args.putLongArray(ContentFragment.ARG_TRAINING_START_LIST, startWordList);

        ContentFragment trainingContent = getTraining(loadTrainingId);

        mainActivity.changeFragment(trainingContent, args, title);
    }

    @Override
    public void onClick(View v) {
        TrainingWordOrder wordOrder1 = wordOrders[nbTrainingWordOrder1.getValue()];
        int count = nbTrainingWordCount.getValue();
        String title = getResources().getString(getArguments().getInt(ContentFragment.ARG_TRAINING_TO_RUN_TITLE));

        Bundle args = new Bundle(5);
        args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, loadTrainingId);
        args.putInt(ContentFragment.ARG_TRAINING_WORD_COUNT, count);
        args.putInt(ContentFragment.ARG_TRAINING_WORD_ORDER, wordOrder1.ordinal());

        String timerOption = answerTimerOptions[nbTrainingAnswerTimer.getValue()];
        if (nbTrainingAnswerTimer.getValue() == 0) timerOption = "0";
        args.putInt(ContentFragment.ARG_TRAINING_ANSWER_TIMER, Integer.valueOf(timerOption));
        ContentFragment trainingContent = getTraining(loadTrainingId);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(loadTrainingId + ".nbTrainingAnswerTimer", Integer.valueOf(timerOption));
        editor.putInt(loadTrainingId + ".nbTrainingWordCount", count);
        editor.putInt(loadTrainingId + ".nbTrainingWordOrder1", wordOrder1.ordinal());
        editor.apply();

        mainActivity.changeFragment(trainingContent, args, title);
    }
}
