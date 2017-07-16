package com.treefrogapps.waveformoptimised;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.treefrogapps.waveformoptimised.WaveFileReader.WaveFile;
import com.treefrogapps.waveformoptimised.WaveFileReader.WaveView;

import java.io.File;
import java.util.ArrayList;

public class WaveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave);

        if(!checkPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, this)){
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, this);
        } else {
            loadFile();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 10 && checkPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, this)){
            loadFile();
        }
    }

    private void requestPermissions(@NonNull String[] permissions, @NonNull Activity activity) {
        final ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (!checkPermissions(new String[]{permission}, activity)) {
                permissionsToRequest.add(permission);
            }
        }
        ActivityCompat.requestPermissions(activity, permissionsToRequest.toArray(new String[permissionsToRequest.size()]), 10);
    }

    private static boolean checkPermissions(@NonNull String[] permissions, Context context) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void loadFile(){
        final FrameLayout container = (FrameLayout) findViewById(R.id.container);
        final File waveFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + "waveFile.wav");
        if(waveFile.exists()){
            final WaveView waveView = new WaveView(this, new WaveFile(waveFile));
            final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            container.addView(waveView, params);
        }
    }
}
