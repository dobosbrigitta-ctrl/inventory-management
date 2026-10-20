/*package org.example;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiTestController {

    private final ChatClient chatClient;


    public AiTestController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai/teszt")
    public String generál(@RequestParam(value = "question", defaultValue = "Hello! If you see this message, Spring AI works successfully on GitHub Models!") String question) {
        return this.chatClient.prompt(question)
                .call()
                .content();
    }
}*/
