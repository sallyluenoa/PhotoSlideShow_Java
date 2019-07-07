package com.example.photoslideshow.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.example.photoslideshow.R;

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
    }
}
