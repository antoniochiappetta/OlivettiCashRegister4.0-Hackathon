package net.spaceify.realtimereceiptsexample;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import net.spaceify.realtimereceiptsexample.services.FetchReceiptsForegroundService;
import net.spaceify.realtimereceiptsexample.R;

/**
 *
 *  A Simple example app for demonstrating how to get new receipts in real time from the SelfScanner JSON-RPC API.
 *
 *  Usage: Start the App and go to https://my.selfscanner.net with your web browser.
 *  Log in with username: hackathon, password: helsinki.
 *  In the location OIH1, navigate to Cash Registers -> OIH1 -> USE and sell some products using "Debug Pay"
 *  The receipts should appear in the TextView of this app in real-time
 *
 *  ToDO: use a real JSON-RCP library instead of manually crafting the calls
 *
**/


public class MainActivity extends Activity {

    // MARK: - Properties

    private TextView textView = null;

    // MARK: - Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.textView = (TextView) findViewById(R.id.textView);

        startFetchService();
    }

    // MARK: - Fetch Receipts

    public void startFetchService() {
        Intent serviceIntent = new Intent(this, FetchReceiptsForegroundService.class);

        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopFetchService() {
        Intent serviceIntent = new Intent(this, FetchReceiptsForegroundService.class);
        stopService(serviceIntent);
    }
}
