package presentation.dash;

import domain.OptionItem;

public final class TableFilterBuilder {
    private TableFilterBuilder() {
    }

    public static String buildProductsCondition(OptionItem categorySelection,
                                                String rawSearch) {
        return buildCategoryCondition(categorySelection)
                + " AND " + buildSearchCondition(rawSearch);
    }

    public static String buildCategoryCondition(OptionItem categorySelection) {
        if (categorySelection != null && categorySelection.id() != null) {
            return "categoryid = " + categorySelection.id();
        }
        return "TRUE";
    }

    public static String buildSearchCondition(String rawSearch) {
        if (rawSearch == null || rawSearch.trim().isEmpty()) {
            return "TRUE";
        }
        String escaped = rawSearch.trim().replace("'", "''");
        return "(barcode ILIKE '%" + escaped + "%' OR name ILIKE '%" + escaped + "%')";
    }
}
