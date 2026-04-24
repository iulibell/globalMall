package com.portal.dto;

import lombok.Data;

@Data
public class WmsWarehouseDto {
    private Long warehouseId;
    private String city;
    private String country;
    private String address;
    private Short status;
}