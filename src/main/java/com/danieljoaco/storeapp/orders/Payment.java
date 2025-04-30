package com.danieljoaco.storeapp.orders;

public class Payment {

    public enum PaymentMethod {
        CREDIT_CARD,
        DEBIT_CARD,
        PAYPAL,
        BANK_TRANSFER,
        CASH
    }

    public enum Franchises {
        VISA,
        MASTERCARD,
        AMERICAN_EXPRESS
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
