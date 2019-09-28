package net.spaceify.realtimereceiptsexample.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.spaceify.realtimereceiptsexample.R;
import net.spaceify.realtimereceiptsexample.MainActivity;
import net.spaceify.realtimereceiptsexample.models.Discount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static android.content.ContentValues.TAG;

public class FetchReceiptsForegroundService extends Service {

    // MARK: - Properties

    public static final String CHANNEL_ID = "FetchReceiptsForegroundServiceChannel";

    // MARK: - Lifecycle

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fetching receipts to show discounts")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        // Start listener on Fetch Receipts
        fetchReceipts();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    // MARK: - Fetch Receipts

    private void fetchReceipts() {

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
                handleReceipt(text);
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

    private final void handleReceipt(final String text) {
        try {
            JSONObject receipt = new JSONObject(text);
            JSONArray products = receipt.getJSONArray("params").getJSONObject(2).getJSONArray("lines");
            List<String> SKUs = new ArrayList<String>();
            for(int i = 0; i < products.length(); i++){
                SKUs.add(products.getJSONObject(i).getString("productSku"));
            }
            String id = receipt.getJSONArray("params").getJSONObject(2).getString("receiptId");
            sendReceipt(id,SKUs);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private final void sendReceipt(final String transactionId, final List<String> items) {
        try {
            URL url = new URL ("https://minethebill.herokuapp.com/api/transaction");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            JSONObject body = new JSONObject();
            body.put("transactionId", transactionId);
            body.put("transactionList", items);
            String jsonInputString = body.toString();
            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                handleDiscounts(response.toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleDiscounts(String text) {
        try {
            JSONObject suggestions = new JSONObject(text);
            JSONArray discountArray = suggestions.getJSONArray("offers");
            ArrayList<Discount> discounts = new ArrayList<Discount>();
            for(int i = 0; i < discountArray.length(); i++){
                discounts.add(new Discount(discountArray.getJSONObject(i).getString("sku"),discountArray.getJSONObject(i).getDouble("discount")));
            }
            sendDiscounts(discounts);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private final void sendDiscounts(final ArrayList<Discount> items) {
        Intent sendDiscounts = new Intent();
        sendDiscounts.setAction("GET_PRODUCT_DISCOUNTS");
        sendDiscounts.putParcelableArrayListExtra("discounts", items);
        sendBroadcast(sendDiscounts);
    }

}
