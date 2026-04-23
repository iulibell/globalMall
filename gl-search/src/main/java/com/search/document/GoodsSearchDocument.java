package com.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 与门户 {@code portal_goods} 可检索字段对齐的 ES 文档；由业务侧入库/上架后同步写入。
 */
@Data
@Document(indexName = "mall-goods")
public class GoodsSearchDocument {

    @Id
    private String goodsId;

    @Field(type = FieldType.Text)
    private String merchantId;

    @Field(type = FieldType.Keyword)
    private String skuName;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Text)
    private String picture;

    @Field(type = FieldType.Integer)
    private Integer category;

    @Field(type = FieldType.Double)
    private Double price;

    /** 与库表一致：1 已入库可展示，检索默认只返回该状态 */
    @Field(type = FieldType.Integer)
    private Integer status;
}
