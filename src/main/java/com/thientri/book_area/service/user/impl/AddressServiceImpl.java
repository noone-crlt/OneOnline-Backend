package com.thientri.book_area.service.user.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thientri.book_area.dto.request.user.AddressRequest;
import com.thientri.book_area.dto.response.user.AddressResponse;
import com.thientri.book_area.exception.BadRequestException;
import com.thientri.book_area.exception.ResourceNotFoundException;
import com.thientri.book_area.mapper.UserMapper;
import com.thientri.book_area.model.user.Address;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.user.AddressRepository;
import com.thientri.book_area.service.user.IAddressService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements IAddressService {

	private final AddressRepository addressRepository;
	private final UserMapper userMapper;

	@Override
	public List<AddressResponse> getAddresses(User user) {
		return addressRepository.findByUserId(user.getId()).stream().map(userMapper::toAddressResponse).toList();
	}

	@Override
	@Transactional
	public AddressResponse addAddress(User user, AddressRequest request) {
		// Nếu đây là địa chỉ đầu tiên, tự động đặt làm mặc định
		List<Address> existing = addressRepository.findByUserId(user.getId());
		boolean shouldBeDefault = existing.isEmpty() || Boolean.TRUE.equals(request.getIsDefault());

		if (shouldBeDefault) {
			clearDefaultForUser(user.getId());
		}

		Address address = Address.builder().user(user).recipientName(request.getRecipientName().trim())
				.recipientPhone(request.getRecipientPhone().trim()).addressLine(request.getAddressLine().trim())
				.provinceId(request.getProvinceId()).provinceName(request.getProvinceName())
				.districtId(request.getDistrictId()).districtName(request.getDistrictName())
				.wardId(request.getWardId()).wardName(request.getWardName()).isDefault(shouldBeDefault).build();

		return userMapper.toAddressResponse(addressRepository.save(address));
	}

	@Override
	@Transactional
	public AddressResponse updateAddress(User user, Long addressId, AddressRequest request) {
		Address address = findOwnedAddress(user, addressId);

		address.setRecipientName(request.getRecipientName().trim());
		address.setRecipientPhone(request.getRecipientPhone().trim());
		address.setAddressLine(request.getAddressLine().trim());
		address.setProvinceId(request.getProvinceId());
		address.setProvinceName(request.getProvinceName());
		address.setDistrictId(request.getDistrictId());
		address.setDistrictName(request.getDistrictName());
		address.setWardId(request.getWardId());
		address.setWardName(request.getWardName());

		if (Boolean.TRUE.equals(request.getIsDefault())) {
			clearDefaultForUser(user.getId());
			address.setIsDefault(true);
		}

		return userMapper.toAddressResponse(addressRepository.save(address));
	}

	@Override
	@Transactional
	public void deleteAddress(User user, Long addressId) {
		Address address = findOwnedAddress(user, addressId);
		boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
		addressRepository.delete(address);

		// Nếu xóa địa chỉ mặc định, đặt địa chỉ đầu tiên còn lại làm mặc định
		if (wasDefault) {
			List<Address> remaining = addressRepository.findByUserId(user.getId());
			if (!remaining.isEmpty()) {
				remaining.get(0).setIsDefault(true);
				addressRepository.save(remaining.get(0));
			}
		}
	}

	@Override
	@Transactional
	public AddressResponse setDefault(User user, Long addressId) {
		Address address = findOwnedAddress(user, addressId);
		clearDefaultForUser(user.getId());
		address.setIsDefault(true);
		return userMapper.toAddressResponse(addressRepository.save(address));
	}

	// ============= Helper methods =============

	private Address findOwnedAddress(User user, Long addressId) {
		Address address = addressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: " + addressId));

		if (!address.getUser().getId().equals(user.getId())) {
			throw new BadRequestException("Bạn không có quyền thao tác trên địa chỉ này.");
		}

		return address;
	}

	private void clearDefaultForUser(Long userId) {
		addressRepository.findByUserId(userId).forEach(a -> {
			if (Boolean.TRUE.equals(a.getIsDefault())) {
				a.setIsDefault(false);
				addressRepository.save(a);
			}
		});
	}
}
