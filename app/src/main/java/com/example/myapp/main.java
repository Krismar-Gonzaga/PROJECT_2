package com.example.myapp;

import android.os.Bundle;

public class main {

    public static void main(String[] args){
        MainActivity Signup = new MainActivity();
        Signup.onCreate(new Bundle());

        Login login = new Login();
        login.onCreate(new Bundle());

        product product = new product();
        product.onCreate(new Bundle());
    }
}
