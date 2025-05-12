package storeApp.orders;

import static storeApp.menu.utils.Utils.capitalize;

public enum StreetType {
    AVENUE, STREET, ROAD, HIGHWAY, BOULEVARD, LANE, DRIVE, CIRCLE, PARKWAY;

    public static String[] getAllStreetTypes() {
        String[] allStreetTypes = new String[StreetType.values().length];
        for (int i = 0; i < StreetType.values().length; i++) {
            allStreetTypes[i] = capitalize(StreetType.values()[i].name());
        }
        return allStreetTypes;
    }
}
