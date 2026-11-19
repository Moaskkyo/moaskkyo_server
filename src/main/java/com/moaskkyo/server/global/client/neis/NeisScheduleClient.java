package com.moaskkyo.server.global.client.neis;

import com.moaskkyo.server.global.client.ExternalSchedule;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.DateTimeException;
import java.time.LocalDate;
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
public class NeisScheduleClient {

    private static final String URL = "https://open.neis.go.kr/hub/SchoolSchedule"
            + "?KEY={key}&Type=json&pIndex=1&pSize=1000"
            + "&ATPT_OFCDC_SC_CODE={atptCode}&SD_SCHUL_CODE={schoolCode}"
            + "&AA_FROM_YMD={from}&AA_TO_YMD={to}";
    private static final String NO_DATA_CODE = "INFO-200";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final NeisProperties properties;

    public List<ExternalSchedule> getSchedules(String atptCode, String schoolCode, LocalDate from, LocalDate to) {
        try {
            String body = restClient.get()
                    .uri(URL, properties.apiKey(), atptCode, schoolCode, from.format(DATE_FORMAT), to.format(DATE_FORMAT))
                    .retrieve()
                    .body(String.class);
            JsonNode root = jsonMapper.readTree(body);

            // 데이터가 없거나 오류일 때는 SchoolSchedule 없이 RESULT만 내려온다.
            JsonNode result = root.path("RESULT");
            if (!result.isMissingNode()) {
                if (NO_DATA_CODE.equals(result.path("CODE").asString(""))) {
                    return List.of();
                }
                throw new IllegalStateException("NEIS 응답 오류: " + result);
            }

            List<ExternalSchedule> schedules = new ArrayList<>();
            for (JsonNode row : root.path("SchoolSchedule").path(1).path("row")) {
                schedules.add(new ExternalSchedule(
                        LocalDate.parse(row.path("AA_YMD").asString(""), DATE_FORMAT),
                        row.path("EVENT_NM").asString("").trim()));
            }
            return schedules;
        } catch (RestClientException | JacksonException | DateTimeException | IllegalStateException e) {
            log.error("NEIS 학사일정 조회 실패", e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}
