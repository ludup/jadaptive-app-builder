package com.jadaptive.app.db;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.BiometricDatabase;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
@Service
public class BiometricDatabaseImpl extends AbstractTenantAwareDatabase implements BiometricDatabase {

	@Override
	public void createBiometricIndex(Tenant tenant, String collection) {
		createVectorIndex(tenant, collection);
	}
	
   @Override
    public void saveBiometricData(Tenant tenant, String collection, User user, List<List<Double>> threeVectors) {
	   
	    Bson filter = Filters.eq("_id", user.getUuid());

	    Bson update = Updates.combine(
	        Updates.set("biometrics.vector", threeVectors),
	        Updates.set("biometrics.updated_at", new Date())
	    );

	    UpdateOptions options = new UpdateOptions().upsert(true);
	    getCollection(collection, tenant.getUuid()).updateOne(filter, update, options);
	    
    }
   
   protected void createVectorIndex(Tenant tenant, String collection) {
	   
	    // 1. Define the vector field mapping
	    Document vectorField = new Document("type", "vector")
	        .append("path", "biometrics.vector")
	        .append("numDimensions", 512)
	        .append("similarity", "cosine");

	    // 2. Build the full index definition
	    Document indexDefinition = new Document("fields", Collections.singletonList(vectorField));

	    // 3. Create the command document
	    Document command = new Document("createSearchIndexes", collection) 
	        .append("indexes", Collections.singletonList(
	            new Document("name", "biometric_vector_index")
	                .append("definition", indexDefinition)
	        ));

	    // 4. Run the command
	    try {
	        Document response = getDatabase(tenant.getUuid()).runCommand(command);
	        System.out.println("Vector index request sent: " + response.toJson());
	    } catch (Exception e) {
	        System.err.println("Index creation failed: " + e.getMessage());
	    }
	}

   @Override
   public List<List<Double>> getBiometricData(Tenant tenant, User user, String collection) {
	   
	   Document doc = getCollection(tenant.getUuid(), collection).find(Filters.eq("_id", user.getUuid())).first();
	   return (List<List<Double>>) doc.get("biometrics.vector");
	   
   }
   
   /**
    * Calculates the Cosine Similarity between two feature vectors.
    * Range: 1.0 (Identical) to 0.0 (Unrelated) to -1.0 (Opposite).
    */
   @Override
   public double calculateCosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
       if (vectorA == null || vectorB == null || vectorA.size() != vectorB.size()) {
           return 0.0; // Mismatch or null data
       }

       double dotProduct = 0.0;
       double normA = 0.0;
       double normB = 0.0;

       for (int i = 0; i < vectorA.size(); i++) {
           double valA = vectorA.get(i);
           double valB = vectorB.get(i);
           
           dotProduct += valA * valB;
           normA += valA * valA;
           normB += valB * valB;
       }

       // Safety: check for zero magnitudes to avoid NaN (Not a Number)
       if (normA == 0 || normB == 0) {
           return 0.0;
       }

       return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
   }
}
