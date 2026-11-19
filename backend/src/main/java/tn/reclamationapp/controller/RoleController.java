package tn.reclamationapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.reclamationapp.dto.MessageResponse;
import tn.reclamationapp.model.Privilege;
import tn.reclamationapp.model.Role;
import tn.reclamationapp.service.RoleService;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasAuthority('ADMIN')")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{codrole}")
    public ResponseEntity<?> getRoleByCode(@PathVariable String codrole) {
        return roleService.getRoleByCode(codrole)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        Role created = roleService.createRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            Role updated = roleService.updateRole(id, role);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(new MessageResponse("Role deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}/privileges")
    public ResponseEntity<List<Privilege>> getPrivilegesByRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getPrivilegesByRole(id));
    }
    
    @PostMapping("/{roleId}/privileges")
    public ResponseEntity<?> addPrivilege(@PathVariable Long roleId, @RequestParam String codMenu) {
        try {
            Privilege privilege = roleService.addPrivilege(roleId, codMenu);
            return ResponseEntity.status(HttpStatus.CREATED).body(privilege);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/privileges/{privilegeId}")
    public ResponseEntity<?> removePrivilege(@PathVariable Long privilegeId) {
        roleService.removePrivilege(privilegeId);
        return ResponseEntity.ok(new MessageResponse("Privilege removed successfully"));
    }
    
    @DeleteMapping("/{roleId}/privileges")
    public ResponseEntity<?> removeAllPrivilegesForRole(@PathVariable Long roleId) {
        roleService.removeAllPrivilegesForRole(roleId);
        return ResponseEntity.ok(new MessageResponse("All privileges removed successfully"));
    }
}
