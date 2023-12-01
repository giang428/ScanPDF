package giang.truong.scanpdf.activity;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import giang.truong.scanpdf.R;
import giang.truong.scanpdf.adapter.ListImageAdapter;
import giang.truong.scanpdf.adapter.ThumbnailPreviewAdapter;
import giang.truong.scanpdf.databinding.ActivityPdfactivityBinding;
import giang.truong.scanpdf.databinding.SaveDialogBinding;
import giang.truong.scanpdf.fragments.ReorderFragment;
import giang.truong.scanpdf.model.Document;
import giang.truong.scanpdf.utils.DepthPageTransformer;
import giang.truong.scanpdf.utils.PDFWriterUtil;
import giang.truong.scanpdf.viewmodel.DocumentViewModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class PDFActivity extends AppCompatActivity {
    private ActivityPdfactivityBinding b;
    private ArrayList<Uri> listImg;
    private ListImageAdapter viewPagerAdapter;
    private DocumentViewModel documentViewModel;
    private AlertDialog prg;
    private ThumbnailPreviewAdapter thumbnailPreviewAdapter;

    private final ActivityResultLauncher<Intent> edited_img =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri i = result.getData().getParcelableExtra(ScanConstants.SCANNED_RESULT);
                    try {
                        int cur = b.viewpager.getCurrentItem();
                        listImg.set(cur, i);
                        viewPagerAdapter.notifyDataSetChanged();
                        b.viewpager.setAdapter(viewPagerAdapter);
                        b.viewpager.setCurrentItem(cur);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(MediaStore.getPickImagesMaxLimit()), uris -> {
                if (!uris.isEmpty()) {
                    Log.d("PhotoPicker", "Number of items selected: " + uris.size());
                    listImg.addAll(uris);
                    viewPagerAdapter.notifyDataSetChanged();
                    b.viewpager.setAdapter(viewPagerAdapter);
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPdfactivityBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        documentViewModel = new ViewModelProvider(this).get(DocumentViewModel.class);
        listImg = getIntent().getParcelableArrayListExtra("LIST_IMG");

        viewPagerAdapter = new ListImageAdapter(this, listImg);
        b.viewpager.setAdapter(viewPagerAdapter);
        b.viewpager.setPageTransformer(true, new DepthPageTransformer());

        thumbnailPreviewAdapter = new ThumbnailPreviewAdapter(this, listImg, integer -> {
            b.viewpager.setCurrentItem(integer);
            return Unit.INSTANCE;
        });
        b.thumbnail.setAdapter(thumbnailPreviewAdapter);
        LinearLayoutManager l = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        b.thumbnail.setLayoutManager(l);

        final LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(b.thumbnail);
        b.thumbnail.setOnFlingListener(snapHelper);

        b.viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                thumbnailPreviewAdapter.updateSelectedPosition(position);
                b.thumbnail.smoothScrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        b.add.setOnClickListener(v -> pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build())
        );

        b.delete.setOnClickListener(v -> {
            int pos = b.viewpager.getCurrentItem();
            if (listImg.size() == 1)
                Toast.makeText(this, R.string.cannot_delete, Toast.LENGTH_SHORT).show();
            else {
                listImg.remove(pos);
                viewPagerAdapter.notifyDataSetChanged();
                b.viewpager.setAdapter(viewPagerAdapter);
                b.viewpager.setCurrentItem(pos);
            }
        });
        b.edit.setOnClickListener(v -> {
            Intent i = new Intent(this, ScanActivity.class);
            i.putExtra(ScanConstants.SELECTED_BITMAP, listImg.get(b.viewpager.getCurrentItem()));
            edited_img.launch(i);
        });
        b.save.setOnClickListener(v -> showSaveDialog());
        b.reorder.setOnClickListener(v -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.reorder_frame, new ReorderFragment());
            transaction.commit();
        });

        b.tabStrip.setDrawFullUnderline(false);
        b.tabStrip.setTabIndicatorColor(Color.WHITE);
    }


    private void showSaveDialog() {
        MaterialAlertDialogBuilder saveDialog = new MaterialAlertDialogBuilder(this);
        SaveDialogBinding sb = SaveDialogBinding.inflate(getLayoutInflater());

        saveDialog.setView(sb.getRoot());
        AlertDialog dg = saveDialog.show();

        if (sb.filename.requestFocus())
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        sb.saveBtn.setOnClickListener(vi -> {
            try {
                String fileName = sb.filename.getEditText().getText().toString();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> savePDF(fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
            dg.dismiss();
        });
        sb.cancelBtn.setOnClickListener(v -> dg.dismiss());
    }
    private void savePDF(String fileName) {

        try {
            final PDFWriterUtil pdfWriterUtil = new PDFWriterUtil();
            for (Uri i : listImg) {
                pdfWriterUtil.addImageUri(this, i);
                Log.i("bitmap dec", i.toString());
            }
            File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS), fileName + ".pdf");
            pdfWriterUtil.write(new FileOutputStream(path));

            Document d = new Document();
            d.setName(fileName);
            d.setPath(path.getAbsolutePath());
            d.setCreatedDate(new SimpleDateFormat(getString(R.string.template_date_pattern), Locale.getDefault()).format(new Date()));
            d.setPages(pdfWriterUtil.getPageCount());
            documentViewModel.saveDocument(d);

            runOnUiThread(() -> Toast.makeText(this, R.string.file_created, Toast.LENGTH_SHORT).show());

            pdfWriterUtil.close();
            finish();

        } catch (NullPointerException e) {
            runOnUiThread(() -> Toast.makeText(this, R.string.file_name_cannot_be_blank, Toast.LENGTH_SHORT).show());
            Log.e("NPE", e.getMessage());
        } catch (IOException ex) {
            runOnUiThread(() -> Toast.makeText(this, R.string.failed_to_create_files, Toast.LENGTH_SHORT).show());
            Log.e("IOEXCEPTIONS", String.valueOf(ex));
        }
    }

    public ArrayList<Uri> getImgs() {
        Log.i("SENT_IMGS", listImg.toString());

        return listImg;
    }

    public void setImgs(ArrayList<Uri> imgs) {
        this.listImg.clear();
        listImg.addAll(imgs);

        Log.i("REORDERED", listImg.toString());

        viewPagerAdapter.notifyDataSetChanged();
        b.viewpager.setAdapter(viewPagerAdapter);

        thumbnailPreviewAdapter.notifyDataSetChanged();
        b.thumbnail.setAdapter(thumbnailPreviewAdapter);
    }
}


