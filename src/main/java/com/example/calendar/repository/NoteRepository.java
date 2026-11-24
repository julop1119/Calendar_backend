package com.example.calendar.repository;

import com.example.calendar.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findBySenderIdOrderBySentAtDesc(Long senderId);

    List<Note> findByRecipientIdOrderBySentAtDesc(Long recipientId);
}
