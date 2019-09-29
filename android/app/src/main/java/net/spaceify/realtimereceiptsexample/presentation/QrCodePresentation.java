package net.spaceify.realtimereceiptsexample.presentation;

import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
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

import java.io.InputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static android.content.Context.WINDOW_SERVICE;

public class QrCodePresentation extends Presentation {

    // MARK: - Properties

    private View qrView;
    private View productView;
    private TextView productNameTextView;
    private TextView productDiscountTextView;
    private TextView productExpiryTextView;


    // TODO view outlets

    // MARK: - Lifecycle

    public QrCodePresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_presentation);

        // Initialize views
        setupProductViews();

    }

    private void setupProductViews() {
        this.qrView = (View) findViewById(R.id.qrCodeView);
        this.productView = (View) findViewById(R.id.viewSelected);
        this.productNameTextView = (TextView) findViewById(R.id.nameTextViewQR);
        this.productDiscountTextView = (TextView) findViewById(R.id.discountTextViewQR);
        this.productExpiryTextView = (TextView) findViewById(R.id.expiryTextViewQR);
    }

    // MARK: - Update UI

    public void updateUI(Bitmap qrcode, Discount product) {
        this.qrView.setBackground(new BitmapDrawable(getResources(), qrcode));
        this.productNameTextView.setText(product.getName());
        this.productDiscountTextView.setText("- " + product.getDiscount() + " %");
        new DownloadImageTask(this.productView).execute(product.getPictureLink());
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        View bmImage;

        public DownloadImageTask(View bmImage) {
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
            bmImage.setBackground(new BitmapDrawable(getResources(), result));
        }
    }
}
