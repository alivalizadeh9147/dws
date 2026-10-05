package ir.av.dws.user.security;

import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Getter
@Component
public class JwtKeyProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtKeyProvider() throws Exception {
        ClassPathResource privateKeyResource = new ClassPathResource("keys/private_key.pem");
        String privateKeyContent;
        try (InputStream inputStream = privateKeyResource.getInputStream()) {
            privateKeyContent = new String(inputStream.readAllBytes())
                    .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
        }
        PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPrivate);

        // خواندن public key
        ClassPathResource publicKeyResource = new ClassPathResource("keys/public_key.pem");
        String publicKeyContent;
        try (InputStream inputStream = publicKeyResource.getInputStream()) {
            publicKeyContent = new String(inputStream.readAllBytes())
                    .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
        }
        X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpecPublic);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}