package sn.suntelecoms.assurway.clearingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sn.suntelecoms.assurway.clearingapi.model.Privilege;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, UUID> {
    Optional<Privilege> findByCode(String code);
    Boolean existsByCode(String code);
    
    List<Privilege> findByParentIdIsNull();
    List<Privilege> findByParentId(UUID parentId);
    
    @Query("SELECT p FROM Privilege p LEFT JOIN FETCH p.children WHERE p.parentId IS NULL ORDER BY p.ordre")
    List<Privilege> findAllHierarchical();
    
    @Query("SELECT p FROM Privilege p WHERE p.isMenu = 'Y' ORDER BY p.ordre")
    List<Privilege> findAllMenuItems();
}