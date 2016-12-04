package lt.ru.lexio.ui.settings;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import lt.ru.lexio.R;
import lt.ru.lexio.db.WordStatisticDAO;
import lt.ru.lexio.ui.DialogHelper;
import lt.ru.lexio.util.DimensionHelper;
import lt.ru.lexio.util.WordLearnNotifyHelper;

/**
 * Created by User on 15.03.2016.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener{

    WordStatisticDAO wordStatisticDAO = null;

    public static final String TAG = SettingsFragment.class.getName();
    public static final String SETTINGS_FILE_NAME = TAG + ".lexio.settings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(SETTINGS_FILE_NAME);
        addPreferencesFromResource(R.xml.pref_general);

        Preference clearStatPref = findPreference("pref_clear_statistics");
        clearStatPref.setOnPreferenceClickListener(this);

        Preference alarmWordLearn = findPreference("pref_alarm_training");
        alarmWordLearn.setOnPreferenceClickListener(this);

        Preference alarmWordTime = findPreference("pref_alarm_training_time");
        alarmWordTime.setOnPreferenceChangeListener(this);

        wordStatisticDAO = new WordStatisticDAO(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        view.setPadding(0, DimensionHelper.getActionBarSize(getActivity()), 0, 0);

        return view;
    }

    private int getH(long time) {
        return (int) TimeUnit.MILLISECONDS.toHours(time);
    }

    private int getM(long time) {
        return (int) TimeUnit.MILLISECONDS.toMinutes(time - getH(time) * 1000 * 60 * 60);
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
            return true;
        } else if ("pref_alarm_training".equalsIgnoreCase(preference.getKey())) {
            if (((SwitchPreference) preference).isChecked()) {
                long time = getPreferenceManager().getSharedPreferences().getLong("pref_alarm_training_time",
                        getResources().getInteger(R.integer.pref_alarm_training_time));
                time += TimeZone.getDefault().getRawOffset();
                WordLearnNotifyHelper.scheduleNotification(getActivity(),
                        WordLearnNotifyHelper.getNotification(getActivity(),
                                getString(R.string.notify_word_learn_body),
                                getString(R.string.notify_word_learn_title)),
                        getH(time), getM(time));
            }
            else
                WordLearnNotifyHelper.cancelNotificationSchedule(getActivity());

            findPreference("pref_alarm_training_time").setEnabled(((SwitchPreference) preference).isChecked());

            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("pref_alarm_training_time".equalsIgnoreCase(preference.getKey())) {
            long time = Long.parseLong(newValue.toString());
            time += TimeZone.getDefault().getRawOffset();
            WordLearnNotifyHelper.scheduleNotification(getActivity(),
                    WordLearnNotifyHelper.getNotification(getActivity(),
                            getString(R.string.notify_word_learn_body),
                            getString(R.string.notify_word_learn_title)),
                    getH(time), getM(time));
            return true;
        }
        return false;
    }
}
