package notification.com.identity.feature.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.com.identity.domain.Authority;
import notification.com.identity.domain.User;
import notification.com.identity.domain.UserAuthority;
import notification.com.identity.feature.dto.user.UserCreateRequest;
import notification.com.identity.feature.dto.user.UserPasswordResetResponse;
import notification.com.identity.feature.dto.user.UserResponse;
import notification.com.identity.feature.mapper.UserMapper;
import notification.com.identity.feature.repository.AuthorityRepository;
import notification.com.identity.feature.repository.UserAuthorityRepository;
import notification.com.identity.feature.repository.UserRepository;
import notification.com.identity.utils.RandomTokenGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthorityRepository authorityRepository;
    private final UserAuthorityRepository userAuthorityRepository;

    @Override
    @Transactional
    public void createNewUser(UserCreateRequest userCreateRequest) {
        log.info("Creating new user: {}", userCreateRequest.username());

        this.existsByUsername(userCreateRequest.username());
        this.existsByEmail(userCreateRequest.email());

        User user = userMapper.fromUserCreationRequest(userCreateRequest);
        user.setUuid(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(userCreateRequest.password()));
        user.setProfileImage("default.png");
        user.setEmailVerified(false);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setIsEnabled(true);

        user = userRepository.save(user);

        UserAuthority defaultUserAuthority = new UserAuthority();
        defaultUserAuthority.setUser(user);
        defaultUserAuthority.setAuthority(authorityRepository.AUTH_USER());

        user.setUserAuthorities(new HashSet<>());
        user.getUserAuthorities().add(defaultUserAuthority);

        if (userCreateRequest.authorities() != null) {
            final User finalUser = user;
            Set<UserAuthority> customAuthorities = userCreateRequest
                    .authorities()
                    .stream()
                    .map(name -> {
                        Authority authority = authorityRepository
                                .findByName(name)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authority has not been found"));
                        UserAuthority userAuthority = new UserAuthority();
                        userAuthority.setUser(finalUser);
                        userAuthority.setAuthority(authority);
                        return userAuthority;
                    })
                    .collect(Collectors.toSet());
            user.getUserAuthorities().addAll(customAuthorities);
        }

        userAuthorityRepository.saveAll(user.getUserAuthorities());
        log.info("User created successfully: {}", userCreateRequest.username());
    }

    @Override
    public void isNotAuthenticated(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
    }

    @Override
    public UserResponse getAuthenticatedUser(Authentication authentication) {
        if (authentication != null) {
            return findByUsername(authentication.getName());
        }
        return null;
    }

    @Override
    @Transactional
    public UserPasswordResetResponse resetPassword(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String newPassword = RandomTokenGenerator.generate(8);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new UserPasswordResetResponse(newPassword);
    }

    @Override
    @Transactional
    public void enable(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User has not been found"));

        user.setIsEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void disable(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User has not been found"));

        user.setIsEnabled(false);
        userRepository.save(user);
    }

    @Override
    public Page<UserResponse> findList(int pageNumber, int pageSize) {
        log.info("Finding user list: page={}, size={}", pageNumber, pageSize);

        Sort sortByCreatedDate = Sort.by(Sort.Direction.DESC, "createdDate");
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sortByCreatedDate);

        Page<User> users = userRepository.findAll(pageRequest);
        return users.map(userMapper::toUserResponse);
    }

    @Override
    public UserResponse findByUsername(String username) {
        log.info("Finding user by username: {}", username);

        User user = userRepository.findByUsernameAndIsEnabledTrue(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User has not been found"));

        return userMapper.toUserResponse(user);
    }

    @Override
    public void checkForPasswords(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password doesn't match!");
        }
    }

    @Override
    public void checkTermsAndConditions(String value) {
        if (!value.equals("true") && !value.equals("false")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal value!");
        } else if (value.equals("false")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Terms and Conditions must be accepted in order to register!");
        }
    }

    @Override
    public void existsByUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists!");
        }
    }

    @Override
    public void existsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists!");
        }
    }

    @Override
    public void checkConfirmPasswords(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password doesn't match!");
        }
    }

    @Override
    @Transactional
    public void verifyEmail(User user) {
        user.setEmailVerified(true);
        log.info("User email has been verified for: {}", user.getUsername());
        userRepository.save(user);
    }

    @Override
    public void checkForOldPassword(String username, String oldPassword) {
        User user = userRepository.findByUsernameAndIsEnabledTrue(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User has not been found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong old password");
        }
    }
}

