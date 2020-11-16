package com.foodtech.back.dto.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class UserUpdateDto {

    @Length(min = 1, max = 20)
    @NotBlank
    private String name;

    public UserUpdateDto(@Length(min = 1, max = 20) @NotBlank String name) {
        this.name = name;
    }
}
