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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Utility {

    public static byte[] convertFileToByteData(File file) {
        byte[] b = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            b = new byte[(int) file.length()];
            fileInputStream.read(b);
        } catch (Exception ex) {
            ex.printStackTrace();
            b = null;
        } finally {
            if (fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        return b;
    }

    public static byte[] convertStreamToByteData(InputStream file) throws IOException {
        byte[] bFile = new byte[file.available()];
        //convert file into array of bytes
        file.read(bFile);
        file.close();
        return bFile;
    }

    public static String getStreamDigest(byte[] stream){
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(stream);
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return String.valueOf(System.currentTimeMillis());
    }

}
