package com.academiq.dto.utilisateur;

import com.academiq.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {

    @NotNull
    private Role role;

    private String motif;
}
