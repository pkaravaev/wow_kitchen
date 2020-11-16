package com.foodtech.back.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foodtech.back.entity.AbstractIdEntity;
import com.foodtech.back.util.converter.PreferencesKeyWordsConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "tb_user_preferences")
@Data
@EqualsAndHashCode(callSuper = false)
public class UserPreferences extends AbstractIdEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnore
    private User user;

    private boolean vegetarian;

    private boolean notSpicy;

    private boolean withoutNuts;

    @Convert(converter = PreferencesKeyWordsConverter.class)
    @NotNull
    @SuppressWarnings("JpaAttributeTypeInspection")
    private Set<String> dontLike;
}
