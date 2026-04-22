package com.system.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * Sa-Token {@code adminInfo} 会话快照；{@code userId} 使用字符串避免前端 JSON 数字精度问题。
 */
@Data
public class UserSessionDto {

    private static final String K_USER_ID = "userId";
    private static final String K_USERNAME = "username";
    private static final String K_USER_TYPE = "userType";
    private static final String K_NICKNAME = "nickname";
    private static final String K_PHONE = "phone";

    private String userId;
    private String username;
    private String userType;
    /** 展示名；登录响应里若为空则回退为 username */
    private String nickname;
    private String phone;

    /** 供 {@code session.set(adminInfo, …)} 使用 */
    public static Map<String, Object> toSessionMap(UserSessionDto dto) {
        Map<String, Object> m = new LinkedHashMap<>(8);
        if (StrUtil.isNotBlank(dto.getUserId())) {
            m.put(K_USER_ID, dto.getUserId());
        }
        if (dto.getUsername() != null) {
            m.put(K_USERNAME, dto.getUsername());
        }
        if (dto.getUserType() != null) {
            m.put(K_USER_TYPE, dto.getUserType());
        }
        if (dto.getNickname() != null) {
            m.put(K_NICKNAME, dto.getNickname());
        }
        if (StrUtil.isNotBlank(dto.getPhone())) {
            m.put(K_PHONE, dto.getPhone());
        }
        return m;
    }

    /**
     * 从 Session 取值解析；兼容历史 {@link Map}（含数字型 userId）、以及曾误入 Session 的 {@link SysUserDto}。
     */
    public static UserSessionDto fromSessionValue(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof UserSessionDto d) {
            return d;
        }
        if (raw instanceof SysUserDto legacy) {
            return fromLogiSysUserDto(legacy);
        }
        if (!(raw instanceof Map<?, ?> map)) {
            return null;
        }
        return fromMap(map);
    }

    private static UserSessionDto fromLogiSysUserDto(SysUserDto legacy) {
        UserSessionDto dto = new UserSessionDto();
        Long uid = legacy.getUserId();
        if (uid != null) {
            dto.setUserId(String.valueOf(uid));
        }
        dto.setUsername(legacy.getUsername());
        dto.setUserType(legacy.getUserType());
        dto.setNickname(legacy.getNickname());
        return dto;
    }

    private static UserSessionDto fromMap(Map<?, ?> map) {
        UserSessionDto dto = new UserSessionDto();
        Object uid = map.get(K_USER_ID);
        if (uid instanceof Number n) {
            dto.setUserId(String.valueOf(n.longValue()));
        } else if (uid != null) {
            String s = uid.toString();
            if (StrUtil.isNotBlank(s)) {
                dto.setUserId(s);
            }
        }
        Object un = map.get(K_USERNAME);
        if (un != null) {
            dto.setUsername(un.toString());
        }
        Object ut = map.get(K_USER_TYPE);
        if (ut != null) {
            dto.setUserType(ut.toString());
        }
        Object nn = map.get(K_NICKNAME);
        if (nn != null && StrUtil.isNotBlank(nn.toString())) {
            dto.setNickname(nn.toString());
        }
        Object ph = map.get(K_PHONE);
        if (ph != null && StrUtil.isNotBlank(ph.toString())) {
            dto.setPhone(ph.toString());
        }
        return dto;
    }
}
