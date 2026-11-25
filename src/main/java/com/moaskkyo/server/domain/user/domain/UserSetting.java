package com.moaskkyo.server.domain.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_setting", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_setting_user", columnNames = "user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_setting_id")
    private Long userSettingId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "birthday_public", nullable = false)
    private boolean birthdayPublic;

    @Column(name = "main_image")
    private String mainImage;

    @Builder
    private UserSetting(User user, boolean birthdayPublic, String mainImage) {
        this.user = user;
        this.birthdayPublic = birthdayPublic;
        this.mainImage = mainImage;
    }
}
