package net.spaceify.realtimereceiptsexample.presentation;

import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.TextView;

import net.spaceify.realtimereceiptsexample.R;
import net.spaceify.realtimereceiptsexample.models.Discount;
import net.spaceify.realtimereceiptsexample.singletons.ConcurrencyManager;

import java.util.ArrayList;

public class DiscountsPresentation extends Presentation {

    // MARK: - Lifecycle

    public DiscountsPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    // MARK: - Properties

    private TextView textView = null;
    private DiscountsReceiver receiver;

    // MARK: - Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discounts_presentation);

        // Initialize text view
        this.textView = (TextView) findViewById(R.id.textView);

        // Start listening for discounts
        receiver = new DiscountsReceiver(new Handler());
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, new IntentFilter("GET_PRODUCT_DISCOUNTS"));
        System.out.println("DiscountsPresentation: I am LISTENING for discounts");
        synchronized (ConcurrencyManager.sharedLock) {
            ConcurrencyManager.sharedLock.notify();
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
                System.out.println("DiscountsReceiver: I got a LIST discounts");
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
