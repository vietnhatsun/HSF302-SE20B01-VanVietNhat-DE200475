package com.example.ui;

import javafx.application.Application;

/** Chạy class này (không chạy trực tiếp JavaFxApplication) để tránh lỗi
 *  "JavaFX runtime components are missing" khi chạy trên classpath. */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }
}

