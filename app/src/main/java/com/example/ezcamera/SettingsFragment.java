package com.example.ezcamera;

import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("deprecation")
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";

    static Camera mCamera;
    static Camera.Parameters mParameters;

    public static final String KEY_PREF_PREV_SIZE = "preview_size";




    private static void initSummary(Preference pref) {
        Log.d(TAG, "initSummary ...");

        if (pref instanceof PreferenceGroup) {
            PreferenceGroup prefGroup = (PreferenceGroup) pref;
            for (int i=0; i<prefGroup.getPreferenceCount(); i++) {
                initSummary(prefGroup.getPreference(i));
            }
        }
        else {
            updatePrefSummary(pref);
        }
    }

    private static void updatePrefSummary(Preference pref) {
        Log.d(TAG, "updatePrefSummary ...");
        if (pref instanceof ListPreference) {
            pref.setSummary(((ListPreference)pref).getEntry());
        }
    }

    public static void init(SharedPreferences sharedPrefs) {
        setPreviewSize(sharedPrefs.getString(KEY_PREF_PREV_SIZE, ""));

        mCamera.stopPreview();
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    private static void setPreviewSize(String value) {
        String[] split = value.split("x");

        mParameters.setPreviewSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        mParameters.setPictureSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    public static void setDefault(SharedPreferences sharedPrefs) {

        String valPreviewSize = sharedPrefs.getString(KEY_PREF_PREV_SIZE, null);
        Log.d(TAG, "setDefault KEY_PREF_PREV_SIZE " + valPreviewSize);

        if (valPreviewSize == null) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(KEY_PREF_PREV_SIZE, getDefaultPreviewSize());
            editor.apply();
        }
    }

    private static String getDefaultPreviewSize() {
        Camera.Size previewSize = mParameters.getPreviewSize();
        Log.d(TAG, "getDefaultPreviewSize " + previewSize.width + "x" + previewSize.height);

        return previewSize.width + "x" + previewSize.height;
    }


    private void stringListToListPreference(List<String> list, String key) {
        final CharSequence[] charSeq = list.toArray(new CharSequence[list.size()]);

        ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(key);
        listPref.setEntries(charSeq);
        listPref.setEntryValues(charSeq);
    }

    private void cameraSizeListToListPreference(List<Camera.Size> list, String key) {

        List<String> stringList = new ArrayList<>();
        for (Camera.Size size : list) {
            String stringSize = size.width + "x" + size.height;
            stringList.add(stringSize);
        }
        stringListToListPreference(stringList, key);
    }

    private void loadSupportedPreviewSize() {
        Log.d(TAG, "loadSupportedPreviewSize ...");
        cameraSizeListToListPreference(mParameters.getSupportedPreviewSizes(), KEY_PREF_PREV_SIZE);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume ...");

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause ...");

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate ...");

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        loadSupportedPreviewSize();

        initSummary(getPreferenceScreen());
    }

    public static void passCamera(Camera camera) {
        mCamera = camera;
        mParameters = camera.getParameters();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


        switch(key) {
            case KEY_PREF_PREV_SIZE:
                setPreviewSize(sharedPreferences.getString(key, ""));
                updatePrefSummary(findPreference(key));
                break;
        }
        mCamera.stopPreview();
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }




}
