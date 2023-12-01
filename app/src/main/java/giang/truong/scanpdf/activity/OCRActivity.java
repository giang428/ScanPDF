package giang.truong.scanpdf.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

import giang.truong.scanpdf.R;
import giang.truong.scanpdf.databinding.ActivityOcractivityBinding;

public class OCRActivity extends AppCompatActivity {
    private Uri img;
    private ActivityOcractivityBinding b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityOcractivityBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        img = Uri.parse(getIntent().getStringExtra("img"));
        Log.i("fffffffffff",img.toString());

        Glide.with(this)
                .load(img)
                .into(b.imageView2);
        b.textView3.setVisibility(View.INVISIBLE);

        b.textView3.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("data", b.textField1.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this,"Copied to clipboad!",Toast.LENGTH_SHORT).show();
        });

        b.button.setOnClickListener(view -> {
            TextRecognizer recognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            InputImage inImage;
            try {
                inImage = InputImage.fromFilePath(this, img);
                Task<Text> result =
                        recognizer.process(inImage)
                                .addOnSuccessListener(visionText -> {
                                    Bitmap t = null;
                                    try {
                                        t = MediaStore.Images.Media.getBitmap(this.getContentResolver(), img).copy(Bitmap.Config.ARGB_8888,true);
                                        Matrix matrix = new Matrix();
                                        matrix.postRotate(90);
                                        t = Bitmap.createBitmap(t, 0, 0, t.getWidth(), t.getHeight(), matrix, true); // rotating bitmap

                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    Canvas cs = new Canvas(t);

                                    b.textField1.setText(visionText.getText());
                                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                                        for (Text.Line line : block.getLines()) {
                                            Rect lineFrame = line.getBoundingBox();
                                            Paint k = new Paint();
                                            k.setStyle(Paint.Style.STROKE);
                                            k.setStrokeWidth(12);
                                            k.setColor(Color.WHITE);
                                            cs.drawRect(lineFrame,k);
                                        }
                                    }
                                    b.imageView2.setImageBitmap(t);

                                    b.textView3.setVisibility(View.VISIBLE);
                                })
                                .addOnFailureListener(
                                        e -> Toast.makeText(getApplicationContext(),"An error occured! " + e.getMessage(),Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


}}