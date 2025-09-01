package notification.com.identity.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.com.identity.domain.Passcode;
import notification.com.identity.domain.User;
import notification.com.identity.repository.PasscodeRepository;
import notification.com.identity.utils.RandomTokenGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasscodeServiceImpl implements PasscodeService {

    private final PasscodeRepository passcodeRepository;

    @Override
    @Transactional
    public void generate(User user) {
        String token = RandomTokenGenerator.generate(6);

        Passcode passcode = new Passcode();
        passcode.setUser(user);
        passcode.setToken(token);
        passcode.setCreatedAt(LocalDateTime.now());
        passcode.setExpiresAt(LocalDateTime.now().plusMinutes(15)); // 15 minutes expiry
        passcode.setIsValidated(true); // Auto-validate for simplicity

        passcodeRepository.save(passcode);

        log.info("Password reset token generated for user: {}", user.getUsername());
        // In a real application, you would send this token via email or SMS
        log.info("Reset token: {}", token);
    }

    @Override
    public boolean isExpired(Passcode passcode) {
        return passcode.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
