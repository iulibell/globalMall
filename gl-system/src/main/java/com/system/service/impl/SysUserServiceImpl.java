package com.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.system.dao.SysUserDao;
import com.system.dto.SysUserDto;
import com.system.entity.SysUser;
import com.system.service.SysUserService;
import jakarta.annotation.Resource;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Resource
    private SysUserDao sysUserDao;

    @Override
    public List<SysUserDto> fetchSysUserInfo(int pageNum, int pageSize) {
        IPage<SysUser> page = new Page<>(pageNum, pageSize);
        sysUserDao.selectPage(page,null);
        return page.convert(sysUser -> {
            SysUserDto sysUserDto = new SysUserDto();
            BeanUtil.copyProperties(sysUser,sysUserDto);
            return sysUserDto;
        }).getRecords();
    }

    @Override
    public List<SysUserDto> fetchSysUserByUserType(int pageNum, int pageSize, String userType) {
        IPage<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> userPage = sysUserDao.selectPage(page,new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserType,userType));
        return userPage.convert(user -> {
            SysUserDto dto = new SysUserDto();
            BeanUtil.copyProperties(user, dto);
            return dto;
        }).getRecords();
    }

    @Override
    public SysUserDto fetchSysUserByUserId(String userId) {
        SysUser sysUser = sysUserDao.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserId,userId));
        if (sysUser == null) {
            return null;
        }
        SysUserDto sysUserDto = new SysUserDto();
        BeanUtil.copyProperties(sysUser,sysUserDto);
        return sysUserDto;
    }

    @Override
    public void updateSysUserInfo(SysUserDto sysUserDto) {
        SysUser sysUser = new SysUser();
        BeanUtil.copyProperties(sysUserDto,sysUser);
        sysUserDao.update(sysUser,new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getUserId,sysUserDto.getUserId()));
    }

    @Override
    public void deleteSysUser(String userId) {
        sysUserDao.delete(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserId,userId));
    }
}
