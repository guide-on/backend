package com.guideon.member.controller;

import com.guideon.member.dto.MemberDTO;
import com.guideon.member.dto.MemberJoinDTO;
import com.guideon.member.service.MemberService;
import com.guideon.security.util.LoginUserProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Api(tags = "회원관리 API", description = "회원 관련 기능 제공")
public class MemberController {
    private final MemberService service;
    private final LoginUserProvider loginUserProvider;

    @PostMapping("")
    @ApiOperation(value = "회원가입")
    public ResponseEntity<MemberDTO> join(@RequestBody MemberJoinDTO member) {
        return ResponseEntity.ok(service.join(member));
    }
}
