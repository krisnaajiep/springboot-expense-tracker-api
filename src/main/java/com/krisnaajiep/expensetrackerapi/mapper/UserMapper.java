package com.krisnaajiep.expensetrackerapi.mapper;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 27/06/25 04.33
@Last Modified 27/06/25 04.33
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.dto.request.RegisterRequestDto;
import com.krisnaajiep.expensetrackerapi.model.User;

public class UserMapper {
    public static User toUser(RegisterRequestDto registerRequestDto) {
        User user = new User();

        user.setName(registerRequestDto.getName().trim());
        user.setEmail(registerRequestDto.getEmail());
        user.setPassword(registerRequestDto.getPassword());

        return user;
    }
}
