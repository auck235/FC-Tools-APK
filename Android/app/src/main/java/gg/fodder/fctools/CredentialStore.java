package gg.fodder.fctools;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

final class CredentialStore {
    private static final String KEY_ALIAS = "fc-tools-login-key";
    private static final String PREFS = "fc_tools_secure";
    private static final String VALUE = "credentials";
    private static final String FALLBACK_VALUE = "credentials_private_fallback";
    private final Context context;

    CredentialStore(Context context) { this.context = context.getApplicationContext(); }

    void save(String email, String password) throws Exception {
        try {
            saveWithKey(email, password);
            preferences().edit().remove(FALLBACK_VALUE).apply();
        } catch (Exception firstFailure) {
            try {
                // A stale or corrupted Android Keystore entry can survive an app update.
                // Remove only the encryption key and retry; the saved value is overwritten.
                deleteKeyIfPresent();
                saveWithKey(email, password);
                preferences().edit().remove(FALLBACK_VALUE).apply();
            } catch (Exception secondFailure) {
                // Keep the credentials in the app's private storage if this device rejects
                // Android Keystore encryption. Other regular apps cannot read this storage.
                String payload = Base64.encodeToString((email + "\n" + password).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                preferences().edit().remove(VALUE).putString(FALLBACK_VALUE, payload).apply();
            }
        }
    }

    private void saveWithKey(String email, String password) throws Exception {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal((email + "\n" + password).getBytes(StandardCharsets.UTF_8));
        byte[] payload = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, payload, 0, iv.length);
        System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
        preferences().edit().putString(VALUE, Base64.encodeToString(payload, Base64.NO_WRAP)).apply();
    }

    String[] read() {
        String encoded = preferences().getString(VALUE, null);
        if (encoded != null) {
            try {
            byte[] payload = Base64.decode(encoded, Base64.NO_WRAP);
            byte[] iv = new byte[12];
            byte[] encrypted = new byte[payload.length - iv.length];
            System.arraycopy(payload, 0, iv, 0, iv.length);
            System.arraycopy(payload, iv.length, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            String[] values = new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8).split("\n", 2);
            return values.length == 2 ? values : null;
            } catch (Exception ignored) { /* Try the private-storage fallback below. */ }
        }
        try {
            String fallback = preferences().getString(FALLBACK_VALUE, null);
            if (fallback == null) return null;
            String[] values = new String(Base64.decode(fallback, Base64.NO_WRAP), StandardCharsets.UTF_8).split("\n", 2);
            return values.length == 2 ? values : null;
        } catch (Exception ignored) { return null; }
    }

    void clear() { preferences().edit().remove(VALUE).remove(FALLBACK_VALUE).apply(); }

    private SharedPreferences preferences() { return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    private SecretKey key() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (store.containsAlias(KEY_ALIAS)) {
            java.security.Key existing = store.getKey(KEY_ALIAS, null);
            if (existing instanceof SecretKey) return (SecretKey) existing;
            store.deleteEntry(KEY_ALIAS);
        }
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build());
        return generator.generateKey();
    }

    private void deleteKeyIfPresent() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (store.containsAlias(KEY_ALIAS)) store.deleteEntry(KEY_ALIAS);
    }
}
