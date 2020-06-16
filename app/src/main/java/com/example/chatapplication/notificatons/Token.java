package com.example.chatapplication.notificatons;


public class Token {
    /*An FCM token or much commonly known as a registrationToken
    and ID issus by the GCM
            * An ID issued By The connection server to the clint app that allows it to received message*/
    String token;

    public Token() {}

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
