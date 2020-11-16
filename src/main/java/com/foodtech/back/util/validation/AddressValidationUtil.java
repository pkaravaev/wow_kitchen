package com.foodtech.back.util.validation;

import com.foodtech.back.entity.model.Address;

import java.util.Set;

public class AddressValidationUtil {

    // На яндекс картах в Казахстане периодически в поле со значением дома попадают обозначения улиц
    private static final Set<String> RESTRICTED_HOME_WORDS = Set.of("улица", "ул.", "проспект", "пр-т", "шоссе", "ш.", "переулок", "пер.");

    public static boolean addressIsValid(Address address) {
        return homeIsValid(address.getHome().trim());
    }

    // Проверяем, что значение дома содержит цифры и не содержит лишних слов
    private static boolean homeIsValid(String home) {
        home = home.trim();
        if ("без дома".equalsIgnoreCase(home)) {
            return true;
        }
        return home.matches(".*\\d.*") && !containsRestrictedWords(home);
    }

    private static boolean containsRestrictedWords(String home) {
        for (String word : RESTRICTED_HOME_WORDS) {
            if (home.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
