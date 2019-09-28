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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.spaceify.realtimereceiptsexample.R;
import net.spaceify.realtimereceiptsexample.models.Discount;
import net.spaceify.realtimereceiptsexample.presentation.DiscountsPresentation;
import net.spaceify.realtimereceiptsexample.singletons.ConcurrencyManager;

import java.util.ArrayList;

public class DiscountsActivity extends AppCompatActivity {

    // MARK: - Properties

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
            discountsPresentation.show();
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

        // TODO set image and name after BE modification

        this.product1NameTextView.setText(items.get(0).getSku());
        this.product1DiscountTextView.setText("-" + items.get(0).getDiscount());

        this.product2NameTextView.setText(items.get(1).getSku());
        this.product2DiscountTextView.setText("-" + items.get(1).getDiscount());

        this.product3NameTextView.setText(items.get(2).getSku());
        this.product3DiscountTextView.setText("-" + items.get(2).getDiscount());
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
