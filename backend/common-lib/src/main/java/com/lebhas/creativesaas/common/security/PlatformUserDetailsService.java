package com.lebhas.creativesaas.common.security;

import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import com.lebhas.creativesaas.common.security.authorization.RolePermissionRegistry;
import com.lebhas.creativesaas.identity.infrastructure.persistence.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolePermissionRegistry rolePermissionRegistry;

    public PlatformUserDetailsService(UserRepository userRepository, RolePermissionRegistry rolePermissionRegistry) {
        this.userRepository = userRepository;
        this.rolePermissionRegistry = rolePermissionRegistry;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmailIgnoreCaseAndDeletedFalse(username)
                .map(user -> PlatformUserDetails.from(user, rolePermissionRegistry))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
    }
}
