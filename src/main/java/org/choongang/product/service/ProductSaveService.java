package org.choongang.product.service;

import lombok.RequiredArgsConstructor;
import org.choongang.admin.product.controllers.RequestProduct;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertException;
import org.choongang.file.service.FileUploadService;
import org.choongang.product.constants.DiscountType;
import org.choongang.product.constants.ProductStatus;
import org.choongang.product.entities.Category;
import org.choongang.product.entities.Product;
import org.choongang.product.repositories.CategoryRepository;
import org.choongang.product.repositories.ProductOptionRepository;
import org.choongang.product.repositories.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSaveService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final FileUploadService fileUploadService;
    private final CategoryRepository categoryRepository;
    private final Utils utils;

    public void save(RequestProduct form) {

        String mode = form.getMode();
        Long seq = form.getSeq();

        Product product = null;

        if (mode.equals("edit") && seq != null) {
            product = productRepository.findById(seq).orElseThrow(ProductNotFoundException::new);

        } else {
            product = new Product();
            product.setGid(form.getGid());
        }

        /* 분류 설정 S */
        String cateCd = form.getCateCd();
        if (StringUtils.hasText(cateCd)) {
            Category category = categoryRepository.findById(cateCd).orElse(null);
            product.setCategory(category);
        }
        /* 분류 설정 E */

        product.setName(form.getName());
        product.setConsumerPrice(form.getConsumerPrice());
        product.setSalePrice(form.getSalePrice());

        product.setStatus(ProductStatus.valueOf(form.getStatus()));
        product.setDiscountType(DiscountType.valueOf(form.getDiscountType()));

        product.setUseStock(form.isUseStock());
        product.setStock(form.getStock());
        product.setExtraInfo(form.getExtraInfo());
        product.setPackageDelivery(form.isPackageDelivery());
        product.setDeliveryPrice(form.getDeliveryPrice());
        product.setDescription(form.getDescription());
        product.setActive(form.isActive());
        product.setUseOption(form.isUseOption());
        product.setOptionName(form.getOptionName());

        productRepository.saveAndFlush(product);

        fileUploadService.processDone(product.getGid());
    }

    /**
     * 상품 목록 수정
     *
     * @param chks
     */
    public void saveList(List<Integer> chks) {
        if (chks == null || chks.isEmpty()) {
            throw new AlertException("수정할 상품을 선택하세요.", HttpStatus.BAD_REQUEST);
        }

        for (int chk : chks) {
            Long seq = Long.valueOf(utils.getParam("seq_" + chk));

            Product product = productRepository.findById(seq).orElse(null);
            if (product == null) continue;

            boolean active = Boolean.valueOf(utils.getParam("active_" + chk));
            int listOrder = Integer.parseInt(utils.getParam("listOrder_" + chk));

            product.setActive(active);
            product.setListOrder(listOrder);
        }

        productRepository.flush();
    }
}
