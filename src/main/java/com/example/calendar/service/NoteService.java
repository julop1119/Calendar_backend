package com.example.calendar.service;

import com.example.calendar.common.NoteStatus;
import com.example.calendar.domain.Note;
import com.example.calendar.domain.User;
import com.example.calendar.dto.NoteResponse;
import com.example.calendar.dto.NoteSendRequest;
import com.example.calendar.dto.NoteTrackingResponse;
import com.example.calendar.dto.UserSummaryResponse;
import com.example.calendar.exception.ForbiddenException;
import com.example.calendar.repository.NoteRepository;
import com.example.calendar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class NoteService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double LOST_PROBABILITY = 0.0001; // 0.01%

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Transactional
    public NoteResponse send(Long senderId, NoteSendRequest request) {
        if (request.recipientId().equals(senderId)) {
            throw new IllegalArgumentException("자기 자신에게는 쪽지를 보낼 수 없어요");
        }
        User sender = getUserWithLocation(senderId, "먼저 내 위치를 공유해주세요");
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new IllegalArgumentException("받는 사람을 찾을 수 없습니다"));
        if (!recipient.hasLocation()) {
            throw new IllegalArgumentException("상대방이 아직 위치를 공유하지 않았어요");
        }

        double distanceKm = haversineKm(
                sender.getLatitude(), sender.getLongitude(),
                recipient.getLatitude(), recipient.getLongitude());
        long durationMillis = Math.max(1, Math.round(distanceKm / request.transportMode().speedKmh() * 3_600_000));
        boolean lost = ThreadLocalRandom.current().nextDouble() < LOST_PROBABILITY;

        Note note = Note.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.content().trim())
                .transportMode(request.transportMode())
                .originLatitude(sender.getLatitude())
                .originLongitude(sender.getLongitude())
                .destinationLatitude(recipient.getLatitude())
                .destinationLongitude(recipient.getLongitude())
                .distanceKm(distanceKm)
                .durationMillis(durationMillis)
                .sentAt(LocalDateTime.now())
                .lost(lost)
                .build();

        return toResponse(noteRepository.save(note), senderId);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> sent(Long userId) {
        return noteRepository.findBySenderIdOrderBySentAtDesc(userId).stream()
                .map(note -> toResponse(note, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> received(Long userId) {
        return noteRepository.findByRecipientIdOrderBySentAtDesc(userId).stream()
                .map(note -> toResponse(note, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteTrackingResponse tracking(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("쪽지를 찾을 수 없습니다: " + noteId));
        if (!note.getSender().getId().equals(userId) && !note.getRecipient().getId().equals(userId)) {
            throw new ForbiddenException("이 쪽지를 볼 권한이 없습니다");
        }

        Progress progress = computeProgress(note);
        Double currentLat = null;
        Double currentLng = null;
        if (progress.status() == NoteStatus.IN_TRANSIT) {
            currentLat = lerp(note.getOriginLatitude(), note.getDestinationLatitude(), progress.fraction());
            currentLng = lerp(note.getOriginLongitude(), note.getDestinationLongitude(), progress.fraction());
        }
        long totalSeconds = Math.max(1, note.getDurationMillis() / 1000);
        long elapsedSeconds = Math.min(totalSeconds, progress.elapsedMillis() / 1000);
        long remainingSeconds = Math.max(0, totalSeconds - elapsedSeconds);

        return new NoteTrackingResponse(
                note.getId(),
                note.getTransportMode(),
                progress.status(),
                note.getDistanceKm(),
                progress.fraction() * 100,
                elapsedSeconds,
                remainingSeconds,
                currentLat,
                currentLng,
                note.getOriginLatitude(),
                note.getOriginLongitude(),
                note.getDestinationLatitude(),
                note.getDestinationLongitude()
        );
    }

    private NoteResponse toResponse(Note note, Long viewerId) {
        Progress progress = computeProgress(note);
        boolean isSender = note.getSender().getId().equals(viewerId);
        boolean contentVisible = progress.status() == NoteStatus.DELIVERED
                || (isSender && progress.status() == NoteStatus.IN_TRANSIT);

        return new NoteResponse(
                note.getId(),
                UserSummaryResponse.from(note.getSender()),
                UserSummaryResponse.from(note.getRecipient()),
                note.getTransportMode(),
                note.getDistanceKm(),
                note.getSentAt(),
                progress.status(),
                progress.fraction() * 100,
                contentVisible ? note.getContent() : null
        );
    }

    private Progress computeProgress(Note note) {
        long elapsedMillis = Math.max(0, Duration.between(note.getSentAt(), LocalDateTime.now()).toMillis());
        double fraction = Math.min(1.0, (double) elapsedMillis / note.getDurationMillis());
        NoteStatus status = elapsedMillis >= note.getDurationMillis()
                ? (note.isLost() ? NoteStatus.LOST : NoteStatus.DELIVERED)
                : NoteStatus.IN_TRANSIT;
        return new Progress(status, fraction, elapsedMillis);
    }

    private record Progress(NoteStatus status, double fraction, long elapsedMillis) {
    }

    private double lerp(double from, double to, double fraction) {
        return from + (to - from) * fraction;
    }

    private User getUserWithLocation(Long userId, String errorMessage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + userId));
        if (!user.hasLocation()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return user;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
