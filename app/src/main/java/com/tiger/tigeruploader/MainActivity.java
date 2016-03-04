package com.tiger.tigeruploader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

    private View.OnClickListener saveSettingListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText baseURL=(EditText)findViewById(R.id.baseURL);
            EditText uploadPage=(EditText)findViewById(R.id.uploadPage);
            EditText galleryPage=(EditText)findViewById(R.id.galleryPage);
            EditText sizePage=(EditText)findViewById(R.id.sizePage);
            EditText httpTimeout=(EditText)findViewById(R.id.httpTimeout);

            preferenceManager.storeValue(URLResolver.BASE_URL_PREF_KEY, baseURL.getText().toString());
            preferenceManager.storeValue(URLResolver.GALLERY_PAGE_PREF_KEY, galleryPage.getText().toString());
            preferenceManager.storeValue(URLResolver.HTTP_TIMEOUT_PREF_KEY, httpTimeout.getText().toString());
            preferenceManager.storeValue(URLResolver.SIZE_PAGE_PREF_KEY, sizePage.getText().toString());
            preferenceManager.storeValue(URLResolver.UPLOAD_PAGE_PREF_KEY, uploadPage.getText().toString());

            Toast.makeText(MainActivity.this,"Preferences Saved",Toast.LENGTH_LONG).show();
        }
    };

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

        mTitle = getTitle();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPlanetTitles = getResources().getStringArray(R.array.menu_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_text, mPlanetTitles));
        mDrawerList.setOnItemClickListener(entryListListener);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_drawer);
        getSupportActionBar().setHomeButtonEnabled(true);

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

        if (savedInstanceState == null) {
            selectItem(0);
        }

        // Set Cancelable as False
        prgDialog.setCancelable(false);
        PermissionHandler.checkPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, "Read SD Card", this);
        PermissionHandler.checkPermission(new String[] {Manifest.permission.INTERNET}, "Access to Internet", this);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
           handleIntent(intent, type);
        }

        preferenceManager = new PreferenceManager(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextual_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
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
    }

    protected void checkPath(){
        Button bUpload = (Button)findViewById(R.id.uploadPicture);
        if (bUpload != null) {
            if (fileByteData != null) {
                bUpload.setEnabled(true);
            } else {
               bUpload.setEnabled(false);
            }
        }
    }

    protected void handleIntent(Intent intent, String type){
        if (type.startsWith("image/")){
            imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            loadImage();
            showImageIntoView();
            checkPath();
        } else if (type.startsWith("text/")){
            String textPath = intent.getStringExtra(Intent.EXTRA_TEXT);
            imageUri = Uri.parse(textPath);
            if (imageUri.getScheme() == null){
                if (new File(textPath).exists()){
                    imageUri = Uri.parse("file://"+textPath);
                    loadImage();
                    showImageIntoView();
                    checkPath();
                } else {
                    //The http uri need to be loaded into a separate thread
                    imageUri = Uri.parse("http://"+textPath);
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
                        }
                    }.execute(null, null, null);
                }
            }
        }
    }

    private void handleContentUri(){
        try {
            InputStream stream = getContentResolver().openInputStream(imageUri);
            if (stream != null) {
                fileByteData = convertStreamToByteData(stream);
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
        } catch (Exception ex){
            Toast.makeText(getApplicationContext(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleContentHTTP(){
        try {
            URL url = new URL(imageUri.toString());
            InputStream stream = url.openStream();
            if (stream != null) {
                fileByteData = convertStreamToByteData(stream);
                fileName =  new File(imageUri.getPath()).getName();
            }
        } catch (Exception ex){
            Toast.makeText(getApplicationContext(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    protected void loadImage(){
        if (imageUri != null) {
            if ("content".equals(imageUri.getScheme())) {
                handleContentUri();
            } else if ("file".equals(imageUri.getScheme())) {
                String imgPath = imageUri.getPath();
                File fileResource = new File(imgPath);
                if (fileResource.exists()){
                    fileName = fileResource.getName();
                    fileByteData = convertFileToByteData(fileResource);
                }
            }
        }
    }

    protected void showImageIntoView(){
        WebView imgView = (WebView) findViewById(R.id.imgView);
        if (imageUri != null && imgView != null) {
            imgView.getSettings().setBuiltInZoomControls(true);
            imgView.loadUrl(imageUri.toString());
        }
    }

    // When Image is selected from Gallery
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
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
            checkPath();
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

    }

    public void uploadButtonPressed(View v) {
        // When Image is selected from Gallery
        if (fileByteData != null) {
            Button bUpload = (Button)findViewById(R.id.uploadPicture);
            bUpload.setEnabled(false);
            prgDialog.setMessage("Converting Image to Binary Data");
            prgDialog.show();
            //upload the image
            uploadWithNotification();
            bUpload.setEnabled(true);
        } else {
            // When Image is not selected from Gallery
            Toast.makeText(getApplicationContext(), "You must select image from gallery before you try to upload", Toast.LENGTH_LONG).show();
        }
    }

    protected void uploadWithNotification() {
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Picture Upload").setContentText("Upload in progress").setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setProgress(10, 3, false);
        // Displays the progress bar for the first time.
        mNotifyManager.notify(PermissionHandler.APP_ID, mBuilder.build());
        uploadImage(mBuilder, mNotifyManager);
    }

    protected void uploadImage(final NotificationCompat.Builder mBuilder, final NotificationManager mNotifyManager) {
        prgDialog.setMessage("Sending File");
        String uploadURL = URLResolver.getUploadPageURL(preferenceManager);
        ServerCaller uploadServer = new ServerCaller(uploadURL){

            @Override
            public void onException(Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPostExecute(String response, int responseCode) {
                prgDialog.hide();
                // When the loop is finished, updates the notification
                mBuilder.setContentText("Upload complete").setProgress(0, 0, false);
                mNotifyManager.notify(PermissionHandler.APP_ID, mBuilder.build());
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("link", response);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), "Link Copied into the Clipboard: " + response, Toast.LENGTH_LONG).show();
                }
                else  if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                    Toast.makeText(getApplicationContext(), "The request from this app triggered and error: " + response, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error Occurred \n  HTTP Status code : " + responseCode, Toast.LENGTH_LONG).show();
                }
            }
        };
        int connectionTimeOut = Integer.parseInt(preferenceManager.getValueString(URLResolver.HTTP_TIMEOUT_PREF_KEY));
        uploadServer.setConnectionTimeout(connectionTimeOut);
        uploadServer.sendFile(fileByteData, fileName);
    }

    public void getInformation() {
        prgDialog.setMessage("Requesting used space");
        String sizeURL = URLResolver.getSizePageURL(preferenceManager);
        ServerCaller uploadServer = new ServerCaller(sizeURL){
            @Override
            public void onException(Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPostExecute(String response, int responseCode) {
                prgDialog.hide();
                TextView txtView = (TextView) findViewById(R.id.spaceControl);
                if (txtView != null) {
                    int size = Integer.parseInt(response);
                    double mbSize = (double)size / 1024 / 1024;
                    txtView.setText("Used Space: " + String.format("%.2f", mbSize) + "MB");
                }
            }
        };
        int connectionTimeOut = Integer.parseInt(preferenceManager.getValueString(URLResolver.HTTP_TIMEOUT_PREF_KEY));
        uploadServer.setConnectionTimeout(connectionTimeOut);
        uploadServer.sendCall("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

        // Create a new fragment and specify the planet to show based on position
        Fragment fragment = new ViewFragment(){

            @Override
            public void onViewCreated(View view, Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                int index = getArguments().getInt(ARG_ENTRY_NUMBER);
                if (index == 0) {
                    showImageIntoView();
                    checkPath();
                } else if (index == 1){
                    initializeSettingsView();
                } else if (index == 2){
                    getInformation();
                }
            }
        };

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
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    protected void initializeSettingsView(){
        EditText baseURL=(EditText)findViewById(R.id.baseURL);
        EditText uploadPage=(EditText)findViewById(R.id.uploadPage);
        EditText galleryPage=(EditText)findViewById(R.id.galleryPage);
        EditText sizePage=(EditText)findViewById(R.id.sizePage);
        EditText httpTimeout=(EditText)findViewById(R.id.httpTimeout);
        baseURL.setText(preferenceManager.getValueString(URLResolver.BASE_URL_PREF_KEY));
        uploadPage.setText(preferenceManager.getValueString(URLResolver.UPLOAD_PAGE_PREF_KEY));
        galleryPage.setText(preferenceManager.getValueString(URLResolver.GALLERY_PAGE_PREF_KEY));
        sizePage.setText(preferenceManager.getValueString(URLResolver.SIZE_PAGE_PREF_KEY));
        httpTimeout.setText(preferenceManager.getValueString(URLResolver.HTTP_TIMEOUT_PREF_KEY));

        Button saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveSettingListener);
    }

    private byte[] convertFileToByteData(File file) {
        byte[] bFile = new byte[(int) file.length()];
        try {
            //convert file into array of bytes
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        return bFile;
    }

    private byte[] convertStreamToByteData(InputStream file) throws IOException {
        byte[] bFile = new byte[file.available()];
        //convert file into array of bytes
        file.read(bFile);
        file.close();
        return bFile;
    }

    /*private String getStreamDigest(byte[] stream){
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

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return String.valueOf(System.currentTimeMillis());
    }*/
}
