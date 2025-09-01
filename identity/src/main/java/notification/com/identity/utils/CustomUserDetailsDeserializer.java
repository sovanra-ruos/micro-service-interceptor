package notification.com.identity.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import lombok.extern.slf4j.Slf4j;
import notification.com.identity.config.CustomUserDetails;
import notification.com.identity.domain.User;
import notification.com.identity.domain.UserAuthority;

import java.io.IOException;
import java.util.Set;

@Slf4j
public class CustomUserDetailsDeserializer extends JsonDeserializer<CustomUserDetails> {

    private static final TypeReference<Set<UserAuthority>> USER_AUTHORITY_SET = new TypeReference<>() {};

    @Override
    public CustomUserDetails deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode jsonNode = mapper.readTree(p);

        JsonNode userJsonNode = readJsonNode(jsonNode, "user");

        Set<UserAuthority> userAuthorities = mapper.convertValue(
                jsonNode.get("userAuthorities"), USER_AUTHORITY_SET);

        Long id = readJsonNode(userJsonNode, "id").asLong();
        String uuid = readJsonNode(userJsonNode, "uuid").asText();
        String username = readJsonNode(userJsonNode, "username").asText();
        String email = readJsonNode(userJsonNode, "email").asText();
        String password = readJsonNode(userJsonNode, "password").asText();
        boolean isEnabled = readJsonNode(userJsonNode, "isEnabled").asBoolean();
        boolean accountNonExpired = readJsonNode(userJsonNode, "accountNonExpired").asBoolean();
        boolean credentialsNonExpired = readJsonNode(userJsonNode, "credentialsNonExpired").asBoolean();
        boolean accountNonLocked = readJsonNode(userJsonNode, "accountNonLocked").asBoolean();

        String familyName = readJsonNode(userJsonNode, "familyName").asText();
        String givenName = readJsonNode(userJsonNode, "givenName").asText();
        String profileImage = readJsonNode(userJsonNode, "profileImage").asText();
        boolean emailVerified = readJsonNode(userJsonNode, "emailVerified").asBoolean();

        User user = new User();
        user.setId(id);
        user.setUuid(uuid);
        user.setUsername(username);
        user.setUserAuthorities(userAuthorities);
        user.setEmail(email);
        user.setPassword(password);
        user.setFamilyName(familyName);
        user.setGivenName(givenName);
        user.setProfileImage(profileImage);
        user.setEmailVerified(emailVerified);
        user.setIsEnabled(isEnabled);
        user.setCredentialsNonExpired(credentialsNonExpired);
        user.setAccountNonLocked(accountNonLocked);
        user.setAccountNonExpired(accountNonExpired);

        CustomUserDetails customUserDetails = new CustomUserDetails();
        customUserDetails.setUser(user);

        return customUserDetails;
    }

    private JsonNode readJsonNode(JsonNode jsonNode, String field) {
        return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
    }
}
