package org.choongang.commons;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
     * @param addField : 추가 필드
     * @param addValue : 추가 값
     * @param encoding : csv 파일 인코딩 : 윈도우즈 -  EUC-KR, 맥 - UTF-8
     * @return
     */
    public CsvUtils makeSql(String filePath, String tableNm, String[] fields, String addField, String addValue, String encoding) {
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

            if (StringUtils.hasText(addField)) sb.append(",").append(addField); // 추가 필드

            sb.append(" ) VALUES (");
            sb.append(Arrays.stream(line).map(s -> "\"" + s + "\"").collect(Collectors.joining(",")));

            if (StringUtils.hasText(addValue)) sb.append(",").append(addValue);

            sb.append(");\n");
            sqlData.add(sb.toString());
        });

        return this;
    }

    public CsvUtils makeSql(String filePath, String tableNm, String[] fields) {
        return makeSql(filePath, tableNm, fields, null, null, "EUC-KR");
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
