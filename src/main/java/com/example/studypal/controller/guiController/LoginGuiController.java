package com.example.studypal.controller.guiController;

import com.example.studypal.bean.CredenzialiBean;
import com.example.studypal.bean.LoggedInUserBean;
import com.example.studypal.controller.applicationController.LoginController;
import com.example.studypal.exceptions.CredenzialiSbagliateException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;

public class LoginGuiController {


    private String loginPage = "com/example/studypal/view/login.fxml";

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Text signUpButton;

    @FXML
    private Label credenzialiError;

    @FXML
    private Label credenzialiSbagliate;

    protected LoggedInUserBean loggedInUserBean;

    private static final Logger logger = Logger.getLogger(LoginGuiController.class.getName());



    @FXML
     void loginMethod() {

        String userEmail = null;
        String userPassword = null;


        //controlla isempty()
        if (!this.emailField.getText().isEmpty() && !this.passwordField.getText().isEmpty()) {
            userEmail = this.emailField.getText();
            userPassword = this.passwordField.getText();
        } else {
            credenzialiError.setText("Campi obbligatori.");
            return;
        }

        try {

            CredenzialiBean credenzialiBean = new CredenzialiBean();
            credenzialiBean.setEmail(userEmail);
            credenzialiBean.setPassword(userPassword);

            //istanziamo il controller applicativo che si deve occupare del login e gli passiamo il bean contenente le credenziali
            LoginController loginController = new LoginController();

            //prendiamo i dati dell'utente loggato (sessione)
            this.loggedInUserBean = loginController.loginMethod(credenzialiBean);

            //in base al ruolo dell'utente loggato carichiamo la pagina corretta della home
            System.out.println("siamo:" + loggedInUserBean.getRuolo());

            caricaHome(loggedInUserBean.getRuolo());

        } catch (CredenzialiSbagliateException e) {
            credenzialiError.setVisible(false);
            credenzialiSbagliate.setText("Credenziali sbagliate.");
        }

    }

    //cambio pagina
    public void caricaRegistrazione () {
        try {
            FXMLLoader loader = new FXMLLoader(LoginGuiController.class.getResource("/com/example/studypal/view/registrazione.fxml"));
            loader.setControllerFactory(c -> new RegistrazioneGuiController(this.loggedInUserBean));
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            Stage stage = (Stage) credenzialiError.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            logger.severe("errore in LoginGuiController " + e.getMessage());

        }
    }


    //metodo che carica la home corretta in base al ruolo
    public void caricaHome(boolean isTutor) {

        try {
            FXMLLoader loader;

            if (isTutor) {
                System.out.println("siamo:" + isTutor);
                loader = new FXMLLoader(LoginGuiController.class.getResource("/com/example/studypal/view/tutor/homeTutor.fxml"));
                loader.setControllerFactory(c -> new HomeTutorGui(loggedInUserBean));
            } else {
                System.out.println("siamo:" + isTutor);

                loader = new FXMLLoader(LoginGuiController.class.getResource("/com/example/studypal/view/studente/homeStudente.fxml"));
                loader.setControllerFactory(c -> new HomeStudenteGui());
            }
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            Stage stage = (Stage) credenzialiError.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            logger.severe("errore in LoginGuiController " + e.getMessage());
        }
    }


}
