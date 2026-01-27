package org.example.chatmind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class ChatMindApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatMindApplication.class, args);
    }

}
