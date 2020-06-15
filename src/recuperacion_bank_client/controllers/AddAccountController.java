/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recuperacion_bank_client.controllers;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.ws.rs.core.GenericType;
import recuperacion_bank_client.Application;
import recuperacion_bank_client.clients.AccountClient;
import recuperacion_bank_client.model.Account;
import recuperacion_bank_client.model.AccountType;
import recuperacion_bank_client.model.Customer;

/**
 * Controller class for the view AddAccountView
 * @author leioa
 */
public class AddAccountController {

    /**
     * The user that is logged
     */
    private Customer user = new Customer();

    /**
     * Logger for AddAccountController class
     */
    private static final Logger LOGGER = Logger.
            getLogger("recuperacion_bank_client.controllers.AddAccountController");
    
    /**
     * FileHandler to write logs on a file
     */
    private FileHandler fileHandlerLogger;

    /**
     * The REST client for accounts
     */
    private static final AccountClient CLIENT = new AccountClient();

    /**
     * The stage of the controller
     */
    public Stage stage;

    /**
     * The stage of the parent window
     */
    public Stage parentStage;
    /**
     * The event that has fired this window opening
     */
    public ActionEvent parentEvent;

    /**
     * TextField that shows the auto-generated id for the new account
     */
    @FXML
    private TextField textFieldId;

    /**
     * ComboBox for selecting the type for the new account
     */
    @FXML
    private ComboBox<AccountType> comboBoxType;

    /**
     * TextField that shows the begin balance at the creation of the account
     */
    @FXML
    private TextField textFieldBeginBalance;

    /**
     * TextField that shows the credit line of the account
     */
    @FXML
    private TextField textFieldCreditLine;

    /**
     * TextArea that shows the description of the account
     */
    @FXML
    private TextArea textAreaDescription;

    /**
     * Label that shows the currency symbol on textFieldBeginBalance based on
     * user Locale
     */
    @FXML
    private Label labelCurrencySymbolBalance;

    /**
     * Label that shows the currency symbol on textFieldCreditLine based on user
     * Locale
     */
    @FXML
    private Label labelCurrencySymbolCredit;

    /**
     * Label that shows the warning for description when it's wrong
     */
    @FXML
    private Label labelWarningDescription;
    /**
     * Button for cancelling the creation of the new account
     */
    @FXML
    private Button buttonExit;

    /**
     * Button for send the request to create a new account
     */
    @FXML
    private Button buttonCreateAccount;

    /**
     * This method initializes the stage and shows the window, sets the
     * visibility of the components and assigns the listeners.
     *
     * @param root Root to assign to the scene
     */
    public void initStage(Parent root) throws Exception {
        Application.setLoggerToFile(LOGGER, fileHandlerLogger);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Add account");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node) parentEvent.getSource()).getScene().getWindow());

        stage.setOnHidden(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/recuperacion_bank_client/views/CustomerAccountsView.fxml"));
                    Parent root = (Parent) loader.load();
                    CustomerAccountsController controller = ((CustomerAccountsController) loader.getController());
                    controller.setUser(user);
                    controller.setStage(parentStage);
                    controller.setFileHandlerLogger(fileHandlerLogger);
                    controller.initStage(root);
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error updating the window");
                    alert.setHeaderText(null);
                    alert.setContentText("An error occurred while updating the accounts window. Please try again.");
                    alert.showAndWait();
                    LOGGER.severe("There was an error opening CustomerAccountsController: " + e.getMessage());
                }
            }
        }
        );

        labelWarningDescription.setVisible(false);
        textFieldCreditLine.setEditable(false);
        textFieldCreditLine.setFocusTraversable(false);
        textFieldCreditLine.setMouseTransparent(true);

        textAreaDescription.focusedProperty().addListener(this::focusChangedDescription);
        textFieldBeginBalance.focusedProperty().addListener(this::focusChangedNumericFields);
        textFieldCreditLine.focusedProperty().addListener(this::focusChangedNumericFields);
        comboBoxType.setOnAction(this::handleComboBoxSelectionAction);
        buttonCreateAccount.setOnAction(this::handleButtonCreateAccountAction);
        buttonExit.setOnAction(this::handleButtonExitAction);

        prepareFields();

        stage.show();

    }

    /**
     * This method generates random and unique id after checking all the
     * accounts Id.
     *
     * @return The generated id
     */
    private Long getRandomId() {
        List<Account> accounts;
        Long id = Long.parseLong("0");
        boolean valid = false;

        while (!valid) {
            id = ThreadLocalRandom.current().nextLong(1, Long.parseLong("9999999999"));
            boolean repeated = false;
            accounts = getAllAccounts();
            for (Account a : accounts) {
                if (a.getId().compareTo(id) == 0) {
                    repeated = true;
                    break;
                }
            }
            if (!repeated) {
                valid = true;
            }
        }
        return id;
    }

    private List<Account> getAllAccounts() {
        List accounts = new ArrayList();
        try {
            accounts = new ArrayList<Account>(
                    CLIENT.findAllAccounts(new GenericType<Set<Account>>() {
                    }));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error communicating with server");
            alert.setHeaderText(null);
            alert.setContentText("Cannot get the accounts. Please, try again later...");
            alert.showAndWait();
            LOGGER.severe("There was an error retrieving the accounts: " + e.getMessage());
            accounts = new ArrayList<Account>();
        }
        return accounts;
    }

    /**
     * This method configures the fields before letting user interact with them
     */
    private void prepareFields() {

        comboBoxType.setItems(FXCollections.observableArrayList(AccountType.values()));
        comboBoxType.setValue(AccountType.STANDARD);

        Pattern pattern = Pattern.compile("(?!\\.)\\d*(\\.\\d{0,2})?");
        UnaryOperator unaryOpeartorNumericFields = (UnaryOperator<TextFormatter.Change>) change -> {
            if (change.getControlNewText().length() < 15) {
                return pattern.matcher(change.getControlNewText()).matches() ? change : null;
            } else {
                return null;
            }
        };

        UnaryOperator unaryOpeartorDescription = (UnaryOperator<TextFormatter.Change>) change -> {
            if (change.getControlNewText().length() < 150) {
                return change;
            } else {
                return null;
            }
        };
        TextFormatter formatterBeginBalance = new TextFormatter(unaryOpeartorNumericFields);
        TextFormatter formatterCreditLine = new TextFormatter(unaryOpeartorNumericFields);
        TextFormatter formatterDescription = new TextFormatter(unaryOpeartorDescription);

        textFieldBeginBalance.setTextFormatter(formatterBeginBalance);
        textFieldCreditLine.setTextFormatter(formatterCreditLine);
        textAreaDescription.setTextFormatter(formatterDescription);

        Currency currency = Currency.getInstance(Locale.getDefault());
        labelCurrencySymbolBalance.setText(currency.getSymbol());
        labelCurrencySymbolCredit.setText(currency.getSymbol());

        textAreaDescription.setWrapText(true);
        textFieldId.setText("" + getRandomId());
    }

    /**
     * This method handles the action of click on Create Account button
     *
     * @param e The event that fired this handler
     */
    private void handleButtonCreateAccountAction(ActionEvent e) {
        if (!textAreaDescription.getText().isEmpty()) {
            Account newAccount = new Account();
            newAccount.setId(Long.parseLong(textFieldId.getText()));
            newAccount.setType(comboBoxType.getValue());
            newAccount.setBeginBalance(Double.parseDouble(textFieldBeginBalance.getText()));
            newAccount.setBalance(Double.parseDouble(textFieldBeginBalance.getText()));
            newAccount.setBeginBalanceTimestamp(Date.from(Instant.now()));
            newAccount.setDescription(textAreaDescription.getText());
            newAccount.setCreditLine(Double.parseDouble(textFieldCreditLine.getText()));
            Set<Customer> customers = new HashSet<Customer>();
            customers.add(user);
            newAccount.setCustomers(customers);
            newAccount.setMovements(new HashSet<>());
            List<Account> accounts = getAllAccounts();

            boolean repeated = false;
            for (Account a : accounts) {
                if (a.getId().compareTo(newAccount.getId()) == 0) {
                    repeated = true;
                    break;
                }
            }
            if (!repeated) {
                createNewAccount(newAccount);
            } else if (repeated) {
                LOGGER.info("The new account id has updated because was repeated at the moment of the creation");
                newAccount.setId(getRandomId());
                textFieldId.setText("" + newAccount.getId());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Updated id");
                alert.setHeaderText(null);
                alert.setContentText("The new account id has updated because was repeated at the moment of the creation");
                alert.showAndWait();
                createNewAccount(newAccount);
            }
        } else {
            labelWarningDescription.setVisible(true);
        }
    }

    /**
     * This method calls the RESY method for the creation of a new account and
     * informs the user about the result
     *
     * @param newAccount the account that will be sended
     */
    private void createNewAccount(Account newAccount) {
        boolean ok = false;
        try {
            CLIENT.createAccount(newAccount);
            ok = true;
            
            LOGGER.info("New account with id: " + newAccount.getId() + " created for customer: " + user.getId());
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error creating the account");
            alert.setHeaderText(null);
            alert.setContentText("Cannot create the new Account. Please, try again later...");
            alert.showAndWait();
            LOGGER.severe("There was an error creating new account: " + ex.getMessage());
        }

        if (ok){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("New account created!");
            alert.setHeaderText(null);
            alert.setContentText("You have succesfully created a new "
                    + newAccount.getType() + " account with id: " + newAccount.getId());
            alert.showAndWait();
            
            textFieldId.setText("" + getRandomId());
            textAreaDescription.setText("");
            textFieldBeginBalance.setText("0");
            textFieldCreditLine.setText("0");
        }
    }

    /**
     * This method handles the action of click on Exit button
     *
     * @param e The event that fired this handler
     */
    private void handleButtonExitAction(ActionEvent e) {
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    /**
     * This method handles when the value of ComboBox has changed
     *
     * @param e The event that fired this handler
     */
    private void handleComboBoxSelectionAction(ActionEvent e) {
        if (comboBoxType.getValue().equals(AccountType.CREDIT)) {
            textFieldCreditLine.setEditable(true);
            textFieldCreditLine.setFocusTraversable(true);
            textFieldCreditLine.setMouseTransparent(false);
        } else {
            textFieldCreditLine.setText("0");
            textFieldCreditLine.setEditable(false);
            textFieldCreditLine.setFocusTraversable(false);
            textFieldCreditLine.setMouseTransparent(true);
        }
    }

    /**
     * Method that checks if text is well formed when focus turns out of a
     * numeric field
     *
     * @param observable
     * @param oldValue
     * @param newValue
     */
    private void focusChangedNumericFields(ObservableValue observable, Boolean oldValue, Boolean newValue) {
        if (!newValue) {
            if (textFieldBeginBalance.getText().isEmpty()) {
                textFieldBeginBalance.setText("0");
            }
            if (textFieldCreditLine.getText().isEmpty()) {
                textFieldCreditLine.setText("0");
            }

            try {
                if (textFieldBeginBalance.getText().
                        charAt(textFieldBeginBalance.getText().length() - 1) == '.') {
                    textFieldBeginBalance.setText(textFieldBeginBalance.getText() + "0");
                }
                if (textFieldCreditLine.getText().
                        charAt(textFieldCreditLine.getText().length() - 1) == '.') {
                    textFieldCreditLine.setText(textFieldCreditLine.getText() + "0");
                }
            } catch (IndexOutOfBoundsException e) {
            }
            DecimalFormat formatter = (DecimalFormat) NumberFormat
                    .getCurrencyInstance(Locale.getDefault());
            if (!textFieldBeginBalance.getText().isEmpty()) {
                textFieldBeginBalance.setText(formatter.format(Double.parseDouble(
                        textFieldBeginBalance.getText())));
            }
            if (!textFieldCreditLine.getText().isEmpty()) {
                textFieldCreditLine.setText(formatter.format(Double.parseDouble(
                        textFieldCreditLine.getText())));
            }
            textFieldCreditLine.setText(""+Double.parseDouble(textFieldCreditLine.getText()));
            textFieldBeginBalance.setText(""+Double.parseDouble(textFieldBeginBalance.getText()));

        } else {
            if (textFieldBeginBalance.isFocused() && 
                    Double.parseDouble(textFieldBeginBalance.getText()) == Double.parseDouble("0")) {
                textFieldBeginBalance.setText("");
            }

            if (textFieldCreditLine.isFocused() && 
                    Double.parseDouble(textFieldCreditLine.getText()) == Double.parseDouble("0")) {
                textFieldCreditLine.setText("");
            }
        }

    }

    /**
     * Method that checks if text is well formed when focus turns out of a
     * numeric field
     *
     * @param observable
     * @param oldValue
     * @param newValue
     */
    private void focusChangedDescription(ObservableValue observable, Boolean oldValue, Boolean newValue) {
        if (!newValue && labelWarningDescription.isVisible() && !textAreaDescription.getText().isEmpty()) {
            labelWarningDescription.setVisible(false);

        }
        if (newValue && labelWarningDescription.isVisible()) {
            labelWarningDescription.setVisible(false);
        }

    }

    /**
     * This method sets the stage
     *
     * @param stage Stage to be set
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Method that sets the file handler for loggers
     *
     * @param fileHandler for logger file
     */
    public void setFileHandlerLogger(FileHandler fileHandler) {
        this.fileHandlerLogger = fileHandler;
    }
    
    /**
     * This method sets the stage of the parent window
     *
     * @param stage Stage to be set
     */
    public void setParentStage(Stage stage) {
        this.parentStage = stage;
    }

    /**
     * This method sets the event that has opened this window
     *
     * @param parentEvent Event to be set
     */
    public void setParentEvent(ActionEvent parentEvent) {
        this.parentEvent = parentEvent;
    }

    /**
     * Method that sets the logged user
     *
     * @param user that is logged
     */
    public void setUser(Customer user) {
        this.user = user;
    }
}
