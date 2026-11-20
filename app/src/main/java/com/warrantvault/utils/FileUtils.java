package com.warrantvault.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {

    public static File createImageFile(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "BILL_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir("Pictures");
        if (storageDir == null) {
            storageDir = context.getFilesDir();
        }
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (Exception e) {
            return new File(storageDir, imageFileName + ".jpg");
        }
    }

    public static String saveBitmapToFile(Context context, Bitmap bitmap) {
        File file = createImageFile(context);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String copyUriToInternalStorage(Context context, Uri uri, String customPrefix) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = (customPrefix != null ? customPrefix : "DOC") + "_" + timeStamp + ".jpg";
            File storageDir = context.getExternalFilesDir("Documents");
            if (storageDir == null) {
                storageDir = context.getFilesDir();
            }
            File destFile = new File(storageDir, fileName);

            try (InputStream in = context.getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(destFile)) {
                if (in == null) return null;
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
                return destFile.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
