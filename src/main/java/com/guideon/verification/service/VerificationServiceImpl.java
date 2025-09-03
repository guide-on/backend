package com.guideon.verification.service;

import com.guideon.member.service.MemberService;
import com.guideon.verification.dto.VerificationVerifyDTO;
import com.guideon.verification.sender.EmailVerificationSender;
import com.guideon.verification.sender.SmsVerificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {
    private final EmailVerificationSender emailSender;
    private final SmsVerificationSender smsSender;
    private final MemberService memberService;

    @Override
    public void sendCode(String target, boolean isEmail) {
        if (isEmail) {
            emailSender.sendCode(target);
        } else {
            smsSender.sendCode(target);
        }
    }

    @Override
    public boolean verifyCode(VerificationVerifyDTO dto) {
        String target = dto.getTarget();
        String code = dto.getCode();
        boolean isEmail = dto.getIsEmail();
        return isEmail
                ? emailSender.verifyCode(target, code)
                : smsSender.verifyCode(target, code);
    }

    @Override
    public void sendCodeByEmailOrPhone(String email, boolean isEmail) {
        String target = isEmail
                ? email
                : memberService.getPhoneByEmail(email);  // 전화번호 조회
        sendCode(target, isEmail);
    }
}
