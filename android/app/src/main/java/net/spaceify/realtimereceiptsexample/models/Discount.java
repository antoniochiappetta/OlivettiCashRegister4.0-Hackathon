package net.spaceify.realtimereceiptsexample.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Discount implements Parcelable {

    // MARK: - Properties

    private String transactionId;
    private String sku;
    private Double discount;
    private String name;
    private String pictureLink;
    private Double price;

    // MARK: - Initialization

    public Discount(String transactionId, String sku, Double discount, String name, String pictureLink, Double price) {
        this.transactionId = transactionId;
        this.sku = sku;
        this.discount = discount;
        this.name = name;
        this.pictureLink = pictureLink;
        this.price = price;
    }

    protected Discount(Parcel in) {
        this.transactionId = in.readString();
        this.sku = in.readString();
        this.discount = in.readDouble();
        this.name = in.readString();
        this.pictureLink = in.readString();
        this.price = in.readDouble();
    }

    public static final Creator<Discount> CREATOR = new Creator<Discount>() {
        @Override
        public Discount createFromParcel(Parcel in) {
            return new Discount(in);
        }

        @Override
        public Discount[] newArray(int size) {
            return new Discount[size];
        }
    };

    public static Creator<Discount> getCREATOR() {
        return CREATOR;
    }

    // MARK: - Getters

    public String getTransactionId() { return this.transactionId; }
    public String getSku() {
        return this.sku;
    }
    public Double getDiscount() {
        return this.discount;
    }
    public String getName() { return this.name; }
    public String getPictureLink() { return this.pictureLink; }
    public Double getPrice() { return this.price; }

    // MARK: - Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.transactionId);
        dest.writeString(this.sku);
        dest.writeDouble(this.discount);
        dest.writeString(this.name);
        dest.writeString(this.pictureLink);
        dest.writeDouble(this.price);
    }
}
