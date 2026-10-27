package com.eripy.banking_system.entities.security;

import java.time.LocalDateTime;
import com.eripy.banking_system.entities.accounts.User;
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
@Table(name = "black_list")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class BlackList {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", updatable = false, nullable = false, unique = true)
    private User user;

    @Column(name = "adding_to", insertable = false, updatable = false)
    private LocalDateTime addingTo;

    public BlackList(
        User user
    ) {
        this.user = user;
    }
}
