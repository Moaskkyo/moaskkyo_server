package com.moaskkyo.server.domain.countdown.service;

import com.moaskkyo.server.global.client.ExternalSchedule;
import com.moaskkyo.server.global.client.neis.NeisScheduleClient;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

// 같은 학교, 같은 기간이면 NEIS를 다시 부르지 않도록 캐시를 둔다. (만료는 application.yaml 설정)
@Component
@RequiredArgsConstructor
public class SchoolScheduleReader {

    private final NeisScheduleClient neisScheduleClient;

    @Cacheable(cacheNames = "neisSchedules")
    public List<ExternalSchedule> read(String atptCode, String schoolCode, LocalDate from, LocalDate to) {
        try {
            return neisScheduleClient.getSchedules(atptCode, schoolCode, from, to);
        } catch (BusinessException e) {
            throw new BusinessException(ErrorCode.NEIS_API_ERROR);
        }
    }
}
