package storeApp.orders;

import static storeApp.menu.utils.Utils.capitalize;

public class Payment {

    public enum PaymentMethod {
        CREDIT_CARD,
        DEBIT_CARD,
        PAYPAL,
        BANK_TRANSFER,
        CASH;

        public static String[] getAllMethods() {

            String[] allMethods = new String[PaymentMethod.values().length];
            for (int i = 0; i < PaymentMethod.values().length; i++) {
                allMethods[i] = capitalize(PaymentMethod.values()[i].name());
            }
        return allMethods;
        }
    }

    public enum Franchises {
        VISA,
        MASTERCARD,
        AMERICAN_EXPRESS;

        public static String[] getAllFranchises() {
            String[] allFranchises = new String[Franchises.values().length];
            for (int i = 0; i < Franchises.values().length; i++) {
                allFranchises[i] = capitalize(Franchises.values()[i].name());
            }
            return allFranchises;
        }
    }

    public record PaymentData(PaymentMethod paymentMethod, Franchises franchises) {
        public PaymentData {
            boolean isCard = paymentMethod == PaymentMethod.CREDIT_CARD
                    || paymentMethod == PaymentMethod.DEBIT_CARD;

            if (isCard && franchises == null) {
                throw new IllegalArgumentException("Franchise is required for card payments.");
            }

            if (!isCard && franchises != null) {
                throw new IllegalArgumentException("Franchise must be null for non-card payments.");
            }
        }
    }
}
