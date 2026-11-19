package com.moaskkyo.server.global.client.holiday;

import com.moaskkyo.server.global.client.ExternalSchedule;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayClient {

    private static final String URL = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo"
            + "?serviceKey={serviceKey}&solYear={year}&solMonth={month}&_type=json&numOfRows=100";
    private static final String SUCCESS_CODE = "00";
    private static final String HOLIDAY_FLAG = "Y";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final HolidayProperties properties;

    // 특일 정보 API는 연·월 단위로만 조회되므로, 기간에 걸친 달마다 호출한다.
    public List<ExternalSchedule> getHolidays(LocalDate from, LocalDate to) {
        try {
            List<ExternalSchedule> holidays = new ArrayList<>();
            for (YearMonth month = YearMonth.from(from); !month.isAfter(YearMonth.from(to)); month = month.plusMonths(1)) {
                String body = restClient.get()
                        .uri(URL, properties.serviceKey(), month.getYear(), String.format("%02d", month.getMonthValue()))
                        .retrieve()
                        .body(String.class);
                JsonNode response = jsonMapper.readTree(body).path("response");
                if (!SUCCESS_CODE.equals(response.path("header").path("resultCode").asString(""))) {
                    throw new IllegalStateException("공휴일 응답 오류: " + response.path("header"));
                }

                // item은 여러 건이면 배열, 한 건이면 객체, 없으면 items가 빈 문자열로 내려온다.
                JsonNode item = response.path("body").path("items").path("item");
                for (JsonNode holiday : item.isObject() ? List.of(item) : item) {
                    if (HOLIDAY_FLAG.equals(holiday.path("isHoliday").asString(""))) {
                        holidays.add(new ExternalSchedule(
                                LocalDate.parse(holiday.path("locdate").asString(""), DATE_FORMAT),
                                holiday.path("dateName").asString("").trim()));
                    }
                }
            }
            return holidays;
        } catch (RestClientException | JacksonException | DateTimeException | IllegalStateException e) {
            log.error("공휴일 조회 실패", e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}
