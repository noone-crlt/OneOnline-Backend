package com.thientri.book_area.model.catalog;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Bắt buộc phải có tên
	@Column(name = "name", length = 255, nullable = false)
	private String name;

	@Column(name = "bio", length = 4000)
	private String bio;

	@Column(name = "avatar", length = 500)
	private String avatar;

	@ManyToMany(mappedBy = "authors")
	@JsonIgnore
	@Builder.Default
	private Set<Book> books = new HashSet<>();
}
