package net.spaceify.realtimereceiptsexample.presentation;

import android.app.Presentation;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;

import net.spaceify.realtimereceiptsexample.R;

public class QrCodePresentation extends Presentation {

    // MARK: - Properties

    // TODO view outlets

    // MARK: - Lifecycle

    public QrCodePresentation(Context outerContext, Display display) {
        super(outerContext, display);

        // TODO setup with product and generate QR code
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_presentation);
    }
}
