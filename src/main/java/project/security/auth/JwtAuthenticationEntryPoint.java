package project.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import project.exceptions.ErrorDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationEntryPoint() {
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        System.out.println("Wysylam kod SC_UNAUTHORIZED");
        ErrorDTO errorDTO = new ErrorDTO(HttpServletResponse.SC_UNAUTHORIZED, authException.getLocalizedMessage());
        String errorDTOJson = objectMapper.writeValueAsString(errorDTO);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(errorDTOJson);

    }
}
