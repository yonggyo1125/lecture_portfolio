# 주소 API 연동 
- DAUM 우편번호 서비스 이용
- https://postcode.map.daum.net/guide

## 회원 가입시 주소 찾기 기능  

> member/controllers/MemberController.java  

```java
...

public class MemberController implements ExceptionProcessor {
    ...

    
    private void commonProcess(String mode, Model model) {
        ...

        List<String> addCss = new ArrayList<>();
        List<String> addScript = new ArrayList<>();
        List<String> addCommonScript = new ArrayList<>();

        if (mode.equals("login")) { // 로그인
            pageTitle = Utils.getMessage("로그인", "commons");

        } else if (mode.equals("join")) { // 회원가입
            addCss.add("member/join");
            addScript.add("member/join");
            addCommonScript.add("address");

        } else if (mode.equals("find_pw")) { // 비밀번호 찾기
            pageTitle = Utils.getMessage("비밀번호_찾기", "commons");
        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("addCss", addCss);
        model.addAttribute("addScript", addScript);
        model.addAttribute("addCommonScript", addCommonScript);
    }
}
```

> member/controllers/RequestJoin.java

```java
...

public class RequestJoin {
    ...
    
    private String zonecode;
    private String address;
    private String addressSub;
    
    ...
}
```

> member/entities/Member.java

```java
...

@Entity
public class Member extends Base {
    ...
    
    @Column(length=10)
    private String zonecode;

    @Column(length=100)
    private String address;

    @Column(length=100)
    private String addressSub;
    
    ...
}
```

> member/service/JoinService.java

```java
...

public class JoinService {
    
    ...
    
    public void process(RequestJoin form, Errors errors) {
        ... 
        
        ...
    }
    
    Member member = new Member();
    member.setEmail(form.getEmail());
    member.setName(form.getName());
    member.setPassword(hash);
    member.setUserId(form.getUserId());

    member.setZonecode(form.getZonecode());
    member.setAddress(form.getAddress());
    member.setAddressSub(form.getAddressSub());

    process(member);
    ...    
}
```

> resources/messages/commons.properties

```properties
...

주소=주소
우편번호=우편번호
나머지_주소=나머지 주소
주소_찾기=주소 찾기
...

```

> resources/templates/front/member/join.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{front/layouts/main}">
<main layout:fragment="content">
    <h1 th:text="#{회원가입}"></h1>

    <form name="frmJoin" method="post" th:action="@{/member/join}" autocomplete="off" th:object="${requestJoin}">

        ...

        <dl>
            <dt th:text="#{주소}"></dt>
            <dd>
                <div>
                    <input type="text" name="zonecode" id="zonecode" th:field="*{zonecode}" readonly th:placeholder="#{우편번호}">
                    <button type="button" th:text="#{주소_찾기}" class="search_address" data-zonecode-id="zonecode" data-address-id="address"></button>
                </div>
                <div>
                    <input type="text" name="address" id="address" th:field="*{address}" readonly th:placeholder="#{주소}">
                </div>
                <div>
                    <input type="text" name="addressSub" th:field="*{addressSub}" th:placeholder="#{나머지_주소}">
                </div>
            </dd>
        </dl>
        
        ...
    </form>
</main>
</html>
```

> 1. search_address 클래스가 지정되어 있으면 클릭시 주소 팝업이 뜹니다.
> 2. data-zonecode-id : 여기에 지정된 id를 가진 input 요소에 검색된 우편번호가 완성됩니다.
> 3. data-address-id : 여기에 지정된 id를 가진 input 요소에 검색된 주소가 완성됩니다.

> resources/static/common/js/address.js

```javascript
var commonLib = commonLib || {};

commonLib.address = {
    /**
    * 다음 주소 API 동적 로딩
    *
    */
    init() {
        const el = document.getElementById("address_api_script");
        if (el) {
            el.parentElement.removeChild(el);
        }

        const script = document.createElement("script");
        script.src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
        script.id="address_api_script";

        document.head.insertBefore(script, document.getElementsByTagName("script")[0]);
    },
    /**
    * 주소 검색
    *
    */
    search(e) {
        const dataset = e.currentTarget.dataset;
        const zonecodeEl = document.getElementById(dataset.zonecodeId);
        const addressEl = document.getElementById(dataset.addressId);

        new daum.Postcode({
            oncomplete: function(data) {
                zonecodeEl.value = data.zonecode;
                addressEl.value = data.roadAddress
            }
        }).open();
    }

}

window.addEventListener("DOMContentLoaded", function() {
    const { address } = commonLib;

    // 초기 주소 API 로드
    address.init();

    const searchAddresses = document.getElementsByClassName("search_address");
    for (const el of searchAddresses) {
        el.addEventListener("click", address.search);
    }
});
```

적용 화면

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/address-api/images/address-api/image1.png)
