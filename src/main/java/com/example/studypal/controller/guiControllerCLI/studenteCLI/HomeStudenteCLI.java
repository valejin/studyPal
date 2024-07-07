package com.example.studypal.controller.guiControllerCLI.studenteCLI;

import com.example.studypal.bean.LoggedInUserBean;
import com.example.studypal.controller.guiControllerCLI.LoginCLI;
import com.example.studypal.other.Printer;
import com.example.studypal.pattern.state.AbstractState;
import com.example.studypal.pattern.state.StateMachineImpl;


public class HomeStudenteCLI extends AbstractState {

    /* Stato HOME*/
    LoggedInUserBean user;

    public HomeStudenteCLI(LoggedInUserBean user){ this.user = user;}

    @Override
    public void action(StateMachineImpl SM){

        /* l'azione della Home sta nel presentare le opzioni disponibili, quindi appare molto semplice*/

        int option = 0;

        switch(option){
            case(1):
                //transizione a prenotaRipetizione
                break;
            case(2):
                //transizione a gestisciPrenotazioni
                break;
            case(3):
                //logout, non va fatto così ma giusto per metterci qualcosa!!!!!
                LoginCLI loginCLI = new LoginCLI();
                this.goNext(SM, loginCLI);
                break;
            default:
                Printer.println("Input invalido.");
                break;
        }
    }

    @Override
    public void mostraMenu(){
        Printer.println("   1. Prenota Ripetizione");
        Printer.println("   2. Gestisci Prenotazioni");
        Printer.println("   0. Esci");
        Printer.println("   Opzione scelta: ");
    }

    @Override
    public void stampaBenvenuto(){
        Printer.println("-------------- HOME STUDENTE --------------");
        Printer.println("Ciao " + this.user.getNome() + ", scegli un'opzione:");
    }

    @Override
    public void entry(StateMachineImpl cli){
        stampaBenvenuto();
        mostraMenu();
    }

}
