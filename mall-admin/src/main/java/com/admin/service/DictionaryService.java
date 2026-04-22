package com.admin.service;

import com.admin.dto.DictionaryDto;
import com.admin.dto.DictionaryOperationDto;

import java.util.List;

public interface DictionaryService {
    List<DictionaryDto> getDictionary(int pageNum, int pageSize);

    /**
     * 添加字典(super操作)
     * @param dictionaryDto 字典dto
     */
    void addDictionary(List<DictionaryDto> dictionaryDto);

    /**
     * 获取字典列表(前端调用)
     * @param dictType 字典类型
     * @return 字典列表
     */
    List<DictionaryDto> getDictionaryList(String dictType);

    /**
     * 更新字典信息(super操作)
     * @param dictionaryOperationDto 字典操作dto
     */
    void updateDictionary(DictionaryOperationDto dictionaryOperationDto);

    /**
     * 删除字典(super操作)
     * @param dictionaryOperationDto 字典操作dto
     */
    void deleteDictionary(DictionaryOperationDto dictionaryOperationDto);
}
