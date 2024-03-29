package com.adryanev.tinkerbrowser;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import com.google.android.material.navigation.NavigationView;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import java.util.ArrayList;
import java.util.ListIterator;


public class MainBrowser extends AppCompatActivity {
    ArrayList<Link> historyStack;
    EditText urlEditText;
    WebView browser;
    Button back;
    Button forward;
    Button refresh;
    Button stop;
    ProgressBar progressBar;
    static final String MYREFSID = "MyPrefs001";
    static final int actionMode = Activity.MODE_PRIVATE;
    ArrayList<String> blockedSites;
    Toolbar toolbar;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    SQLiteOpenHelper siteDataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        siteDataBaseHelper = new SiteDatabaseHelper(this);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if(item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                drawerLayout.closeDrawers();

                switch (item.getItemId()){
                    case R.id.history:
                       history();
                }
                return false;
            }
        });
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.openDrawer,
                R.string.closeDrawer){
            @Override
            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                supportInvalidateOptionsMenu();
                super.onDrawerClosed(drawerView);
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);


        actionBarDrawerToggle.syncState();

        urlEditText = (EditText) findViewById(R.id.url);
        browser = (WebView) findViewById(R.id.webkit);
        back = (Button) findViewById(R.id.backButton);
        forward = (Button) findViewById(R.id.forwardButton);


        refresh = (Button) findViewById(R.id.refreshButton);
        historyStack = new ArrayList<>();
        stop = (Button) findViewById(R.id.stopButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        getBlockedSites();

        //javascript and zoom control
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setBuiltInZoomControls(true);

        //listening enter key
        urlEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    go(v);
                }
                return false;
            }
        });
        //download listener
        browser.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainBrowser.this);
                builder.setTitle(getString(R.string.download));
                builder.setMessage(getString(R.string.downloadQuestion));
                builder.setCancelable(false).setPositiveButton((R.string.ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        download(url);
                    }

                }).setNegativeButton((R.string.cancel), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });

                builder.create().show();


            }
        });
        browser.setWebViewClient(new CustomWebViewClient());
        browser.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                Drawable draw = ResourcesCompat.getDrawable(getResources(),R.drawable.custom_progress_bar,null);
                progressBar.setProgressDrawable(draw);
                progressBar.setProgress(0);
                FrameLayout progressBarLayout = (FrameLayout) findViewById(R.id.progressBarlayout);
                progressBarLayout.setVisibility(View.VISIBLE);
                MainBrowser.this.setProgress(newProgress * 1000);
                progressBar.incrementProgressBy(newProgress);

                if(newProgress == 100){
                    progressBarLayout.setVisibility(View.GONE);

                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);

                MainBrowser.this.setTitle("Tinker Browser "+"- "+MainBrowser.this.browser.getTitle());
                for (Link link : historyStack){
                    if(link.getUrl().equals(browser.getUrl())){
                        link.setTitle(title);
                    }
                }
            }



        });

        //in case browser is losing focus
        browser.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if(!v.hasFocus()){
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
        browser.loadUrl("file:///android_asset/speeddial/tinker_speed_dial.html");
        browser.requestFocus();

    }

    private void download(String url) {
        String filename= url.substring(url.lastIndexOf("/"));
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Tinker Browser "+ url);
        request.setTitle("Downloading " +filename);

        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        request.setVisibleInDownloadsUi(true);

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && browser.canGoBack() ){
            browser.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This method will call sendEmail(String url) method when invoked.
     * @param url
     */
    private void alertBlockedSite(final String url) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainBrowser.this);
        builder.setTitle(getString(R.string.warning));
        builder.setMessage(getString(R.string.warningBlocked));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendEmail(url);
                sendSMS(url);

            }
        });
        builder.setIcon(R.drawable.ic_warning_black_24dp);
        builder.create().show();

    }

    /**
     * Send a sms to certain number
     * @param url
     */
    private void sendSMS(String url) {
        String number = "+6280989999";
        Uri uri = Uri.parse("sms:"+number);
        String smsBody = getString(R.string.tryingOpen)+" ("+url+")";
        Intent i = new Intent(Intent.ACTION_SENDTO,uri);
        i.putExtra("sms_body",smsBody);
        i.setData(uri);
        startActivity(i);

    }

    /**
     * Fetch blocked sites from database and put it into an Array List
     * @return
     */
    private ArrayList<String> getBlockedSites() {

        SQLiteDatabase db = siteDataBaseHelper.getReadableDatabase();

        blockedSites = new ArrayList<>();
        Cursor cursor = db.query("blockedSite",new String[]{"website"},null,null,null,null,null);
        while (cursor.moveToNext()){

            try{
                int value = cursor.getColumnIndex("website");
                if (value < 0) {
                    throw new Exception("not found");
                }
                blockedSites.add(cursor.getString(value));
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        cursor.close();
        db.close();
        return blockedSites;


    }

    /**
     * Executed when back button is clicked.
     * @param view
     */
    public void back(View view) {
        if (checkConnectivity()){
            browser.goBack();
        }
    }

    /**
     * Executed when forward button is clicked
     * @param view
     */
    public void forward(View view) {
        if(checkConnectivity()){
            browser.goForward();
        }
    }

    /**
     * Executed when stop button is clicked
     * @param view
     */
    public void stop(View view) {
        if(checkConnectivity()){
            browser.stopLoading();
        }
    }

    /**
     * Refreshing the browser
     * @param view
     */
    public void refresh(View view) {
        if (checkConnectivity()){
            browser.reload();
        }
    }

    /**
     * Open history layout when clicked
     *
     */
    public void history() {

        Bundle extra = new Bundle();
        extra.putSerializable("history",historyStack);

        Intent intent = new Intent(this, History.class);
        intent.putExtra("extra",extra);
        startActivity(intent);

    }

    /**
     * Checking network status
     */

    private boolean checkConnectivity(){
        boolean enabled = true;

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()){
            enabled = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_warning_black_24dp);
            builder.setMessage(getString(R.string.noConnection));
            builder.setCancelable(false);
            builder.setTitle(getString(R.string.error));
            builder.setNeutralButton(R.string.ok, null);
            builder.create().show();
        }

        return enabled;
    }


    /**
     * Method executed when go button is clicked
     * @param view
     */

    public void go(View view) {
        //hide soft keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(urlEditText.getWindowToken(), 0);

        boolean found = false;
        if (checkConnectivity()) {
            stop.setEnabled(true);

            //checking if user input is valid url

            if (Patterns.WEB_URL.matcher(urlEditText.getText().toString()).matches()) {
                urlEditText.setText("https://" + urlEditText.getText().toString()+"/");

                //checking if the site is from blockedSite List
               for(String url : blockedSites) {
                   if (urlEditText.getText().toString().equals(url)) {
                       alertBlockedSite(urlEditText.getText().toString());
                       found = true;
                       break;

                   }
               }
                if(found == false)browser.loadUrl(urlEditText.getText().toString());

            }else{
                browser.loadUrl("https://www.google.com/search?q=" + urlEditText.getText().toString());
            }
        }
    }


    /**
     * When Application is getting destroyed, send an email to certain recipient
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendEmail();

        clearMyPrefs();
    }

    /**
     * Send a mail that contains a blocked site's url
     * @param url
     */
    private void sendEmail(String url){
        String email = "wanabee54@gmail.com";
        Uri uri = Uri.parse("mailto:"+email);
        Intent intent = new Intent(Intent.ACTION_SENDTO,uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mailSubjectBlock));
        String text = getString(R.string.tryingOpen)+" ("+url+")";
        intent.putExtra(Intent.EXTRA_TEXT,text);
        startActivity(intent);

    }

    /**
     * Sending a mail that contains browser's history
     */
    private void sendEmail() {
        if(!historyStack.isEmpty()) {
            String email = "wanabee54@gmail.com";
            Uri uri = Uri.parse("mailto:" + email);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.browsingHistory));

            StringBuilder stringBuilder = new StringBuilder();

            for (Link history : historyStack) {

                stringBuilder.append(history.getTitle().toString() + "  (" + history.getUrl().toString() + ")\n\n");
            }
            String allHistory = stringBuilder.toString();
            intent.putExtra(Intent.EXTRA_TEXT, allHistory);
            startActivity(intent);
        }
        else{

        }
    }

    /**
     * Clearing shared preferences when app destroyed
     */
    private void clearMyPrefs() {
        SharedPreferences myPrefs = getSharedPreferences(MYREFSID,actionMode);
        SharedPreferences.Editor myEditor = myPrefs.edit();
        myEditor.clear();
        myEditor.commit();
    }


    /**
     * Custom WebView
     */
    class CustomWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (checkConnectivity()){

                //show current url
                MainBrowser.this.urlEditText.setText(url);

                //situs yang sama ditampilkan 1x saja
                boolean a = false;
                ListIterator<Link> listIterator = historyStack.listIterator();
                while (listIterator.hasNext() && !a){

                    if(listIterator.next().getUrl().equals(url)){
                        a = true;
                        listIterator.remove();
                    }

                }

                stop.setEnabled(true);
                stop.setBackgroundResource(R.drawable.ic_close_black_24dp);
                updateButtons();


            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Link link = new Link(url,view.getTitle().toString());
            historyStack.add(link);
            stop.setEnabled(false);
            stop.setBackgroundResource(R.drawable.ic_close_disabled);
        }


    }

    /**
     * Updating button.
     */
    private void updateButtons() {
        if (browser.canGoBack()){
            back.setEnabled(true);
            back.setBackgroundResource(R.drawable.ic_arrow_back_black_24dp);
        }
        else{
            back.setEnabled(false);
            back.setBackgroundResource(R.drawable.ic_arrow_back_disabled);
        }
        if (browser.canGoForward()){
            forward.setEnabled(true);
            forward.setBackgroundResource(R.drawable.ic_arrow_forward_black_24dp);
        }else{
            forward.setEnabled(false);
            forward.setBackgroundResource(R.drawable.ic_arrow_forward_disabled);
        }
    }

    /**
     * Handling configuration change
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.new_browser);

    }

    /**
     * Saving Instance's state
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        browser.saveState(outState);
    }

    /**
     * Restoring Saved Instance
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        browser.restoreState(savedInstanceState);
    }
}
