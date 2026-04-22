package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictionaryDto {
    @NotBlank(message = "字典类型不能为空")
    private String dictType;
    @NotBlank(message = "字典名称不能为空")
    private String dictName;
    @NotBlank(message = "字典值不能为空")
    private String dictValue;
    @NotBlank(message = "排序不能为空")
    private Integer sort;
    @NotBlank(message = "状态不能为空")
    private Short status;
    @NotBlank(message = "语言不能为空")
    private String lang;
}
