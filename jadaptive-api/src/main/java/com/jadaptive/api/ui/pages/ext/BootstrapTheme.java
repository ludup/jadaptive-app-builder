package com.jadaptive.api.ui.pages.ext;

public enum BootstrapTheme {

	DEFAULT,
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
		case DEFAULT:
			return false;
		default:
			return true;
		}
	}

	public static String getThemeCssUrl(BootstrapTheme current) {
		return String.format("/app/content/npm2mvn/npm/bootswatch/current/dist/%s/bootstrap.min.css", current.name().toLowerCase().toString());
	}
}
