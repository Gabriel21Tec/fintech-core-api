package com.glr.fintech.fintech_core_api.infrastructure.persistence;

import com.glr.fintech.fintech_core_api.domain.Account;
import com.glr.fintech.fintech_core_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    Optional<Account> findByAccountNumber(String accountNumber);
}