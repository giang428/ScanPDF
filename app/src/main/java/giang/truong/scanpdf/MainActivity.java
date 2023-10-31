package giang.truong.scanpdf;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomappbar.BottomAppBar;

import java.util.ArrayList;
import java.util.Locale;

import giang.truong.scanpdf.activity.PDFActivity;
import giang.truong.scanpdf.dao.DocumentDatabase;
import giang.truong.scanpdf.databinding.ActivityMainBinding;
import giang.truong.scanpdf.fragments.HomeFragment;
import giang.truong.scanpdf.fragments.SettingFragment;
import giang.truong.scanpdf.utils.FileUtils;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding b;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);

        Locale locale = new Locale(sharedPreferences.getString("language","en"));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            onResume();
        } else {
            allowPermissionForFile();
        }
        DocumentDatabase.getInstance(getApplicationContext());

        FileUtils.clearDirectory(String.valueOf(getExternalFilesDir(Environment.DIRECTORY_PICTURES)));

        HomeFragment homeFragment = new HomeFragment();
        loadFragment(homeFragment);

        b.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            if (item.getItemId() == R.id.setting) {
                fragment = new SettingFragment();
                b.fab.setVisibility(View.GONE);
                b.bottomAppBar.setFabAnchorMode(BottomAppBar.FAB_ANCHOR_MODE_EMBED);
            } else {
                fragment = homeFragment;
                b.fab.setVisibility(View.VISIBLE);
                b.bottomAppBar.setFabAnchorMode(BottomAppBar.FAB_ANCHOR_MODE_CRADLE);
            }
            return loadFragment(fragment);
        });

        ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
                registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(MediaStore.getPickImagesMaxLimit()), uris -> {
                    // Callback is invoked after the user selects media items or closes the
                    // photo picker.
                    if (!uris.isEmpty()) {
                        Log.d("PhotoPicker", "Number of items selected: " + uris.size());
                        Intent i = new Intent(this,PDFActivity.class);
                        i.putParcelableArrayListExtra("LIST_IMG",new ArrayList<>(uris));
                        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(i);
                        finish();
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        b.fab.setOnClickListener(
                v -> pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        }
        return false;

    }

    private void allowPermissionForFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 2);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,}, 2);
        }
    }
}