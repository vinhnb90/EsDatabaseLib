package esolutions.com.esdatabaselib.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

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

}
