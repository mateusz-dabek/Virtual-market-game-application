package project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${app.name}")
    private String appName;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expires_in}")
    private int expirationTime;

    @Value("${jwt.header}")
    private String authHeader;

    private static final String TOKEN_PREFIX = "Bearer";
    private final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    public String generateToken(String userId) {
        return Jwts.builder()
                .setIssuer(appName)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, secret)
                .compact();
    }

    public String refreshToken(String token) {
        Date now = new Date();

        final Claims claims = getAllClaimsFromToken(token);
        claims.setIssuedAt(now);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, secret)
                .compact();
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    private Date generateExpirationDate() {
        return new Date(new Date().getTime() + expirationTime * 1000);
    }

    public String getToken(HttpServletRequest request) {
        String header = request.getHeader(authHeader);
        if (header != null)
            return header.substring(TOKEN_PREFIX.length());
        return null;
    }

    public Long getIdFromToken(String token) {
        return Long.parseLong(getAllClaimsFromToken(token).getSubject());
    }

}
