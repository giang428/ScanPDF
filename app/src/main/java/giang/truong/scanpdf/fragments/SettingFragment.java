package giang.truong.scanpdf.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Locale;

import giang.truong.scanpdf.MainActivity;
import giang.truong.scanpdf.R;


public class SettingFragment extends PreferenceFragmentCompat {
    private static SharedPreferences mSharedPreferences;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.perference, rootKey);
        ListPreference mLanguage = findPreference("language");
        if(mLanguage!=null){
            mLanguage.setOnPreferenceChangeListener((preference, newValue) -> {
                Locale locale = new Locale((String) newValue);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.setLocale(locale);
                requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());
                requireActivity().setResult(12345);
                requireActivity().finish();
                return true;
            });

            }
        }
}