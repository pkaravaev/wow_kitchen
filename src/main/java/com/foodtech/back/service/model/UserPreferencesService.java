package com.foodtech.back.service.model;

import com.foodtech.back.dto.model.ProductPreferences;
import com.foodtech.back.entity.model.PreferencesKeyWord;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.model.UserPreferences;
import com.foodtech.back.entity.model.iiko.Product;
import com.foodtech.back.repository.model.PreferencesKeyWordsRepository;
import com.foodtech.back.repository.model.UserPreferencesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.foodtech.back.util.ValidationUtil.formPreferencesDontLikeSet;

@Service
public class UserPreferencesService {

    private final UserPreferencesRepository repository;

    private final PreferencesKeyWordsRepository keyWordsRepository;

    public UserPreferencesService(UserPreferencesRepository repository, PreferencesKeyWordsRepository keyWordsRepository) {
        this.repository = repository;
        this.keyWordsRepository = keyWordsRepository;
    }

    public Optional<UserPreferences> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public void create(User user) {
        UserPreferences preferences = new UserPreferences();
        preferences.setDontLike(Collections.emptySet());
        preferences.setUser(user);
        repository.save(preferences);
    }

    @Transactional
    public UserPreferences updatePreferences(UserPreferences preferences, Long userId) {
        UserPreferences existingPref = repository.findByUserId(userId).orElseThrow();
        existingPref.setVegetarian(preferences.isVegetarian());
        existingPref.setNotSpicy(preferences.isNotSpicy());
        existingPref.setWithoutNuts(preferences.isWithoutNuts());
        existingPref.setDontLike(formPreferencesDontLikeSet(preferences.getDontLike()));
        Set<String> allSet = keyWordsRepository.findAllWordsSet();
        if (!allSet.containsAll(existingPref.getDontLike())) {
            throw new IllegalArgumentException("Some of preferences key words not found");
        }
        return existingPref;
    }

    public List<String> getAllKeyWordsForApp() {
        List<PreferencesKeyWord> allWords = keyWordsRepository.findAll();
        return allWords.stream().map(PreferencesKeyWord::getWord).collect(Collectors.toList());
    }

    public List<PreferencesKeyWord> getAllKeyWordsForAdmin() {
        return keyWordsRepository.findAll();
    }

    public void addKeyWord(PreferencesKeyWord newWord) {
        if (keyWordsRepository.existsByWordEquals(newWord.getWord())) {
            return;
        }
        keyWordsRepository.save(newWord);
    }

    public void deleteKeyWord(Integer id) {
        keyWordsRepository.deleteById(id);
    }

    void applyPreferences(List<Product> products, Long userId) {
        UserPreferences userPreferences = repository.findByUserId(userId).orElseThrow();
        products.forEach(p -> applyPreferences(p, userPreferences));
    }

    private void applyPreferences(Product product, UserPreferences preferences) {
        boolean vegetarian = preferences.isVegetarian();
        boolean notSpicy = preferences.isNotSpicy();
        boolean withoutNuts = preferences.isWithoutNuts();
        Set<String> dontLike = preferences.getDontLike();

        ProductPreferences productPreferences = ProductPreferences.builder()
                .showVegetarian(vegetarian && product.isVegetarian())
                .showSpicy(notSpicy && product.isSpicy())
                .showWithNuts(withoutNuts && product.isWithNuts())
                .showDontLike(!Collections.disjoint(dontLike, product.getProductPreferenceKeyWords()))
                .build();

        product.setUserPreferences(productPreferences);
    }
}
