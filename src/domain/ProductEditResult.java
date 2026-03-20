package domain;

public record ProductEditResult(int categoryId,
                                Integer supplierId,
                                String partName,
                                float costPrice,
                                float sellingPrice,
                                int stockQuantity,
                                String brand,
                                int reorderLevel,
                                String location,
                                boolean isActive) {
}

