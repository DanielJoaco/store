package storeApp.menu.forms;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import storeApp.orders.Address;
import storeApp.orders.StreetType;

import static storeApp.menu.utils.Utils.showError;


public class AddressFormController {

    @FXML
    private ComboBox<String> comboBoxStreet;

    @FXML
    private TextField txtStNumber, txtStLetter, txtCrossStNumber, txtCrossStLetter, txtHouseNumer, txtHouseLetter, txtIndications, txtPostalCode, txtCity, txtState, txtCountry;

    @FXML
    private Label lblError;

    private Address address;
    private Stage stage;

    public void initialize(Stage stage) {
        this.stage = stage;
        comboBoxStreet.getItems().addAll(StreetType.getAllStreetTypes());

    }

    @FXML
    private void createAddress(){

        String street = comboBoxStreet.getValue() == null ? "" : comboBoxStreet.getValue().replace(" ", "_").toUpperCase();
        StreetType streetType = street.isEmpty() ? null : StreetType.valueOf(street);
        if(streetType == null){
            showError(lblError, "Street type is required");
            return;
        }

        int streetNumber;
        if(txtStNumber.getText().isEmpty()){
            showError(lblError, "Street number is required");
            return;
        }else{
            try{
                streetNumber = Integer.parseInt(txtStNumber.getText());
            }catch (NumberFormatException e){
                showError(lblError, "Street number must be a number");
                return;
            }
        }

        String streetLetter = txtStLetter.getText().isEmpty() ? "" : txtStLetter.getText();

        int crossStreetNumber;
        if(txtCrossStNumber.getText().isEmpty()){
            showError(lblError, "Cross street number is required");
            return;
        }else{
            try{
                crossStreetNumber = Integer.parseInt(txtCrossStNumber.getText());
            } catch (NumberFormatException e){
                showError(lblError, "Cross street number must be a number");
                return;
            }
        }

        String crossStreetLetter = txtCrossStLetter.getText().isEmpty() ? "" : txtCrossStLetter.getText();

        int houseNumber;
        if(txtHouseNumer.getText().isEmpty()){
            showError(lblError, "House number is required");
            return;
        }else{
            try {
                houseNumber = Integer.parseInt(txtHouseNumer.getText());
            } catch (NumberFormatException e) {
                showError(lblError, "House number must be a number");
                return;
            }
        }

        String houseLetter = txtHouseLetter.getText().isEmpty() ? "" : txtHouseLetter.getText();
        String indications = txtIndications.getText().isEmpty() ? "" : txtIndications.getText();
        String postalCode = txtPostalCode.getText().isEmpty() ? "" : txtPostalCode.getText();
        String city = txtCity.getText().isEmpty() ? "" : txtCity.getText();
        String state = txtState.getText().isEmpty() ? "" : txtState.getText();
        String country = txtCountry.getText().isEmpty() ? "" : txtCountry.getText();

        try{
            address = new Address(streetType, streetNumber, streetLetter, crossStreetNumber, crossStreetLetter, houseNumber, houseLetter, indications, postalCode, city, state, country);
            stage.close();
        } catch (Exception e) {
            showError(lblError, e.getMessage());
        }

    }

    public Address getAddress() {return address;}
}
