/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recuperacion_bank_client.controllers;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.ResizeFeatures;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DateStringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.LongStringConverter;
import javax.ws.rs.core.GenericType;
import recuperacion_bank_client.clients.AccountClient;
import recuperacion_bank_client.clients.CustomerClient;
import recuperacion_bank_client.model.Account;
import recuperacion_bank_client.model.AccountType;
import recuperacion_bank_client.model.Customer;
import recuperacion_bank_client.model.Movement;

/**
 *
 * @author leioa
 */
public class CustomerAccountsController {

    /**
     * The user that is logged
     */
    private Customer user = new Customer();

    /**
     * Logger for CustomerAccountsController class
     */
    private static final Logger LOGGER = Logger.
            getLogger("recuperacion_bank_client.controllers.CustomerAccountsController");

    /**
     * Stage of the controller
     */
    private Stage stage;

    /**
     * The REST client for accounts
     */
    private static final AccountClient CLIENT = new AccountClient();

    /**
     * ImageView that contains user's image
     */
    @FXML
    private ImageView imageView;

    /**
     * Label that contains the user name
     */
    @FXML
    private Label labelUserName;

    /**
     * Label that contains some user info
     */
    @FXML
    private Label labelUserInfo;

    /**
     * Label that opens a view for the creation of accounts
     */
    @FXML
    private Button buttonNewAccount;

    /**
     * TableView that shows user's accounts
     */
    @FXML
    private TableView tableView;

    /**
     * TableColumn that shows the ID of the user's accounts
     */
    @FXML
    private TableColumn tableColumnId;

    /**
     * TableColumn that shows the account type of the user's accounts
     */
    @FXML
    private TableColumn tableColumnType;

    /**
     * TableColumn that shows the description of the user's accounts
     */
    @FXML
    private TableColumn tableColumnDescription;

    /**
     * TableColumn that shows the balance of the user's accounts
     */
    @FXML
    private TableColumn tableColumnBalance;

    /**
     * TableColumn that shows the credit line of the user's accounts
     */
    @FXML
    private TableColumn tableColumnCreditLine;

    /**
     * TableColumn that shows the begin balance of the user's accounts
     */
    @FXML
    private TableColumn tableColumnBeginBalance;

    /**
     * TableColumn that shows the timestamp of the first balance of the user's
     * accounts
     */
    @FXML
    private TableColumn tableColumnBeginBalanceTimestamp;

    /**
     * TableColumn that shows the customers of the user's accounts
     */
    @FXML
    private TableColumn tableColumnCustomers;

    /**
     * TableColumn that shows the movements of the user's accounts
     */
    @FXML
    private TableColumn tableColumnMovements;

    /**
     * This method sets the stage
     *
     * @param stage Stage to be set
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * This method initializes the stage and shows the window, sets the
     * visibility of the components and assigns the listeners.
     *
     * @param root Root to assign to the scene
     */
    public void initStage(Parent root) {
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Accounts");
        //stage.setMaximized(true);

        stage.show();

        populateData();

    }

    /**
     * Method that populates the data of the view
     */
    private void populateData() {
        List<Account> accounts = new ArrayList<Account>(getAccounts("" + user.getId()));
        ObservableList<Account> observableAccounts = FXCollections.observableArrayList(accounts);
        populateTable(observableAccounts);
    }

    /**
     * Method that gets all the accounts of a user
     *
     * @param id the id of the user, used as param to filter accounts in REST
     */
    private Set<Account> getAccounts(String id) {
        Set<Account> accounts = new HashSet<Account>();
        try {
            accounts = CLIENT.findAccountsByCustomerId(new GenericType<Set<Account>>() {
            }, id);
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error communicating with server");
            alert.setHeaderText(null);
            alert.setContentText("Cannot get the user's accounts. Please, try again later...");
            alert.showAndWait();
            LOGGER.severe(e.getMessage());
            accounts = new HashSet<Account>();
        }
        return accounts;
    }

    /**
     * Method that fills the data for accounts table
     *
     * @param data contains the accounts to fill the table
     */
    private void populateTable(ObservableList<Account> data) {
        //Column Account ID
        tableColumnId.setCellFactory(TextFieldTableCell.<Account, Long>forTableColumn(new LongStringConverter()));
        tableColumnId.setCellValueFactory(new PropertyValueFactory("id"));

        //Column Account type
        tableColumnType.setCellFactory(TextFieldTableCell.<Account, AccountType>forTableColumn(new StringConverter<AccountType>() {
            @Override
            public String toString(AccountType object) {
                if (object == AccountType.CREDIT) {
                    return "CREDIT";
                } else {
                    return "STANDARD";
                }
            }

            @Override
            public AccountType fromString(String string) {
                if (string == "CREDIT") {
                    return AccountType.CREDIT;
                } else {
                    return AccountType.STANDARD;
                }
            }
        }));
        tableColumnType.setCellValueFactory(new PropertyValueFactory("type"));

        //Column Account description
        
        tableColumnDescription.setCellFactory(t -> {
            TableCell<Account, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(tableColumnDescription.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
        tableColumnDescription.setCellValueFactory(new PropertyValueFactory("description"));

        //Column Account balance
        tableColumnBalance.setCellFactory(TextFieldTableCell.<Account, Double>forTableColumn(new DoubleStringConverter()));
        tableColumnBalance.setCellValueFactory(new PropertyValueFactory("balance"));

        //Column Account credit line
        tableColumnCreditLine.setCellFactory(TextFieldTableCell.<Account, Double>forTableColumn(new DoubleStringConverter()));
        tableColumnCreditLine.setCellValueFactory(new PropertyValueFactory("creditLine"));

        //Column Account begin balance
        tableColumnBeginBalance.setCellFactory(TextFieldTableCell.<Account, Double>forTableColumn(new DoubleStringConverter()));
        tableColumnBeginBalance.setCellValueFactory(new PropertyValueFactory("beginBalance"));

        //Column Account begin balance timestamp
        tableColumnBeginBalanceTimestamp.setCellFactory(TextFieldTableCell.<Account, Date>forTableColumn(new DateStringConverter()));
        tableColumnBeginBalanceTimestamp.setCellValueFactory(new PropertyValueFactory("beginBalanceTimestamp"));

        //Column Account customers
        tableColumnCustomers.setCellFactory(call -> {
            // create a new cell for array lists
            return new ComboBoxTableCell<Account, Set<Customer>>() {
                @Override
                public void updateItem(Set<Customer> item, boolean empty) {
                    super.updateItem(item, empty);
                    // if there is no item, return an empty cell
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        ComboBox<String> box = new ComboBox<>();
                        // set combo box items
                        List<String> customersCombo = new ArrayList<>();
                        for (Customer c : item) {
                            String name = c.getFirstName() + " " + c.getLastName();
                            customersCombo.add(name);
                        }

                        box.setItems(FXCollections.observableArrayList(customersCombo));
                        box.getSelectionModel().selectLast();
                        box.prefWidthProperty().bind(tableColumnCustomers.widthProperty().subtract(5));
                        box.setMaxWidth(Control.USE_PREF_SIZE);
                        // set cell contents
                        setGraphic(box);
                    }
                }
            };
        });
        tableColumnCustomers.setCellValueFactory(new PropertyValueFactory("customers"));

        
        
        //Column Account movements
        tableColumnMovements.setCellFactory(call -> {
            // create a new cell for array lists
            return new ComboBoxTableCell<Account, Set<Movement>>() {
                @Override
                public void updateItem(Set<Movement> item, boolean empty) {
                    super.updateItem(item, empty);
                    // if there is no item, return an empty cell
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        ComboBox<String> box = new ComboBox<>();
                        // set combo box items
                        List<String> movementsCombo = new ArrayList<>();
                        for (Movement m : item) {
                            String movement = m.getDescription() +": "+ m.getAmount();
                            movementsCombo.add(movement);
                        }
                        box.setItems(FXCollections.observableArrayList(movementsCombo));
                        box.prefWidthProperty().bind(tableColumnCustomers.widthProperty().subtract(5));
                        box.getSelectionModel().selectFirst();
                        box.setMaxWidth(Control.USE_PREF_SIZE);
                        box.widthProperty();
                        // set cell contents
                        setGraphic(box);

                    }
                }
            };
        });
        tableColumnMovements.setCellValueFactory(new PropertyValueFactory("movements"));

        tableView.setItems(data);
        
        //AutoSize from last column
        DoubleBinding usedWidth = tableColumnId.widthProperty().add(tableColumnType.widthProperty())
                .add(tableColumnDescription.widthProperty()).add(tableColumnBalance.widthProperty())
                .add(tableColumnCreditLine.widthProperty()).add(tableColumnBeginBalance.widthProperty())
                .add(tableColumnBeginBalanceTimestamp.widthProperty()).add(tableColumnCustomers.widthProperty());
        tableColumnMovements.prefWidthProperty().bind(tableView.widthProperty().subtract(usedWidth));

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
