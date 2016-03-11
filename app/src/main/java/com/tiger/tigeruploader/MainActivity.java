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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity {

    private  ProgressDialog prgDialog;

    private String fileName;

    private byte[] fileByteData;

    private Uri imageUri;

    private static int RESULT_LOAD_IMG = 1;

    private String[] mPlanetTitles;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private CharSequence mTitle;

    private PreferenceManager preferenceManager;

    private ListView.OnItemClickListener entryListListener =  new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prgDialog = new ProgressDialog(this);

        //provide the permission
        PermissionHandler.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, this);
        PermissionHandler.checkPermission(Manifest.permission.INTERNET, this);
        try {
            preferenceManager = new PreferenceManager(this);
            mTitle = getTitle();

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setOverflowIcon(getDrawable(R.mipmap.ic_more_vert_white_24dp));
            setSupportActionBar(toolbar);

            mPlanetTitles = getResources().getStringArray(R.array.menu_array);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerList = (ListView) findViewById(R.id.left_drawer);

            // set up the drawer's list view with items and click listener
            mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_text, mPlanetTitles));
            mDrawerList.setOnItemClickListener(entryListListener);

            // enable ActionBar app icon to behave as action to toggle nav drawer
            ActionBar supportBar = getSupportActionBar();
            supportBar.setDisplayHomeAsUpEnabled(true);
            supportBar.setHomeAsUpIndicator(R.mipmap.ic_menu_white_24dp);
            supportBar.setHomeButtonEnabled(true);

            // ActionBarDrawerToggle ties together the the proper interactions
            // between the sliding drawer and the action bar app icon
            ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
                public void onDrawerClosed(View view) {
                    getSupportActionBar().setTitle(mTitle);
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    getSupportActionBar().setTitle(mTitle);
                    invalidateOptionsMenu();
                }
            };
            mDrawerLayout.addDrawerListener(mDrawerToggle);

            selectItem(0);

            // Set Cancelable as False
            prgDialog.setCancelable(false);

            //Check if it was opened by another app
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                handleIntent(intent, type);
            }
        } catch (Exception ex){
            TigerApplication.ShowException(ex);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        try {
            selectItem(0);
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                handleIntent(intent, type);
            }
        } catch (Exception ex){
            TigerApplication.ShowException(ex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextual_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.action_gallery:
                    String galleryURL = URLResolver.getGalleryPageURL(preferenceManager);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(galleryURL));
                    startActivity(browserIntent);
                    return true;
                case R.id.action_load:
                    // Create intent to Open Image applications like Gallery, Google Photos
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    // Start the Intent
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception ex){
            TigerApplication.ShowException(ex);
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK  && null != data) {
                // Get the Image from data
                imageUri = data.getData();
                loadImage();
                //The selection of the tab will trigger the show of the image into the view once visible
                selectItem(0);
            } else {
                Toast.makeText(this, getString(R.string.errorNoImageSelectedFromGallery), Toast.LENGTH_LONG).show();
            }
            checkPath();
        } catch (Exception ex) {
            resetImage();
            TigerApplication.ShowException(ex);
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }

    protected void checkPath(){
        FloatingActionButton openPhoto = (FloatingActionButton)findViewById(R.id.buttonAddClosePhoto);
        FloatingActionButton upload = (FloatingActionButton)findViewById(R.id.buttonUpload);
        if (openPhoto != null) {
            if (fileByteData != null) {
                openPhoto.setBackgroundTintList(getColorStateList(R.color.buttonDisable));
                openPhoto.setImageResource(R.drawable.ic_close_white_24dp);
                upload.setVisibility(View.VISIBLE);
            } else {
                openPhoto.setBackgroundTintList(getColorStateList(R.color.buttonEnable));
                openPhoto.setImageResource(R.drawable.ic_add_a_photo_white_24dp);
                upload.setVisibility(View.INVISIBLE);
            }
        }
    }

    protected boolean checkUrlExtension(String url){
        if(url.contains(".")) {
            String extension = url.substring(url.lastIndexOf(".") + 1).toLowerCase().trim();
            if (PreferenceManager.ALLOWED_EXTENSIONS.contains(extension)){
                return true;
            }
        }
        return false;
    }

    protected void handleIntent(Intent intent, String type){
        try {
            if (type.startsWith("image/")) {
                imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                loadImage();
                showImageIntoView();
                checkPath();
            } else if (type.startsWith("text/")) {
                String textPath = intent.getStringExtra(Intent.EXTRA_TEXT);
                imageUri = Uri.parse(textPath);
                //Try to found a valid scheme for the text
                if (imageUri.getScheme() == null) {
                    if (new File(textPath).exists()) {
                        imageUri = Uri.parse("file://" + textPath);
                    } else {
                        imageUri = Uri.parse("http://" + textPath);
                    }
                }
                if (imageUri.getScheme() != null) {
                    if (imageUri.getScheme().startsWith("file")) {
                        loadImage();
                        showImageIntoView();
                        checkPath();
                    } else if (imageUri.getScheme().startsWith("http")) {
                        if (checkUrlExtension(imageUri.toString())) {
                            //The http uri need to be loaded into a separate thread
                            new AsyncTask<Void, Void, String>() {

                                @Override
                                protected String doInBackground(Void... params) {
                                    handleContentHTTP();
                                    return "";
                                }

                                @Override
                                protected void onPostExecute(String s) {
                                    showImageIntoView();
                                    checkPath();
                                    if (fileName == null || fileByteData == null) {
                                        //something went wrong while loading, show a message
                                        Toast.makeText(getApplicationContext(), getString(R.string.errorProtocol), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }.execute(null, null, null);
                        } else {
                            String errorMessage = String.format(getString(R.string.errorInvalidImage), imageUri.toString());
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                            resetImage();
                        }
                    }
                }
            }
        } catch (Exception ex){
            resetImage();
            TigerApplication.ShowException(ex);
        }
    }

    private void handleContentUri() throws Exception{
        InputStream stream = getContentResolver().openInputStream(imageUri);
        if (stream != null) {
            fileByteData = Utility.convertStreamToByteData(stream);
            String currentFileName = null;
            //Try to get the name
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            // Get the cursor
            Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgPath = cursor.getString(columnIndex);
                cursor.close();
                if (imgPath != null) {
                    currentFileName = new File(imgPath).getName();
                }
            }
            if (currentFileName == null) {
                currentFileName = new File(imageUri.getPath()).getName();
            }
            fileName = currentFileName;
        }
    }

    private void handleContentHTTP(){
        fileByteData = ServerCaller.downloadFile(imageUri.toString());
        if (fileByteData != null && fileByteData.length != 0) {
            fileName =  new File(imageUri.getPath()).getName();
        } else {
            imageUri = null;
            fileName = null;
            fileByteData = null;
            Toast.makeText(getApplicationContext(), getString(R.string.errorUnableToDownload), Toast.LENGTH_LONG).show();
        }
    }

    protected void loadImage() throws  Exception{
        if (imageUri != null) {
            if ("content".equals(imageUri.getScheme())) {
                handleContentUri();
            } else if ("file".equals(imageUri.getScheme())) {
                String imgPath = imageUri.getPath();
                File fileResource = new File(imgPath);
                if (fileResource.exists()){
                    fileName = fileResource.getName();
                    fileByteData = Utility.convertFileToByteData(fileResource);
                }
            }
        }
    }

    protected void showImageIntoView() {
        if (preferenceManager.isShowPreview()) {
            WebView imgView = (WebView) findViewById(R.id.imgView);
            if (imgView != null) {
                imgView.getSettings().setLoadWithOverviewMode(true);
                imgView.getSettings().setUseWideViewPort(true);
                imgView.getSettings().setDisplayZoomControls(false);
                imgView.getSettings().setBuiltInZoomControls(true);
                imgView.setBackgroundColor(getColor(R.color.webViewBackground));
                if (imageUri != null) {
                    imgView.loadUrl(imageUri.toString());
                } else {
                    imgView.loadUrl("about:blank");
                }
            }
        }
    }

    public void openButtonPressed(View v){
        if (fileByteData != null){
            imageUri = null;
            fileName = null;
            fileByteData = null;
            showImageIntoView();
            checkPath();
        } else {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        }
    }

    public void uploadButtonPressed(View v) {
        try {
            // When Image is selected from Gallery
            if (fileByteData != null) {
                prgDialog.setMessage(getString(R.string.convertingToBytedataMessage));
                prgDialog.show();
                //upload the image
                uploadWithNotification();
            } else {
                // When Image is not selected from Gallery
                Toast.makeText(getApplicationContext(), getString(R.string.errorButtonPress), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex){
            TigerApplication.ShowException(ex);
        }
    }

    protected void uploadWithNotification() {
        //create the notification
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.uploadInProgressMessage))
                    .setSmallIcon(R.drawable.cloud_upload);
        //call the upload
        uploadImage(mNotifyManager, mBuilder);
    }

    protected void uploadImage(final NotificationManager notificationManager, final NotificationCompat.Builder notificationBuilder) {
        prgDialog.setMessage(getString(R.string.sendingFileMessage));
        String uploadURL = URLResolver.getUploadPageURL(preferenceManager);
        final View bUpload = findViewById(R.id.buttonUpload);
        ServerCaller uploadServer = new ServerCaller(uploadURL){

            @Override
            public void onException(Exception ex) {
                super.onException(ex);
                notificationManager.cancel(PermissionHandler.APP_ID);
                bUpload.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPostExecute(String response, int responseCode) {
                prgDialog.hide();
                notificationManager.cancel(PermissionHandler.APP_ID);
                bUpload.setVisibility(View.VISIBLE);
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    ServerResponse parsedResponse = new ServerResponse(response, MainActivity.this);
                    if (parsedResponse.isMessageLink()) {
                        String link = parsedResponse.getResponseMessage();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("link", link);
                        clipboard.setPrimaryClip(clip);
                        String successMessage = String.format(getString(R.string.successMessage), link);
                        Toast.makeText(getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();
                    } else {
                        if (parsedResponse.hasError()){
                            String errorMessage = String.format(getString(R.string.errorServerMessage), parsedResponse.getError());
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                        } else {
                            String errorMessage = String.format(getString(R.string.errorServerMessage), parsedResponse.getResponseMessage());
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else  if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    Toast.makeText(getApplicationContext(), getString(R.string.errorServerNotFound), Toast.LENGTH_LONG).show();
                } else {
                    String errorMessage = String.format(getString(R.string.errorServerCode), String.valueOf(responseCode));
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        };
        int connectionTimeOut = preferenceManager.getHttpTimeout();
        uploadServer.setConnectionTimeout(connectionTimeOut);
        bUpload.setVisibility(View.INVISIBLE);
        uploadServer.sendChunkedFile(fileByteData, fileName, notificationManager, notificationBuilder);
    }

    public void showInformation() {
        try {
            TextView informationLabel = (TextView) findViewById(R.id.informationLabel);
            informationLabel.setMovementMethod(LinkMovementMethod.getInstance());

            TextView spaceLabel = (TextView) findViewById(R.id.spaceControl);
            spaceLabel.setText(getString(R.string.sendingSpaceRequest));

            String sizeURL = URLResolver.getSizePageURL(preferenceManager);
            ServerCaller uploadServer = new ServerCaller(sizeURL) {
                @Override
                public void onException(Exception ex) {
                    super.onException(ex);
                    Toast.makeText(getApplicationContext(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPostExecute(String response, int responseCode) {
                    TextView spaceLabel = (TextView) findViewById(R.id.spaceControl);
                    if (spaceLabel != null) {
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            int size = Integer.parseInt(response);
                            double mbSize = (double) size / 1024 / 1024;
                            String value = String.format("%.2f", mbSize);
                            spaceLabel.setText(String.format(getString(R.string.usedSpace), value));
                        } else {
                            spaceLabel.setText(String.format(getString(R.string.errorSpaceServerCode), String.valueOf(responseCode)));
                        }
                    }
                }
            };
            int connectionTimeOut = preferenceManager.getHttpTimeout();
            uploadServer.setConnectionTimeout(connectionTimeOut);
            uploadServer.sendCall("");
        } catch (Exception ex){
            TigerApplication.ShowException(ex);
        }
    }

    protected void showSettings(){
        try {
            EditText baseURL = (EditText) findViewById(R.id.baseURL);
            EditText uploadPage = (EditText) findViewById(R.id.uploadPage);
            EditText galleryPage = (EditText) findViewById(R.id.galleryPage);
            EditText sizePage = (EditText) findViewById(R.id.sizePage);
            EditText httpTimeout = (EditText) findViewById(R.id.httpTimeout);
            CheckBox showPreview = (CheckBox) findViewById(R.id.showPreview);

            baseURL.setText(preferenceManager.getValueString(URLResolver.BASE_URL_PREF_KEY));
            uploadPage.setText(preferenceManager.getValueString(URLResolver.UPLOAD_PAGE_PREF_KEY));
            galleryPage.setText(preferenceManager.getValueString(URLResolver.GALLERY_PAGE_PREF_KEY));
            sizePage.setText(preferenceManager.getValueString(URLResolver.SIZE_PAGE_PREF_KEY));
            httpTimeout.setText(preferenceManager.getValueString(URLResolver.HTTP_TIMEOUT_PREF_KEY));
            showPreview.setChecked(preferenceManager.isShowPreview());
        } catch (Exception ex){
            TigerApplication.ShowException(ex);
        }
    }

    public void switchPage(int index){
        FrameLayout elementsArea = (FrameLayout)findViewById(R.id.content_frame);
        if (index == 0) {
            elementsArea.setBackgroundColor(getColor(R.color.webViewBackground));
            showImageIntoView();
            checkPath();
        } else if (index == 1) {
            elementsArea.setBackgroundColor(getColor(R.color.otherViewBackground));
            showSettings();
        } else if (index == 2) {
            elementsArea.setBackgroundColor(getColor(R.color.otherViewBackground));
            showInformation();
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        try {
            // Create a new fragment and specify the planet to show based on position
            ViewFragment fragment = new ViewFragment();
            Bundle args = new Bundle();
            args.putInt(ViewFragment.ARG_ENTRY_NUMBER, position);
            fragment.setArguments(args);

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(mPlanetTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } catch (Exception ex){
            TigerApplication.ShowException(ex);
        }
    }

    public PreferenceManager getPreferenceManager(){
        return preferenceManager;
    }

    private void resetImage(){
        fileByteData = null;
        fileName = null;
        imageUri = null;
        showImageIntoView();
    }
}
