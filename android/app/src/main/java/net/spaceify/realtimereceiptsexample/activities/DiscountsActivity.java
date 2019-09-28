package net.spaceify.realtimereceiptsexample.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;

import net.spaceify.realtimereceiptsexample.R;
import net.spaceify.realtimereceiptsexample.models.Discount;
import net.spaceify.realtimereceiptsexample.presentation.DiscountsPresentation;
import net.spaceify.realtimereceiptsexample.presentation.QrCodePresentation;
import net.spaceify.realtimereceiptsexample.singletons.ConcurrencyManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class DiscountsActivity extends AppCompatActivity {

    // MARK: - Properties

    private Discount selectedProduct;
    private Discount product1;
    private Discount product2;
    private Discount product3;

    private Button product1Button;
    private ImageView product1ImageView;
    private TextView product1NameTextView;
    private TextView product1DiscountTextView;
    private TextView product1ExpiryTextView;


    private Button product2Button;
    private ImageView product2ImageView;
    private TextView product2NameTextView;
    private TextView product2DiscountTextView;
    private TextView product2ExpiryTextView;


    private Button product3Button;
    private ImageView product3ImageView;
    private TextView product3NameTextView;
    private TextView product3DiscountTextView;
    private TextView product3ExpiryTextView;

    private DiscountsReceiver receiver;
    private DiscountsPresentation discountsPresentation;
    private QrCodePresentation qrCodePresentation;

    private Bitmap bitmap;
    private QRGEncoder qrgEncoder;

    // MARK: - Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discounts);

        // Initialize views
        setupProduct1Views();
        setupProduct2Views();
        setupProduct3Views();

        // Start listening for discounts
        receiver = new DiscountsReceiver(new Handler());
        registerReceiver(receiver, new IntentFilter("GET_PRODUCT_DISCOUNTS"));
        System.out.println("DiscountsReceiver: I am LISTENING for discounts");
        synchronized (ConcurrencyManager.sharedLock) {
            ConcurrencyManager.sharedLock.notify();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialize presentation
        DisplayManager displayManager = (DisplayManager) getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
        Display[] presentationDisplays =  displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        if (presentationDisplays.length > 0) {
            discountsPresentation = new DiscountsPresentation(this, presentationDisplays[0]);
            qrCodePresentation = new QrCodePresentation(this, presentationDisplays[0]);
            discountsPresentation.show();
            qrCodePresentation.hide();
        }
    }

    private void setupProduct1Views() {
        this.product1Button = (Button) findViewById(R.id.button1);
        this.product1ImageView = (ImageView) findViewById(R.id.imageView1);
        this.product1NameTextView = (TextView) findViewById(R.id.nameTextView1);
        this.product1DiscountTextView = (TextView) findViewById(R.id.discountTextView1);
        this.product1ExpiryTextView = (TextView) findViewById(R.id.expiryTextView1);
    }

    private void setupProduct2Views() {
        this.product2Button = (Button) findViewById(R.id.button2);
        this.product2ImageView = (ImageView) findViewById(R.id.imageView2);
        this.product2NameTextView = (TextView) findViewById(R.id.nameTextView2);
        this.product2DiscountTextView = (TextView) findViewById(R.id.discountTextView2);
        this.product2ExpiryTextView = (TextView) findViewById(R.id.expiryTextView2);
    }

    private void setupProduct3Views() {
        this.product3Button = (Button) findViewById(R.id.button3);
        this.product3ImageView = (ImageView) findViewById(R.id.imageView3);
        this.product3NameTextView = (TextView) findViewById(R.id.nameTextView3);
        this.product3DiscountTextView = (TextView) findViewById(R.id.discountTextView3);
        this.product3ExpiryTextView = (TextView) findViewById(R.id.expiryTextView3);
    }

    // MARK: - Update UI

    private void updateUI(ArrayList<Discount> items) {

        this.product1 = items.get(0);
        this.product2 = items.get(1);
        this.product3 = items.get(2);

        this.product1NameTextView.setText(items.get(0).getName());
        System.out.println(items.get(0).getPictureLink());
        this.product1DiscountTextView.setText("- " + items.get(0).getDiscount() + " %");
        new DownloadImageTask(this.product1ImageView).execute(items.get(0).getPictureLink());

        this.product2NameTextView.setText(items.get(1).getName());
        this.product2DiscountTextView.setText("- " + items.get(1).getDiscount() + " %");
        new DownloadImageTask(this.product2ImageView).execute(items.get(1).getPictureLink());

        this.product3NameTextView.setText(items.get(2).getName());
        this.product3DiscountTextView.setText("- " + items.get(2).getDiscount() + " %");
        new DownloadImageTask(this.product3ImageView).execute(items.get(2).getPictureLink());
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                /////////////////
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in, null, options);

                int scalingFactor = 1;
                int maxMeasure = Math.max(options.outHeight, options.outWidth);

                while (maxMeasure / scalingFactor >= 4096) {
                    scalingFactor *= 2;
                }

                options.inSampleSize = scalingFactor;
                options.inJustDecodeBounds = false;

                mIcon11 = BitmapFactory.decodeStream(in, null, options);

                ////////////////////
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            System.out.println("image downloaded");
            bmImage.setImageBitmap(result);
        }
    }

    // MARK: - Actions

    public void dismissApp(View v) {
        moveTaskToBack(true);
    }

    public void selectProduct1(View v) {
        product1Button.setBackgroundColor(Color.parseColor("#F1E5CF"));
        product2Button.setBackgroundColor(Color.parseColor("#F5EFE6"));
        product3Button.setBackgroundColor(Color.parseColor("#F5EFE6"));
        Discount selectedProduct = this.product1;
        this.performSelection(selectedProduct);
    }

    public void selectProduct2(View v) {
        product1Button.setBackgroundColor(Color.parseColor("#F5EFE6"));
        product2Button.setBackgroundColor(Color.parseColor("#F1E5CF"));
        product3Button.setBackgroundColor(Color.parseColor("#F5EFE6"));
        Discount selectedProduct = this.product2;
        this.performSelection(selectedProduct);
    }

    public void selectProduct3(View v) {
        product1Button.setBackgroundColor(Color.parseColor("#F5EFE6"));
        product2Button.setBackgroundColor(Color.parseColor("#F5EFE6"));
        product3Button.setBackgroundColor(Color.parseColor("#F1E5CF"));
        Discount selectedProduct = this.product3;
        this.performSelection(selectedProduct);
    }

    private class PerformSelection extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                // Send the selection to the BE
                URL url = new URL ("https://minethebill.herokuapp.com/api/selection");
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(false);
                con.setInstanceFollowRedirects(false);
                JSONObject body = new JSONObject();
                body.put("transactionId", params[0]);
                body.put("selectedProduct", params[1]);
                body.put("discount", Double.valueOf(params[2]));
                String jsonInputString = body.toString();
                System.out.println(jsonInputString);
                try(OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                System.out.println(con.getResponseCode());
                System.out.println(con.getResponseMessage());
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject responseJson = new JSONObject(response.toString());
                    String discountSku = responseJson.getString("discountSKU");
                    return discountSku;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String discountSku) {
            handleDiscountSku(selectedProduct,discountSku);
        }
    }

    private void performSelection(@NotNull Discount product) {
        this.selectedProduct = product;
        new PerformSelection().execute(product.getTransactionId(), product.getSku(), String.valueOf(product.getDiscount()));
    }

    private void handleDiscountSku(Discount product, String discountSku) {
        // Generate the QR code
        WindowManager manager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        qrgEncoder = new QRGEncoder(
                discountSku, null,
                QRGContents.Type.TEXT,
                smallerDimension);
        try {
            bitmap = qrgEncoder.encodeAsBitmap();
            // Send the selection to the presentation activity
            discountsPresentation.hide();
            qrCodePresentation.show();
            Intent sendQrCode = new Intent();
            sendQrCode.setAction("GET_PRODUCT_QR_CODE");
            sendQrCode.putExtra("discount", product);
            sendQrCode.putExtra("qrCode", bitmap);
            sendBroadcast(sendQrCode);
        } catch (WriterException e) {
            Log.v("GenerateQRCode", e.toString());
        }
    }

    // MARK: - Listen to Discounts

    class DiscountsReceiver extends BroadcastReceiver {

        // MARK: - Properties

        private final Handler handler; // Handler used to execute code on the UI thread

        // MARK: - Initialization

        public DiscountsReceiver(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("GET_PRODUCT_DISCOUNTS"))
            {
                final ArrayList<Discount> discounts = intent.getParcelableArrayListExtra("discounts");
                System.out.println("DiscountsPresentationReceiver: I got a LIST discounts");
                System.out.println(discounts);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(discounts);
                    }
                });
            }
        }

    }
}
