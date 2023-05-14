package giang.truong.scanpdf.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class PDFWriterUtil {

    private final PdfDocument document = new PdfDocument();


    public void addImageUri(Context context, Uri img) {
        Bitmap bitmap = null;
        ContentResolver contentResolver = context.getContentResolver();
        try {
            if(Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, img);
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, img);
                bitmap = ImageDecoder.decodeBitmap(source);
            }
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int bitmap_width = bitmap.getWidth();
        int bitmap_height = bitmap.getHeight();
        int dens = bitmap.getDensity();
        if(bitmap_width > 595 | bitmap_height > 842){
            bitmap = Bitmap.createScaledBitmap(bitmap, 595, ((595*bitmap_height) /bitmap_width), false);
            bitmap.setDensity(dens);
        }

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.setDensity(dens);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));

        canvas.drawPaint(paint);
        int centerX = (595 - bitmap.getWidth()) / 2;
        int centerY = (842 - bitmap.getHeight()) / 2;
        canvas.drawBitmap(bitmap, centerX, centerY, null);

        document.finishPage(page);
        bitmap.recycle();
        System.gc();
    }

    public void write( FileOutputStream out ) throws IOException {
        document.writeTo(out);
    }

    public int getPageCount(){
        return document.getPages().size();
    }

    public void close() throws IOException {
        document.close();
    }

}
