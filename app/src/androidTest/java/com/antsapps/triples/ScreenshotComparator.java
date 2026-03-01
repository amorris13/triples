package com.antsapps.triples;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import androidx.test.core.app.Screenshot;
import androidx.test.platform.app.InstrumentationRegistry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ScreenshotComparator {

    private static final String TAG = "ScreenshotComparator";
    private static final String GOLDEN_ASSET_PATH = "goldens/";
    private static final float TOLERANCE = 0.01f; // 1%

    public static void compare(String tag) {
        Bundle arguments = InstrumentationRegistry.getArguments();
        boolean recordMode = Boolean.parseBoolean(arguments.getString("recordMode", "false"));

        Bitmap capturedBitmap = Screenshot.capture().getBitmap();

        if (recordMode) {
            saveBitmapToExternalStorage(capturedBitmap, tag);
            return;
        }

        Bitmap referenceBitmap = loadReferenceBitmap(tag);
        if (referenceBitmap == null) {
            fail("Reference bitmap not found for tag: " + tag + ". Run in recordMode to generate it.");
        }

        compareBitmaps(referenceBitmap, capturedBitmap, tag);
    }

    private static void saveBitmapToExternalStorage(Bitmap bitmap, String tag) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File directory = context.getExternalFilesDir("screenshots");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, tag + ".png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.d(TAG, "Saved screenshot to " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to save screenshot", e);
            fail("Failed to save screenshot for tag: " + tag);
        }
    }

    private static Bitmap loadReferenceBitmap(String tag) {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        try (InputStream is = context.getAssets().open(GOLDEN_ASSET_PATH + tag + ".png")) {
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.w(TAG, "Reference bitmap not found: " + tag, e);
            return null;
        }
    }

    private static void compareBitmaps(Bitmap reference, Bitmap captured, String tag) {
        if (reference.getWidth() != captured.getWidth() || reference.getHeight() != captured.getHeight()) {
            fail("Bitmap dimensions mismatch for tag: " + tag +
                 ". Reference: " + reference.getWidth() + "x" + reference.getHeight() +
                 ", Captured: " + captured.getWidth() + "x" + captured.getHeight());
        }

        int width = reference.getWidth();
        int height = reference.getHeight();
        int totalPixels = width * height;
        int differentPixels = 0;

        int[] refPixels = new int[totalPixels];
        int[] capPixels = new int[totalPixels];
        reference.getPixels(refPixels, 0, width, 0, 0, width, height);
        captured.getPixels(capPixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < totalPixels; i++) {
            if (refPixels[i] != capPixels[i]) {
                differentPixels++;
            }
        }

        float differenceRatio = (float) differentPixels / totalPixels;
        Log.d(TAG, "Difference ratio for " + tag + ": " + differenceRatio);

        if (differenceRatio > TOLERANCE) {
            // If failed, save the captured bitmap for debugging
            saveBitmapToExternalStorage(captured, tag + "_failed");
            fail("Screenshot mismatch for tag: " + tag + ". Difference: " + (differenceRatio * 100) + "%");
        }
    }
}
