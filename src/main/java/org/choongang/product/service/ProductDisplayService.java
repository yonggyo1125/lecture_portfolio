package org.choongang.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.admin.config.service.ConfigInfoService;
import org.choongang.admin.config.service.ConfigSaveService;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertException;
import org.choongang.product.entities.Product;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductDisplayService {
    private final ConfigSaveService configSaveService;
    private final ConfigInfoService configInfoService;
    private final ProductInfoService productInfoService;

    private final Utils utils;
    private final HttpServletRequest request;

    public void save(List<Long> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new AlertException("상품 진열을 추가하세요.", HttpStatus.BAD_REQUEST);
        }

       configSaveService.save("displayCodes", codes);

        for (long code : codes) {
            String displayName = utils.getParam("displayName_" + code);
            String[] seqes = utils.getParams("seq_" + code);
            String value = seqes != null && seqes.length > 0 ?
                        Arrays.stream(seqes).collect(Collectors.joining(",")) : "";
            configSaveService.save("display_" + code,
                        new String[] { displayName, value });
        }
    }

    public DisplayData getDisplayData(Long code) {
        
        List<String> data = configInfoService.get("display_" + code, new TypeReference<List<String>>() {}).orElse(null);

        if (data == null) {
            return null;
        }

        List<Product> items = null;



        return DisplayData.builder()
                .code(code)
                .displayName(data.get(0))
                .items(items)
                .build();
    }
}
