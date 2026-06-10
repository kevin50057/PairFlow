package com.pairflow.user;

import com.pairflow.common.error.ApiException;
import com.pairflow.user.dto.UpdateUserRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }

    @Transactional
    public User update(String id, UpdateUserRequest req) {
        User user = getById(id);
        if (req.displayName() != null) user.setDisplayName(req.displayName().trim());
        if (req.avatarUrl() != null) user.setAvatarUrl(req.avatarUrl().isBlank() ? null : req.avatarUrl());
        if (req.birthday() != null) user.setBirthday(req.birthday());
        if (req.gender() != null) user.setGender(req.gender());
        if (req.bio() != null) user.setBio(req.bio().isBlank() ? null : req.bio().trim());
        if (req.timezone() != null) user.setTimezone(req.timezone());
        return user;
    }
}
