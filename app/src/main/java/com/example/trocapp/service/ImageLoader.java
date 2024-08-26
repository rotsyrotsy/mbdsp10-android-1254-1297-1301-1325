package com.example.trocapp.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static void setImageFromUrl(final ImageView imageView, final String urlString) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Download the image from the URL
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                //Bitmap bitmap = BitmapFactory.decodeStream(input);
                //handler.post(() -> imageView.setImageBitmap(bitmap));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, options);
                System.out.println( "Image dimensions: " + options.outWidth + "x" + options.outHeight);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, imageView.getWidth(), imageView.getHeight());
                options.inJustDecodeBounds = false;

                // Restart the input stream since the first one was consumed
                input.close();
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
                handler.post(() -> imageView.setImageBitmap(bitmap));


            } catch (Exception e) {
                e.printStackTrace();
                // Handle the error here, perhaps set a placeholder or error image
            }
        });
    }
}
