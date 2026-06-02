package com.pairflow.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {

    List<UserDevice> findByUserId(String userId);

    Optional<UserDevice> findByUserIdAndFcmToken(String userId, String fcmToken);

    @Modifying
    @Query("delete from UserDevice d where d.fcmToken = :token")
    int deleteByFcmToken(@Param("token") String token);
}
