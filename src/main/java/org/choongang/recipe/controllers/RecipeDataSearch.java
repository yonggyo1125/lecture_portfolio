package org.choongang.recipe.controllers;

import lombok.Data;

@Data
public class RecipeDataSearch {
    private int page = 1;
    private int limit = 20;

    private String sopt;
    private String skey;

    private String category;
    private String subCategory;
}
