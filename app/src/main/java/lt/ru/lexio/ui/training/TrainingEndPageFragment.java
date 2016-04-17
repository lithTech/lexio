package lt.ru.lexio.ui.training;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import lt.ru.lexio.R;

/**
 * Created by lithTech on 02.04.2016.
 */
public class TrainingEndPageFragment extends Fragment {

    ListView lWordStatistic;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_training_end_page, container, false);

        view.findViewById(R.id.bTrainingEndPageTryAgain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getActivity().getFragmentManager()
                        .findFragmentById(R.id.content_fragment_parent);
                if (fragment != null && fragment instanceof TrainingFragmentBase) {
                    fragment.onStart();
                }
            }
        });

        lWordStatistic = (ListView) view.findViewById(R.id.lvTrainingEndPageWordStat);
        


        return view;
    }
}
