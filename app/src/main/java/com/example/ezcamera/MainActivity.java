package com.example.ezcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import static android.app.FragmentTransaction.TRANSIT_FRAGMENT_CLOSE;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    private CameraPreview mPreview;
    private boolean mShow = false;
    private SettingsFragment setFra;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "has camera " +  this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA));
        Log.d(TAG, "has camera " +  Camera.getNumberOfCameras());

        Log.d(TAG, "onCreate ...");
        if (mPreview == null) {
            initCamera();
        }

        SettingsFragment.passCamera(mPreview.getCameraInstance());
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));


        Button buttonSettings = (Button) findViewById(R.id.button_settings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, "buttonSettings onClick ...");
                if (setFra == null) {
                    setFra = new SettingsFragment();
                }

                if (!mShow) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.camera_preview, setFra).addToBackStack(null).commit();
                    mShow = true;
                    Log.d(TAG, "buttonSettings onClick show ...");
                }
                else {
                    getFragmentManager().beginTransaction().setTransition (TRANSIT_FRAGMENT_CLOSE);
                    mShow = false;
                    Log.d(TAG, "buttonSettings onClick hide ...");
                }

            }
        });


        Button buttonpic = (Button) findViewById(R.id.button_pic);
        buttonpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick ...");

                mPreview.takePicture();
            }
        });

    }

    private void initCamera() {
        Log.d(TAG, "initCamera ...");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mPreview = new CameraPreview(this);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume ...");

        if (mPreview == null) {
            initCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause ...");

        mPreview = null;
    }
}