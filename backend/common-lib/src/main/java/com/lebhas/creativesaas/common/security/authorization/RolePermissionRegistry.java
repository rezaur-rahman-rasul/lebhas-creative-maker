package com.lebhas.creativesaas.common.security.authorization;

import com.lebhas.creativesaas.common.security.Permission;
import com.lebhas.creativesaas.common.security.Role;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class RolePermissionRegistry {

    private final Map<Role, Set<Permission>> permissionsByRole = new EnumMap<>(Role.class);

    public RolePermissionRegistry() {
        permissionsByRole.put(Role.MASTER, EnumSet.copyOf(Arrays.asList(Permission.values())));
        permissionsByRole.put(Role.ADMIN, EnumSet.of(
                Permission.USER_VIEW,
                Permission.USER_CREATE,
                Permission.USER_UPDATE,
                Permission.USER_STATUS_UPDATE,
                Permission.CREW_INVITE,
                Permission.WORKSPACE_VIEW,
                Permission.CREATIVE_GENERATE,
                Permission.SESSION_MANAGE));
        permissionsByRole.put(Role.CREW, EnumSet.of(
                Permission.WORKSPACE_VIEW,
                Permission.CREATIVE_GENERATE));
    }

    public Set<Permission> resolve(Set<Role> roles) {
        EnumSet<Permission> resolved = EnumSet.noneOf(Permission.class);
        roles.forEach(role -> resolved.addAll(permissionsByRole.getOrDefault(role, Set.of())));
        return Set.copyOf(resolved);
    }
}
