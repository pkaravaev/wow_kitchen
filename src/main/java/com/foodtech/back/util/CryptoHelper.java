package com.foodtech.back.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class CryptoHelper {

    private static final String ALGORITHM = "AES";

    private static final Integer KEY_SIZE = 256;

    private static String generateSymmetricKey() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
        generator.init(KEY_SIZE);

        SecretKey key = generator.generateKey();

        return Base64.encodeBase64String(key.getEncoded());
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        log.info(generateSymmetricKey());
    }
}
