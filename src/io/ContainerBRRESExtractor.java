package io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ContainerBRRESExtractor {

    // Search for bres since every brres file I know of starts with this in the header
    byte[] searchPattern = "bres".getBytes();

    public void extractContainerBRRES(String inputFilePath, String outputFolderPath, boolean organizeFiles) {
        try {
            byte[] inputFileContent = readBinaryFile(new File(inputFilePath));
            List<Integer> bresOffsets = findBresOffsets(inputFileContent, searchPattern);
            splitBinaryFileByOffsets(inputFileContent, bresOffsets, new File(outputFolderPath), organizeFiles);
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

    private static void splitBinaryFileByOffsets(byte[] content, List<Integer> offsets, File outputDir, boolean organizeFiles) throws IOException {
        if (offsets.isEmpty()) {
            return;
        }

        offsets.add(content.length);

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

            // Get the file length from the header and calculate the actual chunk length
            int chunkFileLength = getFileLengthFromHeader(splitContent);
            if (chunkFileLength > 0 && chunkFileLength <= splitContent.length) {
                splitContent = Arrays.copyOfRange(splitContent, 0, chunkFileLength);
            }

            // Save the chunk to a file
            File outputFile = new File(outputDir, "output_" + i + ".brres");
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(splitContent);
            }

            // If checkbox on UI is checked, then organize the files (this is disabled by default due to performance)
            if (organizeFiles) {
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

                    outputFile.delete();
                }
            }
        }
    }

    private static short readShort(byte[] content, int offset) {
        return (short) ((content[offset] << 8) | (content[offset + 1] & 0xFF));
    }

    private static int readInt(byte[] content, int offset) {
        return ((content[offset] & 0xFF) << 24) |
                ((content[offset + 1] & 0xFF) << 16) |
                ((content[offset + 2] & 0xFF) << 8) |
                (content[offset + 3] & 0xFF);
    }

    // Get the file length from the BRES header
    private static int getFileLengthFromHeader(byte[] content) {
        // Read the Byte Order Mark (BOM) to determine byte order
        short bom = readShort(content, 4);

        //If BOM isn't 0xFEFF, then it isn't big endian. Just use the chunk size as a fallback
        if (bom != (short) 0xFEFF) {
            return content.length;
        }

        // File length is at offset 0x08
        return readInt(content, 8);
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
