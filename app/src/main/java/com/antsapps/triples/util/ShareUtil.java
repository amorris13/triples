package com.antsapps.triples.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ShareUtil {
  public static void shareCsv(Context context, String filename, String content) {
    File cacheFile = new File(context.getCacheDir(), filename);
    try (FileOutputStream out = new FileOutputStream(cacheFile)) {
      out.write(content.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      Log.e("ShareUtil", "Error writing CSV file", e);
      Toast.makeText(context, "Error exporting CSV", Toast.LENGTH_SHORT).show();
      return;
    }

    Uri contentUri =
        FileProvider.getUriForFile(context, "com.antsapps.triples.fileprovider", cacheFile);
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("text/csv");
    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    context.startActivity(Intent.createChooser(shareIntent, "Export Statistics"));
  }
}
