package tn.reclamationapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.reclamationapp.model.Role;
import tn.reclamationapp.model.Users;
import tn.reclamationapp.repository.RoleRepository;
import tn.reclamationapp.repository.UsersRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UsersService {
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }
    
    public Optional<Users> getUserById(String id) {
        return usersRepository.findById(id);
    }
    
    public Optional<Users> getUserByEmail(String email) {
        return usersRepository.findByEmail(email);
    }
    
    public Users createUser(Users user, Set<String> roleCodes) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDefaultPassword(true);
        user.setDisabled(false);
        user.setNbrAttemps(0);
        
        if (roleCodes != null && !roleCodes.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleCode : roleCodes) {
                Role role = roleRepository.findByCodrole(roleCode)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleCode));
                roles.add(role);
            }
            user.setRoles(roles);
        }
        
        return usersRepository.save(user);
    }
    
    public Users updateUser(String id, Users userDetails) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        return usersRepository.save(user);
    }
    
    public void deleteUser(String id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        usersRepository.delete(user);
    }
    
    public Users disableUser(String id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setDisabled(true);
        return usersRepository.save(user);
    }
    
    public Users enableUser(String id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setDisabled(false);
        user.setNbrAttemps(0);
        return usersRepository.save(user);
    }
    
    public Users changePassword(String id, String oldPassword, String newPassword) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setDefaultPassword(false);
        return usersRepository.save(user);
    }
    
    public Users resetPassword(String id, String newPassword) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setDefaultPassword(true);
        return usersRepository.save(user);
    }
    
    public Users assignRoles(String id, Set<String> roleCodes) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        Set<Role> roles = new HashSet<>();
        for (String roleCode : roleCodes) {
            Role role = roleRepository.findByCodrole(roleCode)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleCode));
            roles.add(role);
        }
        
        user.setRoles(roles);
        return usersRepository.save(user);
    }
    
    public List<Users> searchUsers(String search) {
        return usersRepository.searchUsers(search);
    }
    
    public boolean existsByEmail(String email) {
        return usersRepository.existsByEmail(email);
    }
    
    public void incrementLoginAttempts(String email) {
        usersRepository.findByEmail(email).ifPresent(user -> {
            user.setNbrAttemps(user.getNbrAttemps() + 1);
            if (user.getNbrAttemps() >= 3) {
                user.setDisabled(true);
            }
            usersRepository.save(user);
        });
    }
    
    public void resetLoginAttempts(String email) {
        usersRepository.findByEmail(email).ifPresent(user -> {
            user.setNbrAttemps(0);
            usersRepository.save(user);
        });
    }
}
