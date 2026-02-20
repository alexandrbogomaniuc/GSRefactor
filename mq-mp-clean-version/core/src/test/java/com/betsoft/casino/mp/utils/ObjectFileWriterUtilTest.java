package com.betsoft.casino.mp.utils;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ObjectFileWriterUtilTest {

  private Path tempDir;
  private ObjectFileWriterUtil objectFileWriterUtil;

  @Before
  public void setUp() throws IOException {
    objectFileWriterUtil = new ObjectFileWriterUtil();
    // Create a temporary directory for the test
    tempDir = Files.createTempDirectory("testDir");
  }

  @After
  public void tearDown() {
    // Recursively delete the temp directory after tests
    deleteDir(tempDir.toFile());
  }

  private static void deleteDir(File dir) {
    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        deleteDir(file);
      }
    }
    dir.delete();
  }

  @Test
  public void testSaveObjectAsJsonToFileCreatesFileAndDirectory() {
    String fileName = "testFile.json";
    Path filePath = tempDir.resolve("newDir").resolve(fileName);

    Map<String, String> testData = new HashMap<>();
    testData.put("key", "value");

    objectFileWriterUtil.saveObjectAsJsonToFile(testData, filePath.toString());

    assertTrue("File should exist after saving", Files.exists(filePath));
  }

  @Test
  public void testDirectoryAlreadyExists() throws IOException {
    Path dirPath = tempDir.resolve("existingDir");
    Files.createDirectories(dirPath);

    String fileName = "testFile.json";
    Path filePath = dirPath.resolve(fileName);

    Map<String, String> testData = new HashMap<>();
    testData.put("key", "value");

    objectFileWriterUtil.saveObjectAsJsonToFile(testData, filePath.toString());

    assertTrue("Directory should exist", Files.isDirectory(dirPath));
    assertTrue("File should exist after saving", Files.exists(filePath));
  }
}
