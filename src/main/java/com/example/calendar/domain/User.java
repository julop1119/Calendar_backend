package com.example.calendar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String googleId; // Google OAuth sub 필드

    @Column(nullable = false)
    private String email;

    private String name;
    private String profileImageUrl;

    private Double latitude;
    private Double longitude;
    private LocalDateTime locationUpdatedAt;

    @OneToMany(mappedBy = "user")
    private List<CalendarGroupMember> groupMemberships = new ArrayList<>();

    @Builder
    public User(String googleId, String email, String name, String profileImageUrl) {
        this.googleId = googleId;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    // 이름이나 프로필 사진 변경 시 업데이트 로직
    public void update(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    // 쪽지 기능에서 내 위치를 공유/갱신할 때 사용
    public void updateLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationUpdatedAt = LocalDateTime.now();
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
}
