# 사업자 등록번호 검증

## 공공 데이터 OpenAPI

1. [접속 주소](https://www.data.go.kr/data/15081808/openapi.do)(https://www.data.go.kr/data/15081808/openapi.do)
2. API 서비스 신청하기 
    - 활용신청 버튼 클릭
    - 회원가입 필요
    - 서비스 인증키 발급 받기 
      - 일반 인증키(Encoding), 일반 인증키(Decoding)


## 사업자등록정보 진위확인 API

- 요청  URL : POST https://api.odcloud.kr/api/nts-businessman/v1/validate?serviceKey=서비스키


- 요청 데이터(application/json)

```json
{
   "b_no": ["xxxxxxx"] // 사업자번호 "xxxxxxx" 로 조회 시,
}
```

## 사업자등록 상태조회 API 

- 요청  URL : POST https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=서비스키


- 요청 데이터(application/json)

```json
{
   "b_no": ["xxxxxxx"] // 사업자번호 "xxxxxxx" 로 조회 시,
}
```


## API 테스트 

```java
package org.choongang.business;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootTest
public class ApiTest {
   @Test
   @DisplayName("사업자등록 상태 확인 API 테스트")
   void statusApiTest() throws URISyntaxException {

      String url = "https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=<인증 키(Encoding)>";

      RestTemplate restTemplate = new RestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.add("b_no", "2208657343");

      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
      ResponseEntity<String> response = restTemplate.postForEntity(new URI(url), request, String.class);
      String body = response.getBody();
      System.out.println(body);
   }
}

```

다음과 같이 데이터가 나온다면 API 계정에 문제 없음

```json
{"request_cnt":1,"match_cnt":1,"status_code":"OK","data":[{"b_no":"2208657343","b_stt":"계속사업자","b_stt_cd":"01","tax_type":"부가가치세 일반과세자","tax_type_cd":"01","end_dt":"","utcc_yn":"N","tax_type_change_dt":"","invoice_apply_dt":"","rbf_tax_type":"해당없음","rbf_tax_type_cd":"99"}]}
```

# 관리자 페이지 API 설정 추가

> admin/menus/Menu.java

```java
...

public class Menu {
    private final static Map<String, List<MenuDetail>> menus;

    static {
        menus = new HashMap<>();

       menus.put("config", Arrays.asList(
               new MenuDetail("basic", "기본설정", "/admin/config"),
               new MenuDetail("api", "API 설정", "/admin/config/api")
       ));
    }
   ...
}
```

> admin/config/controllers/BasicConfigController.java

```java
public class BasicConfigController implements ExceptionProcessor {

    private final ConfigSaveService saveService;
    private final ConfigInfoService infoService;

    @ModelAttribute("menuCode")
    public String getMenuCode() {
        return "config";
    }

   @ModelAttribute("subMenuCode")
   public String getSubMenuCode() {
      return "basic";
   }

   @ModelAttribute("subMenus")
   public List<MenuDetail> getSubMenus() {
      return Menu.getMenus("config");
   }
   ...
}
```

> admin/config/controllers/ApiConfig.java

```java
package org.choongang.admin.config.controllers;

import lombok.Data;

@Data
public class ApiConfig {
    private String publicOpenApiKey; // 공공 API 인증키
}
```

> admin/config/controllers/ApiConfigController.java

```java
package org.choongang.admin.config.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.admin.config.service.ConfigInfoService;
import org.choongang.admin.config.service.ConfigSaveService;
import org.choongang.commons.ExceptionProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/config/api")
@RequiredArgsConstructor
public class ApiConfigController  implements ExceptionProcessor {

    private final ConfigSaveService saveService;
    private final ConfigInfoService infoService;

    @ModelAttribute("menuCode")
    public String getMenuCode() {
        return "config";
    }

   @ModelAttribute("subMenuCode")
   public String getSubMenuCode() {
      return "api";
   }

   @ModelAttribute("subMenus")
   public List<MenuDetail> getSubMenus() {
      return Menu.getMenus("config");
   }
    
    @ModelAttribute("pageTitle")
    public String getPageTitle() {
        return "API 설정";
    }

   @GetMapping
    public String index(Model model) {

        ApiConfig config = infoService.get("apiConfig", ApiConfig.class).orElseGet(ApiConfig::new);

        model.addAttribute("apiConfig", config);

        return "admin/config/api";
    }

    @PostMapping
    public String save(ApiConfig config, Model model) {

        saveService.save("apiConfig", config);

        model.addAttribute("message", "저장되었습니다.");

        return "admin/config/api";
    }

}
```

> resources/template/admin/config/api.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layouts/main}">

<section layout:fragment="content">
    <div th:replace="~{admin/commons/_message::message}"></div>
    <h1>API 설정</h1>

    <form name="frmSave" method="post" th:action="@{/admin/config/api}" autocomplete="off" th:object="${apiConfig}">
        <h2>공공 API 설정</h2>
        <table class="table_cols">
            <tr>
                <th width="180">인증키(Encoding></th>
                <td>
                    <input type="text" name="publicOpenApiKey" th:field="*{publicOpenApiKey}">
                </td>
            </tr>
        </table>
        <div class="submit_btns">
            <button type="reset" class="btn">다시입력</button>
            <button type="submit" class="btn">저장하기</button>
        </div>
    </form>
</section>
</html>
```

관리자 완성 화면

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/business/images/business/image2.png)


## 사업자 상태 판별 함수 

> commons/api/BusinessPermit.java

```java
package org.choongang.commons.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties
public class BusinessPermit {
    private Integer request_cnt;
    private Integer match_cnt;
    private String status_code;
    private List<BusinessPermitData> data;
}

```

> commons/api/BusinessPermitData.java

```java
package org.choongang.commons.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class BusinessPermitData {
   private String b_no;
   private String b_stt;
   private String b_stt_cd;
   private String tax_type;
   private String tax_type_cd;
   private String end_dt;
   private String utcc_yn;
   private String tax_type_change_dt;
   private String invoice_apply_dt;
   private String rbf_tax_type;
   private String rbf_tax_type_cd;
}
```

> commons/Utils.java

```java
public class Utils {
   private final HttpServletRequest request;
   private final HttpSession session;
   private final FileInfoService fileInfoService;
   private final ConfigInfoService infoService;
   
   ...

   /**
    * 사업자 등록증 상태 체크
    *
    * @param permitNo : 사업자 등록증 번호
    * @return
    */
   public boolean checkBusinessPermit(String permitNo) {
      if (!StringUtils.hasText(permitNo)) {
         return false;
      }

      // 사업자 등록증 번호 숫자만 제외하고 제거(숫자로 형식 통일)
      permitNo = permitNo.replaceAll("\\D", "");

      // API 설정 조회
      ApiConfig config = infoService.get("apiConfig", ApiConfig.class).orElse(null);

      // 설정이 없거나 공공 API 인증 키가 없는 경우 false
      if (config == null || !StringUtils.hasText(config.getPublicOpenApiKey())) {
         return false;
      }

      String url = String.format("https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=%s", config.getPublicOpenApiKey());

      RestTemplate restTemplate = new RestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.add("b_no", permitNo);

      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

      try {
         BusinessPermit response = restTemplate.postForObject(new URI(url), request, BusinessPermit.class);

         List<BusinessPermitData> items = response.getData();
         if (items != null && !items.isEmpty()) {
            BusinessPermitData data = items.get(0);

            String bStt = data.getB_stt();
            return StringUtils.hasText(bStt) && bStt.equals("계속사업자");
         }
         System.out.println(response);
      } catch (URISyntaxException e) {
         e.printStackTrace();
      }

      return false;

   }
}
```

## 구현 함수 동작 테스트

> test/java/.../business/ApiTest.java

```java
@TestPropertySource(properties = "spring.profiles.active=dev")
public class ApiTest {

    @Autowired
    private Utils utils;
    
   ...

   @Test
   @DisplayName("사업자 등록증 상태 체크 함수 테스트")
   void checkBusinessPermit() {
      boolean result = utils.checkBusinessPermit("2208657343");

      assertTrue(result);

   }
}
```


> api/controllers/PublicApiController.java

```java
package org.choongang.api.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.choongang.commons.rests.JSONData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicApiController {
    private final Utils utils;

    @GetMapping("/business_permit/{permitNo}")
    public JSONData<Object> checkBusinessPermit(@PathVariable("permitNo") String permitNo) {
        boolean result = utils.checkBusinessPermit(permitNo);

        JSONData<Object> data = new JSONData<>();
        data.setSuccess(result); // true이면 유효 사업자

        return data;
    }
}
```

## 사용방법
- GET /api/public/business_permit/<사업자 번호>

- 정상적인 사업자라면 다음과 같은 출력화면 확인 가능

![image3](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/business/images/business/image3.png)