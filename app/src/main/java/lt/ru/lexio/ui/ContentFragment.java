package lt.ru.lexio.ui;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidparts.annotation.inject.InjectBundleExtra;

import lt.ru.lexio.R;

/**
 * Created by User on 14.03.2016.
 */
public class ContentFragment extends Fragment {

    public static final String ARG_LAYOUT_TO_APPEND = "layoutToAppend";
    public static final String ARG_ACTION_MENU_ID = "actionMenuId";
    public static final String ARG_NEED_REFRESH = "needRefresh";

    int actionMenuId;

    public ContentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getArguments() != null) {
            actionMenuId = getArguments().getInt(ARG_ACTION_MENU_ID);
            if (actionMenuId != 0)
                inflater.inflate(actionMenuId, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layout = -1;
        if (getArguments() == null)
            layout = R.layout.content_dictionaries;
        else
            layout = getArguments().getInt(ARG_LAYOUT_TO_APPEND);

        View view = inflater.inflate(layout, container, false);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
