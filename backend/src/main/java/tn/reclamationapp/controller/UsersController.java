package tn.reclamationapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.reclamationapp.dto.MessageResponse;
import tn.reclamationapp.model.Users;
import tn.reclamationapp.service.UsersService;

import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UsersController {
    
    @Autowired
    private UsersService usersService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Users>> getAllUsers() {
        return ResponseEntity.ok(usersService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return usersService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return usersService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody Users user, @RequestParam(required = false) Set<String> roles) {
        try {
            Users created = usersService.createUser(user, roles);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error creating user: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody Users user) {
        try {
            Users updated = usersService.updateUser(id, user);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            usersService.deleteUser(id);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> disableUser(@PathVariable String id) {
        try {
            Users disabled = usersService.disableUser(id);
            return ResponseEntity.ok(disabled);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> enableUser(@PathVariable String id) {
        try {
            Users enabled = usersService.enableUser(id);
            return ResponseEntity.ok(enabled);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable String id, @RequestParam String newPassword) {
        try {
            usersService.resetPassword(id, newPassword);
            return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> assignRoles(@PathVariable String id, @RequestBody Set<String> roleCodes) {
        try {
            Users updated = usersService.assignRoles(id, roleCodes);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Users>> searchUsers(@RequestParam String search) {
        return ResponseEntity.ok(usersService.searchUsers(search));
    }
}
