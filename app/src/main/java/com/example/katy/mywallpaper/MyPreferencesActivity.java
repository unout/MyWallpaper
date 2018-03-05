package com.example.katy.mywallpaper;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class MyPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
//        WallpaperManager.getInstance(this).getWallpaperInfo()
        // add a validator to the "numberOfCircles" preference so that it only
        // accepts numbers
        Preference circlePreference = getPreferenceScreen().findPreference("numberOfCircles");
        Preference probabilityPreference = getPreferenceScreen().findPreference("probabilityOfCircle");
//        circlePreference.getEditor().putInt().commit();

        // add the validator
        circlePreference.setOnPreferenceChangeListener(numberCheckListener);
        probabilityPreference.setOnPreferenceChangeListener(probabilityCheckListener);

        findPreference("backgroundColor").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue))));
                return true;
            }
        });
        findPreference("circlesColor").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue))));
                return true;
            }
        });
        findPreference("touchColor").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue))));
                return true;
            }
        });
    }

    Preference.OnPreferenceChangeListener numberCheckListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // check that the string is an integer
            if (newValue != null && newValue.toString().length() > 0
                    && newValue.toString().matches("\\d*")) {
                try {
                    int value = Integer.valueOf(newValue.toString());
                    if (value >= 0 && value <= 100) {
                        preference.setSummary(String.valueOf(value));
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            // If now create a message to the user
            Toast.makeText(MyPreferencesActivity.this, "Invalid Input",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    };

    Preference.OnPreferenceChangeListener probabilityCheckListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // check that the string is an integer
            if (newValue != null && newValue.toString().length() > 0
                    && newValue.toString().matches("\\d*")) {
                try {
                    int value = Integer.valueOf(newValue.toString());
                    if (value >= 0 && value <= 100) {
                        preference.setSummary(String.valueOf(value) + "%");
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            // If now create a message to the user
            Toast.makeText(MyPreferencesActivity.this, "Invalid Input",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    };
}
