package com.thientri.book_area.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank(message = "Tên người nhận không được trống")
    private String recipientName;
    @NotBlank(message = "Số điện thoại không được trống")
    private String recipientPhone;
    @NotBlank(message = "Địa chỉ chi tiết không được trống")
    private String addressLine;
    
    // ID và Tên của Tỉnh/Huyện/Xã (Lấy từ API Giao Hàng)
    @NotBlank private String provinceId;
    @NotBlank private String provinceName;
    @NotBlank private String districtId;
    @NotBlank private String districtName;
    @NotBlank private String wardId;
    @NotBlank private String wardName;
    
    private Boolean isDefault;
}