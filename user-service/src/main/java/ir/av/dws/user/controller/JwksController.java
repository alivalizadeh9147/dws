package ir.av.dws.user.controller;

import ir.av.dws.user.security.JwtKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class JwksController {

    private final JwtKeyProvider jwtKeyProvider;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {

        RSAPublicKey publicKey =
                (RSAPublicKey) jwtKeyProvider.getPublicKey();

        Map<String, Object> key = new HashMap<>();

        key.put("kty", "RSA");
        key.put("use", "sig");
        key.put("alg", "RS256");
        key.put("kid", "7f3c9a21-5d84-4e6b-a17f-92c6d8b4e501");

        key.put("n", base64Url(publicKey.getModulus()));
        key.put("e", base64Url(publicKey.getPublicExponent()));

        Map<String, Object> jwks = new HashMap<>();

        jwks.put("keys", List.of(key));

        return jwks;
    }

    private String base64Url(BigInteger value) {

        byte[] bytes = value.toByteArray();

        if (bytes.length > 1 && bytes[0] == 0) {
            bytes = Arrays.copyOfRange(
                    bytes,
                    1,
                    bytes.length
            );
        }

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}