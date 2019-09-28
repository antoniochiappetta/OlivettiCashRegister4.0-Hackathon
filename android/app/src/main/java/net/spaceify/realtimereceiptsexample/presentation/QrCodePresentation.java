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

    private String TAG = "GenerateQRCode";
    private ImageView qrImageView;
    private ImageView itemImageView;
    private TextView discountTextView;
    private TextView itemTextView;

    private String inputValue;
    private Bitmap bitmap;
    private QRGEncoder qrgEncoder;
    private Context mContext;

    private QRReceiver receiver;


    // TODO view outlets

    // MARK: - Lifecycle

    public QrCodePresentation(Context outerContext, Display display) {
        super(outerContext, display);

        mContext = outerContext;
        // TODO setup with product and generate QR code



}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_presentation);

        qrImageView = (ImageView) findViewById(R.id.QR_Image);

        // Start listening for discounts
        receiver = new QrCodePresentation.QRReceiver(new Handler());
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, new IntentFilter("GET_PRODUCT_DISCOUNTS"));
        System.out.println("QrCodePresentation: I am LISTENING for qrcodes");

        WindowManager manager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        qrgEncoder = new QRGEncoder(
                inputValue, null,
                QRGContents.Type.TEXT,
                smallerDimension);
        try {
            bitmap = qrgEncoder.encodeAsBitmap();
            qrImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v(TAG, e.toString());
        }

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

            if(intent.getAction().equals("GET_PRODUCT_QRCODE"))
            {
                final Bitmap qrcode = (Bitmap) intent.getParcelableExtra("qrcode");
                System.out.println("QRReceiver: I got a qr code");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(qrcode);
                    }
                });
            }
        }

    }
}
