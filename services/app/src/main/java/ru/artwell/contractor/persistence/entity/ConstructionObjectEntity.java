package ru.artwell.contractor.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "construction_objects")
public class ConstructionObjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object_code", nullable = false, unique = true, length = 128)
    private String objectCode;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @Column(name = "address", columnDefinition = "text")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private UserEntity customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id")
    private UserEntity contractor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id")
    private UserEntity designer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private UserEntity supervisor;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active")
    private Boolean active = true;

    protected ConstructionObjectEntity() {
    }

    public ConstructionObjectEntity(String objectCode,
                                    String objectName,
                                    String address,
                                    UserEntity customer,
                                    UserEntity contractor,
                                    UserEntity designer,
                                    UserEntity supervisor,
                                    String status) {
        this.objectCode = objectCode;
        this.objectName = objectName;
        this.address = address;
        this.customer = customer;
        this.contractor = contractor;
        this.designer = designer;
        this.supervisor = supervisor;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    // ─── Геттеры и сеттеры ───

    public String getObjectCode() { return objectCode; }
    public void setObjectCode(String objectCode) { this.objectCode = objectCode; }

    public String getObjectName() { return objectName; }
    public void setObjectName(String objectName) { this.objectName = objectName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public UserEntity getCustomer() { return customer; }
    public void setCustomer(UserEntity customer) { this.customer = customer; }

    public UserEntity getContractor() { return contractor; }
    public void setContractor(UserEntity contractor) { this.contractor = contractor; }

    public UserEntity getDesigner() { return designer; }
    public void setDesigner(UserEntity designer) { this.designer = designer; }

    public UserEntity getSupervisor() { return supervisor; }
    public void setSupervisor(UserEntity supervisor) { this.supervisor = supervisor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

