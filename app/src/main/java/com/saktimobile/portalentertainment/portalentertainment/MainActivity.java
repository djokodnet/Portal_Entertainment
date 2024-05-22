package com.saktimobile.portalentertainment.portalentertainment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends Activity implements ConnectivityReceiver.ConnectivityReceiverListener {

    WebView mainWebView = null;
    String subimsi = null;

    //Permision code that will be checked in the method onRequestPermissionsResult
    private int ACCESS_NETWORK_PERMISSION_CODE = 23;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //must be called before adding content!
        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainWebView = (WebView) findViewById(R.id.mainWebView);
       // if (getSubscriberId() != null){
       //     subimsi = getSubscriberId();
       // } else { subimsi = "1234567890";}

        if (ConnectivityReceiver.isConnected()) {

            //If the app has not the permission then asking for the permission
            requestStoragePermission();

            String suburl = "http://persadasolution.com/portalentertaintment/portalsakti/?loc=view&imsi=" + subimsi;
            mainWebView.loadUrl(suburl);

            mainWebView.setWebViewClient(new MainWebViewClient());

            getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
                    Window.PROGRESS_VISIBILITY_ON);

            mainWebView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    MainActivity.this.setTitle("Loading...");
                    MainActivity.this.setProgress(progress * 100);
                    if (progress == 100) {
                        MainActivity.this.setTitle(view.getTitle());
                    }
                }
            });
        }
        else {
            Intent newIntent=new Intent(MainActivity.this, errorconnection.class);
            startActivityForResult(newIntent,0);
        }
    }

    //We are calling this method to check the permission status
    private boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    private void requestStoragePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_NETWORK_STATE)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, ACCESS_NETWORK_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == ACCESS_NETWORK_PERMISSION_CODE){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Displaying a toast
                Toast.makeText(this,"Permission granted now you can read the network access",Toast.LENGTH_LONG).show();
            }else{
                //Displaying another toast if permission is not granted
                Toast.makeText(this,"You just denied the permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        if (isConnected) {
            //Toast.makeText(getBaseContext(), "Connection Established.", Toast.LENGTH_LONG).show();
            mainWebView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
        } else {
            Toast.makeText(getBaseContext(), "Connection lost.", Toast.LENGTH_LONG).show();
            mainWebView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }

    }


    @Override
    public void onBackPressed(){
        if(mainWebView.canGoBack())
            mainWebView.goBack();
        else
            super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        MyApplication.getInstance().setConnectivityListener(this);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    public String getSubscriberId(){

        String IMSI = null;
        String serviceName = Context.TELEPHONY_SERVICE;
        TelephonyManager m_telephonyManager = (TelephonyManager) getSystemService(serviceName);
        int deviceType = m_telephonyManager.getPhoneType();
        switch (deviceType) {
            case (TelephonyManager.PHONE_TYPE_GSM):
                break;
            case (TelephonyManager.PHONE_TYPE_CDMA):
                break;
            case (TelephonyManager.PHONE_TYPE_NONE):
                break;
            default:
                break;
        }
        IMSI = m_telephonyManager.getSubscriberId();
        return IMSI;
    }

    private class MainWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("Log", "loading: " + url);

            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            view.loadUrl("about:blank");
        }
    }
}

