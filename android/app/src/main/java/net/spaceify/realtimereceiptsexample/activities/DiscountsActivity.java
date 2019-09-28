package net.spaceify.realtimereceiptsexample.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.TextView;

import net.spaceify.realtimereceiptsexample.R;
import net.spaceify.realtimereceiptsexample.models.Discount;
import net.spaceify.realtimereceiptsexample.presentation.DiscountsPresentation;
import net.spaceify.realtimereceiptsexample.singletons.ConcurrencyManager;

import java.util.ArrayList;

public class DiscountsActivity extends AppCompatActivity {

    // MARK: - Properties

    private TextView textView;
    private DiscountsReceiver receiver;
    private DiscountsPresentation discountsPresentation;

    // MARK: - Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discounts);

        // Initialize text view
        this.textView = (TextView) findViewById(R.id.textView);

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
            discountsPresentation.show();
        }
    }

    // MARK: - Update UI

    private void updateUI(ArrayList<Discount> items) {
        this.textView.setText(
                items.get(0).getSku() + "-" + items.get(0).getDiscount()
                        + "\n" +
                        items.get(1).getSku() + "-" + items.get(1).getDiscount()
                        + "\n" +
                        items.get(2).getSku() + "-" + items.get(2).getDiscount());
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
