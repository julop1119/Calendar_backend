package com.example.calendar.common;

/**
 * 쪽지 배송 수단별 평균 속도(km/h). 비행기는 일부러 비현실적으로 빠르게 잡아
 * "1km면 0.1초" 같은 장난스러운 체감을 만든다.
 */
public enum TransportMode {

    WALK(6.0),
    BUS(30.0),
    SUBWAY(40.0),
    PLANE(36_000.0);

    private final double speedKmh;

    TransportMode(double speedKmh) {
        this.speedKmh = speedKmh;
    }

    public double speedKmh() {
        return speedKmh;
    }
}
