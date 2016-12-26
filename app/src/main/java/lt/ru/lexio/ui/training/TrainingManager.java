package lt.ru.lexio.ui.training;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import lt.ru.lexio.R;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.GeneralCallback;
import lt.ru.lexio.ui.settings.SettingsFragment;
import lt.ru.lexio.ui.widget.ListSinglePicker;
import lt.ru.lexio.ui.widget.WordOrderPicker;

/**
 * Created by lithTech on 06.05.2016.
 */
public class TrainingManager extends ContentFragment implements View.OnClickListener{

    TrainingWordOrder currentWordOrder;
    int currentWordCount;
    int currentWordTime;

    int loadTrainingId;

    TextView tvWordOrder;
    TextView tvWordCount;
    TextView tvWordTime;

    String[] answerTimerOptions;

    private SharedPreferences pref;

    private long[] argStartWordList;
    private int argWordOrder;
    private int argWordCount;
    private int argWordTimer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        tvWordOrder = (TextView) view.findViewById(R.id.tvWordOrder);
        tvWordCount = (TextView) view.findViewById(R.id.tvWordCount);
        tvWordTime = (TextView) view.findViewById(R.id.tvWordTime);

        loadTrainingId = getArguments().getInt(ContentFragment.ARG_TRAINING_TO_RUN);

        argStartWordList = getArguments().getLongArray(ContentFragment.ARG_TRAINING_START_LIST);
        argWordOrder = getArguments().getInt(ContentFragment.ARG_TRAINING_WORD_ORDER);
        argWordCount = getArguments().getInt(ContentFragment.ARG_TRAINING_WORD_COUNT);
        argWordTimer = getArguments().getInt(ContentFragment.ARG_TRAINING_WORD_TIME);

        pref = getActivity().getSharedPreferences(SettingsFragment.SETTINGS_FILE_NAME,
                Context.MODE_PRIVATE);
        currentWordTime = pref.getInt(loadTrainingId + ".nbTrainingAnswerTimer", 0);
        currentWordOrder = TrainingWordOrder.values()[pref.getInt(loadTrainingId + ".nbTrainingWordOrder1", 0)];
        currentWordCount = pref.getInt(loadTrainingId + ".nbTrainingWordCount", 5);

        applyTime(currentWordTime);
        applyCount(currentWordCount);
        applyOrder(currentWordOrder);

        View cWordCount = view.findViewById(R.id.cWordCount);
        View cWordOrder = view.findViewById(R.id.cWordOrder);
        View cWordTime = view.findViewById(R.id.cTimer);

        cWordCount.setOnClickListener(this);
        cWordOrder.setOnClickListener(this);
        cWordTime.setOnClickListener(this);

        View bStart = view.findViewById(R.id.bTrainingStart);
        bStart.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (argStartWordList != null)
            startTrainingWithList(argStartWordList, argWordOrder, argWordCount, argWordTimer);
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

    public void startTrainingWithList(long[] startWordList, int wordOrder, int wordCount,
                                      int wordTimer) {
        String title = "";
        int titleResId = getArguments().getInt(ContentFragment.ARG_TRAINING_TO_RUN_TITLE);
        if (titleResId != 0) {
            title = getResources().getString(titleResId);
        }

        Bundle args = new Bundle(5);
        args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, loadTrainingId);
        args.putInt(ContentFragment.ARG_TRAINING_WORD_COUNT, wordCount);
        args.putInt(ContentFragment.ARG_TRAINING_WORD_ORDER, wordOrder);
        args.putInt(ContentFragment.ARG_TRAINING_ANSWER_TIMER, wordTimer);
        args.putLongArray(ContentFragment.ARG_TRAINING_START_LIST, startWordList);

        ContentFragment trainingContent = getTraining(loadTrainingId);

        mainActivity.changeFragment(trainingContent, args, title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bTrainingStart:
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(loadTrainingId + ".nbTrainingAnswerTimer", currentWordTime);
                editor.putInt(loadTrainingId + ".nbTrainingWordCount", currentWordCount);
                editor.putInt(loadTrainingId + ".nbTrainingWordOrder1", currentWordOrder.ordinal());
                editor.apply();

                startTrainingWithList(null, currentWordOrder.ordinal(), currentWordCount, currentWordTime);
                break;

            case R.id.cWordCount:
                int min = getResources().getInteger(R.integer.training_min_words);
                int max = getResources().getInteger(R.integer.training_max_words);
                String[] values = new String[max - min + 1];
                int j = 0;
                for (int i = min; i <= max; i++)
                {
                    values[j++] = String.valueOf(i);
                }
                ListSinglePicker.show(getActivity(),
                        values, R.string.dialog_word_count,
                        new GeneralCallback() {
                            @Override
                            public void done(Object data) {
                                String sCnt = (String) data;
                                currentWordCount = Integer.valueOf(sCnt);
                                applyCount(currentWordCount);
                            }
                        });
                break;

            case R.id.cWordOrder:
                WordOrderPicker.show(getActivity(), new GeneralCallback() {
                    @Override
                    public void done(Object data) {
                        TrainingWordOrder order = (TrainingWordOrder) data;
                        currentWordOrder = order;
                        applyOrder(order);
                    }
                });
                break;

            case R.id.cTimer:
                String[] answerTimerOptions = getResources()
                        .getStringArray(R.array.training_Start_AnswerTimerOptions);

                ListSinglePicker.show(getActivity(),
                        answerTimerOptions, R.string.dialog_word_time,
                        new GeneralCallback() {
                            @Override
                            public void done(Object data) {
                                String sTime = (String) data;
                                int time = 0;
                                if (TextUtils.isDigitsOnly(sTime))
                                    time = Integer.parseInt(sTime);

                                applyTime(time);
                            }
                        });
                break;
        }
    }

    private void applyOrder(TrainingWordOrder order) {
        tvWordOrder.setText(order.getStringResTitleId());
    }

    private void applyCount(int cnt) {
        tvWordCount.setText(cnt + " " + getString(R.string.words_suffix));
    }

    private void applyTime(int time) {
        currentWordTime = time;
        if (currentWordTime == 0)
            tvWordTime.setText(R.string.training_Start_AnswerTimerNoLimit);
        else
            tvWordTime.setText(time + " " + getString(R.string.seconds));
    }
}
