# CSV 파일 읽어오기

## 의존성 추가 

> build.gradle 

```groovy
...
dependencies {
    ...

    implementation 'com.opencsv:opencsv:5.9'
    
    ...
}

...

```

## 공공데이터 포털 : 헌혈의 집 CSV 파일 다운받기

- [다운로드](https://www.data.go.kr/data/15050729/fileData.do#tab-layer-file)

> CSV 파일명이 한글이면 파일명 인코딩에 따른 미 인식 문제가 발생할 수 있으므로 영문, 숫자 조합으로 변경해 줍니다.
>
> 강의 기준에서는 다음 경로로 설정하고 파일명 변경함 : data/data.csv


## CSV 편의 클래스 추가 

> commons/CsvUtils.java

```java
package org.choongang.commons;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CsvUtils {

    private List<String> sqlData; // 생성된 SQL 데이터 담을 공간


    /**
     * CSV 파일을 순서대로 배열로 변환
     *
     * @param filePath : csv 파일 경로
     * @param encoding : csv 파일 인코딩 : 윈도우즈 -  EUC-KR, 맥 - UTF-8
     * @return
     */
    public List<String[]> getData(String filePath, String encoding) {
        List<String[]> lines = new ArrayList<>();
        try {
            CSVReader csvReader = new CSVReader(new FileReader(filePath, Charset.forName(encoding)));
            String[] line;
            while((line = csvReader.readNext()) != null) {
                for (int i = 0; i < line.length; i++) {

                    line[i] = new String(line[i].getBytes(), Charset.forName("UTF-8"));
                }

                lines.add(line);
            }
        } catch (CsvException | IOException e) {
            e.printStackTrace();
        }

        return lines;
    }
    public List<String[]> getData(String filePath) {
        return getData(filePath, "EUC-KR");
    }

    /**
     * SQL로 생성
     *
     * @param filePath : 파일 경로
     * @param tableNm : 테이블명  예) CENTER_INFO
     * @param fields : SQL 생성 필드 예) new String[] { "location", "centerNm", "centerType", "address", "tel"};
     * @param encoding : csv 파일 인코딩 : 윈도우즈 -  EUC-KR, 맥 - UTF-8
     * @return
     */
    public CsvUtils makeSql(String filePath, String tableNm, String[] fields, String encoding) {
        sqlData = new ArrayList<>();

        List<String[]> lines = getData(filePath, encoding);
        if (lines == null || lines.isEmpty() || fields == null || fields.length == 0) {
            return this;
        }

        lines.forEach(line -> {
            StringBuffer sb = new StringBuffer(3000);
            sb.append("INSERT INTO ");
            sb.append(tableNm);
            sb.append(" (");
            sb.append(Arrays.stream(fields).collect(Collectors.joining(",")));
            sb.append(" ) VALUES (");
            sb.append(Arrays.stream(line).map(s -> "\"" + s + "\"").collect(Collectors.joining(",")));
            sb.append(");\n");
            sqlData.add(sb.toString());
        });

        return this;
    }

    public CsvUtils makeSql(String filePath, String tableNm, String[] fields) {
        return makeSql(filePath, tableNm, fields, "EUC-KR");
    }

    /**
     * 가공된 SQL 데이터 반환
     *
     * @return
     */
    public List<String> toList() {
        return sqlData;
    }

    /**
     * 가공된 SQL 데이터 SQL로 파일 작성
     *
     * @param destination : 생성될 파일 경로
     */
    public void toFile(String destination) {
        if (sqlData == null || sqlData.isEmpty()) {
            return;
        }

        Path path = Path.of(destination);

        try {
            Files.write(path, sqlData, Charset.forName("UTF-8"), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
```

> <code>toList()</code>와 <code>toFile(...)</code>은 <code>makeSql()</code> 메서드 호출 후 메서드 체이닝 형태로 연달아 호출합니다.

- <code>toList()</code> :  가공된 sql 데이터를 List 형태로 반환합니다.
- <code>toFile(...)</code> : 가동된 sql 데이터를 특정 파일로 작성합니다. 실 서버 dbeaver에 접속하신 후 생성된 sql 파일을 실행해 주면 됩니다. 다만 테이블이 없다면 오류가 발생하므로 테이블을 먼저 생성합니다.

## 기능 테스트 

> src/test/java/.../dbImport/CSVUtilsTest.java

```java 
package org.choongang.dmImport;

import org.choongang.commons.CsvUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class CSVUtilsTest {
    @Autowired
    private CsvUtils csvUtils;

    @Test
    @DisplayName("CSV 파일 변환 함수 테스트")
    void test1() {
        List<String[]> lines = csvUtils.getData("data/data.csv", "EUC-KR");
        lines.forEach(s -> System.out.println(Arrays.toString(s)));
    }

    @Test
    @DisplayName("CSV 파일 변환 후 SQL 가공 함수 테스트")
    void test2() {
        String[] fields = { "location", "centerNm", "centerType", "address", "tel"};
        List<String> sqlData = csvUtils.makeSql("data/data.csv", "CENTER_INFO", fields, "EUC-KR").toList();

        sqlData.forEach(System.out::println);

    }

    @Test
    @DisplayName("CSV 파일 변환 -> SQL 가공 -> sql 파일로 작성 테스트")
    void test3() {

        String destPath = "data/branch.sql";
        String[] fields = { "location", "centerNm", "centerType", "address", "tel"};
        csvUtils.makeSql("data/data.csv", "CENTER_INFO", fields, "EUC-KR").toFile(destPath);
        File file = new File(destPath);

        assertTrue(file.exists());
    }
}
```

- toList()로 생성한 데이터 화면

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/master/images/csv/image1.png)


- toFile("data/branch.sql") 로 생성한 data/branch.sql 파일 내용

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/master/images/csv/image2.png)
