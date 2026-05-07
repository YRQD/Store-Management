package domain;

public record ProductEditResult(int categoryId,
                                Integer supplierId,
                                String name,
                                float cost,
                                float selling,
                                int storage,
                                int shop,
                                String brand,
                                int reorder,
                                boolean isActive) {
}
