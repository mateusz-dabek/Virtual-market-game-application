package project.security.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;
import project.model.User;
import project.repositories.UserRepository;
import project.security.JwtProvider;
import project.services.UserDetailsServiceImpl;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;

    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, UserDetailsServiceImpl userDetailsService, UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!(request.getRequestURI().equals("/auth/login") || request.getRequestURI().equals("/auth/register")
                || request.getRequestURI().contains("/reset_password") || request.getRequestURI().equals("/auth/verify_code"))) {
            String token = jwtProvider.getToken(request);
            if (token != null) {
                Long id = getIdFromToken(token);
                User user = userRepository.findById(id).get();
                String email = user.getEmail();
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                TokenBasedAuthentication authentication = new TokenBasedAuthentication(userDetails);
                authentication.setToken(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } else {
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private Long getIdFromToken(String token) {
        return jwtProvider.getIdFromToken(token);
    }
}
