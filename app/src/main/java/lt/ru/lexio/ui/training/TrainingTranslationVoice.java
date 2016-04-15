package lt.ru.lexio.ui.training;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatistic;
import lt.ru.lexio.db.WordStatisticDAO;

/**
 * Created by lithTech on 14.04.2016.
 */
public class TrainingTranslationVoice extends TrainingFragmentBase implements View.OnClickListener {

    private EditText edWord;
    private FloatingActionButton bMic;
    private TextView tvAnswer;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected int getTrainingPageContainerId() {
        return R.id.layout_trans_voice_container;
    }

    @Override
    protected int getEndPageContainerId() {
        return R.id.layout_train_end_page;
    }

    @Override
    protected void startTraining() {

    }

    @Override
    protected void onNextQuestion() {

    }

    @Override
    protected void setEndPageStatistics(List<WordStatistic> wordStatistics, int correct, int incorrect) {
        ViewGroup viewGroup = (ViewGroup) getView().findViewById(R.id.layout_train_end_page);
        View fragment = viewGroup.getChildAt(0);
        ((TextView) fragment.findViewById(R.id.tvTrainingEndPageCorrect))
                .setText(String.valueOf(correct));
        ((TextView) fragment.findViewById(R.id.tvTrainingEndPageInCorrect))
                .setText(String.valueOf(incorrect));
    }

    @Override
    protected List<Word> buildWords(Random random, WordDAO wordDAO, WordStatisticDAO wordStatisticDAO) {
        return trainingWordBuilder.build(20, TrainingWordMethod.UNTRAINING_WORDS, getTrainingType());
    }

    @Override
    protected TrainingType getTrainingType() {
        return TrainingType.TRANS_VOICE;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        bMic = (FloatingActionButton) view.findViewById(R.id.bTrainingTransVoiceMic);
        bMic.setOnClickListener(this);
        edWord = (EditText) view.findViewById(R.id.edTrainingTransVoiceWord);
        tvAnswer = (TextView) view.findViewById(R.id.tvTrainingTransVoiceAnswer);

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

                    checkResult(result.get(0));
                }
                break;
            }
        }
    }

    private void checkResult(String speech) {
        tvAnswer.setText(getResources()
                .getString(R.string.Training_TransVoice_AnswerPrefix) + " " +
                speech);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
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
        promptSpeechInput();
    }
}
