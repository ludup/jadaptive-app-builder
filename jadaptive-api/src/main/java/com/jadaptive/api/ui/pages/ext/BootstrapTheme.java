package com.jadaptive.api.ui.pages.ext;

public enum BootstrapTheme {

	DEFAULT,
	JADAPTIVE,
	CERULEAN,
	COSMO,
	CYBORG,
	DARKLY,
	FLATLY,
	JOURNAL,
	LITERA,
	LUMEN,
	LUX,
	MATERIA,
	MINTY,
	MORPH,
	PULSE,
	QUARTZ,
	SANDSTONE,
	SIMPLEX,
	SKETCHY,
	SLATE,
	SOLAR,
	SPACELAB,
	SUPERHERO,
	UNITED,
	VAPOR,
	YETI,
	ZERPHYR;
	
	public boolean isDark() {
		switch(this) {
		case DEFAULT:
		case CYBORG:
		case DARKLY:
		case QUARTZ:
		case SLATE:
		case SOLAR:
		case SUPERHERO:
		case VAPOR:
			return true;
		default:
			return false;
		}
	}

	public static boolean hasCss(BootstrapTheme current) {
		
		switch(current) {
		case JADAPTIVE:
			return false;
		default:
			return true;
		}
	}

	public static String getThemeCssName(BootstrapTheme current) {
		
		switch(current) {
		case DEFAULT:
			/* BPS: I found Darkly embeds a font from Google. If server is
			 * running disconnected from the internet, pages will take 30+
			 * seconds to load. 
			 */
			return JADAPTIVE.name().toLowerCase();
			//return DARKLY.name().toLowerCase();
		default:
			return current.name().toLowerCase();
		}
	}

	public static String getThemeCssUrl(BootstrapTheme current) {
		return String.format("/app/content/npm2mvn/npm/bootswatch/current/dist/%s/bootstrap.min.css", BootstrapTheme.getThemeCssName(current));
	}
}
