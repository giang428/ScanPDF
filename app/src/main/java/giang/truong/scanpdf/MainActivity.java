package giang.truong.scanpdf;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import giang.truong.scanpdf.activity.OCRActivity;
import giang.truong.scanpdf.activity.PDFActivity;
import giang.truong.scanpdf.activity.SettingActivity;
import giang.truong.scanpdf.dao.DocumentDatabase;
import giang.truong.scanpdf.databinding.ActivityMainBinding;
import giang.truong.scanpdf.fragments.HomeFragment;
import giang.truong.scanpdf.utils.FileUtils;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding b;
    private SharedPreferences sharedPreferences;
    boolean isShow = false;
    Uri outputFileUri;
    ArrayList<Uri> te = new ArrayList<>();

    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(100), uris -> {
                if (!uris.isEmpty()) {
                    Log.d("PhotoPicker", "Number of items selected: " + uris.size());
                    Intent i = new Intent(this, PDFActivity.class);
                    ArrayList<Uri> listImages = new ArrayList<>();
                    for (Uri uri : uris) {
                        this.grantUriPermission(this.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        listImages.add(uri);
                    }
                    i.putParcelableArrayListExtra("LIST_IMG", listImages);
                    startActivity(i);
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });
    ActivityResultLauncher<Intent> startSetting =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == 12345) {
                            this.recreate();
                        }
                    });
    ActivityResultLauncher<Intent> captureImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    Intent intent = new Intent(this, OCRActivity.class);
                    intent.putExtra("img", outputFileUri.toString());
                    startActivity(intent);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            onResume();
        } else {
            allowPermissionForFile();
        }

        b = ActivityMainBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);
        //setLocale();
        setSupportActionBar(b.myToolbar);
        b.myToolbar.inflateMenu(R.menu.home_menu);
        setFAB();

        b.scanPDF.setOnClickListener(view1 -> {
            setFAB();
            pickMultipleMedia.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
        });

        b.scanOCR.setOnClickListener(view12 -> {
            setFAB();
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            outputFileUri = FileUtils.newImageUri(this);
            i.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            captureImageLauncher.launch(i);

        });

        DocumentDatabase.getInstance(getApplicationContext());

        HomeFragment homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, homeFragment).commit();


    }

    private void setFAB() {
        b.scanOCR.setVisibility(View.GONE);
        b.scanOCRText.setVisibility(View.GONE);
        b.scanPDF.setVisibility(View.GONE);
        b.scanPDFText.setVisibility(View.GONE);

        b.addButton.shrink();


        b.addButton.setOnClickListener(view -> {
            if (!isShow) {
                b.scanOCR.show();
                b.scanPDF.show();
                b.scanOCRText.setVisibility(View.VISIBLE);
                b.scanPDFText.setVisibility(View.VISIBLE);
                b.addButton.extend();
                isShow = true;
            } else {
                b.scanOCR.hide();
                b.scanPDF.hide();
                b.scanOCRText.setVisibility(View.GONE);
                b.scanPDFText.setVisibility(View.GONE);
                b.addButton.shrink();
                isShow = false;
            }
        });
    }

    private void setLocale() {
        Locale locale = new Locale(sharedPreferences.getString("language", "en"));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void allowPermissionForFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 2);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,}, 2);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingActivity.class);
            startSetting.launch(i);
            return true;
        } else if (id == R.id.action_about) {
            return true;
        } else return super.onOptionsItemSelected(item);

    }
}
