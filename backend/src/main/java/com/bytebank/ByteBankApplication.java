package com.bytebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Ponto de entrada da API do ByteBank Digital.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ByteBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(ByteBankApplication.class, args);
    }
}
