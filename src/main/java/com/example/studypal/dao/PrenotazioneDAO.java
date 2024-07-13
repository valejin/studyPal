package com.example.studypal.dao;

import com.example.studypal.bean.LoggedInUserBean;
import com.example.studypal.exceptions.NonProduceRisultatoException;
import com.example.studypal.model.PrenotazioneModel;
import com.example.studypal.other.Connect;
import com.example.studypal.other.Printer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PrenotazioneDAO {

    /*
        DAO che si occupa della gestione delle prenotazioni di ripetizioni. Contiene i seguenti metodi:
            - prenota: inserisce le richieste di prenotazioni nella tabella "richieste"

           Tutor:
            - Gestione Prenotazione:
                - Prenotazioni attive: prendere le prenotazioni attive dal DB, ovvero quelle già confermate dal tutor
                - Richieste arrivate: prendere le richieste di prenotazione inviate dagli studenti dal DB (FATTO)
     */

    private static final Logger logger = Logger.getLogger(PrenotazioneDAO.class.getName());

    /*-------------------------------------------PRENOTAZIONE----------------------------------------------------------*/
    public void prenota(PrenotazioneModel prenotazioneModel) throws SQLException {
        /*
        fa la query per inserire la richiesta di ripetizione nel database
         */
        Connection connection;
        PreparedStatement statement;


        String query = "INSERT INTO richieste (emailTutor, emailStudente, materia, modLezione, tariffa, giorni, note, nomeTutor, cognomeTutor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ";
        try{
            connection = Connect.getInstance().getDBConnection();
            statement = connection.prepareStatement(query);

            statement.setString(1, prenotazioneModel.getEmailTutor());
            statement.setString(2, prenotazioneModel.getEmailStudente());
            statement.setString(3, prenotazioneModel.getMateria());
            statement.setInt(4, prenotazioneModel.getModLezione());
            statement.setInt(5, prenotazioneModel.getTariffa());
            statement.setString(6, prenotazioneModel.getGiorno());
            statement.setString(7, prenotazioneModel.getNote());
            statement.setString(8, prenotazioneModel.getNome());
            statement.setString(9, prenotazioneModel.getCognome());

            //eseguo
            statement.execute();


        } catch (SQLException e) {
            logger.severe("errore in prenotazioneDAO " + e.getMessage());
            throw e;
            //todo: ??? è necessario rimandarla al controller applicativo?? In teoria questa si verifica solo in caso di errore vero e proprio (dato che è un inserimento)
        }
    }


    /* todo: richieste arrivate e richieste inviate fanno la stessa cosa! Se facessimo setString impostandola a emailTutor/emailStudente facendo un controllo su user.getRuolo?*/
    List<PrenotazioneModel> risultatiRicerca = new ArrayList<>();

    private String sql;

    /*--------------Gestione Prenotazioni (TUTOR): prendere le richieste arrivate da DB ---------------------------*/
    /*--------------Gestione Prenotazioni (TUTOR): prendere le prenotazioni arrivate da DB ------------------------*/
    /*--------------Gestione Prenotazioni (TUTOR): prendere le prenotazioni rifiutate da DB ------------------------*/
        /*
    una volta la richiesta in attesa viene confermata dal tutor, sparisce dalla lista di richieste arrivate
    */
    public List<PrenotazioneModel> richiesteArrivate(String email, int flag) throws NonProduceRisultatoException{
        //viene passato il userModel per prendere email del tutor

        Connection connection;
        PreparedStatement statement = null;
        ResultSet rs = null;

        //query per la ricerca di email del tutor nella lista di tutte le richieste
        if(flag == 0) {
            //qui ho le richieste in attesa
            sql = "SELECT * FROM richieste WHERE emailTutor = ? AND stato = 0";
        }else if(flag == 1){
            //qui ho le richieste confermate =>prenotazioni attive
            sql = "SELECT * FROM richieste WHERE emailTutor = ? AND stato = 1";
        }else if(flag == 2){
            sql = "SELECT * FROM richieste WHERE emailTutor = ? AND stato = 2";
        }


        try{
            connection = Connect.getInstance().getDBConnection();
            statement = connection.prepareStatement(sql);


            //in base al valore del flag: flag == 0 richieste arrivate; flag == 1 prenotazioni attive

            statement.setString(1, email);


            rs = statement.executeQuery();

            if(rs.next()){
                Printer.println("Lista di email richiedenti per il tutor: " + email);

                //prendo email dello studente, materia richiesta, e aggiungo il pulsante VISUALIZZA per ciascun tupla estratta
                do{
                    LoggedInUserBean userTutor = new LoggedInUserBean(rs.getString("emailTutor"), rs.getString("nomeTutor"), rs.getString("cognomeTutor"));

                    List<Integer> valori = new ArrayList<>();
                    valori.add(rs.getInt("modLezione"));
                    valori.add(rs.getInt("tariffa"));
                    valori.add(rs.getInt("stato"));


                    //popolo una nuova istanza di PrenotazioneModel per ritornare al CtlApplicativo
                    PrenotazioneModel risultatoCorrente = new PrenotazioneModel(rs.getInt("idrichieste"), userTutor, rs.getString("emailStudente"), rs.getString("materia"), rs.getString("giorni"), rs.getString("note"), valori);

                    //aggiunggo la tupla in lista dei risultati di ricerca
                    risultatiRicerca.add(risultatoCorrente);

                }while(rs.next());

            }else{
                throw new NonProduceRisultatoException();
            }


        }catch(SQLException e){
            Printer.println("Non ci sono le richieste arrivate.");
            logger.severe("errore in PrenotazioneDAO " + e.getMessage());
        }finally {
            // Chiusura delle risorse
            closeResources(statement,rs);
        }

        return risultatiRicerca;
    }






    /* -------------------------------------------------STUDENTE------------------------------------------------------*/
    public List<PrenotazioneModel> richiesteInviate(String email, Integer flag) throws NonProduceRisultatoException {

        /* metodo che riceve la stringa contenente l'email con cui fare la query
           restituisce una lista contenente tutte le richieste di prenotazione inviate ma ancora in attesa di conferma*/

        List<PrenotazioneModel> listaRichieste = new ArrayList<>();

        Connection connection;
        PreparedStatement statement = null;
        ResultSet rs = null;

        if (flag == 0){
            //richieste inviate in attesa di conferma
            sql = "SELECT * FROM richieste WHERE emailStudente = ? AND stato = 0";

        } else if (flag == 1){
            //richieste confermate (prenotazioni attive)
            sql = "SELECT * FROM richieste WHERE emailStudente = ? AND stato = 1";

        } else if (flag == 2){
            sql = "SELECT * FROM richieste WHERE emailStudente = ? AND stato = 2";

        }

        try{
            connection = Connect.getInstance().getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, email);

            rs = statement.executeQuery();

            if (rs.next()){

                do {

                    LoggedInUserBean userTutor = new LoggedInUserBean(rs.getString("emailTutor"), rs.getString("nomeTutor"), rs.getString("cognomeTutor"));

                    List<Integer> valori = new ArrayList<>();
                    valori.add(rs.getInt("modLezione"));
                    valori.add(rs.getInt("tariffa"));
                    valori.add(rs.getInt("stato"));

                    PrenotazioneModel richiesta = new PrenotazioneModel(rs.getInt("idrichieste"), userTutor,
                            rs.getString("emailStudente"), rs.getString("materia"),
                            rs.getString("giorni"), rs.getString("note"), valori);

                    listaRichieste.add(richiesta);

                } while (rs.next());

            } else {
                throw new NonProduceRisultatoException();
            }

        } catch (SQLException e) {
            Printer.println("Errore in PrenotazioneDAO (metodo: richiesteInviate)");
        } finally {
            closeResources(statement,rs);
        }
        return listaRichieste;
    }



    /*------------------------------------CONFERMA/RIFIUTA (TUTOR) ---------------------------------------------------*/
    public void modificaStatoRichiesta(Integer idRichiesta, Integer stato){
        /*
            stato:
                    0 -> conferma
                    1 -> rifiuta
         */

        Connection connection;
        PreparedStatement statement;
        String query = "UPDATE richieste SET stato = ? WHERE idrichieste = ?";

        try {
            connection = Connect.getInstance().getDBConnection();
            statement = connection.prepareStatement(query);

            statement.setInt(1, stato);
            statement.setInt(2, idRichiesta);

            statement.execute();
            Printer.println("id richiesta: " + idRichiesta);
            Printer.println("Stato della richiesta modificato con successo. Stato: " + stato);

        } catch (SQLException e){
            Printer.println("Errore in PrenotazioneDAO (metodo: modificaStatoRichieste)");

        }

    }

    /*------------------------------------------CANCELLA (STUDENTE)-------------------------------------------*/
    public void cancellaRichiesta(Integer idRichiesta){

        Connection connection;
        PreparedStatement statement;
        String query = "DELETE FROM richieste WHERE idrichieste = ?";
        try {
            connection = Connect.getInstance().getDBConnection();
            statement = connection.prepareStatement(query);
            statement.setInt(1, idRichiesta);
            statement.execute();
            Printer.println("id richiesta da eliminare: " + idRichiesta);

        } catch(SQLException e){
            Printer.println("Errore in PrenotazioneDAO (metodo: cancellaRichiesta)");
        }
        Printer.println("Richiesta eliminata con successo.");
    }


    /*-------------------------------------------RECENSIONE-----------------------------------------------------------*/

    public void recensioneMethod(int idRichiesta, int recensione){

        Connection connection;
        PreparedStatement statement;
        String query ="UPDATE richieste SET recensione = ? WHERE idrichieste = ?";

        try {
            connection = Connect.getInstance().getDBConnection();
            statement = connection.prepareStatement(query);
            statement.setInt(1, recensione);
            statement.setInt(2, idRichiesta);
            statement.execute();

        } catch(SQLException e){
            Printer.println("Errore in PrenotazioneDAO (metodo: recensioneMethod)");
        }
        Printer.println("Recensione salvata con successo.");
    }



    /** Metodo utilizzato per chiudere le risorse utilizzate */
    private void closeResources(Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            handleDAOException(e);
        }
    }


    private void handleDAOException(Exception e) {
        Printer.errorPrint(String.format("PrenotazioneDAO: %s", e.getMessage()));
    }


}