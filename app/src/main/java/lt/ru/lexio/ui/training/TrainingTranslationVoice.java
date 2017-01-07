package lt.ru.lexio.ui.training;

import android.animation.Animator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatistic;
import lt.ru.lexio.db.WordStatisticDAO;
import lt.ru.lexio.ui.settings.SettingsFragment;
import lt.ru.lexio.util.ColorAnimateHelper;

/**
 * Created by lithTech on 14.04.2016.
 */
public class TrainingTranslationVoice extends TrainingFragmentBase implements View.OnClickListener {

    private TextView edWord;
    private View bMic;
    private View bNext;
    private TextView tvAnswer;
    private TextView tvCorrectAnswer;
    private String correctAnswer;
    private Drawable defaultAnswerBg;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    boolean lastResult = false;

    boolean showTranscription = true;

    @Override
    protected int getTrainingPageContainerId() {
        return R.id.layout_trans_voice_container;
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
        checkResult("", false, true);
    }

    @Override
    protected void startTraining() {
        SharedPreferences sp = getActivity()
                .getSharedPreferences(SettingsFragment.SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
        showTranscription = sp.getBoolean("pref_training_voice_show_transcription", true);
    }

    @Override
    protected void onNextQuestion() {
        lastResult = false;
        String text = currentWord.getTranslation();
        if (showTranscription && currentWord.getTranscription() != null &&
                !currentWord.getTranscription().isEmpty())
            text += " [" + currentWord.getTranscription() + "]";
        edWord.setText(text);
        correctAnswer = currentWord.getTitle();
        tvAnswer.setText("");
        tvAnswer.setBackground(defaultAnswerBg);
        tvCorrectAnswer.setText("");
        tvCorrectAnswer.setBackground(defaultAnswerBg);

        promptSpeechInput();
    }

    @Override
    protected void setEndPageStatistics(List<WordStatistic> wordStatistics, int correct, int incorrect) {
        View fragment = getView().findViewById(R.id.layout_train_end_page);

        ((TextView) fragment.findViewById(R.id.tvTrainingEndPageCorrect))
                .setText(String.valueOf(correct));
        ((TextView) fragment.findViewById(R.id.tvTrainingEndPageInCorrect))
                .setText(String.valueOf(incorrect));

        List<EndPageStatistic> statistics = new ArrayList<>(wordStatistics.size());
        for (WordStatistic ws : wordStatistics) {
            EndPageStatistic e = new EndPageStatistic(ws.id,
                    ws.getWord().getTranslation(),
                    ws.getWord().getTitle(), ws.getTrainingResult() == 1);
            statistics.add(e);
        }

        ListView lWordStatistic = (ListView) fragment.findViewById(R.id.lvTrainingEndPageWordStat);
        lWordStatistic.setAdapter(TrainingEndPageFragment.initAdapter(fragment.getContext(), statistics));
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
    protected boolean isSpeechEnabled() {
        return false;
    }

    @Override
    protected TrainingType getTrainingType() {
        return TrainingType.TRANS_VOICE;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        bMic = view.findViewById(R.id.bTrainingTransVoiceMic);
        bNext = view.findViewById(R.id.bTrainingTransVoiceNext);
        bMic.setOnClickListener(this);
        bNext.setOnClickListener(this);
        edWord = (TextView) view.findViewById(R.id.edTrainingTransVoiceWord);
        tvAnswer = (TextView) view.findViewById(R.id.tvTrainingTransVoiceAnswer);
        tvCorrectAnswer = (TextView) view.findViewById(R.id.tvTrainingTransVoiceCorrectAnswer);

        defaultAnswerBg = tvAnswer.getBackground();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    List<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    checkResult(result.toString(), isCorrectAnswer(result), false);
                }
                break;
            }
        }
    }

    private void checkResult(String result, boolean isCorrectAnswer, final boolean showCorrect) {
        bNext.setEnabled(false);
        tvAnswer.setText(getResources()
                .getString(R.string.Training_TransVoice_AnswerPrefix) + " " +
                result);

        final boolean isCorrect = isCorrectAnswer;
        lastResult = isCorrectAnswer;
        int color = getResources().getColor(R.color.colorMandatory);
        if (isCorrect)
            color = getResources().getColor(R.color.correctAnswer);

        Animator.AnimatorListener onEndAnimation = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isCorrect || showCorrect)
                    nextQuestion(isCorrect);
                bNext.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        ColorAnimateHelper.animateBetweenColors(tvAnswer, android.R.drawable.menuitem_background,
                color, getResources().getInteger(R.integer.animation_incorrect_answer), onEndAnimation);
        if (!isCorrect && showCorrect) {
            String text = getResources()
                    .getString(R.string.Training_TransVoice_CorrectAnswerPrefix) + " " +correctAnswer;
            if (currentWord.getTranscription() != null && !currentWord.getTranscription().isEmpty())
                text += " [" + currentWord.getTranscription() + "]";
            tvCorrectAnswer.setText(text);
            ColorAnimateHelper.animateBetweenColors(tvCorrectAnswer, android.R.drawable.menuitem_background,
                    getResources().getColor(R.color.correctAnswer),
                    getResources().getInteger(R.integer.animation_correct_answer), null);
        }
    }

    private boolean isCorrectAnswer(List<String> speech) {
        boolean r = false;
        String ca = correctAnswer.toLowerCase().trim().replace("-", "").replace(" ", "");
        for (String sp : speech) {
            if (ca.equals(sp.toLowerCase().trim().replace("-", "").replace(" ", ""))) {
                r = true;
                break;
            }
        }
        return r;
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, 10);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mainActivity.getCurrentDictionary().getLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.TransVoiceSpeechPromt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getView().getContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == bMic)
            promptSpeechInput();
        if (v == bNext)
        {
            checkResult(tvAnswer.getText().toString(), lastResult, true);
        }
    }
}
