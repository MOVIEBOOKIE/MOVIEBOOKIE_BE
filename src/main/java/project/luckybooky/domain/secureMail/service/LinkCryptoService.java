package project.luckybooky.domain.participation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkCryptoService {
    private final ObjectMapper om = new ObjectMapper();

    @Value("${app.mail-link.secret}")
    private String aesKeyBase64;

    private byte[] key;
    private SecureRandom rnd;

    @PostConstruct
    void init() {
        this.key = Base64.getDecoder().decode(aesKeyBase64);
        this.rnd = new SecureRandom();
    }

    public String encryptPayload(Map<String, Object> payload) {
        try {
            byte[] plain = om.writeValueAsBytes(payload);

            byte[] iv = new byte[12];
            rnd.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec ks = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, ks, new GCMParameterSpec(128, iv));

            byte[] ct = cipher.doFinal(plain);

            ByteBuffer bb = ByteBuffer.allocate(iv.length + ct.length);
            bb.put(iv).put(ct);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array());
        } catch (Exception e) {
            throw new IllegalStateException("encrypt-failed", e);
        }
    }

    public Map<String, Object> decryptPayload(String token) {
        try {
            byte[] raw = Base64.getUrlDecoder().decode(token);
            byte[] iv = new byte[12];
            System.arraycopy(raw, 0, iv, 0, 12);
            byte[] ct = new byte[raw.length - 12];
            System.arraycopy(raw, 12, ct, 0, ct.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec ks = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, ks, new GCMParameterSpec(128, iv));
            byte[] plain = cipher.doFinal(ct);

            return om.readValue(plain, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("decrypt-failed", e);
        }
    }

    public static long nowEpoch() {
        return Instant.now().getEpochSecond();
    }
}