package com.danieljoaco.storeapp.orders;

import java.util.Objects;
import java.util.StringJoiner;

import static com.danieljoaco.storeapp.menu.utils.Utils.capitalize;
import static com.danieljoaco.storeapp.utils.FieldsValidator.*;

public record Address(
        StreetType st,
        int stNum,
        String stLet,
        int crossStNum,
        String crossStLet,
        int houseNum,
        String houseLet,
        String indications,
        String city,
        String postalCode,
        String state,
        String country
) {

    public Address {

        indications = Objects.toString(indications, "");
        Objects.requireNonNull(st, "StreetType it cannot be null");

        if (!stLet.isBlank() && !isValidAlphabeticInput(stLet)) throw new IllegalArgumentException("stLet it must be a valid location");
        if (!crossStLet.isBlank() && !isValidAlphabeticInput(crossStLet)) throw new IllegalArgumentException("crossStLet it must be a valid location");
        if (!houseLet.isBlank() && !isValidAlphabeticInput(houseLet)) throw new IllegalArgumentException("houseLet it must be a valid location");
        if (!indications.isBlank() && !isValidAlphanumericInput(indications)) throw new IllegalArgumentException("indications it must be a valid location");
        if (!isValidLocation(city)) throw new IllegalArgumentException("city it must be a valid location");
        if (!isValidPostalCode(postalCode)) throw new IllegalArgumentException("postalCode it must be a valid postal code");
        if (!isValidLocation(state)) throw new IllegalArgumentException("state it must be a valid location");
        if (!isValidLocation(country)) throw new IllegalArgumentException("country it must be a valid location");
        if (stNum < 0) throw new IllegalArgumentException("stNum it must be >= 0");
        if(crossStNum < 0) throw new IllegalArgumentException("crossStNum it must be >= 0");
        if (houseNum < 0) throw new IllegalArgumentException("houseNum it must be >= 0");
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(capitalize(st.name()) + " " + stNum + stLet
                + " #" + crossStNum + crossStLet
                + "-" + houseNum + houseLet
                + (indications.isBlank() ? "" : ", " + indications));
        sj.add("City: " + city);
        sj.add("Postal code: " + postalCode);
        sj.add("State: " + state);
        sj.add("Country: " + country);
        return sj.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private StreetType st;
        private int numberSt;
        private String letterSt = "";
        private int numberStc;
        private String letterStc = "";
        private int numberHouse;
        private String letterHouse = "";
        private String indications = "";
        private String city;
        private String postalCode;
        private String state;
        private String country;

        public Builder st(StreetType st) {
            this.st = st;
            return this;
        }
        public Builder numberSt(int numberSt) {
            this.numberSt = numberSt;
            return this;
        }
        public Builder letterSt(String letterSt) {
            this.letterSt = letterSt;
            return this;
        }
        public Builder numberStc(int numberStc) {
            this.numberStc = numberStc;
            return this;
        }
        public Builder letterStc(String letterStc) {
            this.letterStc = letterStc;
            return this;
        }
        public Builder numberHouse(int numberHouse) {
            this.numberHouse = numberHouse;
            return this;
        }
        public Builder letterHouse(String letterHouse) {
            this.letterHouse = letterHouse;
            return this;
        }
        public Builder indications(String indications) {
            this.indications = indications;
            return this;
        }
        public Builder city(String city) {
            this.city = city;
            return this;
        }
        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }
        public Builder state(String state) {
            this.state = state;
            return this;
        }
        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Address build() {
            return new Address(
                    st, numberSt, letterSt,
                    numberStc, letterStc,
                    numberHouse, letterHouse,
                    indications,
                    city, postalCode, state, country
            );
        }
    }
}
