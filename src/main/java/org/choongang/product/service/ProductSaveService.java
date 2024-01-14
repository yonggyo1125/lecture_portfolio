package org.choongang.product.service;

import lombok.RequiredArgsConstructor;
import org.choongang.admin.product.controllers.RequestProduct;
import org.choongang.file.service.FileUploadService;
import org.choongang.product.constants.DiscountType;
import org.choongang.product.constants.ProductStatus;
import org.choongang.product.entities.Category;
import org.choongang.product.entities.Product;
import org.choongang.product.repositories.CategoryRepository;
import org.choongang.product.repositories.ProductOptionRepository;
import org.choongang.product.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductSaveService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final FileUploadService fileUploadService;
    private final CategoryRepository categoryRepository;
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
}
