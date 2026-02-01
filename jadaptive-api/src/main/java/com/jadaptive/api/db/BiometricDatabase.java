package com.jadaptive.api.db;

import java.util.List;

import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;

public interface BiometricDatabase {

	void saveBiometricData(Tenant tenant, String collection, User user, List<List<Double>> vectors);

	void createBiometricIndex(Tenant tenant, String collection);

	List<List<Double>> getBiometricData(Tenant tenant, User user, String collection);

	/**
    * Calculates the Cosine Similarity between two feature vectors.
    * Range: 1.0 (Identical) to 0.0 (Unrelated) to -1.0 (Opposite).
    */
	double calculateCosineSimilarity(List<Double> vectorA, List<Double> vectorB);

	boolean hasBiometricData(Tenant tenant, String collection, User user);

	double getBestFaceMatchScore(List<Double> liveVector, List<List<Double>> storedVectors);

}
