package com.foodtech.back.dto.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrganizationInfoDto {

    private String organizationName;

    private String organizationEmail;

    private String organizationSite;

    public OrganizationInfoDto(String organizationName, String organizationEmail, String organizationSite) {
        this.organizationName = organizationName;
        this.organizationEmail = organizationEmail;
        this.organizationSite = organizationSite;
    }
}
