package com.techcourse.service;

import com.techcourse.db.InMemoryUserRepository;
import com.techcourse.model.User;

public class AuthService {

    public static User login(String account, String password) {
        User user = InMemoryUserRepository.findByAccount(account)
                .orElseThrow(IllegalArgumentException::new);

        if (!user.checkPassword(password)) {
            throw new IllegalArgumentException();
        }
        return user;
    }

    public static User register(String account, String password, String email) {
        User user = new User(account, password, email);
        InMemoryUserRepository.save(user);
        return user;
    }
}
