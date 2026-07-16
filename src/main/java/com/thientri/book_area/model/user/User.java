package com.thientri.book_area.model.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.thientri.book_area.model.order.CartItem;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter // Chỉ dùng Getter/Setter, tuyệt đối bỏ @Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "email", length = 255, nullable = false, unique = true)
	private String email;

	@Column(name = "password", length = 255, nullable = false)
	private String password;

	@Column(name = "full_name", length = 255)
	private String fullName;

	@Column(name = "phone", length = 20)
	private String phone;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	// Map chính xác cột status với Enum, báo cho JPA biết để lưu dưới dạng chuỗi
	// (String)
	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20)
	@Builder.Default
	private UserStatus status = UserStatus.ACTIVE;

	@Builder.Default
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	@Builder.Default
	@OneToMany(mappedBy = "user")
	private List<Address> addresses = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "user")
	private List<RefreshToken> refreshTokens = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartItem> cartItems = new ArrayList<>();

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList();
	}

	@Override
	public String getUsername() {
		return getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// Tài khoản bị khóa (BANNED) sẽ không thể đăng nhập
		return this.status != UserStatus.BANNED;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		// Chỉ tài khoản ACTIVE hoặc INACTIVE mới được coi là tồn tại, DELETED thì chặn
		// hoàn toàn
		return this.status != UserStatus.DELETED;
	}
}
