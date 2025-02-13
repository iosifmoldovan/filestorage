package com.filestorage.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.filestorage.dto.FileDto;
import com.filestorage.model.BaseResponseMetadata;
import com.filestorage.model.GetFileResponse;
import com.filestorage.service.FileStorageService;

@WebMvcTest(FileController.class)
public class FileControllerTest {
 
    @Mock
    private FileStorageService fileStorageService;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private FileController fileController;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(fileController).build();
    }

    @Test
    public void testUploadFile_Success() throws Exception {
        /**
         * GIVEN: A valid file to be uploaded
         */
        MockMultipartFile file = new MockMultipartFile("file", "example.txt", "text/plain", "Hello World".getBytes());
        when(fileStorageService.saveFile(file)).thenReturn("example.txt");

        /**
         * WHEN: Upload request is made
         */
        MvcResult result = mockMvc.perform(multipart("/files/upload")
                .file(file))
                .andExpect(status().isOk())
                .andReturn();

        /**
         * THEN: The file should be uploaded successfully
         */
        String response = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(response);
        assertEquals("example.txt", jsonObject.getString("data"));
    }

    @Test(expected = Exception.class)
    public void testUploadFile_EmptyFileName() throws Exception {
        /**
         * GIVEN: An empty file name
         */
        MockMultipartFile file = new MockMultipartFile("file", "", "", new byte[0]);

        /**
         * WHEN: Upload request is made
         */
        doThrow(new Exception("File name is empty")).when(fileStorageService).saveFile(file);

        MvcResult result = mockMvc.perform(multipart("/files/upload")
                .file(file))
                .andExpect(status().isBadRequest())
                .andReturn();

        /**
         * THEN: An error message should be returned
         */
        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("File name is empty"));
    }

    @Test
    public void testUpdateFile_Success() throws Exception {
        // GIVE: A file to upload
        MockMultipartFile file = new MockMultipartFile("file", "example.txt", "text/plain", "Hello World".getBytes());

        // GIVE: fileStorageService să returneze un anumit rezultat
        when(fileStorageService.saveFile(any(MultipartFile.class))).thenReturn("/path/to/uploaded/file");

        // WHEN: The file is uploaded
        MvcResult result = mockMvc.perform(multipart("/files/upload")
                .file(file))
                .andExpect(status().isOk())
                .andReturn();

        // THEN: The file is uploaded successfully
        String response = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(response);
        assertNotNull(jsonObject.get("data"));
        assertEquals("/path/to/uploaded/file", jsonObject.getString("data"));
    }

    @Test
    public void testUpdateFile_Exception() throws Exception {
        /**
         * GIVEN: A valid file and an exception from fileStorageService
         */
        String fileName = "example.txt";
        MockMultipartFile file = new MockMultipartFile("file", "example.txt", "text/plain", "Hello World".getBytes());
        doThrow(new Exception("Test exception")).when(fileStorageService).updateFile(fileName, file);

        /**
         * WHEN: Update request is made
         */
        MvcResult result = mockMvc.perform(put("/files/update/" + fileName)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .requestAttr("file", file))
                .andExpect(status().isBadRequest())
                .andReturn();

        /**
         * THEN: An exception should be thrown
         */
        String response = result.getResponse().getContentAsString();
        assertTrue(response.isEmpty());
    }

    @Test
    public void testGetFile_Success() throws Exception {
        /**
         * GIVEN: A valid file name
         */
        String fileName = "example.txt";
        Path filePath = Files.createTempFile("example", ".txt");
        Files.write(filePath, "Hello World".getBytes());

        when(fileStorageService.getFile(fileName)).thenReturn(filePath);

        /**
         * WHEN: Get file request is made
         */
        MvcResult result = mockMvc.perform(get("/files/download/" + fileName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"example.txt\""))
                .andReturn();

        /**
         * THEN: The file should be returned successfully
         */
        assertNotNull(result);
        assertEquals(200, result.getResponse().getStatus());

        // Nu uita să ștergi fișierul temporar după test
        Files.delete(filePath);
    }

    @Test
    public void testDeleteFile_Success() throws Exception {
        /**
         * GIVEN: A valid file name
         */
        String fileName = "example.txt";
        when(fileStorageService.deleteFile(fileName)).thenReturn(true);

        /**
         * WHEN: Delete file request is made
         */
        MvcResult result = mockMvc.perform(delete("/files/delete/" + fileName))
                .andExpect(status().isOk())
                .andReturn();

        /**
         * THEN: The file should be deleted successfully
         */
        String response = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(response);
        assertNotNull(jsonObject);
        assertEquals(200, result.getResponse().getStatus());
        assertEquals("File deleted: example.txt", jsonObject.getString("data"));
    }

    @Test(expected = Exception.class)
    public void testDeleteFile_Exception() throws Exception {
        /**
         * GIVEN: A valid file name and an exception from fileStorageService
         */
        String fileName = "example.txt";
        doThrow(new Exception("Test exception")).when(fileStorageService).deleteFile(fileName);

        /**
         * WHEN: Delete file request is made
         */
        MvcResult result = mockMvc.perform(delete("/files/delete/" + fileName))
                .andExpect(status().isInternalServerError())
                .andReturn();

        /**
         * THEN: An exception should be thrown
         */
        String response = result.getResponse().getContentAsString();
        assertEquals("Test exception", response);
    }

    @Test
    public void testListFiles_Success() throws Exception {
        /**
         * GIVEN: A valid regex and pagination parameters
         */
        String regex = "example.*";
        int page = 0;
        int size = 10;

        List<FileDto> files = new ArrayList<>();
        files.add(new FileDto("example.txt"));

        GetFileResponse getFileResponse = new GetFileResponse(files);

        BaseResponseMetadata<GetFileResponse> baseResponseMetadata = new BaseResponseMetadata<>(getFileResponse,
                null);

        when(fileStorageService.listFilesMatchingRegex(regex, page, size)).thenReturn(baseResponseMetadata);

        /**
         * WHEN: List files request is made
         */
        MvcResult result = mockMvc.perform(get("/files/search")
                .param("regex", regex)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andReturn();

        /**
         * THEN: The files should be listed successfully
         */
        String response = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(response);
        assertNotNull(jsonObject);
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void testSearchFiles_Success() throws Exception {
        // GIVE: A valid regex and pagination parameters, and a mocked service response
        GetFileResponse fileResponse = new GetFileResponse(Collections.singletonList(new FileDto("file1.txt")));
        BaseResponseMetadata<GetFileResponse> response = new BaseResponseMetadata<>(fileResponse, null);
        when(fileStorageService.listFilesMatchingRegex(".*", 0, 10)).thenReturn(response);

        // WHEN: A search files request is made
        mockMvc.perform(get("/files/search")
                .param("regex", ".*")
                .param("page", "0")
                .param("size", "10"))

                // THEN: The response should be successful and contain the expected file data
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.files[0].fileName").value("file1.txt"));
    }

    @Test(expected = Exception.class)
    public void testListFiles_Exception() throws Exception {
        // GIVEN: A valid regex and pagination parameters, and an exception from
        // fileStorageService
        String regex = "example.*";
        int page = 0;
        int size = 10;

        doThrow(new Exception("Test exception")).when(fileStorageService).listFilesMatchingRegex(regex, page,
                size);

        // WHEN: List files request is made
        MvcResult result = mockMvc.perform(get("/files/search")
                .param("regex", regex)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Test exception"))
                .andReturn();

        // THEN: An exception should be thrown
        assertNotNull(result);
    }

}