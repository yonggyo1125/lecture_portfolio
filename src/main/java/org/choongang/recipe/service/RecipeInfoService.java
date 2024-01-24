package org.choongang.recipe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.commons.ListData;
import org.choongang.commons.Pagination;
import org.choongang.commons.Utils;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.service.FileInfoService;
import org.choongang.recipe.controllers.RecipeDataSearch;
import org.choongang.recipe.controllers.RequestRecipe;
import org.choongang.recipe.entities.QRecipe;
import org.choongang.recipe.entities.Recipe;
import org.choongang.recipe.repositories.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeInfoService {
    private final RecipeRepository recipeRepository;
    private final FileInfoService fileInfoService;
    private final HttpServletRequest request;

    private final EntityManager em;

    /**
     * 레서피 단일 조회
     *
     * @param seq
     * @return
     */
    public Recipe get(Long seq) {
        Recipe data = recipeRepository.findById(seq).orElseThrow(RecipeNotFoundException::new);

        addRecipeInfo(data);

        return data;
    }

    public RequestRecipe getForm(Long seq) {
        Recipe data = get(seq);
        String[] requiredIng = null;
        String[] requiredIngEa = null;
        String[] subIng = null;
        String[] condiments = null;
        try {
            ObjectMapper om = new ObjectMapper();

            if (data.getRequiredIng() == null) {
                List<String[]> requiredIngTmp = om.readValue(data.getRequiredIng(), new TypeReference<>() {
                });


            }


        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        RequestRecipe form = RequestRecipe.builder()
                .seq(data.getSeq())
                .gid(data.getGid())
                .rcpName(data.getRcpName())
                .rcpInfo(data.getRcpInfo())
                .estimatedT(data.getEstimatedT())
                .category(data.getCategory())
                .subCategory(data.getSubCategory())
                .requiredIng(requiredIng)
                .subIng(condiments)
                .mainImages(data.getMainImages())
                .build();

        return form;
    }

    public ListData<Recipe> getList(RecipeDataSearch search) {
        int page = Utils.onlyPositiveNumber(search.getPage(), 1);
        int limit = Utils.onlyPositiveNumber(search.getLimit(), 20);
        int offset = (page - 1) * limit;

        QRecipe recipe = QRecipe.recipe;
        BooleanBuilder andBuilder = new BooleanBuilder();

        PathBuilder<Recipe> pathBuilder = new PathBuilder<>(Recipe.class, "recipe");
        List<Recipe> items = new JPAQueryFactory(em).selectFrom(recipe)
                .leftJoin(recipe.member)
                .fetchJoin()
                .offset(offset)
                .limit(limit)
                .orderBy(
                        new OrderSpecifier(Order.DESC, pathBuilder.get("createdAt"))
                )
                .fetch();

        int total = (int)recipeRepository.count(andBuilder);

        Pagination pagination = new Pagination(page, total, 10, limit, request);

        items.forEach(this::addRecipeInfo);

        return new ListData<>(items, pagination);
    }

    private void addRecipeInfo(Recipe data) {
        String gid = data.getGid();

        List<FileInfo> mainImages = fileInfoService.getListDone(gid);
        data.setMainImages(mainImages);

    }
}
