package com.portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "WmsWarehouseDto")
public class WmsWarehouseDto {
    private Long warehouseId;
    private String city;
    private String country;
    private String address;
    private Short status;
}