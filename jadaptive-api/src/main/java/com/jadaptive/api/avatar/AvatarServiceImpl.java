package com.jadaptive.api.avatar;

import static com.jadaptive.utils.Instrumentation.timed;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;

@Service
public class AvatarServiceImpl implements AvatarService {

	@Autowired
	private ApplicationService applicationService;

	@Override
	public List<Avatar> avatars(AvatarRequest request) {
		return streamAvs(request).toList();
	}

	@Override
	public Avatar avatar(AvatarRequest request) {
		return streamAvs(request).findFirst().orElseThrow(() -> new IllegalArgumentException("No avatar, not even the default."));
	}

	private Stream<Avatar> streamAvs(AvatarRequest request) {
		return applicationService.getBeans(AvatarProvider.class)
				.stream()
				.sorted((p1, p2) -> Integer.valueOf(p1.avatarWeight()).compareTo(p2.avatarWeight()))
				.map(prv -> findAv(request, prv))
				.filter(Optional::isPresent)
				.map(Optional::get);
	}

	private Optional<Avatar> findAv(AvatarRequest request, AvatarProvider prv) {
		try(var timer = timed("AvatarServiceImpl#findAv(" + request.email().orElse("<unknown>") + "," + prv.getClass().getSimpleName())) {
			return prv.find(request);
		}
	}
}
