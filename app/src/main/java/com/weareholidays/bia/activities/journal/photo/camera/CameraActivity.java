package com.weareholidays.bia.activities.journal.photo.camera;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.PlaceholderFragment;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, Camera2BasicFragment.newInstance())
                        .commit();
            }
            else{
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance())
                        .commit();
            }
        }
    }

}
