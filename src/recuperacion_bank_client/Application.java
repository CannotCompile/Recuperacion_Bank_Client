/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recuperacion_bank_client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import recuperacion_bank_client.controllers.CustomerAccountsController;

/**
 *
 * @author leioa
 */
public class Application extends javafx.application.Application {
    
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("views/CustomerAccountsView.fxml"));
        Parent root = (Parent) loader.load();
        CustomerAccountsController controller = ((CustomerAccountsController) loader.getController());
        controller.setStage(primaryStage);
        controller.initStage(root);
    }
    
}