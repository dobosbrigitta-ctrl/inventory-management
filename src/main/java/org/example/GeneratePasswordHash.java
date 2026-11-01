package org.example;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Scanner;


public class GeneratePasswordHash {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter admin password: ");
        String password = scanner.nextLine();

        String hash = new BCryptPasswordEncoder().encode(password);

        System.out.println("BCrypt hash: " + hash);
    }
}