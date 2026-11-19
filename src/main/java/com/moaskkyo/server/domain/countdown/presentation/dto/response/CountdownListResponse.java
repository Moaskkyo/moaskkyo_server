package com.moaskkyo.server.domain.countdown.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CountdownListResponse {

    private final OffsetDateTime serverNow;
    private final List<CountdownResponse> countdowns;
}
