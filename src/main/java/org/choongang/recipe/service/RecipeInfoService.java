package org.choongang.recipe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import org.springframework.util.StringUtils;

import java.util.Arrays;
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

        RequestRecipe form = RequestRecipe.builder()
                .mode("edit")
                .seq(data.getSeq())
                .gid(data.getGid())
                .rcpName(data.getRcpName())
                .rcpInfo(data.getRcpInfo())
                .estimatedT(data.getEstimatedT())
                .category(data.getCategory())
                .subCategory(data.getSubCategory())
                .mainImages(data.getMainImages())
                .amount(data.getAmount())
                .build();

        try {
            ObjectMapper om = new ObjectMapper();

            if (StringUtils.hasText(data.getRequiredIng())) {
                List<String[]> requiredIngTmp = om.readValue(data.getRequiredIng(), new TypeReference<>() {});
                // 필수 재료 내용
                String[] requiredIng = requiredIngTmp.stream().map(s -> s[0]).toArray(String[]::new);
            
                // 필수 재료 수량
                String[] requiredIngEa = requiredIngTmp.stream().map(s -> s[1]).toArray(String[]::new);

                form.setRequiredIng(requiredIng);
                form.setRequiredIngEa(requiredIngEa);
            }

            if (StringUtils.hasText(data.getSubIng())) {
                List<String[]> subIngTmp = om.readValue(data.getSubIng(), new TypeReference<>() {});
                // 부 재료 내용
                String[] subIng = subIngTmp.stream().map(s -> s[0]).toArray(String[]::new);

                // 부 재료 수량
                String[] subIngEa = subIngTmp.stream().map(s -> s[1]).toArray(String[]::new);

                form.setSubIng(subIng);
                form.setSubIngEa(subIngEa);
            }

            if (StringUtils.hasText(data.getCondiments())) {
                List<String[]> condimentsTmp = om.readValue(data.getCondiments(), new TypeReference<>() {});
                // 양념 내용
                String[] condiments = condimentsTmp.stream().map(s -> s[0]).toArray(String[]::new);

                // 양념 수량
                String[] condimentsEa = condimentsTmp.stream().map(s -> s[1]).toArray(String[]::new);

                form.setCondiments(condiments);
                form.setCondimentsEa(condimentsEa);
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        return form;
    }

    public ListData<Recipe> getList(RecipeDataSearch search) {
        int page = Utils.onlyPositiveNumber(search.getPage(), 1);
        int limit = Utils.onlyPositiveNumber(search.getLimit(), 20);
        int offset = (page - 1) * limit;

        QRecipe recipe = QRecipe.recipe;
        BooleanBuilder andBuilder = new BooleanBuilder();


        /* 검색 조건 처리 S */
        String sopt = search.getSopt(); // 옵션
        String skey = search.getSkey(); // 키워드


        sopt = StringUtils.hasText(sopt) ? sopt : "all";

        if (StringUtils.hasText(skey)) {
            skey = skey.trim();
            BooleanExpression rcpCond = recipe.rcpName.contains(skey); // 제목 - rcpName LIKE '%skey%';
            BooleanExpression nameCond = recipe.member.name.contains(skey);
            BooleanExpression userIdCond = recipe.member.userId.contains(skey);
            BooleanExpression rcpIngCond = recipe.keyword.contains("__" + skey + "__");

            if (sopt.equals("rcpName")) { // 제목
                andBuilder.and(rcpCond);
            } else if (sopt.equals("member")) { // 닉네임 + 아이디 (OR)
                BooleanBuilder orBuilder = new BooleanBuilder();
                orBuilder.or(nameCond)
                        .or(userIdCond);
                andBuilder.and(orBuilder);
            } else if (sopt.equals("rcpIng")) { // 재료 + 양념
                andBuilder.and(rcpIngCond);

            } else if (sopt.equals("all")) {
                // 닉네임+아이디도 추가해야함
                BooleanBuilder orBuilder = new BooleanBuilder();
                orBuilder.or(nameCond)
                        .or(userIdCond)
                        .or(rcpCond)
                        .or(rcpIngCond);

               andBuilder.and(orBuilder);

            }
        }
        /* 검색 조건 처리 E */

        PathBuilder<Recipe> pathBuilder = new PathBuilder<>(Recipe.class, "recipe");
        List<Recipe> items = new JPAQueryFactory(em).selectFrom(recipe)
                .leftJoin(recipe.member)
                .fetchJoin()
                .offset(offset)
                .limit(limit)
                .where(andBuilder)
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

    public List<String> getIngredients() {
        List<String> keywordTmp = recipeRepository.getIngredients();

        return keywordTmp == null ? null :
                    keywordTmp.stream().filter(StringUtils::hasText)
                            .flatMap(s -> Arrays.stream(s.split("__")))
                            .filter(StringUtils::hasText)
                            .distinct()
                            .toList();
    }
}
