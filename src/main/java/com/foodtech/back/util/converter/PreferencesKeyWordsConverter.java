package com.foodtech.back.util.converter;

import org.springframework.util.StringUtils;

import javax.persistence.AttributeConverter;
import java.util.*;
import java.util.stream.Collectors;

public class PreferencesKeyWordsConverter implements AttributeConverter<Set<String>, String> {

    private static final String SPLIT_CHAR = "#";

    @Override
    public String convertToDatabaseColumn(Set<String> stringList) {
        if (Objects.isNull(stringList) || stringList.isEmpty()) {
            return "";
        }
        stringList = stringList
                .stream()
                .map(s -> s = s.replaceAll(SPLIT_CHAR, "").replace(".", "").trim().toLowerCase())
                .collect(Collectors.toSet());
        return String.join(SPLIT_CHAR, stringList);
    }

    @Override
    public Set<String> convertToEntityAttribute(String string) {
        return StringUtils.hasText(string) ? new HashSet<>(Arrays.asList(string.split(SPLIT_CHAR))) : new HashSet<>();
    }
}