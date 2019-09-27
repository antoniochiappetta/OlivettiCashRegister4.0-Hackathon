package net.spaceify.realtimereceiptsexample;



import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static android.content.ContentValues.TAG;

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

    private TextView textView = null;


    private final void handleNewReceiptJsonRpc(final String text) {

        //Todo: parse the JSONRPC call here and extract the new receipt


        // Update the UI in the main thread, this callback gets called in some other thread

        Handler mainHandler = new Handler(getMainLooper());
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                textView.append("\n" + text);
            }
        });
    }

    private void testReceipts() {

        OkHttpClient client = new OkHttpClient();
        String url = "wss://selfscanner.net:443/json-rpc";

        Request request = new Request.Builder().url(url).header("Sec-WebSocket-Protocol","json-rpc").build();

        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // connection succeeded
                Log.d(TAG, "Websocket connection succeeded");

                // Register as receipt listener, 11 is the location id for OIH1 and 1 is the sequence number of the JSONRCP call

                webSocket.send("{\"jsonrpc\": \"2.0\", \"method\": \"addReceiptListener\", \"params\": [\"hackathon\", 11, \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyTmFtZSI6ImhhY2thdGhvbiIsInVzZXJUeXBlIjoicmVndWxhciIsImlhdCI6MTU2OTMzNDk0Mn0.wf6JYu6zt0gCxNPMPRWFae9vvlZrj9eaRAgXJIDP3kM\"], \"id\": \"1\"}");

            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // text message received
                Log.d(TAG, text);
                handleNewReceiptJsonRpc(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // binary message received
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                // no more messages and the connection should be released
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                // unexpected error
            Log.d(TAG,"Websocket connection failed "+t.toString());

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.textView = (TextView) findViewById(R.id.textView);
        testReceipts();
    }
}
