package com.admin.service.impl;

import com.admin.dao.DictionaryDao;
import com.admin.dto.DictionaryDto;
import com.admin.entity.Dictionary;
import com.admin.service.DictionaryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryServiceImpl implements DictionaryService {
    @Resource
    private DictionaryDao dictionaryDao;

    @Override
    public List<DictionaryDto> getDictionary(int pageNum, int pageSize) {
        IPage<Dictionary> page = new Page<>(pageNum,pageSize);
        dictionaryDao.selectPage(page,null);
        return page.convert(dictionary -> {
            DictionaryDto dictionaryDto = new DictionaryDto();
            BeanUtils.copyProperties(dictionary,dictionaryDto);
            return dictionaryDto;
        }).getRecords();
    }
}
