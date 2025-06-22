package com.boardtest.dreamchaser.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.LockedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public User create(String username, String password, String nickname) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setCreateDate(LocalDateTime.now());
        user.setRole(UserRole.USER);
        this.userRepository.save(user);
        return user;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> _user = this.userRepository.findByUsername(username);
        if (_user.isEmpty()) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }
        User user = _user.get();


        if (user.isPermanentlyBanned()) {
            throw new LockedException("영구적으로 차단된 계정입니다.");
        }
        if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
            throw new LockedException("일시적으로 차단된 계정입니다. 차단 해제일: " + user.getBannedUntil().toLocalDate());
        }


        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() == UserRole.ADMIN) {
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getKey()));
        } else {
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.getKey()));
        }


        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }


    public User getUser(String username) {
        Optional<User> user = this.userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new RuntimeException("User not found for username: " + username);
        }
    }


    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found for id: " + id));
    }


    public void delete(User user) {
        this.userRepository.delete(user);
    }




    public Page<User> findUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }


    public void banTemporarily(User user, int days) {
        user.setBannedUntil(LocalDateTime.now().plusDays(days));
        userRepository.save(user);
    }


    public void banPermanently(User user) {
        user.setPermanentlyBanned(true);
        userRepository.save(user);
    }

    // 사용자 차단 해제
    public void unban(User user) {
        user.setPermanentlyBanned(false);
        user.setBannedUntil(null);
        userRepository.save(user);
    }
}