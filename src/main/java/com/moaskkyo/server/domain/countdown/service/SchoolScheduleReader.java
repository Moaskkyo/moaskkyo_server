package com.moaskkyo.server.domain.countdown.service;

import com.moaskkyo.server.global.client.ExternalSchedule;
import com.moaskkyo.server.global.client.neis.NeisScheduleClient;
import com.moaskkyo.server.global.error.BusinessException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

// 같은 학교, 같은 기간이면 NEIS를 다시 부르지 않도록 캐시를 둔다. (만료는 application.yaml 설정)
@Slf4j
@Component
@RequiredArgsConstructor
public class SchoolScheduleReader {

    private final NeisScheduleClient neisScheduleClient;

    // NEIS가 응답하지 않아도 하교 남은 시간은 내려갈 수 있도록 빈 목록으로 넘긴다.
    // 빈 목록은 캐시하지 않아 다음 요청에서 다시 시도한다.
    @Cacheable(cacheNames = "neisSchedules", unless = "#result.isEmpty()")
    public List<ExternalSchedule> read(String atptCode, String schoolCode, LocalDate from, LocalDate to) {
        try {
            return neisScheduleClient.getSchedules(atptCode, schoolCode, from, to);
        } catch (BusinessException e) {
            log.warn("NEIS 학사일정을 불러오지 못해 방학, 시험 일정을 비웁니다.", e);
            return List.of();
        }
    }
}
