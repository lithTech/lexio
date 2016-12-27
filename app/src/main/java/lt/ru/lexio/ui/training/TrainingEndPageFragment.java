package lt.ru.lexio.ui.training;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ListView;

import java.util.List;

import lt.ru.lexio.R;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.FragmentTitleMapper;
import lt.ru.lexio.ui.MainActivity;
import lt.ru.lexio.util.AdvertiseHelper;

/**
 * Created by lithTech on 02.04.2016.
 */
public class TrainingEndPageFragment extends Fragment implements View.OnClickListener{

    ListView lWordStatistic;

    int[] trainingButtons = new int[]{
            R.id.bTrainingCards,
            R.id.bTrainingWordByTrans,
            R.id.bTrainingTransByWord,
            R.id.bTrainingTransVoice,
            R.id.bTrainingTransWrite
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_training_end_page, container, false);

        view.findViewById(R.id.bTrainingEndPageNext).setOnClickListener(this);

        lWordStatistic = (ListView) view.findViewById(R.id.lvTrainingEndPageWordStat);

        for (int tb : trainingButtons) {
            View v = view.findViewById(tb);
            v.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void setData(List<EndPageStatistic> statistic) {
        lWordStatistic.setAdapter(initAdapter(getView().getContext(), statistic));
    }

    public static EndPageStatAdapter initAdapter(Context context, List<EndPageStatistic> statistic) {
        EndPageStatAdapter adapter = new EndPageStatAdapter(context, R.layout.content_training_result_item,
                R.id.tvCorrectAnswer,
                statistic);

        return adapter;
    }

    public long[] getWordList() {
        EndPageStatAdapter adapter = (EndPageStatAdapter) lWordStatistic.getAdapter();
        long[] list = new long[adapter.getStatistics().size()];
        List<EndPageStatistic> statistics = adapter.getStatistics();
        for (int i = 0; i < statistics.size(); i++) {
            EndPageStatistic s = statistics.get(i);
            list[i] = s.id;
        } ;
        return list;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bTrainingEndPageTryAgain ||
                v.getId() == R.id.bTrainingEndPageNext) {
            Fragment fragment = getActivity().getFragmentManager()
                    .findFragmentById(R.id.content_fragment_parent);
            if (fragment != null && fragment instanceof TrainingFragmentBase) {
                if (v.getId() == R.id.bTrainingEndPageTryAgain) {
                    int page = ((TrainingFragmentBase) fragment).getCurrentPage();
                    ((TrainingFragmentBase) fragment).setCurrentPage(page - 1);
                }
                fragment.onStart();
            }
        }
        else {
            for (int tb : trainingButtons) {
                if (v.getId() == tb && v.getTag() != null) {
                    TrainingFragmentBase fragment = (TrainingFragmentBase) getActivity().getFragmentManager()
                            .findFragmentById(R.id.content_fragment_parent);

                    String idName = (String) v.getTag();

                    int id = getResources().getIdentifier(idName, "layout", getActivity().getPackageName());

                    TrainingManager trainingManager = new TrainingManager();
                    Bundle args = new Bundle();
                    args.putInt(ContentFragment.ARG_TRAINING_TO_RUN, id);
                    args.putLongArray(ContentFragment.ARG_TRAINING_START_LIST, getWordList());
                    args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, R.layout.content_training_start);
                    args.putInt(ContentFragment.ARG_TRAINING_TO_RUN_TITLE,
                            FragmentTitleMapper.getTitleResId(getResources(), id));

                    args.putInt(ContentFragment.ARG_TRAINING_WORD_COUNT,
                            fragment.getArguments().getInt(ContentFragment.ARG_TRAINING_WORD_COUNT));
                    args.putInt(ContentFragment.ARG_TRAINING_WORD_ORDER,
                            fragment.getArguments().getInt(ContentFragment.ARG_TRAINING_WORD_ORDER));
                    args.putInt(ContentFragment.ARG_TRAINING_WORD_TIME,
                            fragment.getArguments().getInt(ContentFragment.ARG_TRAINING_WORD_TIME));

                    ((MainActivity) getActivity()).changeFragment(trainingManager, args, "");
                }
            }
        }
    }
}
