package esolutions.com.esdatabaselib.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.io.File;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static android.util.Log.d;

/**
 * Created by VinhNB on 11/9/2017.
 */

public class EsDatabaseLibCommon {
    public static boolean hasPermissionWriteSdcard(Context context) {
        boolean hasPermission = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(context.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
            } else {
                hasPermission = true;
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                hasPermission = false;
            else
                hasPermission = true;
        }
        return hasPermission;
    }

    public static boolean createFileIfNotExist(Context context, String sUriLogFile) throws Exception {
        //create file
        File file = new File(sUriLogFile);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            showFolderSdcard(context, sUriLogFile);
            return false;
        }

        return true;
    }

    public static void showFolderSdcard(Context context, String sUriLogFile) throws Exception {
        // Load root folder
        File forder = new File(sUriLogFile);
        String parentPath = forder.getParentFile().getName();
        File folderParent = new File(parentPath);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            String[] allFilesRoot = forder.list();
            for (int i = 0; i < allFilesRoot.length; i++) {
                allFilesRoot[i] = folderParent + allFilesRoot[i];
            }
            if (allFilesRoot != null)
                MediaScannerConnection.scanFile(context, allFilesRoot, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                d("ExternalStorage", "Scanned " + path + ":");
                                d("ExternalStorage", "uri=" + uri);
                            }
                        });
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
                    .parse("file://" + parentPath)));
        }
    }

}
