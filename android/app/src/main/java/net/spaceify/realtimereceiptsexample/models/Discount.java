package net.spaceify.realtimereceiptsexample.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Discount implements Parcelable {

    // MARK: - Properties

    private String sku;
    private Double discount;

    // MARK: - Initialization

    public Discount(String sku, Double discount) {
        this.sku = sku;
        this.discount = discount;
    }

    protected Discount(Parcel in) {
        this.sku = in.readString();
        this.discount = in.readDouble();
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

    // MARK: - Getters and setters

    public String getSku() {
        return this.sku;
    }

    public Double getDiscount() {
        return this.discount;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    // MARK: - Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sku);
        dest.writeDouble(this.discount);
    }
}
