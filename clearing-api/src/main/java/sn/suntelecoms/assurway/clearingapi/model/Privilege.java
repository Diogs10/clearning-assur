package sn.suntelecoms.assurway.clearingapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "privileges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Privilege {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String libelle;

    @Column(nullable = false)
    private Integer niveau;

    @Column(unique = true, nullable = false)
    private String code;

    private String lien;

    private String icon;

    @Column(nullable = false)
    private String isMenu = "N";

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false)
    private Integer ordre = 0;

    @JsonIgnore
    @ManyToMany(mappedBy = "privileges")
    private Set<Role> roles = new HashSet<>();

    // Relations parent-enfant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @JsonIgnore
    private Privilege parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Privilege> children = new HashSet<>();
}