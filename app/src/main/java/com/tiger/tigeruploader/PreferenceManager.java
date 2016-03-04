package com.tiger.tigeruploader;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private SharedPreferences preferences;

    private static final String PREF_NAME = "Configuration";

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
        String httpTimeout = preferences.getString(URLResolver.SIZE_PAGE_PREF_KEY, null);
        if (httpTimeout != null){
            editor.putString(URLResolver.HTTP_TIMEOUT_PREF_KEY, String.valueOf(10000));
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

    public void storeValue(String key, int value){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public String getValueString(String key){
        return preferences.getString(key, null);
    }

    public Integer getValueInt(String key){
        String value = preferences.getString(key, null);
        if (value != null){
            return Integer.parseInt(value);
        } else return null;
    }
}
