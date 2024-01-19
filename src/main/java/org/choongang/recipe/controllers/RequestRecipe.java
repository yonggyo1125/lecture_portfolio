package org.choongang.recipe.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class RequestRecipe {

    private ObjectMapper om;

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

    private String[] requiredIng; // 필수재료
    private String[] requiredIngEa; // 필수재료 수량

    private String[] subIng; // 부재료
    private String[] subIngEa; // 부재료 수량
    
    private String[] condiments; // 양념
    private String[] condimentsEa; // 양념 수량

    public RequestRecipe() {
        om = new ObjectMapper();
    }

    public String getRequiredIngJSON() { // 필수재료 JSON 데이터 변환
        List<String[]> data = new ArrayList<>();
        if (requiredIng != null && requiredIng.length > 0) {
            for (int i = 0; i < requiredIng.length; i++) {
                String content = requiredIng[i];
                if (!StringUtils.hasText(content)) continue;

                String ea = requiredIngEa[i];
                ea = StringUtils.hasText(ea) ? ea : "";

                data.add(new String[] { content.trim(),  ea.trim()});
            }
        }
        String json =  null;
        try {
            json = om.writeValueAsString(data);
        } catch (JsonProcessingException e) {}

        return json;
    }

    public String getSubIngJSON() { // 부재료 JSON 데이터 변환
        List<String[]> data = new ArrayList<>();
        if (subIng != null && subIng.length > 0) {
            for (int i = 0; i < subIng.length; i++) {
                String content = subIng[i];
                if (!StringUtils.hasText(content)) continue;

                String ea = subIngEa[i];
                ea = StringUtils.hasText(ea) ? ea : "";

                data.add(new String[] { content.trim(),  ea.trim()});
            }
        }
        String json =  null;
        try {
            json = om.writeValueAsString(data);
        } catch (JsonProcessingException e) {}

        return json;
    }

    public String getCondimentsJSON() { // 양념 JSON 데이터 변환
        List<String[]> data = new ArrayList<>();
        if (condiments != null && condiments.length > 0) {
            for (int i = 0; i < condiments.length; i++) {
                String content = condiments[i];
                if (!StringUtils.hasText(content)) continue;

                String ea = condimentsEa[i];
                ea = StringUtils.hasText(ea) ? ea : "";

                data.add(new String[] { content.trim(),  ea.trim()});
            }
        }
        String json =  null;
        try {
            json = om.writeValueAsString(data);
        } catch (JsonProcessingException e) {}

        return json;
    }
}
