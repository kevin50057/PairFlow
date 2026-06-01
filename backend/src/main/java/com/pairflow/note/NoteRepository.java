package com.pairflow.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, String> {

    /**
     * Notes visible to {@code viewer}: everything they sent, plus everything addressed
     * to them that has already unlocked. Scheduled future letters stay hidden from the
     * receiver until their unlock time (spec 7.8 / 9.2 conditional visibility).
     */
    @Query("""
            select n from Note n
            where n.coupleId = :coupleId
              and (n.senderId = :viewer
                   or (n.receiverId = :viewer and (n.unlockTime is null or n.unlockTime <= :now)))
            order by n.createdAt desc
            """)
    List<Note> findVisible(@Param("coupleId") String coupleId,
                           @Param("viewer") String viewer,
                           @Param("now") Instant now);
}
