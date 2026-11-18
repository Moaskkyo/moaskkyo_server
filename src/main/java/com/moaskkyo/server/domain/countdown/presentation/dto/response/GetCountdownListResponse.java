package com.moaskkyo.server.domain.countdown.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetCountdownListResponse {

    private final OffsetDateTime serverNow;
    private final List<CountdownSummaryResponse> countdowns;
}
