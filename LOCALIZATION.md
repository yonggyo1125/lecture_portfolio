# 지역화

## 국가별 언어 수동 변경 

> configs/I18NConfig.java
> 
> 1. **language=언어코드**로 언어 수동 변경 가능
> 2. language값으로 쿠키가 1시간 동안 유지되면 유지되는 동안은 해당 언어로 설정
> 3. language 쿠키가 존재하지 않다면 요청 헤더의 Accept-language 값으로 기본 Locale 설정됨  

```java
package org.choongang.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * 다국어 수동 변경 설정
 * 
 */
@Configuration
public class I18NConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(localeChangeInterceptor());

    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("language");
        return localeChangeInterceptor;
    }

    @Bean
    public CookieLocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setCookieName("language");
        resolver.setCookieMaxAge(60 * 60); // 1시간 변경된 언어로 유지

        return resolver;
    }
}

```

> resources/messages/commons_en.properties

```properties
# 회원 공통
회원가입=SIGN UP
이메일=EMAIL
아이디=USER ID
비밀번호=PASSWORD
비밀번호_확인=CONFIRM PASSWORD
회원명=NAME
회원가입_약관=MEMBERSHIP TERMS 
가입하기=JOIN
회원가입_약관에_동의합니다.=I agree the membership terms. 
로그인=LOGIN
LOGIN_MSG={0}({1}) logged in

인증코드전송=Authentication Code
인증코드_입력=Input Code
확인=CONFIRM
재전송=RESEND
확인된_이메일_입니다.=Email Verified.
Email.verification.subject=Email Verification
Email.verification.message=Please Enter the issued authentication code below.

비밀번호_찾기=Find your password
비밀번호를_가입하신_이메일로_전송하였습니다.=비밀번호를 가입하신 이메일로 전송하였습니다.
Email.password.reset=비밀번호 초기화 안내입니다.

자동_로그인=Remember me
```

> http://localhost:3000/member/join 으로 접근했을 때

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/localization/images/localization/image1.png)

> http://localhost:3000/member/join?language=en 으로 접근했을 때

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/localization/images/localization/image2.png)

> 쿠키에 language 값 1시간 유지

![image3](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/localization/images/localization/image3.png)