package com.jadaptive.api.ui.pages.ext;

public enum BootstrapTheme implements Theme {

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
	
	@Override
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

	@Override
	public boolean hasCss() {
		switch(this) {
		case DEFAULT:
			return false;
		default:
			return true;
		}
	}

	@Override
	public String getThemeCssUrl() {
		return String.format("/app/content/npm2mvn/npm/bootswatch/current/dist/%s/bootstrap.min.css", name().toLowerCase().toString());
	}
}
