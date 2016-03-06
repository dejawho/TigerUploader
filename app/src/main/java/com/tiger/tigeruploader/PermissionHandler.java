/** This file is part of TigerUploader, located at
 * https://github.com/dejawho/TigerUploader

 TigerUploader is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Foobar is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Foobar.  If not, see <http://www.gnu.org/licenses/>.**/
package com.tiger.tigeruploader;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class PermissionHandler {

    final public static int APP_ID = 135;

    public static void checkPermission(String permissionID, AppCompatActivity context) {
        try {
            int hasPermission = ContextCompat.checkSelfPermission(context, permissionID);
            if (hasPermission != PackageManager.PERMISSION_GRANTED){
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context, permissionID)) {
                        ActivityCompat.requestPermissions(context, new String[]{permissionID}, APP_ID);
                    }
                    return;
                }
        } catch (Exception ex){
            TigerApplication.ShowException(ex);
            Toast.makeText(context, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
