/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recuperacion_bank_client;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import recuperacion_bank_client.controllers.CustomerAccountsController;
import recuperacion_bank_client.model.Customer;

/**
 * First class loaded in the program execution
 * @author leioa
 */
public class Application extends javafx.application.Application {

    /**
     * Logger for Application class.
     */
    private static final Logger LOGGER = Logger.
            getLogger("recuperacion_bank_client.Application");
    /**
     * FileHandler for writing logs on a file.
     */
    private FileHandler fileHandlerLogger;

    /**
     * The user wich id is read from userId.txt file.
     */
    Customer user = new Customer();

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Entry point for the application. Loads, sets and shows primary window.
     *
     * @param primaryStage The primary window of the application.
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        fileHandlerLogger = new FileHandler("LOG/Logger.log", true);
        setLoggerToFile(LOGGER, fileHandlerLogger);
        readUser();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("views/CustomerAccountsView.fxml"));
        Parent root = (Parent) loader.load();
        CustomerAccountsController controller = ((CustomerAccountsController) loader.getController());
        controller.setUser(this.user);
        controller.setFileHandlerLogger(fileHandlerLogger);
        controller.setStage(primaryStage);
        controller.initStage(root);
    }

    /**
     * This method reads the user ID from a textFile located on files.
     */
    private void readUser() {
        try {
            this.user.setId(Long.parseLong(new String(Files.readAllBytes(Paths.get("USERCONFIG/userId.txt")))));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            LOGGER.severe("There was an error parsing the id from USERCONFIG/userId.txt");
            alert.setTitle("Error retrieving User ID");
            alert.setHeaderText(null);
            alert.setContentText("There was an error reading the file that contains User ID. Please check that is a real ID, and try again");
            alert.showAndWait();
            Runtime.getRuntime().exit(0);
        }
    }

    /**
     * Method used to set a fileHandler for different class loggers
     * @param logger to be handled
     * @param fh File Handler
     */
    public static void setLoggerToFile(Logger logger, FileHandler fh) {
        try {
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.addHandler(fh);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
