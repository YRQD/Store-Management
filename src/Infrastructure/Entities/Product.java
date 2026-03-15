package Infrastructure.Entities;

public class Product {
    public int categoryid;
    public Integer supplierid;
    public String barcode_sku;
    public String partname;
    public float costprice;
    public float sellingprice;
    public int stockquantity;
    public String brand;
    public int reorderlevel;
    public String location;

    public Product(int categoryid, Integer supplierid, String barcode_sku, String partname, float costprice, float sellingprice, int stockquantity, String brand, int reorderlevel, String location) {
        this.categoryid = categoryid;
        this.supplierid = supplierid;
        this.barcode_sku = barcode_sku;
        this.partname = partname;
        this.costprice = costprice;
        this.sellingprice = sellingprice;
        this.stockquantity = stockquantity;
        this.brand = brand;
        this.reorderlevel = reorderlevel;
        this.location = location;
    }

}
