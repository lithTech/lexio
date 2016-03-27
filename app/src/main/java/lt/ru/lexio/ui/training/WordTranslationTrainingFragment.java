package lt.ru.lexio.ui.training;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;

/**
 * Created by lithTech on 27.03.2016.
 */
public class WordTranslationTrainingFragment extends TrainingFragmentBase implements View.OnClickListener {

    Word currentWord = null;
    long currentSessionId = 0;
    int questionNum = 0;
    List<Word> sessionWords = null;
    List<String> sessionAnswers = null;
    Random random = new Random(System.nanoTime());

    Button bAns1 = null;
    Button bAns2 = null;
    Button bAns3 = null;
    Button bAns4 = null;

    @Override
    protected TrainingType getTrainingType() {
        return TrainingType.WORD_TRANS;
    }

    protected void onAnswer(boolean isCorrect) {
        currentSessionId = storeStatistic(currentWord.id, isCorrect, currentSessionId);
        nextQuestion();
    }

    protected void startTraining() {
        questionNum = 0;
        trainingWordBuilder.dictId = getCurrentDictionary().id;
        sessionWords = trainingWordBuilder.build(5, TrainingWordMethod.UNTRAINING_WORDS);
        sessionAnswers = trainingWordBuilder.buildRandomAnswers(5, 4);

        nextQuestion();
    }

    @Override
    public void onStart() {
        super.onStart();
        startTraining();
    }

    private void nextQuestion() {
        currentWord = sessionWords.get(questionNum);
        String correctAnswer = currentWord.getTranslation();
        int s = sessionAnswers.size();
        String[] ans = new String[]{
                currentWord.getTranslation(),
                sessionAnswers.get(random.nextInt(s)),
                sessionAnswers.get(random.nextInt(s)),
                sessionAnswers.get(random.nextInt(s))
        };
        List<String> answers = Arrays.asList(ans);
        Collections.shuffle(answers, random);
        int correctNum = answers.indexOf(correctAnswer) + 1;

        setQuestion(currentWord.getTitle(), currentWord.getContext(), answers.get(0),
                answers.get(1),
                answers.get(2),
                answers.get(3),
                correctNum);

        questionNum++;
    }

    protected void setQuestion(String word, String context, String ans1, String ans2, String ans3, String ans4,
                               int correctNum) {
        EditText edWord = (EditText) getView().findViewById(R.id.edTrainingWord);
        TextView tvContext = (TextView) getView().findViewById(R.id.tvTrainingContext);

        edWord.setText(word);
        tvContext.setText(context);
        if (context == null || context.isEmpty())
            tvContext.setVisibility(View.INVISIBLE);
        else tvContext.setVisibility(View.VISIBLE);

        bAns1.setText(ans1);
        bAns1.setTag(correctNum == 1);
        bAns2.setText(ans2);
        bAns2.setTag(correctNum == 2);
        bAns3.setText(ans3);
        bAns3.setTag(correctNum == 3);
        bAns4.setText(ans4);
        bAns4.setTag(correctNum == 4);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        bAns1 = (Button) view.findViewById(R.id.bWordTransAnswer1);
        bAns2 = (Button) view.findViewById(R.id.bWordTransAnswer2);
        bAns3 = (Button) view.findViewById(R.id.bWordTransAnswer3);
        bAns4 = (Button) view.findViewById(R.id.bWordTransAnswer4);

        bAns1.setOnClickListener(this);
        bAns2.setOnClickListener(this);
        bAns3.setOnClickListener(this);
        bAns4.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {
        Button bAns = (Button) v;
        boolean correct = (boolean) bAns.getTag();
        onAnswer(correct);
    }
}
