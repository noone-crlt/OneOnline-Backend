package com.thientri.book_area.service.engagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thientri.book_area.dto.response.engagement.UserLibraryResponse;
import com.thientri.book_area.mapper.EngagementMapper;
import com.thientri.book_area.model.user.User;
import com.thientri.book_area.repository.engagement.UserLibraryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LibraryService {
	private final UserLibraryRepository libraryRepository;
	private final EngagementMapper engagementMapper;

	@Transactional(readOnly = true)
	public Page<UserLibraryResponse> getLibrary(User user, int page, int size) {
		return libraryRepository.findByUserIdOrderByAcquiredAtDesc(user.getId(), PageRequest.of(page, size))
				.map(item -> engagementMapper.toUserLibraryResponse(item,
						item.getProgress() != null ? item.getProgress() : 0));
	}

	@Transactional
	public void saveProgress(User user, Long editionId, int progress) {
		libraryRepository.findByUserIdAndEditionId(user.getId(), editionId).ifPresent(item -> {
			item.setProgress(progress);
			libraryRepository.save(item);
		});
	}
}
