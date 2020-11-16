package com.foodtech.back.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "tb_preferences_key_words")
@Data
@EqualsAndHashCode(exclude = {"id"})
@NoArgsConstructor
public class PreferencesKeyWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    private String word;

    public PreferencesKeyWord(String word) {
        this.word = word;
    }
}
