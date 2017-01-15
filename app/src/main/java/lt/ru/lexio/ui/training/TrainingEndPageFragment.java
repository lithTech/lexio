package lt.ru.lexio.ui.training;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionMenu;

import java.util.List;

import lt.ru.lexio.R;
import lt.ru.lexio.ui.ContentFragment;
import lt.ru.lexio.ui.FragmentTitleMapper;
import lt.ru.lexio.ui.MainActivity;
import lt.ru.lexio.util.TutorialHelper;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.target.Target;
import uk.co.deanwild.materialshowcaseview.target.ViewTarget;

/**
 * Created by lithTech on 02.04.2016.
 */
public class TrainingEndPageFragment extends Fragment implements View.OnClickListener, FloatingActionMenu.OnMenuToggleListener {

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

        FloatingActionMenu trainingMenu = (FloatingActionMenu) view.findViewById(R.id.amTrainingMenu);
        trainingMenu.setOnMenuToggleListener(this);

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
        lWordStatistic.setAdapter(setData(getView().getContext(), statistic));
    }

    public static EndPageStatAdapter setData(Context context, List<EndPageStatistic> statistic) {
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
        if (v.getId() == R.id.bTrainingEndPageNext) {
            Fragment fragment = getActivity().getFragmentManager()
                    .findFragmentById(R.id.content_fragment_parent);
            if (fragment != null && fragment instanceof TrainingFragmentBase) {
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

                    TrainingFragmentBase trFr = (TrainingFragmentBase) ((MainActivity) getActivity()).getCurrentFragment();
                    int currentPage = trFr.currentPage;

                    TrainingManager trainingManager = new TrainingManager();
                    Bundle args = new Bundle();
                    args.putInt(ContentFragment.ARG_TRAINING_TO_RUN, id);
                    args.putLongArray(ContentFragment.ARG_TRAINING_START_LIST, getWordList());
                    args.putInt(ContentFragment.ARG_LAYOUT_TO_APPEND, R.layout.content_training_start);
                    args.putInt(ContentFragment.ARG_TRAINING_TO_RUN_TITLE,
                            FragmentTitleMapper.getTitleResId(getResources(), id));
                    args.putInt(ContentFragment.ARG_TRAINING_TO_RUN_PAGE, currentPage);

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

    @Override
    public void onMenuToggle(boolean opened) {
        /*for (int button : trainingButtons) {
            View view = getView().findViewById(button);
            if (view != null) {
                String idName = (String) view.getTag();
                int id = getResources().getIdentifier(idName, "layout", getActivity().getPackageName());
                int currentId = getArguments().getInt(ContentFragment.ARG_TRAINING_TO_RUN);

                if (id == currentId) {
                    view.setVisibility(opened ? View.GONE : View.VISIBLE);
                }
            }
        }*/
    }
}
