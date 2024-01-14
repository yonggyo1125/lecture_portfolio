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
