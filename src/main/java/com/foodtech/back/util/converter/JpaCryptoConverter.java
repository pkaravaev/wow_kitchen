package com.foodtech.back.util.converter;

import com.foodtech.back.config.ResourcesProperties;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.ZeroSaltGenerator;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;

import static java.util.Objects.nonNull;

@Component
@Slf4j
public class JpaCryptoConverter implements AttributeConverter<String, String> {

    private StandardPBEStringEncryptor encryptor;

    public JpaCryptoConverter(ResourcesProperties properties) {
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setProvider(new BouncyCastleProvider());
        encryptor.setSaltGenerator(new ZeroSaltGenerator());
        encryptor.setAlgorithm(properties.getAlgorithm());
        encryptor.setPassword(properties.getSymmetricKey());
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        String encrypt = encryptor.encrypt(attribute);
        return nonNull(attribute) ? encrypt : null;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return nonNull(dbData) ? encryptor.decrypt(dbData) : null;
    }
}
