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

	public static boolean hasCss(BootstrapTheme current) {
		
		switch(current) {
		case DEFAULT:
		case JADAPTIVE:
			return false;
		default:
			return true;
		}
	}
}
