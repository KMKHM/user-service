package com.lucid.userservice.controller;

import com.lucid.userservice.config.jwt.TokenProvider;
import com.lucid.userservice.controller.request.SignupRequest;
import com.lucid.userservice.service.MemberService;
import com.lucid.userservice.service.response.EmailVerificationResponse;
import com.lucid.userservice.service.response.MemberResponse;
import com.lucid.userservice.util.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
//@RequestMapping("/")
public class MemberController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @GetMapping("/home")
    public String home() {
        return "hi";
    }

    @PostMapping("/sign-up")
    public ResponseEntity<MemberResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.signup(request.toServiceDto()));
    }

    @GetMapping("/info")
    public ResponseEntity<MemberResponse> info() {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.info());
    }

    @PatchMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String refreshToken = tokenProvider.resolveRefreshToken(request);
        String accessToken = tokenProvider.resolveAccessToken(request);
        memberService.logout(refreshToken, accessToken);
        return ResponseEntity.status(HttpStatus.OK).body("logout!!");
    }

    @PostMapping("/emails/verification-requests")
    public ResponseEntity sendEmail(@RequestParam("email") String email) {
        memberService.sendCode(email);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/emails/verify-code")
    public ApiResponse<EmailVerificationResponse> verifyCode(@RequestParam("email") String email,
                                                             @RequestParam("code") String code) {
        return memberService.verifyCode(email, code);
    }
}
