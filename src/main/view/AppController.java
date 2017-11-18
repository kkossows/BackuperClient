package main.view;

import main.user.User;

/**
 * Created by rkossowski on 18.11.2017.
 */
public class AppController {
    User user;





    /*
    Metoda, dzięki której jesteśmy w stanie przekazać obecnego użytkownika do aplikacji
     */
    public void setUser(User user){
        this.user = user;
    }
}
