package com.hyunjun.backend.account.security;

import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email)  {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "계정을 찾을 수 없습니다."
                        ));

        return new AccountPrincipal(
                account.getId(),
                email,
                account.getPasswordHash()
        );
    }
}
