package sn.suntelecoms.assurway.clearingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sn.suntelecoms.assurway.clearingapi.model.Role;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByAuthority(String authority);
    Boolean existsByAuthority(String authority);
    
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.privileges WHERE r.id = :id")
    Optional<Role> findByIdWithPrivileges(UUID id);
    
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.privileges WHERE r.authority = :authority")
    Optional<Role> findByAuthorityWithPrivileges(String authority);
}