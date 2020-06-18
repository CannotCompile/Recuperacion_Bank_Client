/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recuperacion_bank_client.controllers;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.LongStringConverter;
import javax.ws.rs.core.GenericType;
import recuperacion_bank_client.Application;
import recuperacion_bank_client.clients.AccountClient;
import recuperacion_bank_client.model.Account;
import recuperacion_bank_client.model.AccountType;
import recuperacion_bank_client.model.Customer;

/**
 * Controller class for the view CustomerAccountView
 * @author Adrian Garcia
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
     * FileHandler to write logs in a file
     */
    private FileHandler fileHandlerLogger;
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
        Application.setLoggerToFile(LOGGER, fileHandlerLogger);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Accounts");
        stage.setMinWidth(520);
        stage.setMinHeight(320);
        buttonNewAccount.setOnAction(this::handleButtonNewAccountAction);

        stage.show();

        populateData();

    }

    /**
     * Method that populates the data of the view
     */
    private void populateData() {
        List<Account> accounts = new ArrayList<Account>(getCustomerAccounts("" + user.getId()));
        try {
            if (accounts.get(0) != null) {
                Set<Customer> customers = accounts.get(0).getCustomers();
                for (Customer c : customers) {
                    if (c.getId().equals(user.getId())) {
                        user = c;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error retrieving accounts");
            alert.setHeaderText(null);
            alert.setContentText("Cannot get the user information. Please check that ID exists and has accounts and try again later.");
            alert.showAndWait();
            LOGGER.severe("There was an error retrieving user information: Please, check the user ID from USERCONFIG/userId.txt and Add a new account");
            if(user.getFirstName() == null){
                user.setFirstName("");
                user.setMiddleInitial("");
                user.setLastName("");
                
            }
        }
        labelUserName.setText(user.getFirstName() + " " + user.getMiddleInitial() + " " + user.getLastName());
        labelUserInfo.setText("ID: " + user.getId());
        ObservableList<Account> observableAccounts = FXCollections.observableArrayList(accounts);
        try {
            populateTable(observableAccounts);
        }
        catch(Exception e) {
            LOGGER.severe("There was an error populating the table: "+e.getMessage());
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error showing info");
            alert.setHeaderText(null);
            alert.setContentText("There was an error showing the data, please, try again.");
            alert.showAndWait();
        }
        File file = new File("resources/img/userDefault.png");
        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);
    }

    /**
     * Method that gets all the accounts of a user
     *
     * @param id the id of a customer, used as param to filter accounts in REST
     */
    public static Set<Account> getCustomerAccounts(String id) {
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
            LOGGER.severe("There was an error retrieving the accounts: "+e.getMessage());
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
        tableColumnId.setStyle("-fx-alignment: TOP-RIGHT;");

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
        tableColumnType.setStyle("-fx-alignment: TOP-CENTER;");

        //Column Account description
        tableColumnDescription.setCellFactory(column -> {
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
        tableColumnBalance.setCellFactory(column -> getFormattedCurrencyTableCell());
        tableColumnBalance.setCellValueFactory(new PropertyValueFactory("balance"));
        tableColumnBalance.setStyle("-fx-alignment: TOP-RIGHT;");
        
        //Column Account credit line
        tableColumnCreditLine.setCellFactory(column -> getFormattedCurrencyTableCell());
        tableColumnCreditLine.setCellValueFactory(new PropertyValueFactory("creditLine"));
        tableColumnCreditLine.setStyle("-fx-alignment: TOP-RIGHT;");

        //Column Account begin balance
        tableColumnBeginBalance.setCellFactory(column -> getFormattedCurrencyTableCell());
        tableColumnBeginBalance.setCellValueFactory(new PropertyValueFactory("beginBalance"));
        tableColumnBeginBalance.setStyle("-fx-alignment: TOP-RIGHT;");

        //Column Account begin balance timestamp
        tableColumnBeginBalanceTimestamp.setCellFactory(column -> {
            TableCell<Account, Date> cell = new TableCell<Account, Date>() {
                private DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(df.format(item));
                    }
                }
            };

            return cell;
        });
        tableColumnBeginBalanceTimestamp.setCellValueFactory(new PropertyValueFactory("beginBalanceTimestamp"));
        tableColumnBeginBalanceTimestamp.setStyle("-fx-alignment: TOP-CENTER;");
        
        tableColumnBeginBalanceTimestamp.setSortType(TableColumn.SortType.DESCENDING);
        tableView.setItems(data);
        tableView.getSortOrder().add(tableColumnBeginBalanceTimestamp);
        

        //Auto resize table columns on screen resize
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (stage.isMaximized()) {
                tableColumnId.prefWidthProperty().bind(tableView.widthProperty());
                tableColumnBeginBalance.prefWidthProperty().bind(tableView.widthProperty());
                tableColumnBalance.prefWidthProperty().bind(tableView.widthProperty());
                tableColumnCreditLine.prefWidthProperty().bind(tableView.widthProperty());
                tableColumnDescription.prefWidthProperty().bind(tableView.widthProperty());
                tableColumnType.prefWidthProperty().bind(tableView.widthProperty());
            } else {
                tableColumnId.prefWidthProperty().bind(tableColumnId.minWidthProperty());
                tableColumnBeginBalance.prefWidthProperty().bind(tableColumnBalance.minWidthProperty());
                tableColumnBalance.prefWidthProperty().bind(tableColumnBalance.minWidthProperty());
                tableColumnCreditLine.prefWidthProperty().bind(tableColumnCreditLine.minWidthProperty());
                tableColumnDescription.prefWidthProperty().bind(tableColumnDescription.minWidthProperty());
                tableColumnType.prefWidthProperty().bind(tableColumnType.minWidthProperty());

            }

            DoubleBinding usedWidth = tableColumnId.widthProperty().add(tableColumnType.widthProperty())
                    .add(tableColumnDescription.widthProperty()).add(tableColumnBalance.widthProperty())
                    .add(tableColumnCreditLine.widthProperty()).add(tableColumnBeginBalance.widthProperty());
            tableColumnBeginBalanceTimestamp.prefWidthProperty().bind(tableView.widthProperty().subtract(usedWidth));
        });

    }

    /**
     * Method that sets the logged user
     *
     * @param user that is logged
     */
    public void setUser(Customer user) {
        this.user = user;
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
     * Handles the action event of buttonNewSoftware
     *
     * @param event The action event
     */
    private void handleButtonNewAccountAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recuperacion_bank_client/views/AddAccountView.fxml"));
            Parent root = (Parent) loader.load();
            AddAccountController controller = ((AddAccountController) loader.getController());
            controller.setStage(new Stage());
            controller.setParentEvent(event);
            controller.setParentStage(stage);
            controller.setFileHandlerLogger(fileHandlerLogger);
            controller.setUser(user);
            controller.initStage(root);
        } catch (Exception ex) {
            LOGGER.severe("Error opening AddAccount: "+ex.getMessage());
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error opening new view");
            alert.setHeaderText(null);
            alert.setContentText("There was an error opening the view. Please try again later");
            alert.showAndWait();
        }
    }
    
    /**
     * This method will return modified TableCell with formated view for monetary values
     * based on the user Locale
     * @return the TableCell that will be set as cell factory 
     */
    private TableCell<Account, Double> getFormattedCurrencyTableCell () {
        TableCell<Account, Double> cell = new TableCell<Account, Double>() {
                private DecimalFormat formatter = (DecimalFormat) NumberFormat
                        .getCurrencyInstance(Locale.getDefault());

                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };

            return cell;
    }

}
