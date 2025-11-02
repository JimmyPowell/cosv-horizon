package tech.cspioneer.backend.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class OAuthStateSigner {
    public static String encode(String json, String secret) {
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        byte[] sig = hmacSha256(payload, secret);
        String p = Base64.getUrlEncoder().withoutPadding().encodeToString(payload);
        String s = Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        return p + "." + s;
    }

    public static String decode(String state, String secret) {
        try {
            int dot = state.indexOf('.');
            if (dot <= 0) return null;
            String p = state.substring(0, dot);
            String s = state.substring(dot + 1);
            byte[] payload = Base64.getUrlDecoder().decode(p);
            byte[] sig = Base64.getUrlDecoder().decode(s);
            byte[] expect = hmacSha256(payload, secret);
            if (!constantTimeEquals(sig, expect)) return null;
            return new String(payload, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] hmacSha256(byte[] payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) result |= a[i] ^ b[i];
        return result == 0;
    }
}
