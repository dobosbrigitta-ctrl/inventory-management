package org.example;

//import org.springframework.ai.chat.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AiInventoryController {

    private final ProductRepository productRepository;
    //private final ChatClient chatClient;

    public AiInventoryController(ProductRepository productRepository/*, ChatClient chatClient*/) {
        this.productRepository = productRepository;
        //this.chatClient = chatClient;
    }

    private String getInventoryContext() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(p -> String.format("Product: %s, Barcode: %s, Expiration: %s, Quantity: %d",
                        p.getName(), p.getBarcode(), p.getExpirationDate(), p.getQuantity()))
                .collect(Collectors.joining("\n"));
    }

    @GetMapping("/expiry-report")
    public String getExpiryReport() {
        String inventory = getInventoryContext();
        String prompt = "You are an expert inventory supervisor. Based on today's date (" + LocalDate.now() + ") and the following inventory data, check which items are expiring within the next 2 weeks. Provide a neat summary and suggest what the kitchen should cook using these soon-to-expire ingredients:\n" + inventory;
        //return chatClient.call(prompt);
        return "";
    }

    @GetMapping("/purchase-draft")
    public String getPurchaseDraft() {
        String inventory = getInventoryContext();
        String prompt = "Analyze this inventory data. Identify any items where the quantity has dropped below 10 units. Automatically generate a professional, formal business email draft in English to the supplier requesting a reorder for those low-stock products:\n" + inventory;
        //return chatClient.call(prompt);
        return "";
    }

    @GetMapping("/generate-recipe")
    public String generateRecipe() {
        String inventory = getInventoryContext();
        String prompt = "Look at the available stock in the inventory. Generate a creative and delicious recipe that maximizes the use of these ingredients, specifically prioritizing items that are closer to expiry or highly available:\n" + inventory;
        //return chatClient.call(prompt);
        return "";
    }
}
