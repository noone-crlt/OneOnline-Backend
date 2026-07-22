package com.thientri.book_area.controller.auth;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thientri.book_area.dto.request.user.AddressRequest;
import com.thientri.book_area.dto.response.ApiResponse;
import com.thientri.book_area.dto.response.user.AddressResponse;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.service.user.IAddressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/addresses")
@RequiredArgsConstructor
public class AddressController {

	private final IAddressService addressService;

	@GetMapping
	public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(ApiResponse.success(addressService.getAddresses(user)));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<AddressResponse>> addAddress(@AuthenticationPrincipal User user,
			@Valid @RequestBody AddressRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Thêm địa chỉ thành công!", addressService.addAddress(user, request)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(@AuthenticationPrincipal User user,
			@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
		return ResponseEntity
				.ok(ApiResponse.success("Cập nhật địa chỉ thành công!", addressService.updateAddress(user, id, request)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteAddress(@AuthenticationPrincipal User user,
			@PathVariable Long id) {
		addressService.deleteAddress(user, id);
		return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ thành công!", null));
	}

	@PatchMapping("/{id}/default")
	public ResponseEntity<ApiResponse<AddressResponse>> setDefault(@AuthenticationPrincipal User user,
			@PathVariable Long id) {
		return ResponseEntity
				.ok(ApiResponse.success("Đặt địa chỉ mặc định thành công!", addressService.setDefault(user, id)));
	}
}
