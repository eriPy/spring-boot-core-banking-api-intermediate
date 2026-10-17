package com.eripy.banking_system.entities.accounts;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "person_types")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class PersonType {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_type", insertable = false, updatable = false)
    @NotBlank(message = "The person type is required")
    private String personType;

    @Column(name = "salary_cap", insertable  = false, updatable = false)    
    @DecimalMin(value = "0.01", message = "The salary cap is required")
    private BigDecimal salaryCap;

    public PersonType(String personType, BigDecimal salaryCap) {
        this.personType = personType;
        this.salaryCap = salaryCap;
    }
}