package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.UserUpdateDto;
import com.spotseeker.copliot.exception.ResourceNotFoundException;
import com.spotseeker.copliot.model.User;
import com.spotseeker.copliot.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User updateProfile(Long userId, UserUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update email if provided
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        // Update mobile if name field is actually mobile (legacy compatibility)
        if (dto.getName() != null) {
            user.setMobile(dto.getName());
        }

        return userRepository.save(user);
    }
}
