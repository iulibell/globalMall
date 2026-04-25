package com.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.admin.dao.DictionaryDao;
import com.admin.dto.DictionaryDto;
import com.admin.dto.DictionaryOperationDto;
import com.admin.entity.Dictionary;
import com.admin.service.DictionaryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DictionaryServiceImpl implements DictionaryService {
    @Resource
    private DictionaryDao dictionaryDao;

    @Override
    public List<DictionaryDto> getDictionary(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        IPage<Dictionary> page = new Page<>(pageNum,pageSize);
        dictionaryDao.selectPage(page,null);
        return page.convert(dictionary -> {
            DictionaryDto dictionaryDto = new DictionaryDto();
            BeanUtils.copyProperties(dictionary,dictionaryDto);
            return dictionaryDto;
        }).getRecords();
    }

    @Override
    public void addDictionary(List<DictionaryDto> dictionaryDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        for (DictionaryDto dto : dictionaryDto) {
            Dictionary entity = new Dictionary();
            BeanUtils.copyProperties(dto, entity);
            dictionaryDao.insert(entity);
        }
    }

    @Override
    public List<DictionaryDto> getDictionaryList(String dictType) {
        List<Dictionary> list = dictionaryDao.selectList(
                new LambdaQueryWrapper<Dictionary>()
                        .eq(Dictionary::getDictType, dictType)
                        .orderByAsc(Dictionary::getSort));
        return list.stream().map(dictionary -> {
            DictionaryDto dictionaryDto = new DictionaryDto();
            BeanUtils.copyProperties(dictionary, dictionaryDto);
            return dictionaryDto;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateDictionary(DictionaryOperationDto dictionaryOperationDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        LambdaQueryWrapper<Dictionary> w = new LambdaQueryWrapper<Dictionary>()
                .eq(Dictionary::getDictType, dictionaryOperationDto.getDictType())
                .eq(Dictionary::getDictValue, dictionaryOperationDto.getDictValue());
        if (StringUtils.hasText(dictionaryOperationDto.getLang())) {
            w.eq(Dictionary::getLang, dictionaryOperationDto.getLang());
        }
        Dictionary row = dictionaryDao.selectOne(w);
        if (row == null) {
            throw new IllegalArgumentException("dictionary entry not found for update");
        }
        row.setDictName(dictionaryOperationDto.getDictName());
        dictionaryDao.updateById(row);
    }

    @Override
    public void deleteDictionary(DictionaryOperationDto dictionaryOperationDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        LambdaQueryWrapper<Dictionary> w = new LambdaQueryWrapper<Dictionary>()
                .eq(Dictionary::getDictType, dictionaryOperationDto.getDictType())
                .eq(Dictionary::getDictValue, dictionaryOperationDto.getDictValue());
        if (StringUtils.hasText(dictionaryOperationDto.getLang())) {
            w.eq(Dictionary::getLang, dictionaryOperationDto.getLang());
        }
        dictionaryDao.delete(w);
    }
}
