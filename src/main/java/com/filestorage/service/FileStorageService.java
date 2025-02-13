package com.filestorage.service;

import com.filestorage.dto.FileDto;
import com.filestorage.model.BaseResponseMetadata;
import com.filestorage.model.GetFileResponse;
import com.filestorage.model.Metadata;
import com.filestorage.util.FileStorageUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service handling file storage operations including create, read, update,
 * delete, and search.
 */
@Service
public class FileStorageService {

    private static final Logger logger = LogManager.getLogger(FileStorageService.class);
    private static final String STORAGE_DIR = "data-storage";

    @Autowired
    private FileStorageUtil fileStorageUtil;

    public FileStorageService() {
        initializeStorage();
    }

    /**
     * Ensures the storage directory exists when the application starts.
     */
    private void initializeStorage() {
        Path storagePath = Paths.get(STORAGE_DIR);
        if (!Files.exists(storagePath)) {
            try {
                Files.createDirectories(storagePath);
                logger.info("FileStorageService.initializeStorage(): Storage directory initialized.");
            } catch (IOException e) {
                logger.error("FileStorageService.initializeStorage(): Failed to create storage directory", e);
                throw new RuntimeException("Storage initialization failed", e);
            }
        }
    }

    /**
     * Saves a file to structured storage.
     * 
     * TODO: Implement file scanning for security (virus scanning).
     * TODO: Consider limiting file size or zip compression. (Added a max size of
     * 10MB in the app.properties file)
     */
    public String saveFile(MultipartFile file) {
        logger.info("FileStorageService.saveFile(): In... fileName={}", file.getOriginalFilename());

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        fileStorageUtil.validateFileName(fileName);
        Path filePath = fileStorageUtil.resolveFilePath(fileName);

        try {
            Files.createDirectories(filePath.getParent());

            if (Files.exists(filePath)) {
                logger.debug("FileStorageService.saveFile(): File already exists at {}", filePath);
                return STORAGE_DIR + "/" + Paths.get(STORAGE_DIR).relativize(filePath).toString().replace("\\", "/");
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("FileStorageService.saveFile(): Out... File successfully stored at {}", filePath);
            return STORAGE_DIR + "/" + Paths.get(STORAGE_DIR).relativize(filePath).toString().replace("\\", "/");
        } catch (IOException e) {
            logger.error("FileStorageService.saveFile(): Error saving file {}", fileName, e);
            throw new RuntimeException("File saving failed", e);
        }
    }

    /**
     * Updates an existing file's content.
     */
    public String updateFile(String fileName, MultipartFile newFile) throws Exception {
        logger.info("FileStorageService.updateFile(): In... fileName={}", fileName);

        String uploadedFileName = newFile.getOriginalFilename();
        if (uploadedFileName != null && !uploadedFileName.equals(fileName)) {
            logger.debug("FileStorageService.updateFile(): File name mismatch - expected '{}', but got '{}'", fileName,
                    uploadedFileName);
            throw new IllegalArgumentException(
                    "File name mismatch: expected '" + fileName + "', but received '" + uploadedFileName + "'.");
        }

        fileStorageUtil.validateFileName(fileName);
        Path filePath = fileStorageUtil.resolveFilePath(fileName);

        if (!Files.exists(filePath)) {
            logger.debug("FileStorageService.updateFile(): File not found {}", filePath);
            throw new FileNotFoundException("File not found: " + fileName);
        }

        try {
            Path tempFilePath = Paths.get(STORAGE_DIR, fileName + ".tmp");
            Files.copy(newFile.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            Files.move(tempFilePath, filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("FileStorageService.updateFile(): Out... File updated at {}", filePath);
            return STORAGE_DIR + "/" + Paths.get(STORAGE_DIR).relativize(filePath).toString().replace("\\", "/");
        } catch (IOException e) {
            logger.error("FileStorageService.updateFile(): Error updating file {}", fileName, e);
            throw new RuntimeException("File update failed", e);
        }
    }

    /**
     * Retrieves the file from storage.
     */
    public Path getFile(String fileName) throws FileNotFoundException {
        logger.info("FileStorageService.getFile(): In... fileName={}", fileName);
        Path filePath = fileStorageUtil.resolveFilePath(fileName);

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            logger.debug("FileStorageService.getFile(): File not found {}", filePath);
            throw new FileNotFoundException("File not found: " + fileName);
        }

        logger.info("FileStorageService.getFile(): Out... File retrieved {}", filePath);
        return filePath;
    }

    /**
     * Deletes a file.
     */
    public boolean deleteFile(String fileName) throws Exception {
        logger.info("FileStorageService.deleteFile(): In... fileName={}", fileName);
        Path filePath = getFile(fileName);
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            logger.info("FileStorageService.deleteFile(): Out... File deleted={}", deleted);
            return deleted;
        } catch (IOException e) {
            logger.error("FileStorageService.deleteFile(): Error deleting file {}", fileName, e);
            throw new RuntimeException("File deletion failed", e);
        }
    }

    /**
     * Lists files matching a regex with pagination.
     */
    public BaseResponseMetadata<GetFileResponse> listFilesMatchingRegex(String regex, int page, int size) {
        logger.info("FileStorageService.listFilesMatchingRegex(): In... regex={}, page={}, size={}", regex, page, size);

        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            logger.error("FileStorageService.listFilesMatchingRegex(): Invalid regex pattern: {}", regex);
            throw new IllegalArgumentException("Invalid regex pattern: " + regex);
        }

        List<FileDto> fileDtoList = new ArrayList<>();
        int offset = page * size;
        AtomicInteger matchedFilesCounter = new AtomicInteger(0); // Ensures thread-safe counting

        try (Stream<Path> foldersStream = Files.list(Paths.get(STORAGE_DIR)).filter(Files::isDirectory)) {
            // Sort folders ascending before parallel processing
            List<Path> sortedFolders = foldersStream.sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());

            // Parallel processing for counting total matching files
            int totalMatchingFiles = sortedFolders.parallelStream()
                    .mapToInt(folder -> {
                        String threadName = Thread.currentThread().getName();
                        logger.info("FileStorageService.listFilesMatchingRegex(): Processing folder: {} on thread: {}",
                                folder, threadName);

                        try (Stream<Path> files = Files.list(folder).filter(Files::isRegularFile)) {
                            return (int) files.filter(file -> pattern.matcher(file.getFileName().toString()).matches())
                                    .count();
                        } catch (IOException e) {
                            logger.error("Error counting files in {}", folder, e);
                            return 0;
                        }
                    })
                    .sum();

            // Sequential processing for collecting paginated results (ensuring order)
            for (Path folder : sortedFolders) {
                try (Stream<Path> files = Files.list(folder).filter(Files::isRegularFile)) {
                    for (Path file : (Iterable<Path>) files::iterator) {
                        String fileName = file.getFileName().toString();
                        if (pattern.matcher(fileName).matches()) {
                            int currentMatchCount = matchedFilesCounter.incrementAndGet();
                            if (currentMatchCount > offset && fileDtoList.size() < size) {
                                fileDtoList.add(new FileDto(fileName));
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error("Error listing files in {}", folder, e);
                }
                if (fileDtoList.size() >= size) {
                    break; // Stop once enough results are collected
                }
            }

            logger.info(
                    "FileStorageService.listFilesMatchingRegex(): Out... Found {} files for this page, Total matching items: {}",
                    fileDtoList.size(), totalMatchingFiles);

            return new BaseResponseMetadata<>(new GetFileResponse(fileDtoList),
                    new Metadata(totalMatchingFiles, page, size), null);
        } catch (IOException e) {
            logger.error("FileStorageService.listFilesMatchingRegex(): Error listing files", e);
            throw new RuntimeException("File listing failed", e);
        }
    }

    /**
     * Counts the total number of files stored in the structured directory.
     * TODO: Cache file count and update on add/delete to improve performance.
     */
    public long countFiles() {
        logger.info("FileStorageService.countFiles(): In... Counting total stored files.");
        try {
            long totalFiles = Files.walk(Paths.get(STORAGE_DIR))
                    .filter(Files::isRegularFile)
                    .count();

            logger.info("FileStorageService.countFiles(): Out... Total files counted={}", totalFiles);
            return totalFiles;
        } catch (IOException e) {
            logger.error("FileStorageService.countFiles(): Error accessing storage directory", e);
            throw new RuntimeException("Error counting files", e);
        }
    }
}
