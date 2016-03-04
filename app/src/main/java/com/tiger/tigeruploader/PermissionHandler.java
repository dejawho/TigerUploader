package com.tiger.tigeruploader;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by marco on 02/03/16.
 */
public class PermissionHandler {

    final public static int APP_ID = 135;

    public static void checkPermission(String[] permissionIDs, String permissionName, AppCompatActivity context) {
        ArrayList<String> missingPermissions = new ArrayList<String>();
        for(String permissionID : permissionIDs){
            int hasPermission = ContextCompat.checkSelfPermission(context, permissionID);
            if (hasPermission != PackageManager.PERMISSION_GRANTED){
                missingPermissions.add(permissionID);
            }
        }

        if (!missingPermissions.isEmpty()) {
            for (final String missingPermission : missingPermissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context, missingPermission)) {
                    try {
                        ActivityCompat.requestPermissions(context, new String[]{missingPermission}, APP_ID);
                    } catch (Exception ex){
                        Toast.makeText(context, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                    return;
                }
            }
        }
    }
}
