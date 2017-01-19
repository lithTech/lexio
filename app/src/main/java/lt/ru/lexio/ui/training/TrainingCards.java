package lt.ru.lexio.ui.training;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;

import java.util.Date;
import java.util.List;
import java.util.Random;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;
import lt.ru.lexio.db.WordDAO;
import lt.ru.lexio.db.WordStatistic;
import lt.ru.lexio.db.WordStatisticDAO;
import lt.ru.lexio.ui.Flip3dAnimation;
import lt.ru.lexio.util.AdvertiseHelper;

/**
 * Created by lithTech on 19.04.2016.
 */
public class TrainingCards extends TrainingFragmentBase implements View.OnTouchListener, View.OnClickListener {

    TextView tvWord;
    TextView tvTranscription;
    View content;
    LinearLayout card;

    FloatingActionButton bTrainingCardsWrong;
    FloatingActionButton bTrainingCardsOk;
    Boolean result = null;

    final int DEFAULT_CARD_TEXT_SIZE = 30;

    View bReload;
    View bNext;

    float nqSWIPE_MIN = 150;
    float nqX1, nqX2;

    float fcSWIPE_MIN = 10;
    float fcX1, fcX2;

    Flip3dAnimation flip3dAnimationRtL;
    Flip3dAnimation flip3dAnimationLtR;

    @Override
    protected int getTrainingPageContainerId() {
        return R.id.layout_trans_cards_container;
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
        nextQuestion(false);
    }

    @Override
    protected void startTraining() {
        showAnswerButtons((ViewGroup) getView(), View.VISIBLE);
    }

    @Override
    protected void onNextQuestion() {
        tvWord.setText(currentWord.getTitle());
        tvWord.setTag(true);

        tvTranscription.setText("");
        if (currentWord.getTranscription() != null && !currentWord.getTranscription().isEmpty())
            tvTranscription.setText("[" + currentWord.getTranscription() + "]");

        result = null;
        WordStatistic statistic = sessionStatistics.get(currentWord.id);
        if (statistic != null) {
            result = statistic.getTrainingResult() == 1;
        }
        setAnswerColors();
    }

    public void flipCard() {

        tvWord.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CARD_TEXT_SIZE);

        if ((boolean)tvWord.getTag() == true) {
            tvWord.setText(currentWord.getTranslation());
        }
        else
            tvWord.setText(currentWord.getTitle());
        tvWord.setTag(!(boolean) tvWord.getTag());
    }

    @Override
    protected void setEndPageStatistics(int correct, int incorrect) {
        ViewGroup currentView = (ViewGroup) getView();

        showAnswerButtons(currentView, View.GONE);

        EndPageStatisticFiller filler = new EndPageStatisticFiller() {
            @Override
            public void fill(Word word, @Nullable WordStatistic storedStatistic, EndPageStatistic out) {
                out.question = word.getTitle();
                out.correctAnswer = word.getTranslation();
            }
        };
        List<EndPageStatistic> statistics = getEndPageStatistic(filler);

        ViewGroup viewGroup = (ViewGroup) currentView.findViewById(R.id.layout_train_end_page);
        View fragment = viewGroup.getChildAt(0);
        ((TextView) fragment.findViewById(R.id.tvTrainingEndPageCorrect))
                .setText(String.valueOf(correct));
        ((TextView) fragment.findViewById(R.id.tvTrainingEndPageInCorrect))
                .setText(String.valueOf(incorrect));

        ListView lWordStatistic = (ListView) fragment.findViewById(R.id.lvTrainingEndPageWordStat);
        lWordStatistic.setAdapter(TrainingEndPageFragment.setData(fragment.getContext(), statistics));

        ViewGroup adView = (ViewGroup) getView().findViewById(R.id.adView);
        if (adView != null)
            if (!AdvertiseHelper.isFlavorWithoutAds())
                AdvertiseHelper.loadAd(getActivity(), adView,
                        getString(R.string.content_endpagestat_banner), null);
    }

    private void showAnswerButtons(ViewGroup currentView, int visible) {
        View answerButtons = currentView.findViewById(R.id.cTrainingCardAnswerButtons);
        answerButtons.setVisibility(visible);
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
        return true;
    }

    private Flip3dAnimation getFlipAnimation(float fD, float tD, Animation.AnimationListener onEnd) {
        float center = card.getMeasuredWidth() / 2.0f;
        Flip3dAnimation flip3dAnimation = new Flip3dAnimation(fD, tD, center, center);
        flip3dAnimation.setDuration(200);
        flip3dAnimation.setFillAfter(false);
        flip3dAnimation.setInterpolator(new AccelerateInterpolator());
        flip3dAnimation.setAnimationListener(onEnd);
        return flip3dAnimation;
    }

    @Override
    protected TrainingType getTrainingType() {
        return TrainingType.CARDS;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        card = (LinearLayout) view.findViewById(R.id.cTrainingCard);
        card.setOnTouchListener(this);
        tvWord = (TextView) card.findViewById(R.id.tvWord);
        tvTranscription = (TextView) card.findViewById(R.id.tvTranscription);

        content = view.findViewById(R.id.layout_trans_cards_container);
        content.setOnTouchListener(this);

        createAnimationsAndResolveWidths();

        bTrainingCardsOk = (FloatingActionButton) view.findViewById(R.id.bTrainingCardsOK);
        bTrainingCardsWrong = (FloatingActionButton) view.findViewById(R.id.bTrainingCardsWrong);
        bTrainingCardsOk.setOnClickListener(this);
        bTrainingCardsWrong.setOnClickListener(this);

        return view;
    }

    private void createAnimationsAndResolveWidths() {
        final Animation.AnimationListener onFlipEnd = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                flipCard();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        ViewTreeObserver vto = card.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                flip3dAnimationLtR = getFlipAnimation(0, 180, onFlipEnd);
                flip3dAnimationRtL = getFlipAnimation(0, -180, onFlipEnd);

                ViewTreeObserver obs = card.getViewTreeObserver();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == content) {
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    nqX1 = event.getX();
                    return true;
                case MotionEvent.ACTION_UP:
                    nqX2 = event.getX();
                    float deltaX = nqX2 - nqX1;
                    boolean isR2L = deltaX < 0;
                    boolean isSwipe = Math.abs(deltaX) > nqSWIPE_MIN;
                    if (isSwipe && isR2L)
                    {
                        nextQuestion(result);
                        return true;
                    }
                    else if (isSwipe && !isR2L)
                    {
                        prevQuestion(result);
                        return true;
                    }
                    break;
            }
        }
        else if (v == card) {
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    fcX1 = event.getX();
                    return true;
                case MotionEvent.ACTION_UP:
                    fcX2 = event.getX();
                    float deltaX = fcX2 - fcX1;
                    boolean isR2L = deltaX < 0;
                    boolean isSwipe = Math.abs(deltaX) > fcSWIPE_MIN;
                    if (!isSwipe)
                    {
                        Animation animation = flip3dAnimationLtR;
                        if (isR2L)
                            animation = flip3dAnimationRtL;
                        card.startAnimation(animation);
                        return true;
                    }
                    else if (isR2L)
                        nextQuestion(result);
                    else
                        prevQuestion(result);
                    break;
            }
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        if (v == bReload) {
            setCurrentPage(currentPage - 1);
            onStart();
        } else if (v == bNext) {
            onStart();
        } else if (v == bTrainingCardsOk) {
            result = Boolean.TRUE;
        } else if (v == bTrainingCardsWrong) {
            result = Boolean.FALSE;
        }
        setAnswerColors();
    }

    private void setAnswerColors() {
        int h = bTrainingCardsOk.getColorPressed();
        int n = getResources().getColor(R.color.colorPrimaryDark);
        bTrainingCardsOk.setColorNormal(result == Boolean.TRUE ? h : n);
        bTrainingCardsWrong.setColorNormal(result == Boolean.FALSE ? h : n);
    }
}
