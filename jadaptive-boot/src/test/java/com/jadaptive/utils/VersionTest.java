package com.jadaptive.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;

public class VersionTest {

	
	@Test
	public void testSnapshotVersion() {
		Version version = new Version("1.2.3-SNAPSHOT");
		assertTrue(version.isSnapshot());
	}
	
	@Test
	public void testSnapshotLowerThanReleaseOfSameVersion() {
		Version version = new Version("1.2.3");
		Version snapshot = new Version("1.2.3-SNAPSHOT");
		assertTrue(version.compareTo(snapshot) > 0);
		assertTrue(snapshot.compareTo(version) < 0);
	}
	
	@Test
	public void testIsNotSnapshotVersion() {
		Version version = new Version("1.2.3");
		assertFalse(version.isSnapshot());
	}
	
	@Test
	public void testVersionsEqual() {
		Version version = new Version("1.2.3");
		Version version2 = new Version("1.2.3");
		assertTrue(version.compareTo(version2) == 0);
	}
	
	@Test
	public void testPatchLevel() {
		Version version = new Version("0.0.3");
		Version version2 = new Version("0.0.4");
		assertTrue(version.compareTo(version2) < 0);
		assertTrue(version2.compareTo(version) > 0);
	}
	
	@Test
	public void testMinorLevel() {
		Version version = new Version("0.1.0");
		Version version2 = new Version("0.2.0");
		assertTrue(version.compareTo(version2) < 0);
		assertTrue(version2.compareTo(version) > 0);
	}
	
	@Test
	public void testMajorLevel() {
		Version version = new Version("1.0.0");
		Version version2 = new Version("2.0.0");
		assertTrue(version.compareTo(version2) < 0);
		assertTrue(version2.compareTo(version) > 0);
	}
	
	@Test
	public void testMajorGreaterThanMinorLevel() {
		Version version = new Version("0.1.0");
		Version version2 = new Version("1.0.0");
		assertTrue(version.compareTo(version2) < 0);
		assertTrue(version2.compareTo(version) > 0);
	}
	
	@Test
	public void testMinorGreaterThanPatchLevel() {
		Version version = new Version("0.0.1");
		Version version2 = new Version("0.1.0");
		assertTrue(version.compareTo(version2) < 0);
		assertTrue(version2.compareTo(version) > 0);
	}
}
