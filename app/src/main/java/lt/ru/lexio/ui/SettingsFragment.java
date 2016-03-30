package lt.ru.lexio.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lt.ru.lexio.R;
import lt.ru.lexio.db.WordStatisticDAO;

/**
 * Created by User on 15.03.2016.
 */
public class SettingsFragment extends ContentFragment implements View.OnClickListener {

    WordStatisticDAO wordStatisticDAO = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        wordStatisticDAO = new WordStatisticDAO(view.getContext());

        view.findViewById(R.id.bSettingsClearStatistics).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bSettingsClearStatistics) {
            if (DialogHelper.confirm(getActivity(), "Statistics clear", "Are you sure?!?", "Cancel",
                    "Sure", new Runnable() {
                        @Override
                        public void run() {
                            wordStatisticDAO.clear();
                        }
                    }, null));
        }
    }
}
