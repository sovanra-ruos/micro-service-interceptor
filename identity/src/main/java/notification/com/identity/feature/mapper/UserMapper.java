package notification.com.identity.feature.mapper;

import notification.com.identity.domain.User;
import notification.com.identity.feature.dto.RegisterRequest;
import notification.com.identity.feature.dto.user.UserCreateRequest;
import notification.com.identity.feature.dto.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "authorities", expression = "java(mapAuthorities(user))")
    UserResponse toUserResponse(User user);

    UserCreateRequest mapRegisterRequestToUserCreationRequest(RegisterRequest registerRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "isEnabled", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "userAuthorities", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "ipAddress", ignore = true)
    User fromUserCreationRequest(UserCreateRequest userCreateRequest);

    default Set<String> mapAuthorities(User user) {
        if (user.getUserAuthorities() == null) {
            return Set.of();
        }
        return user.getUserAuthorities().stream()
                .map(ua -> ua.getAuthority().getName())
                .collect(Collectors.toSet());
    }
}

