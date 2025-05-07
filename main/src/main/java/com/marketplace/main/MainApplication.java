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
                "|      http://localhost:8080/api/swagger-ui/index.html      |\n" +
                "|===========================================================|\n" +
                "|                      Mongo Express                        |\n" +
                "|-----------------------------------------------------------|\n" +
                "|                  http://localhost:8081                    |\n" +
                "|===========================================================|\n"



        );
    }

}
