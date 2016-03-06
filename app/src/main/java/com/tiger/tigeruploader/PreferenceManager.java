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

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;

public class PreferenceManager {

    private SharedPreferences preferences;

    private static final String PREF_NAME = "Configuration";

    private static final String SHOW_PREVIEW_KEY = "showPreview";

    public final static HashSet<String> ALLOWED_EXTENSIONS = loadAllowedExtensions();

    public PreferenceManager(Context context){
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean modified = false;
        SharedPreferences.Editor editor = preferences.edit();
        String baseURL = preferences.getString(URLResolver.BASE_URL_PREF_KEY, null);
        if (baseURL == null){
            editor.putString(URLResolver.BASE_URL_PREF_KEY, URLResolver.DEFAULT_BASE_URL);
            modified = true;
        }
        String galleryPage = preferences.getString(URLResolver.GALLERY_PAGE_PREF_KEY, null);
        if (galleryPage == null){
            editor.putString(URLResolver.GALLERY_PAGE_PREF_KEY, URLResolver.DEFAULT_SERVER_GALLERY_REMOTE);
            modified = true;
        }
        String uploadPage = preferences.getString(URLResolver.UPLOAD_PAGE_PREF_KEY, null);
        if (uploadPage == null){
            editor.putString(URLResolver.UPLOAD_PAGE_PREF_KEY, URLResolver.DEFAULT_SERVER_UPLOAD_REMOTE);
            modified = true;
        }
        String sizePage = preferences.getString(URLResolver.SIZE_PAGE_PREF_KEY, null);
        if (sizePage == null){
            editor.putString(URLResolver.SIZE_PAGE_PREF_KEY, URLResolver.DEFAULT_SERVER_SIZE_REMOTE);
            modified = true;
        }
        String httpTimeout = preferences.getString(URLResolver.HTTP_TIMEOUT_PREF_KEY, null);
        if (httpTimeout == null){
            editor.putString(URLResolver.HTTP_TIMEOUT_PREF_KEY, String.valueOf(URLResolver.DEFAULT_SERVER_HTTP_TIMEOUT));
            modified = true;
        }
        String showPreview = preferences.getString(SHOW_PREVIEW_KEY, null);
        if (showPreview == null){
            editor.putString(SHOW_PREVIEW_KEY, String.valueOf(true));
            modified = true;
        }

        if (modified){
            editor.commit();
        }
    }

    public void storeValue(String key, String value){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getValueString(String key){
        return preferences.getString(key, null);
    }

    public boolean isShowPreview(){
        String value = preferences.getString(SHOW_PREVIEW_KEY, null);
        if (value != null){
            return Boolean.parseBoolean(value);
        } else return true;
    }

    public int getHttpTimeout(){
        String value = preferences.getString(URLResolver.HTTP_TIMEOUT_PREF_KEY, null);
        if (value != null){
           try{
               return Integer.parseInt(value);
           } catch (Exception ex){
               return URLResolver.DEFAULT_SERVER_HTTP_TIMEOUT;
           }
        } else return URLResolver.DEFAULT_SERVER_HTTP_TIMEOUT;
    }

    public void setShowPreview(boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SHOW_PREVIEW_KEY, String.valueOf(value));
        editor.commit();
    }

    private static HashSet<String> loadAllowedExtensions(){
        HashSet<String> result = new HashSet<>();
        result.add("jpeg");
        result.add("jpg");
        result.add("png");
        result.add("bmp");
        result.add("gif");
        return result;
    }
}
