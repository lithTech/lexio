package lt.ru.lexio.ui;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lt.ru.lexio.R;
import lt.ru.lexio.db.WordStatisticDAO;

/**
 * Created by User on 15.03.2016.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    WordStatisticDAO wordStatisticDAO = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        Preference clearStatPref = findPreference("pref_clear_statistics");
        clearStatPref.setOnPreferenceClickListener(this);

        wordStatisticDAO = new WordStatisticDAO(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);


        int[] actionBarSizeArr = new int[] { android.R.attr.actionBarSize };
        int indexOfAttrTextSize = 0;
        TypedValue typedValue = new TypedValue();
        TypedArray a = getActivity().obtainStyledAttributes(typedValue.data, actionBarSizeArr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();

        view.setPadding(0, actionBarSize, 0, 0);

        return view;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ("pref_clear_statistics".equalsIgnoreCase(preference.getKey())) {
            if (DialogHelper.confirm(getActivity(),
                    getActivity().getString(R.string.settings_ClearAlert_Header),
                    getActivity().getString(R.string.settings_ClearAlert_Message),
                    getActivity().getString(R.string.dialog_Cancel),
                    getActivity().getString(R.string.dialog_Clear),
                    new Runnable() {
                        @Override
                        public void run() {
                            wordStatisticDAO.clear();
                        }
                    }, null));
        }
        return false;
    }
}
