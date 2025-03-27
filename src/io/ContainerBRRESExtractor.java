package io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ContainerBRRESExtractor {

    // Search for bres since every brres file I know of starts with this in the header
    byte[] searchPattern = "bres".getBytes();

    public void extractContainerBRRES(String inputFilePath, String outputFolderPath) {
        try {
            byte[] fileContent = readBinaryFile(new File(inputFilePath));
            List<Integer> bresOffsets = findBresOffsets(fileContent, searchPattern);
            splitBinaryFileByOffsets(fileContent, bresOffsets, new File(inputFilePath), new File(outputFolderPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] readBinaryFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int byteRead;
        while ((byteRead = inputStream.read()) != -1) {
            byteArrayOutputStream.write(byteRead);
        }
        inputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    private static List<Integer> findBresOffsets(byte[] content, byte[] pattern) {
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i <= content.length - pattern.length; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (content[i + j] != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                offsets.add(i);
                i += pattern.length - 1;
            }
        }
        return offsets;
    }

    private static void splitBinaryFileByOffsets(byte[] content, List<Integer> offsets, File inputFile, File outputDir) throws IOException {
        if (offsets.isEmpty()) {
            return;
        }

        offsets.add(content.length);

        String fileExtension = ".brres";

        Map<String, byte[]> patterns = new HashMap<>();
        patterns.put("AnmChr", "AnmChr(NW4R)".getBytes());
        patterns.put("AnmShp", "AnmShp(NW4R)".getBytes());
        patterns.put("AnmScn", "AnmScn(NW4R)".getBytes());
        patterns.put("AnmClr", "AnmClr(NW4R)".getBytes());
        patterns.put("AnmVis", "AnmVis(NW4R)".getBytes());
        patterns.put("Textures", "Textures(NW4R)".getBytes());
        patterns.put("3DModels", "3DModels(NW4R)".getBytes());

        // Split off the file at each bres header
        for (int i = 0; i < offsets.size() - 1; i++) {
            int start = offsets.get(i);
            int end = offsets.get(i + 1);
            byte[] splitContent = Arrays.copyOfRange(content, start, end);

            File outputFile = new File(outputDir, "output_" + i + fileExtension);

            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(splitContent);
            }

            List<String> matchedFolders = new ArrayList<>();
            for (Map.Entry<String, byte[]> entry : patterns.entrySet()) {
                byte[] patternBytes = entry.getValue();
                if (containsPattern(splitContent, patternBytes)) {
                    matchedFolders.add(entry.getKey());
                }
            }

            if (!matchedFolders.isEmpty()) {
                for (String folderName : matchedFolders) {
                    File folder = new File(outputDir, folderName);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    File newFile = new File(folder, outputFile.getName());
                    try {
                        Files.copy(outputFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static boolean containsPattern(byte[] content, byte[] pattern) {
        for (int i = 0; i <= content.length - pattern.length; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (content[i + j] != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        return false;
    }
}
