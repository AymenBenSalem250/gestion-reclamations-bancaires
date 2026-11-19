package tn.reclamationapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.reclamationapp.model.Menu;
import tn.reclamationapp.model.Privilege;
import tn.reclamationapp.model.Role;
import tn.reclamationapp.repository.MenuRepository;
import tn.reclamationapp.repository.PrivilegeRepository;
import tn.reclamationapp.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PrivilegeRepository privilegeRepository;
    
    @Autowired
    private MenuRepository menuRepository;
    
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }
    
    public Optional<Role> getRoleByCode(String codrole) {
        return roleRepository.findByCodrole(codrole);
    }
    
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }
    
    public Role updateRole(Long id, Role roleDetails) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        
        role.setLibrole(roleDetails.getLibrole());
        role.setCodrole(roleDetails.getCodrole());
        
        return roleRepository.save(role);
    }
    
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        roleRepository.delete(role);
    }
    
    public List<Privilege> getPrivilegesByRole(Long roleId) {
        return privilegeRepository.findByRole_Id(roleId);
    }
    
    public Privilege addPrivilege(Long roleId, String codMenu) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        Menu menu = menuRepository.findByCodmenu(codMenu)
                .orElseThrow(() -> new RuntimeException("Menu not found"));
        
        Privilege privilege = new Privilege();
        privilege.setRole(role);
        privilege.setMenu(menu);
        
        return privilegeRepository.save(privilege);
    }
    
    public void removePrivilege(Long privilegeId) {
        privilegeRepository.deleteById(privilegeId);
    }
    
    public void removeAllPrivilegesForRole(Long roleId) {
        privilegeRepository.deleteByRole_Id(roleId);
    }
}
