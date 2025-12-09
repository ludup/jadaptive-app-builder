package com.jadaptive.app.scheduler;

import java.time.Duration;

public class Jobs {

	public final static String formatDisplayDuration(Duration duration) {
		var ms = duration.toMillis();
		if(ms < 1000) {
			return String.format("%d ms", ms);
		}
		else {
			var secs = duration.toSeconds();
			if(secs < 60) {
				return String.format("%d seconds", secs);
			}
			else {
				var mins = duration.toMinutes();
				if(mins < 60) {
					return String.format("%d minutes", mins);
				}
				else {
					var hours = duration.toHours();
					if(hours < 24) {
						return String.format("%d hours %d minutes", hours, mins % 60);
					}
					else {
						var days = duration.toDays();
						return String.format("%d days %d hours", days, hours % 24);
					}
				}
			}
		}
	}
}
