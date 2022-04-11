package com.bakeoff.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class TestUtils {

  public static InputStream getResourceAsStream(String fileName) {
    File testFile = new File(fileName.startsWith("/") ? fileName : "src/test/resources/" + fileName);
    Object stream;
    if (testFile.exists()) {
      try {
        stream = new FileInputStream(testFile);
      } catch (FileNotFoundException var4) {
        throw new RuntimeException("Cannot find test resource: " + fileName);
      }
    } else {
      stream = TestUtils.class.getResourceAsStream(fileName.startsWith("/") ? fileName : "/" + fileName);
    }

    if (stream == null) {
      throw new RuntimeException("Cannot find test resource: " + fileName);
    } else {
      return (InputStream)stream;
    }
  }

  public static String getResource(String fileName) {
    Scanner scanner = (new Scanner(getResourceAsStream(fileName))).useDelimiter("\\A");
    return scanner.hasNext() ? scanner.next() : "";
  }
  
}
