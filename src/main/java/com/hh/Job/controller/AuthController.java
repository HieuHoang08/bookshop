package com.hh.Job.controller;

import com.hh.Job.domain.User;
import com.hh.Job.domain.request.ReqLoginDTO;
import com.hh.Job.domain.response.ResLoginDTO;
import com.hh.Job.service.UserService;
import com.hh.Job.util.SecurityUtil;
import com.hh.Job.util.annotation.APImessage;
import com.hh.Job.util.error.IdInvalidException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil, UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @Value("${hh.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO reqLoginDto) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(reqLoginDto.getUsername(), reqLoginDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(token);

        // create token
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        User crUserDb = userService.handleGetUserByUsername(reqLoginDto.getUsername());

        if (crUserDb == null) {
            // nếu user không tồn tại => trả lỗi
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                crUserDb.getId(),
                crUserDb.getName(),
                crUserDb.getEmail());
        resLoginDTO.setUserLogin(userLogin);

        String access_token = this.securityUtil.createAccessToken(authentication.getName(), resLoginDTO.getUserLogin());
        resLoginDTO.setAccessToken(access_token);

        // create refresh token
        String refresh_token = this.securityUtil.createRefreshToken(reqLoginDto.getUsername(), resLoginDTO);
        resLoginDTO.setRefreshToken(refresh_token);

        // update user
        this.userService.updateUserToken(reqLoginDto.getUsername(), refresh_token);

        // set cookies
        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(resLoginDTO);
    }

    @GetMapping("/auth/account")
    @APImessage("fetch account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        User crrUserDB = userService.handleGetUserByUsername(email);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAcc = new ResLoginDTO.UserGetAccount();

        if (crrUserDB != null) {
            userLogin.setId(crrUserDB.getId());
            userLogin.setEmail(crrUserDB.getEmail());
            userLogin.setUsername(crrUserDB.getName());
            userGetAcc.setUser(userLogin);
        }
        return ResponseEntity.ok().body(userGetAcc);
    }

    @GetMapping("/auth/refresh")
    @APImessage("Get User by refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "hh") String refresh_token) throws IdInvalidException {
        if (refresh_token.equals("hh")) {
            throw new IdInvalidException("ban khong co refresh token o day");
        }

        // check valid
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
        String email = decodedToken.getSubject();

        // check user by token + email
        User crrUser = this.userService.getUserByRefreshTokenAndEmail(refresh_token, email);
        if (crrUser == null) {
            throw new IdInvalidException("Token khong hop le");
        }

        // issue new token/set refresh token as cookies
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        User crUserDb = userService.handleGetUserByUsername(email);

        if (crUserDb == null) {
            // nếu user không tồn tại => trả lỗi
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(crUserDb.getId(), crUserDb.getName(), crUserDb.getEmail());
        resLoginDTO.setUserLogin(userLogin);

        String access_token = this.securityUtil.createAccessToken(email, resLoginDTO.getUserLogin());
        resLoginDTO.setAccessToken(access_token);

        // create refresh token
        String new_refresh_token = this.securityUtil.createRefreshToken(email, resLoginDTO);
        resLoginDTO.setRefreshToken(new_refresh_token); // FIX: Set new refresh token instead of old one

        // update user
        this.userService.updateUserToken(email, new_refresh_token); // FIX: Update with new refresh token

        // set cookies
        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", new_refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(resLoginDTO);
    }

    @PostMapping("/auth/logout")
    @APImessage("ban da logout thanh cong")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        if (email.equals("")) {
            throw new IdInvalidException(" access token khong hop le");
        }

        // update refresh token = null
        this.userService.updateUserToken(email, "");

        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(null);
    }
}
