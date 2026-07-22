package com.thientri.book_area.service.user;

import java.util.List;

import com.thientri.book_area.dto.request.user.AddressRequest;
import com.thientri.book_area.dto.response.user.AddressResponse;
import com.thientri.book_area.model.user.User;

public interface IAddressService {
	List<AddressResponse> getAddresses(User user);

	AddressResponse addAddress(User user, AddressRequest request);

	AddressResponse updateAddress(User user, Long addressId, AddressRequest request);

	void deleteAddress(User user, Long addressId);

	AddressResponse setDefault(User user, Long addressId);
}
