package com.abhinand.bookmymatch.security;

import com.abhinand.bookmymatch.entity.User;
import com.abhinand.bookmymatch.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PasswordChangeRequiredHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    
    private final UserRepository userRepository;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null && user.isMustChangePassword()) {
            // Redirect to password change page
            getRedirectStrategy().sendRedirect(request, response, "/change-password");
        } else {
            // Continue with normal login flow -> redirect to matches
            setDefaultTargetUrl("/matches");
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
