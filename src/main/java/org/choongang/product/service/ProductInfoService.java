package org.choongang.product.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.admin.product.controllers.RequestProduct;
import org.choongang.commons.ListData;
import org.choongang.commons.Pagination;
import org.choongang.commons.Utils;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.service.FileInfoService;
import org.choongang.product.constants.ProductStatus;
import org.choongang.product.controllers.ProductSearch;
import org.choongang.product.entities.Category;
import org.choongang.product.entities.Product;
import org.choongang.product.entities.QProduct;
import org.choongang.product.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.data.domain.Sort.Order.desc;

@Service
@RequiredArgsConstructor
public class ProductInfoService {

    private final ProductRepository productRepository;
    private final FileInfoService fileInfoService;
    private final HttpServletRequest request;

    /**
     * 상품 개별 조회
     *
     * @param seq : 상품 번호
     * @return
     */
    public Product get(Long seq) {
        Product product = productRepository.findById(seq).orElseThrow(ProductNotFoundException::new);

        addProductInfo(product);

        return product;

    }

    /**
     * Product -> RequestProduct 변환
     *
     * @param seq
     * @return
     */
    public RequestProduct getForm(Long seq) {
        Product product = get(seq);
        RequestProduct form = new ModelMapper().map(product, RequestProduct.class);

        Category category = product.getCategory();
        if (category != null) {
            form.setCateCd(category.getCateCd());
        }

        form.setStatus(product.getStatus().name());
        form.setDiscountType(product.getDiscountType().name());

        form.setMode("edit");

        return form;
    }

    /**
     * 상품 목록 조회
     *
     * @param search : 검색 조건
     * @param isAll : true - 미노출 상품도 모두 보이기 
     * @return
     */
    public ListData<Product> getList(ProductSearch search, boolean isAll) {
        int page = Utils.onlyPositiveNumber(search.getPage(), 1);
        int limit = Utils.onlyPositiveNumber(search.getLimit(), 20);

        QProduct product = QProduct.product;
        BooleanBuilder andBuilder = new BooleanBuilder();

        /* 검색 조건 처리 S */
        List<String> cateCd = search.getCateCd();
        List<Long> seq = search.getSeq();
        List<String> statuses = search.getStatuses();
        LocalDate sdate = search.getSdate();
        LocalDate edate = search.getEdate();
        String name = search.getName();

        // 상품 분류 처리
        if (cateCd != null && !cateCd.isEmpty()) {
            andBuilder.and(product.category.cateCd.in(cateCd));
        }

        // 상품 번호 처리
        if (seq != null && !seq.isEmpty()) {
            andBuilder.and(product.seq.in(seq));
        }

        // 상품 상태 처리
        if (statuses != null && !statuses.isEmpty()) {
            List<ProductStatus> _statuses = statuses.stream().map(ProductStatus::valueOf).toList();
            andBuilder.and(product.status.in(_statuses));
        }

        // 상품 등록일자 검색 처리 S
        if (sdate != null) {
            andBuilder.and(product.createdAt.goe(LocalDateTime.of(sdate, LocalTime.of(0,0,0))));
        }

        if (edate != null) {
            andBuilder.and(product.createdAt.loe(LocalDateTime.of(edate, LocalTime.of(23, 59, 59))));
        }
        // 상품 등록일자 검색 처리 E

        // 상품명 검색
        if (StringUtils.hasText(name)) {
            andBuilder.and(product.name.contains(name.trim()));
        }

        // 키워드 검색 처리 E

        /* 검색 조건 처리 E */

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(desc("listOrder"), desc("createdAt")));

        Page<Product> data = productRepository.findAll(andBuilder, pageable);
        Pagination pagination = new Pagination(page, (int)data.getTotalElements(), 10, limit, request);

        List<Product> items = data.getContent();
        items.forEach(this::addProductInfo); // 추가 정보 처리

        return new ListData<>(items, pagination);
    }

    /**
     * 상품 추가 정보 처리
     *
     * @param product
     */
    public void addProductInfo(Product product) {
        String gid = product.getGid();

        List<FileInfo> editorImages = fileInfoService.getListDone(gid, "editor");
        List<FileInfo> mainImages = fileInfoService.getListDone(gid, "main");
        List<FileInfo> listImages = fileInfoService.getListDone(gid, "list");

        product.setEditorImages(editorImages);
        product.setMainImages(mainImages);
        product.setListImages(listImages);
    }
}
