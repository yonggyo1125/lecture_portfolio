package org.choongang.recipe.controllers;

import lombok.Data;

@Data
public class RecipeDataSearch {
    private int page = 1;
    private int limit = 20;
}
