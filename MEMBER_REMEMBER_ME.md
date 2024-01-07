# 자동 로그인(스프링 시큐리티 - remember-me 활용)

## 스프링 시큐리티 remember-me 

> 1. 세션이 만료되었거나 브라우저를 종료 후 다시 시작해도 로그인 상태를 유히자는 기능
> 2. 쿠키에 <code>remember-me</code>가 있다면 해당 토큰을 이용하여 로그인을 다시 시도하여 로그인 유지
> 3. 로그이웃 하면 remember-me 쿠키도 삭제되고 로그인 유지 기능은 동작하지 않는다.

## 스프링 시큐리티 설정에 코드 추가 

> configs/SecurityConfig.java

```java
... 

@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberInfoService memberInfoService;
    
    ...

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        ...

        /* 자동 로그인 설정 S */
        http.rememberMe(c -> {
                    c.rememberMeParameter("autoLogin") // 자동 로그인으로 사용할 요청 파리미터 명, 기본값은 remember-me
                    .tokenValiditySeconds(60 * 60 * 24 * 30) // 로그인을 유지할 기간(30일로 설정), 기본값은 14일
                    .userDetailsService(memberInfoService) // 재로그인을 하기 위해서 인증을 위한 필요 UserDetailsService 구현 객체
                    .authenticationSuccessHandler(new LoginSuccessHandler()); // 자동 로그인 성공시 처리 Handler

        });
        /* 자동 로그인 설정 E */

        return http.build();
    }
}
```


> resources/messages/commons.properties

```properties
...

자동_로그인=자동 로그인

```

> resources/templates/front/member/login.html
> 
> resources/templates/mobile/member/login.html

```html
<form name="frmLogin" method="post" th:action="@{/member/login}" autocomplete="off">
    ...
    <div>
        <input type="checkbox" name="autoLogin" value="true" id="autoLogin">
        <label for="autoLogin" th:text="#{자동_로그인}"></label>
    </div>
    ...
</form>
```


로그인 페이지 구현 화면 



> 자동 로그인을 체크하고 로그인을 하면 remember-me라는 쿠키가 생성이 되며 해당 토큰으로 remember-me 필터에서 자동 로그인을 처리하게 됩니다. 
>
> 정상적으로 자동 로그인이 되었다면 다음과 같은 쿠키를 확인할 수 있습니다. 