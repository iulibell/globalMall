package com.system.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.constant.AuthConstant;
import com.common.exception.Assert;
import com.system.dao.SysUserDao;
import com.system.dto.LoginParamDto;
import com.system.dto.UserSessionDto;
import com.system.entity.SysUser;
import com.system.service.CacheService;
import com.system.service.LoginAndLogoutService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LoginAndLogoutServiceImpl implements LoginAndLogoutService {

    @Resource
    private StpLogic stpLogic;

    private static final Map<String, String> LOGI_ALIAS_TO_ROLE;
    private static final Map<String, List<String>> PERMISSIONS_BY_ROLE;

    static {
        Map<String, String> mallAlias = new HashMap<>();
        putMall(mallAlias, "1", "super");
        putMall(mallAlias, "2", "manager");
        putMall(mallAlias, "3", "merchant");
        putMall(mallAlias, "4", "user");
        putMall(mallAlias, "5", "reviewer");
        LOGI_ALIAS_TO_ROLE = Collections.unmodifiableMap(mallAlias);

        PERMISSIONS_BY_ROLE = Map.of(
                "super", List.of("super", "manager","reviewer"),
                "manager", List.of("manager"),
                "keeper", List.of("merchant"),
                "driver", List.of("user"),
                "reviewer", List.of("reviewer"));
    }

    private static void putMall(Map<String, String> aliasToRole, String storedDigit, String roleKey) {
        aliasToRole.put(storedDigit, roleKey);
        aliasToRole.put(roleKey, roleKey);
    }

    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private CacheService cacheService;

    /**
     * 注册等场景写入的 userType 可能为 1–5，权限与登录入口校验统一成英文角色 key。
     */
    public static String normalizeUserTypeToRoleKey(String userType) {
        String t = trimOrNull(userType);
        return t == null ? null : LOGI_ALIAS_TO_ROLE.get(t);
    }

    private static String trimOrNull(String userType) {
        if (StrUtil.isBlank(userType)) {
            return null;
        }
        return userType.trim();
    }

    private static String logiStpLoginId(SysUser user) {
        if (StrUtil.isNotBlank(user.getUserId())) {
            try {
                return user.getUserId().trim();
            } catch (NumberFormatException ignored) {
                Assert.fail("login_stp_error");
            }
        }
        Assert.fail("login_account_data_error");
        return "";
    }

    @Override
    @SuppressWarnings("null")
    public SaTokenInfo login(LoginParamDto loginParamDto, String requiredRoleKey) {
        if (StrUtil.isEmpty(loginParamDto.getUsername()) || StrUtil.isEmpty(loginParamDto.getPassword())) {
            Assert.fail("login_account_or_password_empty");
        }
        if (StrUtil.isBlank(requiredRoleKey)) {
            Assert.fail("login_role_invalid");
        }

        List<SysUser> candidates = sysUserDao.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginParamDto.getUsername()));
        if (candidates == null || candidates.isEmpty()) {
            Assert.fail("login_account_not_found");
        }
        SysUser sysUser = null;
        for (SysUser candidate : candidates) {
            if (requiredRoleKey.equals(normalizeUserTypeToRoleKey(candidate.getUserType()))) {
                sysUser = candidate;
                break;
            }
        }
        // 选择了不匹配的登录身份（或库中 userType 异常）时，给出明确业务提示，而非 MyBatis 多结果异常
        if (sysUser == null) {
            Assert.fail("validation_usertype_invalid");
        }
        String storedHash = sysUser.getPassword();
        if (StrUtil.isEmpty(storedHash)) {
            Assert.fail("login_account_data_error");
        }
        // 哈希非合法 BCrypt 时 checkpw 抛 IllegalArgumentException，若未捕获会变成 HTTP 500，前端只能看到「内部错误」
        try {
            if (!BCrypt.checkpw(loginParamDto.getPassword(), storedHash)) {
                Assert.fail("login_password_wrong");
            }
        } catch (IllegalArgumentException ex) {
            log.warn("密码字段非合法 BCrypt 哈希，按密码错误处理 user={}", loginParamDto.getUsername());
            Assert.fail("login_password_wrong");
        }
        if (!Objects.equals(sysUser.getStatus(), (short) 1)) {
            Assert.fail("login_account_disabled");
        }
        String actualRole = normalizeUserTypeToRoleKey(sysUser.getUserType());
        if (actualRole == null) {
            Assert.fail("login_account_type_invalid");
        }
        if (!requiredRoleKey.equals(actualRole)) {
            Assert.fail("validation_usertype_invalid");
        }

        String userId = logiStpLoginId(sysUser);
        stpLogic.login(userId);

        UserSessionDto sessionDto = new UserSessionDto();
        sessionDto.setUserId(userId);
        sessionDto.setUsername(sysUser.getUsername());
        sessionDto.setUserType(sysUser.getUserType());
        sessionDto.setNickname(sysUser.getNickname());
        sessionDto.setPhone(sysUser.getPhone());

        stpLogic.getSession().set(AuthConstant.STP_ADMIN_INFO, UserSessionDto.toSessionMap(sessionDto));

        List<String> permissions = getPermissionsByRoleKey(actualRole);
        stpLogic.getSession().set("permissions", permissions);
        cacheService.setLoginStatus(userId, sysUser, TimeUnit.HOURS);
        log.info("登录成功，用户ID：{}，角色：{}", userId, actualRole);
        return stpLogic.getTokenInfo();
    }

    @Override
    public Map<String, Object> buildLoginResponse(SaTokenInfo saTokenInfo, String tokenPrefix) {
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("token", saTokenInfo.getTokenValue());
        tokenMap.put("tokenHead", tokenPrefix.trim() + " ");
        UserSessionDto dto = UserSessionDto.fromSessionValue(stpLogic.getSession().get(AuthConstant.STP_ADMIN_INFO));
        String displayName = "";
        String resolvedRoleKey = null;
        if (dto != null) {
            displayName = StrUtil.blankToDefault(StrUtil.nullToEmpty(dto.getNickname()), dto.getUsername());
            if (StrUtil.isNotBlank(dto.getUserId())) {
                tokenMap.put("userId", dto.getUserId());
            }
            tokenMap.put("username", dto.getUsername());
            resolvedRoleKey = normalizeUserTypeToRoleKey(dto.getUserType());
            if (resolvedRoleKey != null) {
                tokenMap.put("role", resolvedRoleKey);
            }
        }
        tokenMap.put("nickname", displayName);
        @SuppressWarnings("unchecked")
        List<String> perms = (List<String>) stpLogic.getSession().get("permissions");
        if (perms == null || perms.isEmpty()) {
            if (resolvedRoleKey != null) {
                perms = getPermissionsByRoleKey(resolvedRoleKey);
            } else {
                perms = Collections.emptyList();
            }
        }
        tokenMap.put("permissions", perms);
        return tokenMap;
    }

    @Override
    public void logout() {
        UserSessionDto sessionDto = UserSessionDto.fromSessionValue(stpLogic.getSession().get(AuthConstant.STP_ADMIN_INFO));
        if (sessionDto != null && StrUtil.isNotBlank(sessionDto.getUserId())) {
            try {
                cacheService.removeLoginStatus(Long.parseLong(sessionDto.getUserId().trim()));
            } catch (NumberFormatException ignored) {
                /* userId 非数字则跳过缓存清理 */
            }
        }
        stpLogic.logout();
    }

    private List<String> getPermissionsByRoleKey(String roleKey) {
        return PERMISSIONS_BY_ROLE.getOrDefault(roleKey, Collections.emptyList());
    }
}
