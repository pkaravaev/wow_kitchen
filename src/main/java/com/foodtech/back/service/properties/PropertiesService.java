package com.foodtech.back.service.properties;

import com.foodtech.back.entity.util.AppProperty;
import com.foodtech.back.repository.util.PropertiesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PropertiesService {

    public static final String TOKEN_EXPIRED_MIN = "TOKEN_EXPIRED_MIN";
    public static final String ORGANIZATION_NAME = "organization_name";
    public static final String ORGANIZATION_EMAIL = "organization_email";
    public static final String ORGANIZATION_SITE = "organization_site";

    private final PropertiesRepository repository;

    public PropertiesService(PropertiesRepository repository) {
        this.repository = repository;
    }

    public List<AppProperty> getAll() {
        return repository.findAllByOrderById();
    }

    public Map<String, String> getByNames(List<String> properties) {
        return repository.findByNameIn(properties)
                .stream()
                .collect(Collectors.toMap(AppProperty::getName, AppProperty::getValue));
    }

    @Transactional
    public boolean saveProperty(Integer id, String value) {
        AppProperty appProperty = repository.findById(id).orElseThrow();
        if (!valueIsValid(appProperty.getName(), value)) {
            return false;
        }
        appProperty.setValue(value);
        return true;
    }

    private boolean valueIsValid(String propertyName, String value) {
        if (TOKEN_EXPIRED_MIN.equals(propertyName)) {
            return tokenValueIsValid(value);
        }

        if (ORGANIZATION_NAME.equals(propertyName)
                ||ORGANIZATION_EMAIL.equals(propertyName)
                ||ORGANIZATION_SITE.equals(propertyName)) {
            return StringUtils.hasText(value);
        }

        return false;
    }

    private boolean tokenValueIsValid(String value) {
        if (!value.chars().allMatch(Character::isDigit)) {
            return false;
        }

        long millis = Long.parseLong(value);
        return millis > 0 && millis < 10080;
    }

}
