package br.com.phoebus.android.payments.demo.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ConvertBitmapToBase64 {

    public static String toCustomChannelImageBase64(Bitmap source, int channels) {
        if(channels == 0){
            return "Something wrong happened";
        }
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int[] pixels = new int[width * height];
            source.getPixels(pixels, 0, width, 0, 0, width, height);

            int[] outputPixels = new int[width * height];

            for (int i = 0; i < pixels.length; i++) {
                int color = pixels[i];

                // Convert to grayscale using standard luminance calculation
                int grayscale = (int) ((0.299 * Color.red(color)) +
                        (0.587 * Color.green(color)) +
                        (0.114 * Color.blue(color)));

                switch (channels) {
                    case 3: // 3 channels: RGB (grayscale represented as R = G = B)
                        outputPixels[i] = Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
                        break;

                    case 4: // 4 channels: ARGB
                        outputPixels[i] = Color.argb(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color)); // Full alpha
                        break;

                    default:
                        throw new IllegalArgumentException("Unsupported number of channels: " + channels);
                }
            }

            Bitmap.Config config = (channels == 3)? Bitmap.Config.RGB_565: Bitmap.Config.ARGB_8888;
            Bitmap customBitmap = Bitmap.createBitmap(width, height, config);
            customBitmap.setPixels(outputPixels, 0, width, 0, 0, width, height);

            return getBitmapToBase64(customBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static String getBitmapToBase64(Bitmap receipt) {

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            receipt.compress(Bitmap.CompressFormat.WEBP, 0, bytes);

            byte[] b = bytes.toByteArray();

            return Base64.encodeToString(b, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
