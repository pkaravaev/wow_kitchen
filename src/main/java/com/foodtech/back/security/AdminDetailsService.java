package com.foodtech.back.security;

import com.foodtech.back.entity.auth.Admin;
import com.foodtech.back.entity.auth.AuthorizedAdmin;
import com.foodtech.back.repository.auth.AdminRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public AdminDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Admin> adminOpt = adminRepository.findByNameEquals(username);

        if (adminOpt.isEmpty()) {
            throw new UsernameNotFoundException("Admin with name " + username + " not found");
        }

        Admin admin = adminOpt.get();
        return new AuthorizedAdmin(admin.getName(), admin.getPassword(), admin.getRoles());
    }
}
