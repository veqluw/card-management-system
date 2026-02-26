package test.task.bankcards.exception;

import test.task.bankcards.util.enums.RoleType;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(RoleType role) {
        super("Role not found: " + role);
    }
}
