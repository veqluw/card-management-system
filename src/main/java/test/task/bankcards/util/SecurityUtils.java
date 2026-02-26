package test.task.bankcards.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import test.task.bankcards.entity.User;
import test.task.bankcards.security.UserDetailsImpl;

@Component
public class SecurityUtils {

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(132);
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            throw new RuntimeException("No authenticated user");
        }
        return ((UserDetailsImpl) auth.getPrincipal()).user();
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
