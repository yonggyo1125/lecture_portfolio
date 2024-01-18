package org.choongang.recipe.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class RequestRecipe {
    private String mode = "add";
    private Long seq;

    private String gid = UUID.randomUUID().toString();

    @NotBlank
    private String rcpName;
    private String rcpInfo;

    private int estimatedT;

    private String category;
    private String subCategory;
    private int amount;

    private String requiredIng;
    private String subIng;
    private String condiments;
}
