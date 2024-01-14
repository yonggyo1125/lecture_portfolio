# 엑셀 파일 읽어오기

## 의존성 추가

> build.gradle 

```groovy
...

dependencies {
    ...

    implementation 'org.apache.poi:poi-ooxml:5.2.5'
    
    ...
}

...

```

> 마이크로소프트 오피스 파일(xls, xlsx, pdf, ppt, doc, docx) 등은 Apache POI 구현체 에서 제공하며  
> 
> Apache POI API Based On OPC and OOXML Schemas 의존성을 찾아서 추가하면 됩니다.


## 읽어올 엑셀 데이터 가공

> data/schools.xlsx : 1조 데이터 기준으로 학교명과 이메일 도메인만 필요하므로 원 엑셀파일에서 2개 셀만 따로 가공

