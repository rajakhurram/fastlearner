package com.vinncorp.fast_learner.mock.role;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.role.Role;
import com.vinncorp.fast_learner.repositories.role.IRoleRepository;
import com.vinncorp.fast_learner.services.role.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoleServiceTest {

    @InjectMocks
    private RoleService roleService;

    @Mock
    private IRoleRepository roleRepo;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Get role when valid data provided")
    public void testGetByType_Success() throws EntityNotFoundException {
        String roleType = "ADMIN";
        Role role = new Role();
        role.setType(roleType);

        when(roleRepo.findByType(roleType)).thenReturn(Optional.of(role));

        Role result = roleService.getByType(roleType);

        assertNotNull(result);
        assertEquals(roleType, result.getType());
        verify(roleRepo).findByType(roleType);
    }

    @Test
    @DisplayName("Get role when invalid data provided")
    void testGetByType_RoleNotFound() {
        String roleType = "NON_EXISTENT_ROLE";

        when(roleRepo.findByType(anyString())).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            roleService.getByType(roleType);
        });

        assertEquals("No role found by type: " + roleType, thrown.getMessage());
        verify(roleRepo).findByType(roleType);
    }
}
