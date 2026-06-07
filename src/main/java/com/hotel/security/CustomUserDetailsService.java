package com.hotel.security;

import com.hotel.dao.UserDao;
import com.hotel.model.User;
import com.hotel.model.enums.UserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.RECEPTIONIST) {
            throw new UsernameNotFoundException("User is not authorized to access this application");
        }

        return new HotelUserDetails(user);
    }
}
