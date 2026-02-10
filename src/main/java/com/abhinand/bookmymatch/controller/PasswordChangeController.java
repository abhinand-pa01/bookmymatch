package com.abhinand.bookmymatch.controller;

import com.abhinand.bookmymatch.entity.User;
import com.abhinand.bookmymatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
@RequestMapping("/change-password")
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping
    public String changePasswordPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        model.addAttribute("mustChange", user.isMustChangePassword());
        model.addAttribute("username", user.getUsername());
        
        return "auth/change-password";
    }
    
    @PostMapping
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                return "redirect:/change-password";
            }
            
            // Validate new passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "New passwords do not match");
                return "redirect:/change-password";
            }
            
            // Validate new password is different
            if (currentPassword.equals(newPassword)) {
                redirectAttributes.addFlashAttribute("error", "New password must be different from current password");
                return "redirect:/change-password";
            }
            
            // Validate password strength
            if (newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters long");
                return "redirect:/change-password";
            }
            
            // Check password complexity
            if (!isPasswordStrong(newPassword)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character");
                return "redirect:/change-password";
            }
            
            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setMustChangePassword(false);
            userRepository.save(user);
            
            log.info("Password changed successfully for user: {}", user.getUsername());
            redirectAttributes.addFlashAttribute("message", "Password changed successfully! Please login with your new password.");
            
            return "redirect:/logout";
            
        } catch (Exception e) {
            log.error("Error changing password", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while changing password");
            return "redirect:/change-password";
        }
    }
    
    private boolean isPasswordStrong(String password) {
        // At least 8 characters, 1 uppercase, 1 lowercase, 1 number, 1 special char
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        return hasUpper && hasLower && hasDigit && hasSpecial && password.length() >= 8;
    }
}
