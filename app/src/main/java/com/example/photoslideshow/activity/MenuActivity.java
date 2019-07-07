package com.example.photoslideshow.activity;

import android.os.Bundle;

import com.example.photoslideshow.R;
import com.example.photoslideshow.fragment.MenuPreferenceFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_layout, MenuPreferenceFragment.newInstance())
                    .commit();
        }
    }
}
