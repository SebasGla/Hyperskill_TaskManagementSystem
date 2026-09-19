package taskmanagement.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import taskmanagement.user.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repository;


    public UserDetailsServiceImpl(UserRepository repository, PasswordEncoder encoder){
        this.repository = repository;

    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        repository.findUserByEmail(email).orElseThrow(() ->new UsernameNotFoundException("Email not found!"));
        return null;
    }

}
