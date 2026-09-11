package br.com.fwnet.timetracking.mapper;

import br.com.fwnet.timetracking.dto.response.UserResponse;
import br.com.fwnet.timetracking.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}