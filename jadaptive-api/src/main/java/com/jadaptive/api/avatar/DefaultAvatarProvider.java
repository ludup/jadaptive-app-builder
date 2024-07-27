package com.jadaptive.api.avatar;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.Html;

@Component
public class DefaultAvatarProvider implements AvatarProvider {

	@Override
	public Optional<Avatar> find(AvatarRequest request) {
		return Optional.of(() -> 
			Html.i("fa", "fa-user")
		);
	}

	@Override
	public int avatarWeight() {
		return Integer.MAX_VALUE;
	}

}
