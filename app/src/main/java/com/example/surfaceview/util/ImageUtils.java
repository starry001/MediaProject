package com.example.surfaceview.util;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class ImageUtils {
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;

    public static void saveImageData(byte[] imageData) {
        File imageFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (imageFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(imageData);
            fos.close();
        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
        }
    }

    private static File getOutputMediaFile(int type) {
        File imageFileDir =
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Image");
        if (!imageFileDir.exists()) {
            if (!imageFileDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        File imageFile;
        if (type == MEDIA_TYPE_IMAGE) {
            imageFile = new File(imageFileDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            imageFile = new File(imageFileDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else return null;
        return imageFile;
    }

    /**
     * 获得最接近频幕宽度的尺寸
     *
     * @param n        放大几倍 （>0)
     */
    public static Camera.Size getCurrentScreenSize(Activity context, List<Camera.Size> sizeList, int n) {
        if (sizeList != null && sizeList.size() > 0) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels * n;
            int[] arry = new int[sizeList.size()];
            int temp = 0;
            for (Camera.Size size : sizeList) {
                arry[temp++] = Math.abs(size.width - screenWidth);
            }
            temp = 0;
            int index = 0;
            for (int i = 0; i < arry.length; i++) {
                if (i == 0) {
                    temp = arry[i];
                    index = 0;
                } else {
                    if (arry[i] < temp) {
                        index = i;
                        temp = arry[i];
                    }
                }
            }
            return sizeList.get(index);
        }
        return null;
    }
}
