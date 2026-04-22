package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictionaryOperationDto {
    @NotBlank(message = "字典类型不能为空")
    private String dictType;
    @NotBlank(message = "字典名字不能为空")
    private String dictName;
    @NotBlank(message = "字典值不能为空")
    private String dictValue;
    /**
     * 语言：1 中文 2 英文 3 俄文 4 日文（与 dictionary.lang 一致）。传入时按 dictType+dictValue+lang 定位行，dictName 作为要写入的新文案（见 {@link com.admin.service.impl.DictionaryServiceImpl#updateDictionary}）。
     */
    private String lang;
}
