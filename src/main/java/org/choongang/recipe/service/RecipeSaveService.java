package org.choongang.recipe.service;

import lombok.RequiredArgsConstructor;
import org.choongang.file.service.FileUploadService;
import org.choongang.member.MemberUtil;
import org.choongang.recipe.controllers.RequestRecipe;
import org.choongang.recipe.entities.Recipe;
import org.choongang.recipe.repositories.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RecipeSaveService {
    private final RecipeRepository recipeRepository;
    private final FileUploadService fileUploadService;
    private final MemberUtil memberUtil;

    public void save(RequestRecipe form) {
        Long seq = form.getSeq();
        String mode = form.getMode();
        mode = StringUtils.hasText(mode) ? mode : "add";

        Recipe recipe = null;
        if (mode.equals("add") && seq != null) {
            recipe = recipeRepository.findById(seq).orElseThrow(RecipeNotFoundException::new);
        } else {
            recipe = new Recipe();
            recipe.setGid(form.getGid());
            recipe.setMember(memberUtil.getMember());
        }

        recipe.setRcpName(form.getRcpName());
        recipe.setRcpInfo(form.getRcpInfo());
        recipe.setEstimatedT(form.getEstimatedT());
        recipe.setCategory(form.getCategory());
        recipe.setSubCategory(form.getSubCategory());
        recipe.setRequiredIng(form.getRequiredIngJSON());
        recipe.setSubIng(form.getSubIngJSON());
        recipe.setCondiments(form.getCondimentsJSON());

        recipeRepository.saveAndFlush(recipe);

        fileUploadService.processDone(form.getGid());
    }
}
