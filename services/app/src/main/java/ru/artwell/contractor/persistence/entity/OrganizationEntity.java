package ru.artwell.contractor.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "organizations")
public class OrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_name", nullable = false)
    private String orgName;

    @Column(name = "org_short_name")
    private String orgShortName;

    @Column(name = "org_type", length = 64)
    private String orgType;

    @Column(name = "inn", unique = true, length = 32)
    private String inn;

    @Column(name = "kpp", length = 32)
    private String kpp;

    @Column(name = "legal_address", columnDefinition = "text")
    private String legalAddress;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected OrganizationEntity() {
    }

    public OrganizationEntity(String orgName,
                              String orgShortName,
                              String orgType,
                              String inn,
                              String kpp,
                              String legalAddress,
                              boolean active) {
        this.orgName = orgName;
        this.orgShortName = orgShortName;
        this.orgType = orgType;
        this.inn = inn;
        this.kpp = kpp;
        this.legalAddress = legalAddress;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgShortName() { return orgShortName; }
    public void setOrgShortName(String orgShortName) { this.orgShortName = orgShortName; }

    public String getOrgType() { return orgType; }
    public void setOrgType(String orgType) { this.orgType = orgType; }

    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }

    public String getKpp() { return kpp; }
    public void setKpp(String kpp) { this.kpp = kpp; }

    public String getLegalAddress() { return legalAddress; }
    public void setLegalAddress(String legalAddress) { this.legalAddress = legalAddress; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
