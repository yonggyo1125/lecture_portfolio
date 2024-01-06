# 이메일 전송 

## 설정하기

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
> java -jar 파일명.jar --mail.username=아이디 --mail.password=비밀번호


#### 인텔리제이

- **Edit Configuration** 클릭

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image1.png)

- 환경변수를 다음과 같이 설정

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/member-email/images/email/image2.png)

## 소스 구현하기

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



# 활용하기
## 비밀번호 초기화
## 회원 가입 인증