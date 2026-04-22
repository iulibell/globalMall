package com.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@TableName("dictionary")
public class Dictionary {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Size(max = 50)
    @Schema(description = "字典类型")
    private String dictType;
    @Size(max = 500)
    @Schema(description = "字典名称")
    private String dictName;
    @Size(max = 50)
    @Schema(description = "字典值")
    private String dictValue;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "状态:0->禁用,1->启用(默认)")
    private Short status;
    @Schema(description = "语言: 1->cn中文,2->en英文,3->ru俄文")
    private String lang;
}
