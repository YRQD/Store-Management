package domain;

public record MarkupRequest(double percentage, Integer categoryId, boolean updateCost, boolean updateSelling) {}
