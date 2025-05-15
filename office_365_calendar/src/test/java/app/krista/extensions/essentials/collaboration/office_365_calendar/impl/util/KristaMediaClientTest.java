package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util;

import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.KristaMediaClient;
import app.krista.ksdk.files.FileHandle;
import app.krista.ksdk.files.FileRepository;
import app.krista.model.base.File;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class KristaMediaClientTest {

    private KristaMediaClient mediaClient;
    private FileRepository fileRepository;
    private FileHandle fileHandle;

    @Before
    public void setUp() {
        mediaClient = new KristaMediaClient();
        fileRepository = mock(FileRepository.class);
        fileHandle = mock(FileHandle.class);
        mediaClient.fileRepository = fileRepository;
    }

    @Test
    public void testToKristaFile_supportedFormat() throws Exception {
        java.io.File testFile = java.io.File.createTempFile("test", ".txt");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("Hello, Krista!");
        }

        File mockedKristaFile = mock(File.class);
        when(fileRepository.createNewFileByName(anyString())).thenReturn(fileHandle);
        when(fileHandle.getFile()).thenReturn(mockedKristaFile);

        doAnswer(invocation -> {
            InputStream in = invocation.getArgument(0);
            assertNotNull(in);
            return null;
        }).when(fileHandle).setContent(any(InputStream.class));

        File result = mediaClient.toKristaFile(testFile);
        assertNotNull(result);

        verify(fileRepository).createNewFileByName(testFile.getName());
        verify(fileHandle).setContent(any(FileInputStream.class));
    }

    @Test
    public void testToKristaFile_unsupportedFormat_compresses() throws Exception {
        java.io.File testFile = java.io.File.createTempFile("test", ".html");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("Malicious Content");
        }

        File mockedKristaFile = mock(File.class);
        when(mockedKristaFile.getFileName()).thenReturn("test.zip");

        when(fileRepository.createNewFileByName(anyString())).thenReturn(fileHandle);
        when(fileHandle.getFile()).thenReturn(mockedKristaFile);

        File result = mediaClient.toKristaFile(testFile);
        assertNotNull(result);
        assertTrue(result.getFileName().endsWith(".zip"));
    }


    @Test
    public void testToJavaFile_success() throws Exception {
        File mockedKristaFile = mock(File.class);
        when(mockedKristaFile.getFileName()).thenReturn("download.txt");

        when(fileRepository.getFile(mockedKristaFile)).thenReturn(fileHandle);

        ByteArrayInputStream mockInput = new ByteArrayInputStream("Downloaded content".getBytes());
        when(fileHandle.getContent()).thenReturn(mockInput);

        java.io.File resultFile = mediaClient.toJavaFile(mockedKristaFile);
        assertNotNull(resultFile);
        assertTrue(resultFile.exists());
        assertEquals("download.txt", resultFile.getName());
    }

    @Test
    public void testCompressFile_createsZip() throws Exception {
        java.io.File originalFile = java.io.File.createTempFile("sample", ".log");
        try (FileWriter writer = new FileWriter(originalFile)) {
            writer.write("Zip me up!");
        }

        String zipPath = originalFile.getParent() + "/sample.zip";
        KristaMediaClient.compressFile(zipPath, originalFile.getAbsolutePath());

        java.io.File zipFile = new java.io.File(zipPath);
        assertTrue(zipFile.exists());
        assertTrue(zipFile.length() > 0);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry = zis.getNextEntry();
            assertNotNull(entry);
            assertEquals(originalFile.getName(), entry.getName());
        }
    }
}
