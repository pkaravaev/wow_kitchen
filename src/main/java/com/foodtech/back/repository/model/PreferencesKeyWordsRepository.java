package com.foodtech.back.repository.model;

import com.foodtech.back.entity.model.PreferencesKeyWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface PreferencesKeyWordsRepository extends JpaRepository<PreferencesKeyWord, Integer> {

    @Query("SELECT w.word from PreferencesKeyWord w")
    Set<String> findAllWordsSet();

    boolean existsByWordEquals(String word);
}
