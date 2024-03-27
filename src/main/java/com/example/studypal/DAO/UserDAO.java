package com.example.studypal.DAO;


import com.example.studypal.model.CredenzialiModel;
import com.example.studypal.model.UserModel;
import com.example.studypal.exceptions.LoginDBException;

import java.sql.*;
import java.util.logging.Logger;


public class UserDAO {

    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());


    public UserModel loginMethod(CredenzialiModel credenzialiModel) throws LoginDBException{
        UserModel userModel = new UserModel();

        String path = "jdbc:mysql://localhost:3306/studypal";
        String username = "root";
        String password = "Valentina";
        PreparedStatement statement = null;

        //query per verificare credenziali utente
        String query = "SELECT * FROM utente where email=? AND password=?";

        try{

            Connection connection = DriverManager.getConnection(path, username, password);
            statement = connection.prepareStatement(query);
            statement.setString(1, credenzialiModel.getEmail());
            statement.setString(2, credenzialiModel.getPassword());

            try (ResultSet rs = statement.executeQuery()) {

                if(!rs.next()) {
                   System.out.println("Il ResultSet è vuoto.");
                    throw new LoginDBException(0);
                }
                else {
                    userModel.setNome(rs.getString("nome"));
                    userModel.setEmail(rs.getString("email"));
                    System.out.println(userModel.getEmail());
                    userModel.setCognome(rs.getString("cognome"));
                    System.out.println("Accesso effettuato per l'utente: " + userModel.getNome());
                }
            }

        }catch (SQLException e) {
            logger.severe("errore in userDAO " + e.getMessage());
        }

        return userModel;
    }
}
