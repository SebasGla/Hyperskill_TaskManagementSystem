package taskmanagement.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taskmanagement.dto.TokenDto;

@RestController
@RequestMapping("/api/auth/token")
public class AuthController {

    private final JwtEncoder jwtEncoder;

    public AuthController(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping()
    public ResponseEntity<TokenDto> token(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(60, ChronoUnit.SECONDS))
                .claim("scope", authorities)
                .build();

        return ResponseEntity.ok(new TokenDto(jwtEncoder.encode(JwtEncoderParameters.from(claimsSet))
                .getTokenValue()));
    }
}
