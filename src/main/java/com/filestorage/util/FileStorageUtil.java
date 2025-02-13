package com.filestorage.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for file storage operations such as hashing, validation, and
 * path resolution.
 */
@Component
public class FileStorageUtil {

    private static final Logger logger = LogManager.getLogger(FileStorageUtil.class);
    private static final String FILE_NAME_PATTERN = "^[a-zA-Z0-9_-]{1,64}$";
    private static final String STORAGE_DIR = "data-storage";

    /**
     * Generates a SHA-256 hash for the given file name.
     *
     * @param fileName The file name to hash.
     * @return The hexadecimal hash representation.
     */
    public String generateFileHash(String fileName) {
        logger.debug("FileStorageUtil.generateFileHash(): Hashing file name '{}'", fileName);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileName.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            logger.debug("FileStorageUtil.generateFileHash(): Generated hash '{}' for file '{}'", hexString, fileName);
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("FileStorageUtil.generateFileHash(): Error hashing file name '{}'", fileName, e);
            throw new RuntimeException("Error hashing file name", e);
        }
    }

    /**
     * Validates the given file name against allowed patterns.
     *
     * @param fileName The file name to validate.
     */
    public void validateFileName(String fileName) {
        String baseName = getFileNameWithoutExtension(fileName);
        logger.debug("FileStorageUtil.validateFileName(): Validating file name '{}'", baseName);
        if (!baseName.matches(FILE_NAME_PATTERN)) {
            logger.warn("FileStorageUtil.validateFileName(): Invalid file name '{}'", fileName);
            throw new IllegalArgumentException("Invalid file name: " + fileName);
        }
    }

    /**
     * Extracts the base name of a file, removing its extension.
     *
     * @param fileName The full file name.
     * @return The base name without extension.
     */
    public String getFileNameWithoutExtension(String fileName) {
        logger.debug("FileStorageUtil.getFileNameWithoutExtension(): Processing file name '{}'", fileName);
        String baseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
        logger.debug("FileStorageUtil.getFileNameWithoutExtension(): Extracted base name '{}'", baseName);
        return baseName;
    }

    /**
     * Resolves the full storage path for a given file based on its hash.
     *
     * @param fileName The original file name.
     * @return The resolved storage path.
     */
    public Path resolveFilePath(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty when resolving path");
        }
        logger.debug("FileStorageUtil.resolveFilePath(): Resolving path for file '{}'", fileName);
        String hash = generateFileHash(fileName);
        String subfolder = hash.substring(0, 2); // First 2 characters for subfolder distribution
        Path path = Paths.get(STORAGE_DIR, subfolder, fileName);
        logger.debug("FileStorageUtil.resolveFilePath(): Resolved path '{}'", path);
        return path;
    }
}
