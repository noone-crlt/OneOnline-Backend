package com.thientri.book_area.mapper;

import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.thientri.book_area.dto.response.user.AddressResponse;
import com.thientri.book_area.dto.response.user.AuthResponse;
import com.thientri.book_area.model.user.Address;
import com.thientri.book_area.model.user.Role;
import com.thientri.book_area.model.user.User;

@Component
public class UserMapper {

	public AuthResponse toAuthResponse(User user, String accessToken, String refreshToken) {
		if (user == null)
			return null;

		return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).userId(user.getId())
				.email(user.getEmail()).fullName(user.getFullName()).phone(user.getPhone())
				.roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList())).build();
	}

	public AddressResponse toAddressResponse(Address address) {
		if (address == null)
			return null;

		return AddressResponse.builder().id(address.getId()).recipientName(address.getRecipientName())
				.recipientPhone(address.getRecipientPhone()).addressLine(address.getAddressLine())
				.provinceId(address.getProvinceId()).provinceName(address.getProvinceName())
				.districtId(address.getDistrictId()).districtName(address.getDistrictName())
				.wardId(address.getWardId()).wardName(address.getWardName())
				.isDefault(Boolean.TRUE.equals(address.getIsDefault())).build();
	}
}
