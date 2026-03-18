package presentation.dash;

import domain.OptionItem;

public final class TableFilterBuilder {
    private TableFilterBuilder() {
    }

    public static String buildProductsCondition(String locationAllLabel,
                                                String locationShopLabel,
                                                String locationStorageLabel,
                                                Object locationSelection,
                                                OptionItem categorySelection,
                                                String rawSearch) {
        return buildLocationCondition(locationAllLabel, locationShopLabel, locationStorageLabel, locationSelection)
                + " AND " + buildCategoryCondition(categorySelection)
                + " AND " + buildSearchCondition(rawSearch);
    }

    public static String buildLocationCondition(String locationAllLabel,
                                                String locationShopLabel,
                                                String locationStorageLabel,
                                                Object locationSelection) {
        String location = locationSelection == null ? locationAllLabel : locationSelection.toString();
        if (locationShopLabel.equalsIgnoreCase(location)) {
            return "location = 'Shop'";
        }
        if (locationStorageLabel.equalsIgnoreCase(location)) {
            return "location = 'Storage'";
        }
        return "TRUE";
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
        return "(barcode_sku ILIKE '%" + escaped + "%' OR partname ILIKE '%" + escaped + "%')";
    }
}

