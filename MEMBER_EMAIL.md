# 이메일 전송 

## 설정하기

## 구글 이메일 기준 : 앱 비밀번호 발급

- <b>Google 계정 관리 -> 보안 -> 2단계 인증 -> 앱 비밀번호</b> 에서 발급 받는다. 
- 설정시에 필요하므로 발급 받은 비밀번호를 따로 보관합니다.

### 의존성 추가 - build.gradle 

```groovy

dependencies {
    ...
    implementation 'org.springframework.boot:spring-boot-starter-mail'
    ...
}
```

### application.yml 수정

```yaml
spring:
  mail:
    host: smtp.gmail.com # SMTP 서버 호스트
    port: 587 # SMTP 서버 포트
    username: ${mail.username} # 이메일 아이디, 주소가 test@gmail.com이면 test
    password: ${mail.password} # 앱 비밀번호
    properties:
      mail:
        smtp:
          auth: true # 사용자 인증 시도 여부 - 기본값 false
          timeout: 5000 # Socket Read Timeout 시간(ms) - 기본값 : 무한대
          starttls:
            enable: true # StartTLS 활성화 여부 - 기본값 false
```

### 환경 변수 설정

> 메일 정보와 앱 비밀번호가 유출되는 것을 막기 위해 application.yml에서 username과 password를 직접적으로 쓰지 않고 <code>${mail.username}</code>, <code>${mail.password}</code>와 같이 작성하여 환경변수를 설정해준다. 

#### 배포시
> java -jar 파일명.jar --mail.username=아이디 --mail.password=앱 비밀번호


#### 인텔리제이

- **Edit Configuration** 클릭

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image1.png)

- 환경변수를 다음과 같이 설정

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image2.png)

## 기본소스 구현

> email/service/EmailMessage.java

```java
package org.choongang.email.service;

/**
 * 이메일 메세지 데이터 클래스
 * 
 * @param to : 수신인
 * @param subject : 제목 
 * @param message : 내용
 */
public record EmailMessage(
        String to,
        String subject,
        String message
) {}
```

> email/service/EmailSendService.java : 이메일 전송 서비스

```java
package org.choongang.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSendService {
    private final JavaMailSender javaMailSender;

    public boolean sendMail(EmailMessage message) {

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(message.to()); // 메일 수신자
            mimeMessageHelper.setSubject(message.subject());  // 메일 제목
            mimeMessageHelper.setText(message.message(), true); // 메일 내용
            javaMailSender.send(mimeMessage);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return false;

    }
}
```

### 전송 테스트 하기 

> 테스트 환경 변수 설정 

Edit Configuration클릭 하여 테스트 클래스 실행 시 환경 변수에 mail.username, mail.password를 다음과 같이 설정 합니다.

![image3](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image3.png)
  
> test/java/.../email/EmailSendTest.java

```java
package org.choongang.email;

import org.choongang.email.service.EmailMessage;
import org.choongang.email.service.EmailSendService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class EmailSendTest {
    @Autowired
    private EmailSendService emailSendService;

    @Test
    void sendTest() {
        EmailMessage message = new EmailMessage("yonggyo00@kakao.com", "제목...", "내용...");
        boolean success = emailSendService.sendMail(message);

        assertTrue(success);
    }
}
```

메일이 다음과 같이 전송되어 있다면 정상 동작하는것 입니다.  

![image4](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image4.png)

## 이메일 템플릿 구현

> email/service/EmailSendService.java : 타임리프 템플릿 기능 추가 

```java
package org.choongang.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailSendService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    /**
     * 메일 전송
     *
     * @param message : 수신인, 메일 제목, 메일 내용
     * @param tpl : 템플릿 사용하는 경우 템플릿 이름
     * @param tplData : 치환코드
     * @return
     */
    public boolean sendMail(EmailMessage message, String tpl, Map<String, Object> tplData) {
        String text = null;
        /**
         * 이메일 템플릿 사용하는 경우 EmailMessage의 제목, 내용, 수신인 및 tplData 추가 치환 속성을 전달하고
         * 타임리프로 번역된 텍스트를 반환 값으로 처리
         */
        if (StringUtils.hasText(tpl)) {
            tplData = Objects.requireNonNullElse(tplData, new HashMap<>());
            Context context = new Context();

            tplData.put("to", message.to());
            tplData.put("subject", message.subject());
            tplData.put("message", message.message());

            context.setVariables(tplData);

            text = templateEngine.process("email/" + tpl, context);
        } else { // 템플릿 전송이 아닌 경우 메세지로 대체
            text = message.message();
        }

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(message.to()); // 메일 수신자
            mimeMessageHelper.setSubject(message.subject());  // 메일 제목
            mimeMessageHelper.setText(text, true); // 메일 내용
            javaMailSender.send(mimeMessage);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean sendMail(EmailMessage message) {
        return sendMail(message, null, null);
    }
}
```

> resources/templates/email/auth.html : 이메일 인증 템플릿 

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
    <head>
        <title>이메일 인증</title>
    </head>
    <body>
        <dl>
            <dt>제목</dt>
            <dd th:text="${subject}"></dd>
        </dl>
        <dl>
            <dt>내용</dt>
            <dd th:utext="${message}"></dd>
        </dl>
        <dl>
            <dt>인증번호</dt>
            <dd th:text="${authNum}"></dd>
        </dl>
    </body>
</html>
```

### 전송 테스트 하기

> test/java/.../email/EmailSendTest.java

```java
...

@SpringBootTest
public class EmailSendTest {
    ...

    @Test
    @DisplayName("템플릿 형태로 전송 테스트")
    void sendWithTplTest() {
        EmailMessage message = new EmailMessage("yonggyo00@kakao.com", "제목...", "내용...");
        Map<String,Object> tplData = new HashMap<>();
        tplData.put("authNum", "123456");
        boolean success = emailSendService.sendMail(message, "auth", tplData);

        assertTrue(success);
    }
}
```

전송 성공하면 다음과 같은 메일을 수신 받습니다.

![image5](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image5.png)

# 활용하기

## 회원 가입 인증

> 1. 회원가입 양식에 이메일 입력항목 옆 인증 버튼 추가 
> 2. 인증버튼을 클릭하면 입력한 이메일로 인증 번호 전송 
> 3. 인증번호는 3분으로 유효시간 설정, 인증시간 카운트 시작
> 4. 회원 가입처리에서도 이메일 인증 여부 체크

### 이메일 인증 소스 구현 

> resources/messages/commons.properties

```properties

...

인증코드전송=인증코드전송
인증코드_입력=인증코드 입력
확인=확인
재전송=재전송
확인된_이메일_입니다.=확인된 이메일 입니다.
Email.verification.subject=회원가입 이메일 인증메일 입니다.
Email.verification.message=발급된 인증코드를 회원가입 항목에 입력하세요.
```

> email/service/EmailVerifyService.java

```java
package org.choongang.email.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailVerifyService {
    private final EmailSendService sendService;
    private final HttpSession session;

    /**
     * 이메일 인증 번호 발급 전송
     *
     * @param email
     * @return
     */
    public boolean sendCode(String email) {
        int authNum = (int)(Math.random() * 99999);

        session.setAttribute("EmailAuthNum", authNum);
        session.setAttribute("EmailAuthStart", System.currentTimeMillis());

        EmailMessage emailMessage = new EmailMessage(
                email,
                Utils.getMessage("Email.verification.subject", "commons"),
                Utils.getMessage("Email.verification.message", "commons"));
        Map<String, Object> tplData = new HashMap<>();
        tplData.put("authNum", authNum);

        return sendService.sendMail(emailMessage, "auth", tplData);
    }

    /**
     * 발급 받은 인증번호와 사용자 입력 코드와 일치 여부 체크
     *
     * @param code
     * @return
     */
    public boolean check(int code) {

        Integer authNum = (Integer)session.getAttribute("EmailAuthNum");
        Long stime = (Long)session.getAttribute("EmailAuthStart");
        if (authNum != null && stime != null) {
            /* 인증 시간 만료 여부 체크 - 3분 유효시간 S */
            boolean isExpired = (System.currentTimeMillis() - stime.longValue()) > 1000 * 60 * 3;
            if (isExpired) { // 만료되었다면 세션 비우고 검증 실패 처리
                session.removeAttribute("EmailAuthNum");
                session.removeAttribute("EmailAuthStart");

                return false;
            }
            /* 인증 시간 만료 여부 체크 E */

            // 사용자 입력 코드와 발급 코드가 일치하는지 여부 체크
            boolean isVerified = code == authNum.intValue();
            session.setAttribute("EmailAuthVerified", isVerified);

            return isVerified;
        }

        return false;
    }
}
```

> src/test/java/.../email/EmailSendTest.java

```java
...
@SpringBootTest
public class EmailSendTest {
    ...

    @Autowired
    private EmailVerifyService emailVerifyService;
    
    ...

    @Test
    @DisplayName("이메일 인증 번호 전송 테스트")
    void emailVerifyTest() {
        boolean result = emailVerifyService.sendCode("yonggyo00@kakao.com");
        assertTrue(result);
    }
}
```

전송에 성공하면 다음과 같이 인증번호가 발급 된 메일을 수신 받습니다.

![image6](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image6.png)

### 이메일 인증 요청 API 구현

> email/controllers/ApiEmailController.java

```java
package org.choongang.email.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.rests.JSONData;
import org.choongang.email.service.EmailVerifyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class ApiEmailController {

    private final EmailVerifyService verifyService;

    /**
     * 이메일 인증 코드 발급
     *
     * @param email
     * @return
     */
    @GetMapping("/verify")
    public JSONData<Object> sendVerifyEmail(@RequestParam("email") String email) {
        JSONData<Object> data = new JSONData<>();

        boolean result = verifyService.sendCode(email);
        data.setSuccess(result);

        return data;
    }

    /**
     * 발급받은 인증코드와 사용자 입력 코드의 일치 여부 체크
     *
     * @param authNum
     * @return
     */
    @GetMapping("/auth_check")
    public JSONData<Object> checkVerifiedEmail(@RequestParam("authNum") int authNum) {
        JSONData<Object> data = new JSONData<>();

        boolean result = verifyService.check(authNum);
        data.setSuccess(result);

        return data;
    }
}
```

> src/test/java/.../email/EmailApiTest.java : API 통합 테스트

```java
package org.choongang.email;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EmailApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("이메일 인증 코드 발급 및 검증 테스트")
    void sendVerifyEmailTest() throws Exception {
        /* 인증 코드 발급 테스트 S */
        HttpSession session = mockMvc.perform(get("/api/email/verify?email=yonggyo00@kakao.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
        Integer authNum = (Integer)session.getAttribute("EmailAuthNum");
        /* 인증 코드 발급 테스트 E */

        /* 인증 코드 검증 테스트 S */
        mockMvc.perform(get("/api/email/auth_check?authNum=" + authNum.intValue()))
                .andDo(print());
        /* 인증 코드 검증 테스트 E */
    }
}
```


### 회원가입 전용 자바스크립트, 스타일시트 파일 추가 
> 1. resources/static/front/js/member/join.js
> 2. resources/static/front/css/member/join.css
> 3. resources/static/mobile/js/member/join.js
> 4. resources/static/mobile/css/member/join.css

### 회원가입 주소 유입시 회원가입 전용 자바스크립트, 스타일시트 추가, 이메일 인증 여부 세션값 초기 처리

> member/controllers/MemberController.java

```java
...
@SessionAttributes("EmailAuthVerified")
public class MemberController implements ExceptionProcessor {

    ...

    @GetMapping("/join")
    public String join(@ModelAttribute RequestJoin form, Model model) {
        
        ... 
        
        // 이메일 인증 여부 false로 초기화
        model.addAttribute("EmailAuthVerified", false);
        
        return utils.tpl("member/join");
    }

    @PostMapping("/join")
    public String joinPs(@Valid RequestJoin form, Errors errors, Model model, SessionStatus sessionStatus) {
        
        ...

        // EmailAuthVerified 세션값 비우기 */
        sessionStatus.setComplete();

        return "redirect:/member/login";
    }

    ...

    private void commonProcess(String mode, Model model) {
        mode = StringUtils.hasText(mode) ? mode : "join";
        String pageTitle = Utils.getMessage("회원가입", "commons");

        List<String> addCss = new ArrayList<>();
        List<String> addScript = new ArrayList<>();

        if (mode.equals("login")) { // 로그인
            pageTitle = Utils.getMessage("로그인", "commons");

        } else if (mode.equals("join")) { // 회원가입
            addCss.add("member/join");
            addScript.add("member/join");
        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("addCss", addCss);
        model.addAttribute("addScript", addScript);
    }
}
```
### 회원가입 이메일 중복 여부 체크 추가 

> member/controllers/ApiMemberController.java 

```java
package org.choongang.member.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.ExceptionRestProcessor;
import org.choongang.commons.rests.JSONData;
import org.choongang.member.repositories.MemberRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class ApiMemberController implements ExceptionRestProcessor {

    private final MemberRepository memberRepository;

    /**
     * 이메일 중복 여부 체크
     * @param email
     * @return
     */
    @GetMapping("/email_dup_check")
    public JSONData<Object> duplicateEmailCheck(@RequestParam("email") String email) {
        boolean isExists = memberRepository.existsByEmail(email);

        JSONData<Object> data = new JSONData<>();
        data.setSuccess(isExists);

        return data;
    }
}
```

### 회원가입 템플릿에 이메일 인증 관련 코드 추가

> resources/templates/front/member/join.html

```html
...
<dl>
    <dt th:text="#{이메일}"></dt>
    <dd>
        <div>
            <input type="text" name="email" th:field="*{email}" th:readonly="${session.EmailAuthVerified != null && session.EmailAuthVerified}">
            <button th:if="${session.EmailAuthVerified == null || !session.EmailAuthVerified}" type="button" id="email_verify" th:text="#{인증코드전송}"></button>
        </div>
        <div class="auth_box">
            <th:block th:if="${session.EmailAuthVerified == null || !session.EmailAuthVerified}">
                <input type="text" id="auth_num" th:placeholder="#{인증코드_입력}">
                <span id="auth_count">03:00</span>
                <button type="button" id="email_confirm" th:text="#{확인}" disabled></button>
                <button type="button" id="email_re_verify" th:text="#{재전송}" disabled></button>
            </th:block>
            <th:block th:unless="${session.EmailAuthVerified == null || !session.EmailAuthVerified}">
                <span class='confirmed' th:text="#{확인된_이메일_입니다.}"></span>
            </th:block>
        </div>
        <div class="error" th:each="err : ${#fields.errors('email')}" th:text="${err}"></div>

    </dd>
</dl>
...
```

> resources/templates/mobile/member/join.html

```html
...
<div>
    <input type="text" name="email" th:field="*{email}" th:placeholder="#{이메일}" th:readonly="${session.EmailAuthVerified != null && session.EmailAuthVerified}">
    <button th:if="${session.EmailAuthVerified == null || !session.EmailAuthVerified}" type="button" id="email_verify" th:text="#{인증코드전송}"></button>
</div>
<div class="auth_box">
    <th:block th:if="${session.EmailAuthVerified == null || !session.EmailAuthVerified}">
        <input type="text" id="auth_num" th:placeholder="#{인증코드_입력}">
        <span id="auth_count">03:00</span>
        <button type="button" id="email_confirm" th:text="#{확인}" disabled></button>
        <button type="button" id="email_re_verify" th:text="#{재전송}" disabled></button>
    </th:block>
    <th:block th:unless="${session.EmailAuthVerified == null || !session.EmailAuthVerified}">
        <span class='confirmed' th:text="#{확인된_이메일_입니다.}"></span>
    </th:block>
</div>
<div class="error" th:each="err : ${#fields.errors('email')}" th:text="${err}"></div>
...

```

> resources/static/common/js/common.js : 이메일 인증 메일 보내기 및 검증 함수 추가

```javascript

...

var commonLib = commonLib || {};

/**
* ajax 처리
*
* @param method : 요청 메서드 - GET, POST, PUT ...
* @param url : 요청 URL
* @param responseType : json - 응답 결과를 json 변환, 아닌 경우는 문자열로 반환
*/
commonLib.ajaxLoad = function(method, url, params, responseType) {
    method = !method || !method.trim()? "GET" : method.toUpperCase();
    const token = document.querySelector("meta[name='_csrf']").content;
    const header = document.querySelector("meta[name='_csrf_header']").content;
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open(method, url);
        xhr.setRequestHeader(header, token);

        xhr.send(params);
        responseType = responseType?responseType.toLowerCase():undefined;
        if (responseType == 'json') {
            xhr.responseType=responseType;
        }

        xhr.onreadystatechange = function() {
            if (xhr.status == 200 && xhr.readyState == XMLHttpRequest.DONE) {
                const resultData = responseType == 'json' ? xhr.response : xhr.responseText;

                resolve(resultData);
            }
        };

        xhr.onabort = function(err) {
            reject(err);
        };

        xhr.onerror = function(err) {
            reject(err);
        };

        xhr.ontimeout = function(err) {
            reject(err);
        };
    });
};

/**
* 이메일 인증 메일 보내기
*
* @param email : 인증할 이메일
*/
commonLib.sendEmailVerify = function(email) {
    const { ajaxLoad } = commonLib;

    const url = `/api/email/verify?email=${email}`;

    ajaxLoad("GET", url, null, "json")
        .then(data => {
            if (typeof callbackEmailVerify == 'function') { // 이메일 승인 코드 메일 전송 완료 후 처리 콜백
                callbackEmailVerify(data);
            }
        })
        .catch(err => console.error(err));
};

/**
* 인증 메일 코드 검증 처리
*
*/
commonLib.sendEmailVerifyCheck = function(authNum) {
    const { ajaxLoad } = commonLib;
    const url = `/api/email/auth_check?authNum=${authNum}`;

    ajaxLoad("GET", url, null, "json")
        .then(data => {
            if (typeof callbackEmailVerifyCheck == 'function') { // 인증 메일 코드 검증 요청 완료 후 처리 콜백
                callbackEmailVerifyCheck(data);
            }
        })
        .catch(err => console.error(err));
};
```

> resources/static/front/js/member/join.js
> 
> resources/static/mobile/js/member/join.js

```javascript
window.addEventListener("DOMContentLoaded", function() {
    /* 인증 코드 전송 S */
    const emailVerifyEl = document.getElementById("email_verify"); // 인증코드 전송
    const emailConfirmEl = document.getElementById("email_confirm"); // 확인 버튼
    const emailReVerifyEl = document.getElementById("email_re_verify"); // 재전송 버튼
    const authNumEl = document.getElementById("auth_num"); // 인증코드
    if (emailVerifyEl) {
        emailVerifyEl.addEventListener("click", function() {
            const { ajaxLoad, sendEmailVerify } = commonLib;
            const email = frmJoin.email.value.trim();
            if (!email) {
                alert('이메일을 입력하세요.');
                frmJoin.email.focus();
                return;
            }

            /* 이메일 확인 전 이미 가입된 이메일인지 여부 체크 S */
            ajaxLoad("GET", `/api/member/email_dup_check?email=${email}`, null, "json")
                .then(data => {
                    if (data.success) { // 중복이메일인 경우
                        alert("이미 가입된 이메일입니다.");
                        frmJoin.email.focus();
                    } else { // 중복이메일이 아닌 경우
                        sendEmailVerify(email); // 이메일 인증 코드 전송
                        this.disabled = frmJoin.email.readonly = true;

                         /* 인증코드 재전송 처리 S */
                         if (emailReVerifyEl) {
                            emailReVerifyEl.addEventListener("click", function() {
                                sendEmailVerify(email);
                            });
                         }

                          /* 인증코드 재전송 처리 E */

                          /* 인증번호 확인 처리 S */
                          if (emailConfirmEl && authNumEl) {
                            emailConfirmEl.addEventListener("click", function() {
                                const authNum = authNumEl.value.trim();
                                if (!authNum) {
                                    alert("인증코드를 입력하세요.");
                                    authNumEl.focus();
                                    return;
                                }

                                // 인증코드 확인 요청
                                const { sendEmailVerifyCheck } = commonLib;
                                sendEmailVerifyCheck(authNum);
                            });
                          }
                          /* 인증번호 확인 처리 E */
                    }
                });

            /* 이메일 확인 전 이미 가입된 이메일인지 여부 체크 E */
        });
    }
    /* 인증 코드 전송 E */
});


/**
* 이메일 인증 메일 전송 후 콜백 처리
*
* @param data : 전송 상태 값
*/
function callbackEmailVerify(data) {
    if (data && data.success) { // 전송 성공
        alert("인증코드가 이메일로 전송되었습니다. 확인후 인증코드를 입력하세요.");

        /** 3분 유효시간 카운트 */
        authCount.start();

    } else { // 전송 실패
        alert("인증코드 전송에 실패하였습니다.");
    }
}

/**
* 인증메일 코드 검증 요청 후 콜백 처리
*
* @param data : 인증 상태 값
*/
function callbackEmailVerifyCheck(data) {
    if (data && data.success) { // 인증 성공
        /**
        * 인증 성공시
        * 1. 인증 카운트 멈추기
        * 2. 인증코드 전송 버튼 제거
        * 3. 이메일 입력 항목 readonly 속성으로 변경
        * 4. 인증 성공시 인증코드 입력 영역 제거
        * 5. 인증 코드 입력 영역에 "확인된 이메일 입니다."라고 출력 처리
        */

        // 1. 인증 카운트 멈추기
        if (authCount.intervalId) clearInterval(authCount.intervalId);

        // 2. 인증코드 전송 버튼 제거
        const emailVerifyEl = document.getElementById("email_verify");
        emailVerifyEl.parentElement.removeChild(emailVerifyEl);

        // 3. 이메일 입력 항목 readonly 속성으로 변경
        frmJoin.email.readonly = true;

        // 4. 인증 성공시 인증코드 입력 영역 제거, 5. 인증 코드 입력 영역에 "확인된 이메일 입니다."라고 출력 처리
        const authBoxEl = document.querySelector(".auth_box");
        authBoxEl.innerHTML = "<span class='confirmed'>확인된 이메일 입니다.</span>";

    } else { // 인증 실패
        alert("이메일 인증에 실패하였습니다.");
    }
}

/**
* 유효시간 카운트
*
*/
const authCount = {
    intervalId : null,
    count : 60 * 3, // 유효시간 3분
    /**
    * 인증 코드 유효시간 시작
    *
    */
    start() {
        const countEl = document.getElementById("auth_count");
        if (!countEl) return;

        this.initialize(); // 초기화 후 시작

        this.intervalId = setInterval(function() {

            authCount.count--;
            if (authCount.count < 0) {
                authCount.count = 0;
                clearInterval(authCount.intervalId);

                const emailConfirmEl = document.getElementById("email_confirm"); // 확인 버튼
                const emailReVerifyEl = document.getElementById("email_re_verify"); // 재전송 버튼
                const emailVerifyEl = document.getElementById("email_verify"); // 인증코드 전송
                emailConfirmEl.disabled = emailReVerifyEl.disabled = true;
                emailVerifyEl.disabled = frmJoin.email.readonly = false;
                return;
            }

            const min = Math.floor(authCount.count / 60);
            const sec = authCount.count - min * 60;

            countEl.innerHTML=`${("" + min).padStart(2, '0')}:${("" + sec).padStart(2, '0')}`;
        }, 1000);
    },

    /**
    * 인증 코드 유효시간 초기화
    *
    */
    initialize() {
        const countEl = document.getElementById("auth_count");
        const emailVerifyEl = document.getElementById("email_verify"); // 인증코드 전송
        const emailConfirmEl = document.getElementById("email_confirm"); // 확인 버튼
        const emailReVerifyEl = document.getElementById("email_re_verify"); // 재전송 버튼
        emailConfirmEl.disabled = emailReVerifyEl.disabled = false;
        emailVerifyEl.disabled = frmJoin.email.readonly = true;

        this.count = 60 * 3;
        if (this.intervalId) clearInterval(this.intervalId);
        countEl.innerHTML = "03:00";
    }
};
```

완성 화면

![image7](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image7.png)

## 비밀번호 초기화

> 1. 로그인페이지에서 비밀번호 찾기 링크 추가 
> 2. 비밀번호 찾기 페이지에 가입 이메일과 회원명 검증 
> 3. 가입 이메일과 회원명으로 일치하는 회원이 있다면 비밀번호를 임의의 비밀번호로 초기화
> 4. 가입한 이메일로 초기화된 비밀번호 전송


### 로그인페이지에서 비밀번호 찾기 링크 추가 

> resources/messages/commons.properties

```properties
...

비밀번호_찾기=비밀번호 찾기
비밀번호를_가입하신_이메일로_전송하였습니다.=비밀번호를 가입하신 이메일로 전송하였습니다.
Email.password.reset=비밀번호 초기화 안내입니다.

...

```

> resources/messages/validations.properties

```properties

...

# 비밀번호 찾기
NotBlank.requestFindPw.name=회원명을 입력하세요.
```

> resources/templates/front/member/login.html
> 
> resources/templates/mobile/member/login.html

```html
...

<h1 th:text="#{로그인}"></h1>
<form name="frmLogin" method="post" th:action="@{/member/login}" autocomplete="off">
    ...

    <a th:href="@{/member/find_pw}" th:text="#{비밀번호_찾기}"></a>
    
    ... 
</form>

...

```

> member/repositories/MemberRepository.java : 이메일과 회원명으로 조회되는지 체크하는 메서드 추가 

```java
... 

public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member> {
    ...

    /**
     * 이메일과 회원명으로 조회되는지 체크
     *
     * @param email
     * @param name
     * @return
     */
    default boolean existsByEmailAndName(String email, String name) {
        QMember member = QMember.member;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(member.email.eq(email))
                .and(member.name.eq(name));

        return exists(builder);
    }
}
```

> member/controllers/RequestFindPw.java : 비밀번호 찾기 양식 관련 커맨드 객체 추가 

```java
package org.choongang.member.controllers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 비밀번호 찾기 커맨드 객체 정의
 *
 */
public record RequestFindPw(
        @NotBlank @Email
        String email,

        @NotBlank
        String name
) {}
```

> member/controllers/FindPwValidator.java : 이메일 + 회원명 조합으로 조회 검증

```java
package org.choongang.member.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.member.repositories.MemberRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 비밀번호 찾기 추가 검증 처리
 *
 */
@Component
@RequiredArgsConstructor
public class FindPwValidator implements Validator {

    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestFindPw.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        // 이메일 + 회원명 조합으로 조회 되는지 체크
        RequestFindPw form = (RequestFindPw) target;
        String email = form.email();
        String name = form.name();

        if (StringUtils.hasText(email) && StringUtils.hasText(name) && !memberRepository.existsByEmailAndName(email, name)) {
            errors.reject("NotFound.member");
        }
    }
}
```

> resources/messages/errors.properties 

```properties
...

NotFound.member=가입되지 않은 회원입니다.
```

> commons/Utils.java : 자리수 지정 랜덤 문자열 생성 함수 추가

```java
...

public class Utils {
    ...

    /**
     * 알파벳, 숫자, 특수문자 조합 랜덤 문자열 생성
     *
     * @return
     */
    public String randomChars() {
        return randomChars(8);
    }

    public String randomChars(int length) {
        // 알파벳 생성
        Stream<String> alphas = IntStream.concat(IntStream.rangeClosed((int)'a', (int)'z'), IntStream.rangeClosed((int)'A', (int)'Z')).mapToObj(s -> String.valueOf((char)s));

        // 숫자 생성
        Stream<String> nums = IntStream.range(0, 10).mapToObj(String::valueOf);

        // 특수문자 생성
        Stream<String> specials = Stream.of("~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "-", "=", "[", "{", "}", "]", ";", ":");

        List<String> chars = Stream.concat(Stream.concat(alphas, nums), specials).collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(chars);

        return chars.stream().limit(length).collect(Collectors.joining());
    }
}
```

> member/service/FindPwService.java : 비밀번호 초기화 처리 및 메일 전송 서비스

```java
package org.choongang.member.service;


import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.choongang.email.service.EmailMessage;
import org.choongang.email.service.EmailSendService;
import org.choongang.member.controllers.FindPwValidator;
import org.choongang.member.controllers.RequestFindPw;
import org.choongang.member.entities.Member;
import org.choongang.member.repositories.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.Map;

/**
 * 비밀번호 찾기 양식 검증 및 초기화 메일 전송
 *
 */
@Service
@RequiredArgsConstructor
public class FindPwService {

    private final FindPwValidator validator;
    private final MemberRepository repository;
    private final EmailSendService sendService;
    private final PasswordEncoder encoder;
    private final Utils utils;

    public void process(RequestFindPw form, Errors errors) {
        validator.validate(form, errors);
        if (errors.hasErrors()) { // 유효성 검사 실패시에는 처리 중단
            return;
        }

        // 비밀번호 초기화
        reset(form.email());

    }

    public void reset(String email) {
        /* 비밀번호 초기화 S */
        Member member = repository.findByEmail(email).orElseThrow(MemberNotFoundException::new);

        String newPassword = utils.randomChars(12); // 초기화 비밀번호는 12자로 생성
        member.setPassword(encoder.encode(newPassword));

        repository.saveAndFlush(member);

        /* 비밀번호 초기화 E */
        EmailMessage emailMessage = new EmailMessage(email, Utils.getMessage("Email.password.reset", "commons"), Utils.getMessage("Email.password.reset", "commons"));
        Map<String, Object> tplData = new HashMap<>();
        tplData.put("password", newPassword);
        sendService.sendMail(emailMessage, "password_reset", tplData);
    }
}
```

> resources/templates/email/password_reset.html : 비밀번호 초기화 이메일 템플릿

```html 
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>비밀번호 초기화 안내</title>
</head>
<body>
    <div th:text="${subject}"></div>
    <div>
        <th:block th:text="#{비밀번호}"></th:block>: <th:block th:text="${password}"></th:block>
    </div>
</body>
</html>
```

> src/test/java/.../member/FindPwServiceTest.java : 초기화된 비밀번호 업데이트 및 메일전송 테스트

```java 
package org.choongang.member;

import org.choongang.member.service.FindPwService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class FindPwServiceTest {

    @Autowired
    private FindPwService service;

    @Test
    @DisplayName("비밀번호 초기화 및 초기화된 메일 이메일 전송 테스트")
    void resetTest() {
        assertDoesNotThrow(() -> service.reset("yonggyo00@kakao.com"));
    }
}
```

테스트에 통과하면 초기화된 비밀번호 안내 메일이 전송되며, 새로운 비밀번호로 로그인이 되면 성공입니다.

![image8](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image8.png)


> member/controllers/MemberController.java : 비밀번호 찾기 컨트롤러 처리

```java
...
public class MemberController implements ExceptionProcessor {
    ...
    
    private final FindPwService findPwService;
    
    ...

    /**
     * 비밀번호 찾기 양식
     *
     * @param model
     * @return
     */
    @GetMapping("/find_pw")
    public String findPw(@ModelAttribute RequestFindPw form, Model model) {
        commonProcess("find_pw", model);

        return utils.tpl("member/find_pw");
    }

    /**
     * 비밀번호 찾기 처리
     *
     * @param model
     * @return
     */
    @PostMapping("/find_pw")
    public String findPwPs(@Valid RequestFindPw form, Errors errors, Model model) {
        commonProcess("find_pw", model);

        findPwService.process(form, errors); // 비밀번호 찾기 처리

        if (errors.hasErrors()) {
            return utils.tpl("member/find_pw");
        }

        // 비밀번호 찾기에 이상 없다면 완료 페이지로 이동
        return "redirect:/member/find_pw_done";
    }

    /**
     * 비밀번호 찾기 완료 페이지
     *
     * @param model
     * @return
     */
    @GetMapping("/find_pw_done")
    public String findPwDone(Model model) {
        commonProcess("find_pw", model);

        return utils.tpl("member/find_pw_done");
    }

    private void commonProcess(String mode, Model model) {
        ...

        if (mode.equals("login")) { // 로그인
            pageTitle = Utils.getMessage("로그인", "commons");

        } else if (mode.equals("join")) { // 회원가입
            addCss.add("member/join");
            addScript.add("member/join");

        } else if (mode.equals("find_pw")) { // 비밀번호 찾기
            pageTitle = Utils.getMessage("비밀번호_찾기", "commons");
        }
        
        ...
    }
}
```

> resources/templates/front/member/find_pw.html : 비밀번호 찾기 템플릿 

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{front/layouts/main}">
<main layout:fragment="content">
    <h1 th:text="#{비밀번호_찾기}"></h1>
    <div th:text="#{비밀번호를_가입하신_이메일로_전송하였습니다.}"></div>
    <a th:href="@{/member/login}" th:text="#{로그인}"></a>
</main>
</html>
```


> resources/templates/mobile/member/find_pw.html : 비밀번호 찾기 템플릿

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{mobile/layouts/main}">
<main layout:fragment="content">
    <h1 th:text="#{비밀번호_찾기}"></h1>

    <form name="frmFindPw" method="post" th:action="@{/member/find_pw}" autocomplete="off" th:object="${requestFindPw}">

        <input type="text" name="email" th:field="*{email}" th:placeholder="#{이메일}">
        <div class="error" th:each="err : ${#fields.errors('email')}" th:text="${err}"></div>


        <input type="text" name="email" th:field="*{name}" th:placeholder="#{회원명}">
        <div class="error" th:each="err : ${#fields.errors('name')}" th:text="${err}"></div>

        <button type="submit" th:text="#{비밀번호_찾기}"></button>
        <div class="error global" th:each="err : ${#fields.globalErrors()}" th:text="${err}"></div>
    </form>
</main>
</html>
```

> resources/templates/front/member/find_pw_done.html : 비밀번호 찾기 완료 페이지

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{front/layouts/main}">
<main layout:fragment="content">
    <h1 th:text="#{비밀번호_찾기}"></h1>
    <div th:text="#{비밀번호를_가입하신_이메일로_전송하였습니다.}"></div>
    <a th:href="@{/member/login}" th:text="#{로그인}"></a>
</main>
</html>
```

> resources/templates/mobile/member/find_pw_done.html : 비밀번호 찾기 완료 페이지

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{mobile/layouts/main}">
<main layout:fragment="content">
    <h1 th:text="#{비밀번호_찾기}"></h1>
    <div th:text="#{비밀번호를_가입하신_이메일로_전송하였습니다.}"></div>
    <a th:href="@{/member/login}" th:text="#{로그인}"></a>
</main>
</html>
```


비밀번호 찾기 화면 

![image9](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image9.png)



비밀번호 찾기 완료 화면

![image10](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image10.png)

