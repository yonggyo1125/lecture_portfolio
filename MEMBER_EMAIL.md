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

- 환경변수를 다음과 같이 설정


# 활용하기
## 비밀번호 초기화
## 회원 가입 인증