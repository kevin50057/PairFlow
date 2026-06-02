package com.pairflow.user;

import com.pairflow.config.CurrentUser;
import com.pairflow.notification.UserDevice;
import com.pairflow.notification.UserDeviceRepository;
import com.pairflow.notification.dto.RegisterDeviceRequest;
import com.pairflow.user.dto.UpdateUserRequest;
import com.pairflow.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserDeviceRepository deviceRepository;

    public UserController(UserService userService, UserDeviceRepository deviceRepository) {
        this.userService = userService;
        this.deviceRepository = deviceRepository;
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(userService.getById(CurrentUser.id()));
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateUserRequest req) {
        return UserResponse.from(userService.update(CurrentUser.id(), req));
    }

    /** Register or refresh an FCM device token for push notifications. */
    @PostMapping("/me/device")
    public Map<String, Object> registerDevice(@Valid @RequestBody RegisterDeviceRequest req) {
        String userId = CurrentUser.id();
        deviceRepository.findByUserIdAndFcmToken(userId, req.fcmToken()).ifPresentOrElse(
                existing -> existing.setPlatform(req.platform()),
                () -> {
                    UserDevice device = new UserDevice();
                    device.setUserId(userId);
                    device.setFcmToken(req.fcmToken());
                    device.setPlatform(req.platform());
                    deviceRepository.save(device);
                });
        return Map.of("ok", true);
    }

    /** Unregister a device token (on logout or token refresh by the client). */
    @DeleteMapping("/me/device")
    public Map<String, Object> unregisterDevice(@RequestParam String fcmToken) {
        deviceRepository.deleteByFcmToken(fcmToken);
        return Map.of("ok", true);
    }
}
