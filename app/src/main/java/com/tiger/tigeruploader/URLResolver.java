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

public class URLResolver {

    public static final String BASE_URL_PREF_KEY = "baseURL";

    public static final String GALLERY_PAGE_PREF_KEY = "galleryPage";

    public static final String  UPLOAD_PAGE_PREF_KEY = "uploadPage";

    public static final String SIZE_PAGE_PREF_KEY = "sizePage";

    public static final String HTTP_TIMEOUT_PREF_KEY = "httpTimeout";

    public static String DEFAULT_BASE_URL = "your server page";

    public static String DEFAULT_SERVER_UPLOAD_REMOTE = "upload_image.php";

    public static String DEFAULT_SERVER_GALLERY_REMOTE = "gallery.php";

    public static String DEFAULT_SERVER_SIZE_REMOTE = "imagesSize.php";

    protected static String getBaseURL(PreferenceManager preferenceManager){
        String baseUrl = preferenceManager.getValueString(BASE_URL_PREF_KEY);
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        return baseUrl;
    }

    public static String getGalleryPageURL(PreferenceManager preferenceManager){
        String galleryPage = preferenceManager.getValueString(GALLERY_PAGE_PREF_KEY);
        return getBaseURL(preferenceManager) + galleryPage;
    }

    public static String getUploadPageURL(PreferenceManager preferenceManager){
        String uploadPage = preferenceManager.getValueString(UPLOAD_PAGE_PREF_KEY);
        return getBaseURL(preferenceManager) + uploadPage;
    }

    public static String getSizePageURL(PreferenceManager preferenceManager){
        String sizePage = preferenceManager.getValueString(SIZE_PAGE_PREF_KEY);
        return getBaseURL(preferenceManager) + sizePage;
    }
}
