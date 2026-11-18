package com.moaskkyo.server.domain.countdown.presentation.dto.response;

import com.moaskkyo.server.domain.countdown.domain.CountdownType;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CountdownSummaryResponse {

    private final CountdownType type;
    private final String title;
    private final OffsetDateTime targetAt;
}
