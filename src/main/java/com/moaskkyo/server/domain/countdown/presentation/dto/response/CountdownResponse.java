package com.moaskkyo.server.domain.countdown.presentation.dto.response;

import com.moaskkyo.server.domain.countdown.domain.CountdownType;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CountdownResponse {

    private final CountdownType type;
    private final String title;
    private final OffsetDateTime targetAt;

    public static CountdownResponse empty(CountdownType type, String title) {
        return CountdownResponse.builder()
                .type(type)
                .title(title)
                .build();
    }
}
