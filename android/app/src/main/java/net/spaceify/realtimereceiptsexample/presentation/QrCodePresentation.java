package net.spaceify.realtimereceiptsexample.presentation;

import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;

import net.spaceify.realtimereceiptsexample.R;
import net.spaceify.realtimereceiptsexample.models.Discount;

import java.io.InputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static android.content.Context.WINDOW_SERVICE;

public class QrCodePresentation extends Presentation {

    // MARK: - Properties

    private ImageView qrImageView;
    private ImageView productImageView;
    private TextView productNameTextView;
    private TextView productDiscountTextView;
    private TextView productExpiryTextView;

    private QRReceiver receiver;


    // TODO view outlets

    // MARK: - Lifecycle

    public QrCodePresentation(Context outerContext, Display display) {
        super(outerContext, display);

        // Start listening for discounts
        receiver = new QRReceiver(new Handler());
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, new IntentFilter("GET_PRODUCT_QR_CODE"));
        System.out.println("QrCodePresentation: I am LISTENING for qr codes");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_presentation);

        // Initialize views
        setupProductViews();

    }

    private void setupProductViews() {
        this.qrImageView = (ImageView) findViewById(R.id.qrCodeImageView);
        this.productImageView = (ImageView) findViewById(R.id.imageViewSelected);
        this.productNameTextView = (TextView) findViewById(R.id.nameTextViewQR);
        this.productDiscountTextView = (TextView) findViewById(R.id.discountTextViewQR);
        this.productExpiryTextView = (TextView) findViewById(R.id.expiryTextViewQR);
    }

    // MARK: - Update UI

    private void updateUI(Bitmap qrcode, Discount product) {
        this.qrImageView.setImageBitmap(qrcode);
        this.productNameTextView.setText(product.getName());
        this.productDiscountTextView.setText("- " + product.getDiscount() + " %");
        new DownloadImageTask(this.productImageView).execute(product.getPictureLink());
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
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    // MARK: - Listen to QR codes

    class QRReceiver extends BroadcastReceiver {

        // MARK: - Properties

        private final Handler handler; // Handler used to execute code on the UI thread

        // MARK: - Initialization

        public QRReceiver(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("GET_PRODUCT_QR_CODE"))
            {
                final Bitmap qrCode = intent.getParcelableExtra("qrCode");
                final Discount discount = intent.getParcelableExtra("discount");
                System.out.println("QRReceiver: I got a qr code");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(qrCode,discount);
                    }
                });
            }
        }

    }
}
