package lt.ru.lexio.ui.training;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

import lt.ru.lexio.R;
import lt.ru.lexio.db.Word;

/**
 * Created by lithTech on 02.04.2016.
 */
public class TrainingEndPageFragment extends Fragment implements View.OnClickListener{

    ListView lWordStatistic;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_training_end_page, container, false);

        view.findViewById(R.id.bTrainingEndPageTryAgain).setOnClickListener(this);
        view.findViewById(R.id.bTrainingEndPageNext).setOnClickListener(this);

        lWordStatistic = (ListView) view.findViewById(R.id.lvTrainingEndPageWordStat);

        return view;
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

    @Override
    public void onClick(View v) {
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
}
