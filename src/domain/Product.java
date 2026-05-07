package domain;

public class Product {
    public int categoryid;
    public Integer supplierid;
    public String barcode;
    public String name;
    public float cost;
    public float sell;
    public int storage;
    public int shop;
    public String brand;
    public int reorder;

    public Product(int categoryid, Integer supplierid, String barcode, String name, float cost, float sell,
                   int storage, int shop, String brand, int reorder) {
        this.categoryid = categoryid;
        this.supplierid = supplierid;
        this.barcode = barcode;
        this.name = name;
        this.cost = cost;
        this.sell = sell;
        this.storage = storage;
        this.shop = shop;
        this.brand = brand;
        this.reorder = reorder;
    }

}
