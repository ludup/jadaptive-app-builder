package com.jadaptive.app.templates;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.bytebuddy.dynamic.ClassFileLocator;

public class ClassChecksumGenerator {

    public static String getChecksum(Class<?> clz) throws IOException {
    	return getChecksum(getBytecode(clz));
    }
    
    public static String getChecksum(byte[] code) throws IOException {
    	try {
    		MessageDigest md = MessageDigest.getInstance("SHA-256");
	        try (InputStream fis = new ByteArrayInputStream(code)) {
	            byte[] dataBytes = new byte[1024];
	            int nread;
	            while ((nread = fis.read(dataBytes)) != -1) {
	                md.update(dataBytes, 0, nread);
	            }
	        }
	        byte[] mdBytes = md.digest();
	
	        // Convert the byte array to a hexadecimal string
	        BigInteger bigInt = new BigInteger(1, mdBytes);
	        String checksum = bigInt.toString(16);
	        
	        // Pad with leading zeros if necessary
	        while (checksum.length() < 64) {
	            checksum = "0" + checksum;
	        }
	        
	        return checksum;
        
    	} catch(NoSuchAlgorithmException e) {
    		throw new IllegalStateException(e.getMessage(), e);
    	}
    }
    
    /**
     * Retrieves the bytecode of a given Class object.
     *
     * @param clazz The Class object for which to retrieve the bytecode.
     * @return The byte array containing the class bytecode, or null if not found.
     */
    public static byte[] getBytecode(Class<?> clazz) {
        try {
            // 1. Create a ClassFileLocator for the class's ClassLoader.
            // This is crucial as it tells Byte Buddy where to look for the .class file.
            ClassFileLocator classFileLocator = ClassFileLocator.ForClassLoader.of(clazz.getClassLoader());

            // 2. Locate the class using its fully qualified name.
            ClassFileLocator.Resolution resolution = classFileLocator.locate(clazz.getName());

            // 3. Check if the class was found and retrieve the bytecode.
            if (resolution.isResolved()) {
                return resolution.resolve();
            } else {
                System.err.println("Could not locate class file for: " + clazz.getName());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}