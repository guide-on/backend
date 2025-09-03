package com.guideon.common.redis;

public class RedisKeyUtil {
    public static String sms(String phone) {
        return "sms:" + phone;
    }

    public static String email(String email) {
        return "email:" + email;
    }

    public static String resend(String type) {
        return "resend:" + type;
    }

    public static String verified(String type) {
        return "verified:" + type;
    }

    public static String refreshToken(Long memberId) {
        return "RT:" + memberId;
    }
}
