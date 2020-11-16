package com.foodtech.back.dto.auth;

import com.foodtech.back.dto.model.FullMobileNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthDto {

    @Valid
    @NotNull
    private FullMobileNumber fullNumber;
}
