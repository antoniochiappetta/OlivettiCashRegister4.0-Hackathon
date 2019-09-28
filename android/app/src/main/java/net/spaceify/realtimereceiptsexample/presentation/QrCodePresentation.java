package net.spaceify.realtimereceiptsexample.presentation;

import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;

import net.spaceify.realtimereceiptsexample.R;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static android.content.Context.WINDOW_SERVICE;

public class QrCodePresentation extends Presentation {

    // MARK: - Properties

    private ImageView qrImageView;
    private ImageView itemImageView;
    private TextView discountTextView;
    private TextView itemTextView;

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

        qrImageView = (ImageView) findViewById(R.id.QR_Image);

    }

    // MARK: - Update UI

    private void updateUI(Bitmap qrcode) {
        this.qrImageView.setImageBitmap(qrcode);
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
                System.out.println("QRReceiver: I got a qr code");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(qrCode);
                    }
                });
            }
        }

    }
}
