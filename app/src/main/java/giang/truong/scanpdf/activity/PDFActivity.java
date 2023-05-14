package giang.truong.scanpdf.activity;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import giang.truong.scanpdf.R;
import giang.truong.scanpdf.adapter.ListImageAdapter;
import giang.truong.scanpdf.databinding.ActivityPdfactivityBinding;
import giang.truong.scanpdf.databinding.SaveDialogBinding;
import giang.truong.scanpdf.fragments.ReorderFragment;
import giang.truong.scanpdf.model.Document;
import giang.truong.scanpdf.utils.DepthPageTransformer;
import giang.truong.scanpdf.utils.PDFWriterUtil;
import giang.truong.scanpdf.viewmodel.DocumentViewModel;
import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.type.ButtonGravity;
import gun0912.tedimagepicker.builder.type.MediaType;

public class PDFActivity extends AppCompatActivity {
    private ActivityPdfactivityBinding b;
    private ArrayList<Uri> listImg;
    private ListImageAdapter viewPagerAdapter;
    private DocumentViewModel documentViewModel;
    private AlertDialog prg;

    private final ActivityResultLauncher<Intent> edited_img =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
                if(result.getResultCode() == RESULT_OK && result.getData() != null){
                    Uri i = result.getData().getParcelableExtra(ScanConstants.SCANNED_RESULT);
                    try {
                        int cur = b.viewpager.getCurrentItem();
                        listImg.set(cur,i);
                        viewPagerAdapter.notifyDataSetChanged();
                        b.viewpager.setAdapter(viewPagerAdapter);
                        b.viewpager.setCurrentItem(cur);
                     } catch (Exception e){e.printStackTrace();}
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

        b.add.setOnClickListener(v -> {
            TedImagePicker.with(this)
                    .mediaType(MediaType.IMAGE)
                    .buttonGravity(ButtonGravity.BOTTOM)
                    .startMultiImage(uriList -> {
                                listImg.addAll(uriList);
                                viewPagerAdapter.notifyDataSetChanged();
                                b.viewpager.setAdapter(viewPagerAdapter);
                            }
                    );
        });

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
            i.putExtra(ScanConstants.SELECTED_BITMAP,listImg.get(b.viewpager.getCurrentItem()));
            edited_img.launch(i);
        });
        b.save.setOnClickListener(v -> showSaveDialog());
        b.reorder.setOnClickListener(v -> {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.reorder_frame, new ReorderFragment());
                transaction.commit();
        });
    }

    private void showSaveDialog() {
        MaterialAlertDialogBuilder progress =
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.drawable.ic_pdf)
                        .setTitle(R.string.creating)
                        .setMessage(R.string.creating_pdf_this_process_may_tatke_a_while);

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
                executor.execute(() -> {
                    runOnUiThread(() -> prg = progress.show());
                    savePDF(fileName);
                });
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
            File path = new File(getExternalFilesDir(DIRECTORY_DOCUMENTS), fileName + ".pdf");
            pdfWriterUtil.write(new FileOutputStream(path));

            Document d = new Document();
            d.setName(fileName);
            d.setPath(path.getAbsolutePath());
            d.setCreatedDate(new SimpleDateFormat(getString(R.string.template_date_pattern), Locale.getDefault()).format(new Date()));
            d.setPages(pdfWriterUtil.getPageCount());
            documentViewModel.saveDocument(d);

            runOnUiThread(() -> Toast.makeText(this, R.string.file_created, Toast.LENGTH_SHORT).show());

            pdfWriterUtil.close();
            finishAndRemoveTask();

        } catch (NullPointerException e) {
            runOnUiThread(() -> Toast.makeText(this, R.string.file_name_cannot_be_blank, Toast.LENGTH_SHORT).show());
            Log.e("NPE", e.getMessage());
        } catch (IOException ex) {
            runOnUiThread(() -> Toast.makeText(this, R.string.failed_to_create_files, Toast.LENGTH_SHORT).show());
            Log.e("IOEXCEPTIONS", ex.getMessage());
        }
    }
    public ArrayList<Uri> getImgs(){
        Log.i("SENT_IMGS",listImg.toString());

        return listImg;
    }
    public void setImgs(ArrayList<Uri> imgs){
        this.listImg.clear();
        listImg.addAll(imgs);
        Log.i("REORDERED",listImg.toString());
        viewPagerAdapter.notifyDataSetChanged();
        b.viewpager.setAdapter(viewPagerAdapter);
    }
}


