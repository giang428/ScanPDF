package giang.truong.scanpdf.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import giang.truong.scanpdf.R;
import giang.truong.scanpdf.databinding.ActivitySettingBinding;
import giang.truong.scanpdf.fragments.SettingFragment;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getSupportFragmentManager()
                .beginTransaction().replace(R.id.fff,new SettingFragment()).commit();
    }

    @Override
    public void finish() {
        super.finish();
    }
}