package com.example.hotelio.controllers;

import com.example.hotelio.entities.User;

/**
 * Classe pour gérer la session utilisateur
 */
public class SessionManager {

    private static User currentUser = null;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearCurrentUser() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}