package com.tiger.tigeruploader;

public class URLResolver {

    public static final String BASE_URL_PREF_KEY = "baseURL";

    public static final String GALLERY_PAGE_PREF_KEY = "galleryPage";

    public static final String  UPLOAD_PAGE_PREF_KEY = "uploadPage";

    public static final String SIZE_PAGE_PREF_KEY = "sizePage";

    public static final String HTTP_TIMEOUT_PREF_KEY = "httpTimeout";

    public static String DEFAULT_BASE_URL = "http://imageupload-tigeruploader.rhcloud.com/";

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
