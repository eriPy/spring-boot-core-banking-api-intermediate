package com.eripy.banking_system.repositories.accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountsType;
import com.eripy.banking_system.entities.accounts.PersonType;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountCard(String accountCard);

    boolean existsByEmail(String email);

    boolean existsByIdCard(String idCard);

    boolean existsByEmailAndIdCard(String email, String idCard);

    boolean existsByEmailAndIdCardAndAccountType(String email, String idCard, AccountsType accountType);

    boolean existsByEmailAndIdCardAndPersonType(String email, String idCard, PersonType personType);
}