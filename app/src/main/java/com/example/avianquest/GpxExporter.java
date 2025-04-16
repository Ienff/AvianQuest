package com.example.avianquest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class GpxExporter {
    public static void createSaveFileIntent(Context context, List<TrackPoint> trackPoints, ActivityResultLauncher<Intent> launcher) {
        if (trackPoints == null || trackPoints.isEmpty()) {
            Toast.makeText(context, "无轨迹数据可导出", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String fileName = "track_" + fileNameFormat.format(new Date()) + ".gpx";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/gpx+xml");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        launcher.launch(intent);
    }

    public static boolean saveGpxToUri(Context context, Uri uri, List<TrackPoint> trackPoints) {
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                String gpxContent = createGpxContent(trackPoints);
                outputStream.write(gpxContent.getBytes());
                outputStream.close();
                return true;
            }
        } catch (IOException e) {
            Log.e("GpxExporter", "Error saving GPX file", e);
            Toast.makeText(context, "保存轨迹失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public static boolean exportToGpx(Context context, List<TrackPoint> trackPoints) {
        if (trackPoints == null || trackPoints.isEmpty()) {
            Toast.makeText(context, "No track data to export", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            File path = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (!path.exists()) {
                path.mkdirs();
            }

            SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            String fileName = "track_" + fileNameFormat.format(new Date()) + ".gpx";
            File file = new File(path, fileName);
            FileOutputStream fos = new FileOutputStream(file);

            String gpxContent = createGpxContent(trackPoints);
            fos.write(gpxContent.getBytes());
            fos.close();

//            Toast.makeText(context, "轨迹导出至 " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d("GpxExporter", "File saved at: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e("GpxExporter", "Error saving GPX file", e);
            Toast.makeText(context, "Error saving track: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Method for content creation
    public static String createGpxContent(List<TrackPoint> trackPoints) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        StringBuilder gpxBuilder = new StringBuilder();
        gpxBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        gpxBuilder.append("<gpx version=\"1.1\" creator=\"AvianQuest\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
        gpxBuilder.append("  <trk>\n");
        gpxBuilder.append("    <name>AvianQuest Track</name>\n");
        gpxBuilder.append("    <trkseg>\n");

        for (TrackPoint point : trackPoints) {
            gpxBuilder.append("      <trkpt lat=\"").append(point.getLatLng().latitude)
                    .append("\" lon=\"").append(point.getLatLng().longitude).append("\">\n");

            if (point.getAltitude() != 0) {
                gpxBuilder.append("        <ele>").append(point.getAltitude()).append("</ele>\n");
            }

            gpxBuilder.append("        <time>").append(dateFormat.format(point.getTimestamp())).append("</time>\n");
            gpxBuilder.append("      </trkpt>\n");
        }

        gpxBuilder.append("    </trkseg>\n");
        gpxBuilder.append("  </trk>\n");
        gpxBuilder.append("</gpx>");

        return gpxBuilder.toString();
    }
}