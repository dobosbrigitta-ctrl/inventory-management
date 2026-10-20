/*package org.example;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final ChatModel chatModel;

    public TestController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/chat")
    public String generate(@RequestParam(value = "message", defaultValue = "Explain how AI works in a few words") String message) {
        return chatModel.call(message);
    }

}*/
