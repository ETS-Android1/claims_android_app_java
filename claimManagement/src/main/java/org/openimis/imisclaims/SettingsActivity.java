package org.openimis.imisclaims;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SettingsActivity extends ImisActivity {

    Button btnSaveRarPwd, btnDefaultRarPassword;
    EditText etRarPassword;
    private String salt, password;
    public static String generatedSalt;
    Global global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        actionBar.setTitle("Settings");

        btnSaveRarPwd = (Button)findViewById(R.id.btnSaveRarPwd);
        etRarPassword = (EditText)findViewById(R.id.rarPassword);
        btnDefaultRarPassword = (Button) findViewById(R.id.btnDefaultRarPassword);

        btnSaveRarPwd.setOnClickListener(view -> {
            if(etRarPassword.getText().length() == 0){
               ShowDialog("Rar password required");
            }
            else {
                password = etRarPassword.getText().toString();
                saveRarPassword(password);
                ShowDialog("Password has been changed");
                etRarPassword.setText("");
            }

        });

        btnDefaultRarPassword.setOnClickListener(view -> {
            password = global.getDefaultRarPassword();
            saveRarPassword(password);
            ShowDialog("Password has been changed to the default rar password");
        });

    }

    public static String getGeneratedSalt() {
        return generatedSalt;
    }

    private SecretKeySpec generateKey(String encPassword) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = encPassword.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    public String encryptRarPwd(String dataToEncrypt, String encPassword) throws Exception{
        SecretKeySpec key = generateKey(encPassword);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(dataToEncrypt.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);
        return encryptedValue;
    }

    public String decryptRarPwd(String dataToDecrypt, String decPassword) throws Exception {
        SecretKeySpec key = generateKey(decPassword);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(dataToDecrypt, Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);

        return decryptedValue;
    }

    public String generateSalt(){
        final Random r = new SecureRandom();
        byte[] salt = new byte[32];
        r.nextBytes(salt);
        String encodedSalt = Base64.encodeToString(salt, Base64.DEFAULT);

        return encodedSalt;
    }

    public void saveRarPassword(String password){
        try {
            SharedPreferences sharedPreferences = global.getDefaultSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            salt = generateSalt();
            String trimSalt = salt.trim();
            String encryptedPassword = encryptRarPwd(password, trimSalt);
            String trimEncryptedPassword = encryptedPassword.trim();
            editor.putString("rarPwd", trimEncryptedPassword);
            editor.putString("salt", trimSalt);
            editor.apply();
        }
        catch (Exception e) {
            e.getMessage();
        }
    }

    protected AlertDialog ShowDialog(String msg){
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Ok), (dialog, which) -> {
                    //et.requestFocus();
                    return;
                }).show();
    }
}
