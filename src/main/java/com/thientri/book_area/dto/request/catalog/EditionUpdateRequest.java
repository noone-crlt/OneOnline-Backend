package com.thientri.book_area.dto.request.catalog;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class EditionUpdateRequest {
	private BigDecimal originalPrice;
	private BigDecimal salePrice;
	private Integer stock;
	private Integer duration;
	private List<Long> narratorIds;

}
