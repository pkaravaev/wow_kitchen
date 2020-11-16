package com.foodtech.back.dto.auth;

import com.foodtech.back.dto.model.FullMobileNumber;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class RefreshCredentialsDto extends CredentialsDto {

    @Valid
    @NotNull
    private FullMobileNumber fullNumber;

}
