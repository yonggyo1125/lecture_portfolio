package org.choongang.product.service;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.choongang.product.entities.Category;
import org.choongang.product.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryDeleteService {

    private final Utils utils;
    private final CategoryRepository categoryRepository;

    /**
     * 분류 개별 삭제
     *
     * @param cateCd : 분류 코드 
     */
    public void delete(String cateCd) {
        Category category = categoryRepository.findById(cateCd).orElseThrow(CategoryNotFoundException::new);
        categoryRepository.delete(category);

        categoryRepository.flush();
    }

    /**
     * 분류 목록 삭제
     *
     * @param chks : 목록 선택 순번
     */
    public void deleteList(List<Integer> chks) {
        chks.forEach(chk -> delete(utils.getParam("cateCd_" + chk)));
    }
}
