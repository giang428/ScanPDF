package giang.truong.scanpdf.utils;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileUtils {

    public static List<File> getAllFiles(String dirPath ){

        List<File> fileList = new ArrayList<>();

        final File sd = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        File targetDirectory = new File( sd, dirPath );

        if( targetDirectory.listFiles() != null ){
            Collections.addAll(fileList, targetDirectory.listFiles());
        }

        fileList.sort(Comparator.comparingLong(File::lastModified));
        return fileList;

    }

    public static void mkdir(Context c, String dirPath ){

        final File sd = c.getExternalFilesDir(DIRECTORY_DOCUMENTS);

        File storageDirectory = new File(sd, dirPath);
        if (!storageDirectory.exists()) {
            storageDirectory.mkdir();
        }
    }

    public static void clearDirectory( String dirPath ){

        //final File sd = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        File targetDirectory = new File( dirPath );

        if( targetDirectory.listFiles() != null ){
            for( File tempFile : targetDirectory.listFiles() ){
                tempFile.delete();
            }
        }

    }

    public static void writeFile( String baseDirectory, String filename, FileWritingCallback callback ) throws IOException {
        final File sd = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        String absFilename = baseDirectory + filename;
        File dest = new File(sd, absFilename);
        FileOutputStream out = new FileOutputStream(dest);
        callback.write( out );
        out.flush();
        out.close();
    }

    public static void removeFile(  String filepath) {
        File targetFile = new File(filepath);
        targetFile.delete();
    }

    public static void moveFile(  String oldFilepath, String newFilePath) {
        final File sd = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        File targetFile = new File( sd, oldFilepath );
        targetFile.renameTo(new File(sd, newFilePath));
    }

    public static String getFileName(String path) {
        if (path == null)
            return null;

        int index = path.lastIndexOf("/");
        return index < path.length() ? path.substring(index + 1) : null;
    }

    public static void viewFile(Context mContext, String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(Intent.EXTRA_TEXT, FileUtils.getFileName(path));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri fileUri;
        fileUri = FileProvider.getUriForFile(
                mContext
                , "giang.truong.scanpdf.provider"
                , new File(path));
        intent.setDataAndType(fileUri, "application/pdf");
        mContext.startActivity(Intent.createChooser(intent, "View file"));
    }

    public static void shareFile(Context mContext, String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, FileUtils.getFileName(path));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri fileUri;
        fileUri = FileProvider.getUriForFile(
                mContext
                , "giang.truong.scanpdf.provider"
                , new File(path));
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setType("application/pdf");
        mContext.startActivity(Intent.createChooser(intent, "Share file"));
    }
}
