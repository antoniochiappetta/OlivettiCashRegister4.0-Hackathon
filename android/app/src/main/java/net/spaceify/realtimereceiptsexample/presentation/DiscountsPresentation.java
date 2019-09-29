package net.spaceify.realtimereceiptsexample.presentation;

import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.spaceify.realtimereceiptsexample.R;
import net.spaceify.realtimereceiptsexample.activities.DiscountsActivity;
import net.spaceify.realtimereceiptsexample.models.Discount;
import net.spaceify.realtimereceiptsexample.singletons.ConcurrencyManager;

import java.io.InputStream;
import java.util.ArrayList;

public class DiscountsPresentation extends Presentation {

    // MARK: - Properties

    private Button product1Button;
    private View product1View;
    private TextView product1NameTextView;
    private TextView product1DiscountTextView;
    private TextView product1ExpiryTextView;


    private Button product2Button;
    private View product2View;
    private TextView product2NameTextView;
    private TextView product2DiscountTextView;
    private TextView product2ExpiryTextView;


    private Button product3Button;
    private View product3View;
    private TextView product3NameTextView;
    private TextView product3DiscountTextView;
    private TextView product3ExpiryTextView;

    // MARK: - Lifecycle

    public DiscountsPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    // MARK: - Initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discounts_presentation);

        // Initialize views
        setupProduct1Views();
        setupProduct2Views();
        setupProduct3Views();
    }

    private void setupProduct1Views() {
        this.product1Button = (Button) findViewById(R.id.button1);
        this.product1View = (View) findViewById(R.id.view1);
        this.product1NameTextView = (TextView) findViewById(R.id.nameTextView1);
        this.product1DiscountTextView = (TextView) findViewById(R.id.discountTextView1);
        this.product1ExpiryTextView = (TextView) findViewById(R.id.expiryTextView1);
    }

    private void setupProduct2Views() {
        this.product2Button = (Button) findViewById(R.id.button2);
        this.product2View = (View) findViewById(R.id.view2);
        this.product2NameTextView = (TextView) findViewById(R.id.nameTextView2);
        this.product2DiscountTextView = (TextView) findViewById(R.id.discountTextView2);
        this.product2ExpiryTextView = (TextView) findViewById(R.id.expiryTextView2);
    }

    private void setupProduct3Views() {
        this.product3Button = (Button) findViewById(R.id.button3);
        this.product3View = (View) findViewById(R.id.view3);
        this.product3NameTextView = (TextView) findViewById(R.id.nameTextView3);
        this.product3DiscountTextView = (TextView) findViewById(R.id.discountTextView3);
        this.product3ExpiryTextView = (TextView) findViewById(R.id.expiryTextView3);
    }

    // MARK: - Update UI

    public void updateUI(ArrayList<Discount> items) {

        this.product1NameTextView.setText(items.get(0).getName());
        this.product1DiscountTextView.setText("- " + items.get(0).getDiscount() + " %");
        new DownloadImageTask(this.product1View).execute(items.get(0).getPictureLink());

        this.product2NameTextView.setText(items.get(1).getName());
        this.product2DiscountTextView.setText("- " + items.get(1).getDiscount() + " %");
        new DownloadImageTask(this.product2View).execute(items.get(1).getPictureLink());

        this.product3NameTextView.setText(items.get(2).getName());
        this.product3DiscountTextView.setText("- " + items.get(2).getDiscount() + " %");
        new DownloadImageTask(this.product3View).execute(items.get(2).getPictureLink());
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
