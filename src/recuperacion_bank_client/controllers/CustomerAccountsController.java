/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recuperacion_bank_client.controllers;

import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import recuperacion_bank_client.clients.CustomerClient;
import recuperacion_bank_client.model.Customer;

/**
 *
 * @author leioa
 */
public class CustomerAccountsController {

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
     * If true the syntax is correct, if false there's an error with the syntax
     */
    private boolean checkedSyntax;
    
    private static final CustomerClient CLIENT = new CustomerClient();
    
    
    
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
        stage.show();
        
    }
    
}
