package net.spaceify.realtimereceiptsexample.models;

public class Discount {

    // MARK: - Properties

    String sku;
    Double discount;

    // MARK: - Initialization

    public Discount(String sku, Double discount) {
        this.sku = sku;
        this.discount = discount;
    }

}
