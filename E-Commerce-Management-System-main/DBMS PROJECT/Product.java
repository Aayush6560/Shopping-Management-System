public class Product {
    private int productId;
    private String style;
    private int stockQuantity;
    private double originalPrice;
    private double discountedPrice;

    // Constructor
    public Product(int productId, String style, int stockQuantity, double originalPrice, double discountedPrice) {
        this.productId = productId;
        this.style = style;
        this.stockQuantity = stockQuantity;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
    }

    // Getters
    public int getProductId() {
        return productId;
    }

    public String getStyle() {
        return style;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public double getDiscountedPrice() {
        return discountedPrice;
    }

    // Setters (optional but useful if product details can change)
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setDiscountedPrice(double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    // Helper method to calculate discount %
    public double getDiscountPercentage() {
        if (originalPrice == 0) return 0;
        return ((originalPrice - discountedPrice) / originalPrice) * 100;
    }

    @Override
    public String toString() {
        return "Product ID: " + productId +
               ", Style: " + style +
               ", Stock: " + stockQuantity +
               ", Original Price: ₹" + originalPrice +
               ", Discounted Price: ₹" + discountedPrice +
               " (" + String.format("%.1f", getDiscountPercentage()) + "% off)";
    }
}
