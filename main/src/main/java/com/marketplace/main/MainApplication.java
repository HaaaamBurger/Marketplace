package com.marketplace.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);

        System.out.println("\n" +
                "|===========================================================|\n" +
                "|                        Swagger UI                         |\n" +
                "|-----------------------------------------------------------|\n" +
                "|         http://localhost:8080/swagger-ui/index.html       |\n" +
                "|===========================================================|\n" +
                "|                        Home Page                          |\n" +
                "|-----------------------------------------------------------|\n" +
                "|                 http://localhost:8080/home                |\n" +
                "|===========================================================|\n" +
                "|                     Admin credentials                     |\n" +
                "|-----------------------------------------------------------|\n" +
                "|      email: admin@gmail.com   password: adminPassword1    |\n" +
                "|===========================================================|\n"
        );
    }

}
