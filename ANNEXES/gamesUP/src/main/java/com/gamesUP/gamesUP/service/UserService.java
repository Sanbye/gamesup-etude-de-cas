package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.user.UserRegisterRequest;
import com.gamesUP.gamesUP.dto.user.UserResponse;
import com.gamesUP.gamesUP.dto.user.UserRoleUpdateRequest;
import com.gamesUP.gamesUP.dto.user.UserUpdateRequest;
import java.util.List;

public interface UserService {

    List<UserResponse> findAll();

    UserResponse findById(Long id);

    UserResponse register(UserRegisterRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    UserResponse updateRole(Long id, UserRoleUpdateRequest request);

    void delete(Long id);
}
