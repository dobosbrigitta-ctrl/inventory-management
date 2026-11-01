package org.example.controller;

import org.example.entity.FoodBatch;
import org.example.entity.UserEntity;
import org.example.repository.FoodBatchRepository;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.math.BigDecimal;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final FoodBatchRepository foodBatchRepository;
    private final PasswordEncoder passwordEncoder;

    public DashboardController(UserRepository userRepository,
                               ProductRepository productRepository,
                               FoodBatchRepository foodBatchRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.foodBatchRepository = foodBatchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String showDashboard(Model model) {
        List<FoodBatch> currentBatches = foodBatchRepository.findAll();
        model.addAttribute("batches", currentBatches);
        model.addAttribute("products", currentBatches);
        return "dashboard";
    }

    @PostMapping("/dashboard/batches/{id}/update")
    @Transactional
    public String updateBatchFromWeb(
            @PathVariable Long id,
            @RequestParam("newQuantity") BigDecimal newQuantity,
            @RequestParam("productName") String productName,
            @RequestParam("barcode") String barcode) {

        if (newQuantity != null && newQuantity.compareTo(BigDecimal.ZERO) >= 0) {
            foodBatchRepository.findById(id).ifPresent(batch -> {
                batch.setQuantity(newQuantity);

                if (batch.getProduct() != null) {
                    batch.getProduct().setName(productName);
                    batch.getProduct().setBarcode(barcode);
                    productRepository.save(batch.getProduct());
                }

                foodBatchRepository.save(batch);
            });
        }
        return "redirect:/";
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage() {
        return "change-password";
    }

    @PostMapping("/change-password")
    @Transactional
    public String changePassword(@RequestParam("newPassword") String newPassword) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged in user not found in DB"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(user);

        return "redirect:/change-password?success";
    }
}
