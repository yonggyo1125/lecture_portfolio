package org.choongang.product.service;

import lombok.RequiredArgsConstructor;
import org.choongang.admin.product.controllers.RequestCategory;
import org.choongang.commons.Utils;
import org.choongang.product.entities.Category;
import org.choongang.product.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorySaveService {

    private final CategoryRepository repository;
    private final Utils utils;

    public void save(RequestCategory form) {
        Category category = new ModelMapper().map(form, Category.class);

        repository.saveAndFlush(category);
    }

    /**
     * 목록 조회
     *
     * @param chks : checkbox 선택 번호
     */
    public void saveList(List<Integer> chks) {
        for (int chk : chks) {
            String cateCd = utils.getParam("cateCd_" + chk);
            Category category = repository.findById(cateCd).orElse(null);
            if (category == null) continue;

            category.setCateNm(utils.getParam("cateNm_" + chk));
            category.setActive(Boolean.parseBoolean(utils.getParam("active_" + chk)));
            category.setListOrder(Integer.parseInt(utils.getParam("listOrder_" + chk)));
        }

        repository.flush();
    }

}
