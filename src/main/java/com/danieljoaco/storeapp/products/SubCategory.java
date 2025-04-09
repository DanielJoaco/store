package com.danieljoaco.storeapp.products;

public class SubCategory extends Category {

    private  final String name;

    public enum SubCategories {
        LIPS("LIPSTICKS", "DELINERS", "SOFT_LIPS", "TINTE", "GLOSS"),
        EYES("DELINERS", "EYELINER", "EYELASH", "EYE_SHADOW"),
        SKIN("FOUNDATIONS", "CONCEALERS", "SETTERS", "BLUSHES", "POWDERS");

        private final String[] items;

        SubCategories(String... items) {
            this.items = items;
        }

        public String[] getItems() {
            return items;
        }

    private static boolean isValidSubCategory(String category, String subcategory) {

            try {
                SubCategories subCategoryEnum = SubCategories.valueOf(category.toUpperCase());
                for (String item : subCategoryEnum.getItems()) {
                    if (item.equalsIgnoreCase(subcategory)) {
                        return true;
                    }
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
            return false;
        }
    }

    public SubCategory(String category, String subcategory) {
        super(category);

        if (!SubCategories.isValidSubCategory(category, subcategory)) {
            throw new IllegalArgumentException("The subcategory '" + subcategory + "' isn´t for the category '" + category + "'.");
        }

        this.name = subcategory.toUpperCase();
    }

    public String getName() {
        return name;
    }
}
