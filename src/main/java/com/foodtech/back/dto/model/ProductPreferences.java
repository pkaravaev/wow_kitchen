package com.foodtech.back.dto.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductPreferences {

    private boolean showVegetarian;
    private boolean showSpicy;
    private boolean showWithNuts;
    private boolean showDontLike;

    @Builder
    private ProductPreferences(boolean showVegetarian, boolean showSpicy, boolean showWithNuts,
                               boolean showDontLike) {
        this.showVegetarian = showVegetarian;
        this.showSpicy = showSpicy;
        this.showWithNuts = showWithNuts;
        this.showDontLike = showDontLike;
    }
}
