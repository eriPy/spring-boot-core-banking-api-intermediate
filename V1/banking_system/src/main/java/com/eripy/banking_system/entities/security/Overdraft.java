package com.eripy.banking_system.entities.security;

import java.math.BigDecimal;
import com.eripy.banking_system.entities.accounts.PersonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "overdrafts")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class Overdraft {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_type_id", insertable = false, updatable = false)
    private PersonType personType;

    @Column(name = "overdraft_value", insertable = false, updatable = false, unique = true)
    private BigDecimal overdraftValue;
}
