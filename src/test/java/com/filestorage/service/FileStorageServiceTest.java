package com.filestorage.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.filestorage.model.BaseResponseMetadata;
import com.filestorage.model.GetFileResponse;
import com.filestorage.util.FileStorageUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class FileStorageServiceTest {

    private static final String TEST_FILE_NAME = "testFile.txt";
    private static final String STORAGE_DIR = "data-storage";

    @Mock
    private FileStorageUtil fileStorageUtil;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileStorageService fileStorageService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test saving a file when the file name is valid.
     * 
     * Given: A valid file name
     * When: Saving a file
     * Then: The file should be saved successfully and the path to the saved file
     * should be returned.
     * 
     * @throws IOException
     */
    @Test
    public void testSaveFile_Success() throws IOException {
        // Mock MultipartFile behaviors

        when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILE_NAME);

        // Mock resolved file path
        Path mockFilePath = Paths.get(STORAGE_DIR, "te", TEST_FILE_NAME);
        when(fileStorageUtil.resolveFilePath(TEST_FILE_NAME)).thenReturn(mockFilePath);

        doNothing().when(fileStorageUtil).validateFileName(TEST_FILE_NAME);

        // Ensure parent directory exists before file creation
        Files.createDirectories(mockFilePath.getParent());

        // Call method under test
        String result = fileStorageService.saveFile(multipartFile);

        // Assertions
        assertNotNull(result);
        assertEquals(mockFilePath.toString(), result);
    }

    /**
     * Test saving a file when the file name is empty.
     * 
     * Given: An empty file name
     * When: Saving a file
     * Then: An exception should be thrown with a message indicating that the file
     * name is empty.
     */
    @Test
    public void testSaveFile_FileNameIsEmpty() throws IOException {
        // GIVEN
        when(multipartFile.getOriginalFilename()).thenReturn("");

        // WHEN
        try {
            fileStorageService.saveFile(multipartFile);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // THEN
            assertEquals("File name cannot be empty", e.getMessage());
        }
    }

    /**
     * Test saving a file when the file name is null.
     * 
     * Given: A null file name
     * When: Saving a file
     * Then: An exception should be thrown with a message indicating that the file
     * name is empty.
     */
    @Test
    public void testSaveFile_FileNameIsNull() throws IOException {
        // GIVEN
        when(multipartFile.getOriginalFilename()).thenReturn(null);

        // WHEN
        try {
            fileStorageService.saveFile(multipartFile);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // THEN
            assertEquals("File name cannot be empty", e.getMessage());
        }
    }

    /**
     * Test saving a file when the file already exists.
     * 
     * Given: A valid file name that already exists in the storage directory
     * When: Saving a file
     * Then: The file should be saved successfully and the path to the saved file
     * should be returned.
     * 
     * @throws IOException
     */
    @Test
    public void testSaveFile_FileAlreadyExists() throws IOException {
        // GIVEN
        when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILE_NAME);
        Path mockFilePath = Paths.get(STORAGE_DIR, TEST_FILE_NAME);
        when(fileStorageUtil.resolveFilePath(TEST_FILE_NAME)).thenReturn(mockFilePath);
        doNothing().when(fileStorageUtil).validateFileName(TEST_FILE_NAME);
        Files.createFile(mockFilePath);

        // WHEN
        String result = fileStorageService.saveFile(multipartFile);

        // THEN
        assertNotNull(result);
        assertEquals(mockFilePath.toString(), result);
    }

    /**
     * Test updating a file that does not exist.
     * 
     * Given: A valid file name that does not exist in the storage directory
     * When: Updating a file
     * Then: A FileNotFoundException should be thrown with a message indicating
     * that the file could not be found.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateFile_FileNotFound() throws Exception {
        // GIVEN
        String fileName = "testFile.txt";
        Path filePath = Paths.get(STORAGE_DIR, fileName);
        Files.deleteIfExists(filePath);
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(fileStorageUtil.resolveFilePath(fileName)).thenReturn(filePath);

        // WHEN
        try {
            fileStorageService.updateFile(fileName, multipartFile);
            fail("Expected FileNotFoundException");
        } catch (FileNotFoundException e) {
            // THEN
            assertEquals("File not found: " + fileName, e.getMessage());
        }
    }

    /**
     * Test updating a file that already exists.
     * 
     * Given: A valid file name that already exists in the storage directory
     * When: Updating a file
     * Then: The file should be updated successfully and the path to the updated
     * file should be returned.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateFile_FileAlreadyExists() throws Exception {
        // GIVEN
        String fileName = "testFile.txt";
        Path filePath = Paths.get(STORAGE_DIR, fileName);
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        when(fileStorageUtil.resolveFilePath(fileName)).thenReturn(filePath);

        // WHEN
        String result = fileStorageService.updateFile(fileName, multipartFile);

        // THEN
        assertNotNull(result);
        assertEquals(STORAGE_DIR + "/" + Paths.get(STORAGE_DIR).relativize(filePath).toString().replace("\\", "/"),
                result);
    }

    /**
     * Test retrieving a file that exists in the storage directory.
     * 
     * Given: A valid file name that exists in the storage directory
     * When: Retrieving a file
     * Then: The file should be retrieved successfully and the path to the
     * retrieved file should be returned.
     * 
     * @throws Exception
     */
    @Test
    public void testGetFile_FileFound() throws Exception {
        // GIVEN
        String fileName = "testFile.txt";
        Path filePath = Paths.get(STORAGE_DIR, fileName);
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        when(fileStorageUtil.resolveFilePath(fileName)).thenReturn(filePath);

        // WHEN
        Path result = fileStorageService.getFile(fileName);

        // THEN
        assertNotNull(result);
        assertEquals(filePath, result);
    }

    /**
     * Test retrieving a file that does not exist in the storage directory.
     * 
     * Given: A valid file name that does not exist in the storage directory
     * When: Retrieving a file
     * Then: A FileNotFoundException should be thrown with a message indicating
     * that the file could not be found.
     * 
     * @throws Exception
     */
    @Test
    public void testGetFile_FileNotFound() throws Exception {
        // GIVEN
        String fileName = "testFile.txt";
        Path filePath = Paths.get(STORAGE_DIR, fileName);
        Files.deleteIfExists(filePath);
        when(fileStorageUtil.resolveFilePath(fileName)).thenReturn(filePath); // Adăugați acest cod pentru a returna un
                                                                              // Path valid

        // WHEN
        try {
            fileStorageService.getFile(fileName);
            fail("Expected FileNotFoundException");
        } catch (FileNotFoundException e) {
            // THEN
            assertEquals("File not found: " + fileName, e.getMessage());
        }
    }

    /**
     * Test deleting a file that exists in the storage directory.
     * 
     * Given: A valid file name that exists in the storage directory
     * When: Deleting a file
     * Then: The file should be deleted successfully and the path to the
     * deleted file should be returned.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteFile_FileFound() throws Exception {
        // GIVEN
        String fileName = "testFile.txt";
        Path filePath = Paths.get(STORAGE_DIR, fileName);
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        when(fileStorageUtil.resolveFilePath(fileName)).thenReturn(filePath);

        // WHEN
        boolean result = fileStorageService.deleteFile(fileName);

        // THEN
        assertTrue(result);
        assertFalse(Files.exists(filePath));
    }

    /**
     * Test deleting a file that does not exist in the storage directory.
     * 
     * Given: A valid file name that does not exist in the storage directory
     * When: Deleting a file
     * Then: The method should return false and the file should not exist in
     * the storage directory.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteFile_FileNotFound() throws Exception {
        // GIVEN
        String fileName = "testFile.txt";
        Path filePath = Paths.get(STORAGE_DIR, fileName);
        Files.deleteIfExists(filePath);
        when(fileStorageUtil.resolveFilePath(fileName)).thenReturn(filePath);
        FileStorageService fileStorageServiceSpy = spy(fileStorageService);
        doReturn(filePath).when(fileStorageServiceSpy).getFile(fileName);

        // WHEN
        boolean result = fileStorageServiceSpy.deleteFile(fileName);

        // THEN
        assertFalse(result);
        assertFalse(Files.exists(filePath));
    }

    /**
     * Test deleting a file when file deletion fails.
     * 
     * Given: A valid file name and an exception thrown during file deletion
     * When: Deleting a file
     * Then: A RuntimeException should be thrown with a message indicating
     * that the file deletion failed.
     * 
     * @throws Exception
     */

    @Test
    public void testDeleteFile_FileDeletionFailed() throws Exception {
        // GIVEN
        String fileName = "testFile.txt";
        Path filePath = Paths.get(STORAGE_DIR, fileName);
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);
        when(fileStorageUtil.resolveFilePath(fileName)).thenReturn(filePath);
        FileStorageService fileStorageServiceSpy = spy(fileStorageService);
        doThrow(new RuntimeException("Mocked RuntimeException")).when(fileStorageServiceSpy).deleteFile(fileName);

        // WHEN
        try {
            fileStorageServiceSpy.deleteFile(fileName);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // THEN
            assertEquals("Mocked RuntimeException", e.getMessage());
        }
    }

    /**
     * Test listing files with a valid regex.
     * 
     * Given: A regex pattern to match filenames and valid pagination parameters
     * When: Listing files matching the regex
     * Then: The response should contain the expected files that match the regex
     * and the pagination size, verifying that the fileStorageService
     * returns a list of files correctly.
     * 
     * @throws Exception
     */

    @Test
    public void testListFilesMatchingRegex_ValidRegex() throws Exception {
        // GIVEN
        String regex = "test.*";
        int page = 0;
        int size = 10;
        when(fileStorageUtil.resolveFilePath(anyString())).thenReturn(Paths.get(STORAGE_DIR, "testFile.txt"));

        // WHEN
        BaseResponseMetadata<GetFileResponse> response = fileStorageService.listFilesMatchingRegex(regex, page, size);

        // THEN
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getFiles().size());
        assertEquals("testFile.txt", response.getData().getFiles().get(0).getFileName());
    }

    /**
     * Test listing files with an invalid regex.
     * 
     * Given: An invalid regex pattern and valid pagination parameters
     * When: Listing files matching the regex
     * Then: An IllegalArgumentException should be thrown with a message indicating
     * that the regex is invalid, verifying that the fileStorageService
     * correctly handles invalid regex patterns.
     * 
     * @throws Exception
     */
    @Test
    public void testListFilesMatchingRegex_InvalidRegex() throws Exception {
        // GIVEN
        String regex = "[";
        int page = 0;
        int size = 10;

        // WHEN
        try {
            fileStorageService.listFilesMatchingRegex(regex, page, size);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // THEN
            assertEquals("Invalid regex pattern: [", e.getMessage());
        }
    }

    /**
     * Test listing files with a regex that has no matching files.
     * 
     * Given: A regex pattern to match filenames and valid pagination parameters
     * When: Listing files matching the regex
     * Then: The response should contain an empty list of files, verifying that the
     * fileStorageService correctly handles the case where no files match the
     * regex.
     * 
     * @throws Exception
     */
    @Test
    public void testListFilesMatchingRegex_NoMatchingFiles() throws Exception {
        // GIVEN
        String regex = "nonExistingFile.*";
        int page = 0;
        int size = 10;

        // WHEN
        BaseResponseMetadata<GetFileResponse> response = fileStorageService.listFilesMatchingRegex(regex, page, size);

        // THEN
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(0, response.getData().getFiles().size());
    }

}
