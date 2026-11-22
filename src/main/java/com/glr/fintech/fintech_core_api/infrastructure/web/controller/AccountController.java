package com.glr.fintech.fintech_core_api.infrastructure.web.controller;

import com.glr.fintech.fintech_core_api.domain.Account;
import com.glr.fintech.fintech_core_api.domain.User;
import com.glr.fintech.fintech_core_api.infrastructure.persistence.AccountRepository;
import com.glr.fintech.fintech_core_api.infrastructure.persistence.UserRepository;
import com.glr.fintech.fintech_core_api.infrastructure.web.dto.AccountResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountController(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    // Crear una nueva cuenta para el usuario autenticado
    @PostMapping
    public ResponseEntity<?> createAccount(Authentication authentication, @RequestParam(defaultValue = "CLP") String currency) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar un número de cuenta único aleatorio
        String accountNumber = "CLP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Account newAccount = Account.builder()
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .currency(currency)
                .user(user)
                .build();

        accountRepository.save(newAccount);

        return ResponseEntity.ok("¡Cuenta creada exitosamente con número: " + accountNumber + "!");
    }

    // Listar las cuentas del usuario autenticado
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getUserAccounts(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Account> accounts = accountRepository.findByUser(user);

        List<AccountResponse> response = accounts.stream().map(acc -> AccountResponse.builder()
                .id(acc.getId())
                .accountNumber(acc.getAccountNumber())
                .balance(acc.getBalance())
                .currency(acc.getCurrency())
                .build()).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}