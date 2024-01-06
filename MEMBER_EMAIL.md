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

인증하기=인증하기
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
            return code == stime.intValue();
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



### 회원가입 전용 자바스크립트, 스타일시트 파일 추가 
> 1. static/front/js/member/join.js
> 2. static/front/css/member/join.css
> 3. static/mobile/js/member/join.js
> 4. static/mobile/css/member/join.css

### 회원가입 주소 유입시 회원가입 전용 자바스크립트, 스타일시트 추가 

> member/controllers/MemberController.java

```java
...
public class MemberController implements ExceptionProcessor {

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

### 회원가입 템플릿에 인증하기 버튼 추가
> resources/templates/front/member/join.html

```html
...
<dl>
    <dt th:text="#{이메일}"></dt>
    <dd>
        <input type="text" name="email" th:field="*{email}">
        <button type="button" id="email_verify" th:text="#{인증하기}"></button>
        <div class="error" th:each="err : ${#fields.errors('email')}" th:text="${err}"></div>
    </dd>
</dl>
...
```


## 비밀번호 초기화

