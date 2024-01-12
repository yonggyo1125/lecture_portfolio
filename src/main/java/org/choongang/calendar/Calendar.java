package org.choongang.calendar;

import org.choongang.commons.Utils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Lazy
@Component
public class Calendar {

    /**
     * 달력 데이터
     *
     * 달력을 만들때 가장 중요한 항목 2가지
     * 1. 매 월 1일의 시작 요일 구하기 : 1일이 달력에서 얼마만큼 떨어져 있는지 위치를 구하는 정보로 사용
     *      - java.time 패키지에서 요일은 getDayOfWeek().getValue()로 구할 수 있으나
     *      - 1~7(월~일)로 나오므로 일요일 부터 시작하는 달력이면 7 -> 0으로 변경한다.
     * 2. 매 월의 마지막 일자 구하기 : 28, 29, 30, 31 등 월마다 달라질 수 있는 값, 다음달 1일에서 하루를 현재 달의 마지막 날짜를 구할 수 있음
     */
    public Map<String, Object> getData(Integer _year, Integer _month) {
        int year, month = 0;
        if (_year == null || _month == null) { // 년도와 월 값이 없으면 현재 년도, 월로 고정
            LocalDate today = LocalDate.now();
            year = today.getYear();
            month = today.getMonthValue();
        } else {
            year = _year.intValue();
            month = _month.intValue();
        }

        LocalDate sdate = LocalDate.of(year, month, 1);
        LocalDate eDate = sdate.plusMonths(1L).minusDays(1);
        int sYoil = sdate.getDayOfWeek().getValue(); // 매월 1일 요일

        sYoil = sYoil == 7 ? 0 : sYoil;

        int start = sYoil * -1;

        int cellNum = sYoil + eDate.getDayOfMonth() > 35 ? 42 : 35;


        Map<String, Object> data = new HashMap<>();

        List<String> days = new ArrayList<>(); // 날짜, 1, 2, 3,
        List<String> dates = new ArrayList<>(); // 날짜 문자열 2024-01-12
        List<String> yoils = new ArrayList<>(); // 요일 정보

        for (int i = start; i < cellNum + start; i++) {
            LocalDate date = sdate.plusDays(i);

            int yoil = date.getDayOfWeek().getValue();
            yoil = yoil == 7 ? 0 : yoil; // 0 ~ 6 (일 ~ 토)
            days.add(String.valueOf(date.getDayOfMonth()));
            dates.add(date.toString());
            yoils.add(String.valueOf(yoil));

            data.put("days", days);
            data.put("dates", dates);
            data.put("yoils", yoils);
        }

        // 이전달 년도, 월
        LocalDate prevMonthDate = sdate.minusMonths(1L);
        data.put("prevYear", String.valueOf(prevMonthDate.getYear())); // 이전달 년도
        data.put("prevMonth", String.valueOf(prevMonthDate.getMonthValue())); // 이전달 월

        // 다음달 년도, 월
        LocalDate nextMonthDate = sdate.plusMonths(1L);
        data.put("nextYear", String.valueOf(nextMonthDate.getYear())); // 다음달 년도
        data.put("nextMonth", String.valueOf(nextMonthDate.getMonthValue())); // 다음달 월

        // 현재 년도, 월
        data.put("year", String.valueOf(year));
        data.put("month", String.valueOf(month));

        // 요일 제목
        data.put("yoilTitles", getYoils());

        return data;
    }

    /**
     * 매개변수가 없는 데이터는 현재 일자 기준의 년도, 월로 달력 데이터 생성
     *
     * @return
     */
    public Map<String, Object> getData() {
        return getData(null, null);
    }

    /**
     * 요일 목록
     *
     * @return
     */
    public List<String> getYoils() {

        return Arrays.asList(
                Utils.getMessage("일", "commons"),
                Utils.getMessage("월", "commons"),
                Utils.getMessage("화", "commons"),
                Utils.getMessage("수", "commons"),
                Utils.getMessage("목", "commons"),
                Utils.getMessage("금", "commons"),
                Utils.getMessage("토", "commons")
        );
    }
}