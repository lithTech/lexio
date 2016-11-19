package lt.ru.lexio.ui;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
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
    public static final String ARG_TRAINING_TO_RUN_TITLE = "trainingToRunTitle";
    public static final String ARG_TRAINING_TO_RUN = "trainingToRun";
    public static final String ARG_TRAINING_START_LIST = "trainingStartWordList";
    public static final String ARG_TRAINING_WORD_ORDER = "trainingWordOrder";
    public static final String ARG_TRAINING_ANSWER_TIMER = "trainingAnswerTimer";
    public static final String ARG_TRAINING_WORD_COUNT = "trainingWordCount";

    protected MainActivity mainActivity;

    int actionMenuId;

    public ContentFragment() {
    }

    public int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            this.mainActivity = (MainActivity) activity;
        }
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
