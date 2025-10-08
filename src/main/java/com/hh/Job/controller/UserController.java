package com.hh.Job.controller;

import com.hh.Job.domain.User;
import com.hh.Job.domain.response.user.ResCreateUserDTO;
import com.hh.Job.domain.response.user.ResUpdateUserDTO;
import com.hh.Job.domain.response.user.ResUserDTO;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.user.UserDTO;
import com.hh.Job.service.UserService;
import com.hh.Job.util.annotation.APImessage;
import com.hh.Job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService,  PasswordEncoder passwordEncoder) {

        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping("/users")
    @APImessage("creat a new user")
    public ResponseEntity<ResCreateUserDTO> createUser(@Valid @RequestBody User user)

            throws  IdInvalidException {

        boolean isEmailExit = this.userService.isEmailExists(user.getEmail());
        if (isEmailExit) {
            throw new IdInvalidException(
                    "Email" + user.getEmail() + " da ton tai, vui long xu dung email khac");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        user.setCreatedAt(Instant.now());

        User hhUser = this.userService.handleCreateUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToRestCreateUserDTO(hhUser));
    }

    @DeleteMapping("/users/{id}")
    @APImessage("Delete a user")
    public ResponseEntity<Object> deleteUser( @PathVariable("id") Long id)
            throws IdInvalidException {
        User crrentUser = this.userService.fetchUserById(id);
        if(crrentUser == null) {
            throw new IdInvalidException("User voi id " + id + " khong ton tai ");
        }
        this.userService.handleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("null");
    }

    @GetMapping("/users/{id}")
    @APImessage("fetch user by id")

    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserDTOById(id);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/users")
    @APImessage("Update a user")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User user)
            throws IdInvalidException {
        User h2User = this.userService.handleUpdateUser(user);
        if(h2User == null) {
            throw new IdInvalidException("User voi id " + user.getId() + " khong ton tai");
        }

        return ResponseEntity.ok(this.userService.convertToRestUpdateUserDTO(h2User));
    }


    // phan trang voi pageAble
//    @GetMapping("/users")
//    public ResponseEntity<ResultPaginationDTO> getAllUsers(
//            @RequestParam ("current") Optional<String> currentOptional,
//            @RequestParam("pageSize") Optional<String> pageSizeOptional
//            ){
//
//        String sCurrentPage = currentOptional.isPresent() ? currentOptional.get() : "";
//        String sPageSize = pageSizeOptional.isPresent() ? pageSizeOptional.get() : "";
//
//        int crr = Integer.parseInt(sCurrentPage);
//        int page = Integer.parseInt(sPageSize);
//        Pageable pageable = PageRequest.of(crr - 1, page);
//        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchAllUser(pageable));
//    }

    // phan trang specification(filter)
    @GetMapping("/users")
    @APImessage("fetch all user")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec,
            Pageable pageable
    ){
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchAllUser( spec,  pageable));
    }
}
