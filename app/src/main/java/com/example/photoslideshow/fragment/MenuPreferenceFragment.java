package com.example.photoslideshow.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.example.photoslideshow.R;
import com.example.photoslideshow.utils.PreferenceUtils;

public class MenuPreferenceFragment extends PreferenceFragmentCompat {

    public static MenuPreferenceFragment newInstance() {
        return newInstance(null);
    }

    public static MenuPreferenceFragment newInstance(@Nullable String rootKey) {
        MenuPreferenceFragment fragment = new MenuPreferenceFragment();
        if (rootKey != null) {
            Bundle args = new Bundle();
            args.putString(ARG_PREFERENCE_ROOT, rootKey);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference accountPref = findPreference(getString(R.string.pref_key_account_name));
        accountPref.setSummary(PreferenceUtils.getAccountName(getContext()));

        Preference emailPref = findPreference(getString(R.string.pref_key_email_address));
        emailPref.setSummary(PreferenceUtils.getEmail(getContext()));

        ListPreference maxCountPref = (ListPreference) findPreference(getString(R.string.pref_key_max_count_list));
        maxCountPref.setSummary(maxCountPref.getValue());
        maxCountPref.setOnPreferenceChangeListener((preference, newValue) -> {
            maxCountPref.setSummary(String.valueOf(newValue));
            return true;
        });

        ListPreference timeIntervalPref = (ListPreference) findPreference(getString(R.string.pref_key_time_interval_list));
        timeIntervalPref.setSummary(timeIntervalPref.getValue());
        timeIntervalPref.setOnPreferenceChangeListener((preference, newValue) -> {
            timeIntervalPref.setSummary(String.valueOf(newValue));
            return true;
        });
    }
}
