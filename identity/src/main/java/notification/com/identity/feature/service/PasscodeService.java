package notification.com.identity.feature.service;

import notification.com.identity.domain.Passcode;
import notification.com.identity.domain.User;

public interface PasscodeService {
    void generate(User user);
    boolean isExpired(Passcode passcode);
}