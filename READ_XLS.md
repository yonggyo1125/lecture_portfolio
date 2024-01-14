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

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/master/images/excel/image1.png)

## Excel 편의 클래스 추가 

> commons/ExcelUtils.java

```java
package org.choongang.commons;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ExcelUtils {

    private List<String> sqlData; // 생성된 SQL 데이터 담을 공간

    /**
     * 엑셀 데이터 추출
     *
     * @param filePath : 엑셀 파일 경로
     * @param cellNums : 데이터로 추출할 셀번호, 0번 부터 시작
     * @param sheetNo : 엑셀 시트 번호, 0번 부터 시작
     * @return
     */
    public List<String[]> getData(String filePath, int[] cellNums, int sheetNo) {
        Path path = Path.of(filePath);
        sheetNo = sheetNo < 0 ? 0 : sheetNo; // sheetNo가 0미만 인 경우 첫번째 시트인 0으로 변경

        if (cellNums == null || cellNums.length == 0) {
            return null;
        }

        List<String[]> data = new ArrayList<>();
        try (InputStream in = Files.newInputStream(path, StandardOpenOption.READ);
             OPCPackage opcPackage = OPCPackage.open(in)) {

            XSSFWorkbook workbook = new XSSFWorkbook(opcPackage);
            XSSFSheet sheet = workbook.getSheetAt(sheetNo);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 행별 데이터 추출
                XSSFRow row = sheet.getRow(i);

                String[] items = new String[cellNums.length];
                for (int j = 0; j < cellNums.length; j++) {
                    items[j] = getCellData(row.getCell(cellNums[j]));
                }

                data.add(items);
            }

        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * 엑셀 데이터를 문자열로 결합
     *
     * @param delimiter : 구분 문자
     * @return
     */
    public List<String> getData(String filePath, int[] cellNums, int sheetNo, String delimiter) {

        return getData(filePath, cellNums, sheetNo).stream().map(s -> Arrays.stream(s).collect(Collectors.joining(delimiter))).toList();
    }

    public String getCellData(XSSFCell cell) {
        if (cell == null) return "";

        // 데이터 형식 관련 오류 방지를 위해 문자가 아닌 자료형인 경우 문자형으로 변경
        if (cell.getCellType() != CellType.STRING) {
            cell.setCellType(CellType.STRING);
        }

        return Objects.requireNonNullElse(cell.getStringCellValue(), "");
    }

    /**
     * SQL로 생성
     *
     * @param filePath : 파일 경로
     * @param cellNums : 데이터로 추출할 셀번호, 0번 부터 시작
     * @param sheetNo : 엑셀 시트 번호, 0번 부터 시작
     * @param tableNm : 테이블명  예) CENTER_INFO
     * @param fields : SQL 생성 필드 예) new String[] { "location", "centerNm", "centerType", "address", "tel"};
     * @return
     */
    public ExcelUtils makeSql(String filePath, int[] cellNums, int sheetNo, String tableNm, String[] fields) {
        sqlData = new ArrayList<>();

        List<String[]> lines = getData(filePath, cellNums,sheetNo);
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

> src/test/java/.../dmImport/ExcelUtilsTest.java

```java
package org.choongang.dmImport;

import org.choongang.commons.ExcelUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ExcelUtilsTest {

    @Autowired
    private ExcelUtils utils;

    @Test
    @DisplayName("엑셀 파일 -> List<String[]> 으로 변환 테스트")
    void test1() {
        List<String[]> data = utils.getData("data/schools.xlsx", new int[] {0, 1}, 0);
        data.forEach(s -> System.out.println(Arrays.toString(s)));
    }

    @Test
    @DisplayName("엑셀파일 -> List<String[]> -> SQL 목록 변환 테스트")
    void test2() {
        String[] fields = { "NAME", "DOMAIN" };
        List<String> sqlData = utils.makeSql("data/schools.xlsx", new int[] {0, 1}, 0, "SCHOOLS", fields).toList();
        sqlData.forEach(System.out::println);
    }

    @Test
    @DisplayName("엑셀파일 -> List<String[]> -> SQL 파일 변환 테스트")
    void test3() {
        String destPath = "data/schools.sql";
        String[] fields = { "NAME", "DOMAIN" };
        utils.makeSql("data/schools.xlsx", new int[] {0, 1}, 0, "SCHOOLS", fields).toFile(destPath);
        File file = new File(destPath);

        assertTrue(file.exists());
    }

    @Test
    @DisplayName("엑셀파일 -> delimiter 문자열을 결합한 List<String> 변환 테스트")
    void test4() {
        List<String> data = utils.getData("data/schools.xlsx", new int[] {0, 1}, 0,"_");
        data.forEach(System.out::println);
    }
}
```

- getData()로 추출한 엑셀 데이터

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/master/images/excel/image2.png)

- toList()로 생성한 데이터 화면

![image3](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/master/images/excel/image3.png)


- toFile("data/schools.sql") 로 생성한 data/schools.sql 파일 내용

![image4](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/master/images/excel/image4.png)

> 1조의 경우 학교명_도메인으로 데이터를 가공해야 하는 것으로 알고 있습니다.
> 
> 학교별 도메인이 홈페이지 주소라 2차 가공할것이 좀더 있지만 test4() 내용을 살펴보고 2차 가공을 생각해 봅시다!

![image5](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/master/images/excel/image5.png)