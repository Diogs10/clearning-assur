package sn.suntelecoms.assurway.clearingapi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.suntelecoms.assurway.clearingapi.model.Privilege;
import sn.suntelecoms.assurway.clearingapi.model.Role;
import sn.suntelecoms.assurway.clearingapi.model.User;
import sn.suntelecoms.assurway.clearingapi.repository.PrivilegeRepository;
import sn.suntelecoms.assurway.clearingapi.repository.RoleRepository;
import sn.suntelecoms.assurway.clearingapi.repository.UserRepository;

import java.util.*;

/**
 * Initialise les données de base au démarrage de l'application :
 * - Rôle ADMIN
 * - Utilisateur admin
 * - Privilèges de sécurité
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PrivilegeRepository privilegeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        Role adminRole = createAdminRoleIfNotExists();

        createPrivilegesIfNotExists();

        assignAllPrivilegesToAdmin(adminRole);

        createAdminUserIfNotExists(adminRole);

    }

    private Role createAdminRoleIfNotExists() {
        Optional<Role> existingRole = roleRepository.findByAuthority("ADMIN");
        
        if (existingRole.isPresent()) {
            return existingRole.get();
        }

        Role adminRole = new Role();
        adminRole.setAuthority("ADMIN");
        adminRole = roleRepository.save(adminRole);
        
        return adminRole;
    }

    private void createAdminUserIfNotExists(Role adminRole) {
        Optional<User> existingUser = userRepository.findByEmail("admin@example.com");
        
        if (existingUser.isPresent()) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin@example.com");
        admin.setEmail("admin@example.com");
        admin.setFirstName("admin");
        admin.setLastName("admin");
        admin.setDisplayName("admin admin");
        admin.setTelephone("770000000");
        admin.setPassword(passwordEncoder.encode("P@sser123"));
        admin.setEnabled(true);
        admin.setHasPasswordUpdate(false);
        
        admin.setRoles(new HashSet<>(Collections.singletonList(adminRole)));
        
        userRepository.save(admin);
        
    }

    private void createPrivilegesIfNotExists() {

        Privilege securite = createOrGetPrivilege(
            "01000", "Securite", 1, null, "lock", "oui", null, 1
        );

        Privilege utilisateur = createOrGetPrivilege(
            "01100", "Utilisateur", 2, "/admin/utilisateurs", "list", "oui", securite.getId(), 1
        );
        
        createOrGetPrivilege("01101", "Lister utilisateur", 3, null, null, "non", utilisateur.getId(), 1);
        createOrGetPrivilege("01102", "Ajouter utilisateur", 3, null, null, "non", utilisateur.getId(), 2);
        createOrGetPrivilege("01103", "Modifier utilisateur", 3, null, null, "non", utilisateur.getId(), 3);
        createOrGetPrivilege("01104", "Supprimer utilisateur", 3, null, null, "non", utilisateur.getId(), 4);

        Privilege role = createOrGetPrivilege(
            "01200", "Role", 2, "/admin/roles", "list", "oui", securite.getId(), 2
        );
        
        createOrGetPrivilege("01201", "Lister role", 3, null, null, "non", role.getId(), 1);
        createOrGetPrivilege("01202", "Ajouter role", 3, null, null, "non", role.getId(), 2);
        createOrGetPrivilege("01203", "Modifier role", 3, null, null, "non", role.getId(), 3);
        createOrGetPrivilege("01204", "Supprimer role", 3, null, null, "non", role.getId(), 4);
        createOrGetPrivilege("01205", "Affecter privilge", 3, null, null, "non", role.getId(), 5);
        createOrGetPrivilege("01206", "Dupliquer role", 3, null, null, "non", role.getId(), 6);

        Privilege privilege = createOrGetPrivilege(
            "01300", "Privilege", 2, "/admin/privileges", "list", "oui", securite.getId(), 3
        );
        
        createOrGetPrivilege("01301", "Lister privilege", 3, null, null, "non", privilege.getId(), 1);
        createOrGetPrivilege("01302", "Ajouter privilege", 3, null, null, "non", privilege.getId(), 2);
        createOrGetPrivilege("01303", "Modifier privilege", 3, null, null, "non", privilege.getId(), 3);
        createOrGetPrivilege("01304", "Supprimer privilege", 3, null, null, "non", privilege.getId(), 4);

    }

    private Privilege createOrGetPrivilege(String code, String libelle, int niveau, 
                                          String lien, String icon, String isMenu, 
                                          UUID parentId, int ordre) {
        Optional<Privilege> existing = privilegeRepository.findByCode(code);
        
        if (existing.isPresent()) {
            return existing.get();
        }

        Privilege privilege = new Privilege();
        privilege.setCode(code);
        privilege.setLibelle(libelle);
        privilege.setNiveau(niveau);
        privilege.setLien(lien);
        privilege.setIcon(icon);
        privilege.setIsMenu(isMenu);
        privilege.setParentId(parentId);
        privilege.setOrdre(ordre);
        
        privilege = privilegeRepository.save(privilege);
        
        return privilege;
    }

    private void assignAllPrivilegesToAdmin(Role adminRole) {
        List<Privilege> allPrivileges = privilegeRepository.findAll();
        
        if (allPrivileges.isEmpty()) {
            return;
        }

        adminRole = roleRepository.findByIdWithPrivileges(adminRole.getId())
                .orElse(adminRole);

        Set<Privilege> currentPrivileges = adminRole.getPrivileges();
        
        if (currentPrivileges.size() == allPrivileges.size()) {
            return;
        }

        adminRole.setPrivileges(new HashSet<>(allPrivileges));
        roleRepository.save(adminRole);
        
    }
}