package com.danieljoaco.storeapp.menu.forms;

import static com.danieljoaco.storeapp.menu.utils.Utils.*;

import com.danieljoaco.storeapp.user.Admin;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Abstract base class for form controllers in the application.
 * Provides common functionality for form handling, validation, and user feedback.
 */
public abstract class FormController {

    protected Stage stage;
    protected Admin adminLogin;
    protected Label lblError;

    /**
     * Enum to represent the different modes a form can be in.
     */
    protected enum FormMode {
        CREATE,
        EDIT
    }

    protected FormMode currentMode;

    /**
     * Initialize the controller with the given stage and admin login.
     *
     * @param stage the Stage object
     * @param adminLogin the Admin user that is logged in
     */
    public void initialize(Stage stage, Admin adminLogin) {
        this.stage = stage;
        this.adminLogin = adminLogin;
        this.currentMode = FormMode.CREATE;
    }

    /**
     * Set the error label for the form.
     *
     * @param lblError the Label object to show errors
     */
    public void setErrorLabel(Label lblError) {
        this.lblError = lblError;
    }

    /**
     * Get the current stage.
     *
     * @return the current Stage
     */
    protected Stage getStage() {
        return stage;
    }

    /**
     * Handle form submission. To be implemented by subclasses.
     */
    protected abstract void handleSubmit();

    /**
     * Reset all form fields. To be implemented by subclasses.
     */
    protected abstract void resetFields();

    /**
     * Disable all form fields. To be implemented by subclasses.
     */
    protected abstract void disableAllFields();

    /**
     * Perform validation on the form data. To be implemented by subclasses.
     *
     * @return true if validation passes, false otherwise
     */
    protected abstract boolean validateFormData();

    /**
     * Try to perform an action and show appropriate feedback.
     *
     * @param action the action to perform
     * @param successMsg the message to show on success
     */
    protected void tryFormAction(Runnable action, String successMsg) {
        tryAction(lblError, action, successMsg);
    }

    /**
     * Show success message and close the form after a delay.
     *
     * @param successMsg the success message to display
     */
    protected void showSuccessAndClose(String successMsg) {
        showSuccess(lblError, successMsg);
        disableAllFields();
        closeAfterDelay(stage);
    }

    /**
     * Show an error message on the form.
     *
     * @param message the error message to display
     */
    protected void showFormError(String message) {
        showError(lblError, message);
    }

    /**
     * Process after submission logic. Default implementation disables fields,
     * shows success message and closes the form after delay.
     *
     * @param itemType the type of item that was processed
     * @param itemName the name of the item that was processed
     * @param action the action that was performed (created, updated, etc.)
     */
    protected void processAfterSubmit(String itemType, String itemName, String action) {
        disableAllFields();
        showSuccess(lblError, itemName + " " + itemType + " successfully " + action + "!");
        closeAfterDelay(stage);
    }

    /**
     * Process continue creating logic. If user wants to create another item,
     * reset fields, otherwise close the form.
     *
     * @param itemType the type of item to create
     * @return true if user wants to continue creating, false otherwise
     */
    protected boolean processContinueCreating(String itemType) {
        boolean continueCreate = askConfirmation("Create another " + itemType + "?");

        if (continueCreate) {
            resetFields();
            return true;
        } else {
            disableAllFields();
            closeAfterDelay(stage);
            return false;
        }
    }
}