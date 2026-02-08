package io;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class ContainerBRRESExtractor {

    // Search for bres since every brres file I know of starts with this in the header
    private static final byte[] BRES = { 'b', 'r', 'e', 's' };

    private static final Map<String, byte[]> PATTERNS = Map.of(
            "AnmChr", "AnmChr(NW4R)".getBytes(),
            "AnmShp", "AnmShp(NW4R)".getBytes(),
            "AnmScn", "AnmScn(NW4R)".getBytes(),
            "AnmClr", "AnmClr(NW4R)".getBytes(),
            "AnmVis", "AnmVis(NW4R)".getBytes(),
            "Textures", "Textures(NW4R)".getBytes(),
            "3DModels", "3DModels(NW4R)".getBytes()
    );

    public void extractContainerBRRES(String inputFilePath, String outputFolderPath) {
        try {
            byte[] inputFileContent = Files.readAllBytes(new File(inputFilePath).toPath());
            List<Integer> bresOffsets = findBresOffsets(inputFileContent);
            splitBinaryFileByOffsets(inputFileContent, bresOffsets, new File(outputFolderPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Integer> findBresOffsets(byte[] content) {
        List<Integer> offsets = new ArrayList<>();

        for (int i = 0; i < content.length - 4; i++) {
            if (content[i]     == BRES[0] &&
                    content[i + 1] == BRES[1] &&
                    content[i + 2] == BRES[2] &&
                    content[i + 3] == BRES[3]) {

                offsets.add(i);
                i += 3;
            }
        }
        return offsets;
    }

    private static void splitBinaryFileByOffsets(byte[] content, List<Integer> offsets, File outputDir) throws IOException {
        if (offsets.isEmpty()) {
            return;
        }

        offsets.add(content.length);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Split off the file at each bres header
        for (int i = 0; i < offsets.size() - 1; i++) {
            int start = offsets.get(i);
            int end = offsets.get(i + 1);

            // Get the file length from the header and calculate the actual chunk length
            int chunkFileLength = getFileLengthFromHeader(content, start);
            int maxLength = end - start;
            int length = Math.min(chunkFileLength, maxLength);

            if (length <= 0 || start + length > content.length) {
                continue;
            }

            Set<String> matchedFolders = detectFolders(content, start, length);

            for (String folderName : matchedFolders) {
                File folder = new File(outputDir, folderName);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                writeFile(folder, "output_" + i + ".brres", content, start, length);
            }
        }
    }

    private static void writeFile(File dir, String name, byte[] data, int offset, int length)
            throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream(new File(dir, name))) {
            outputStream.write(data, offset, length);
        }
    }

    private static Set<String> detectFolders(byte[] content, int offset, int length) {
        Set<String> matchedFolders = new HashSet<>();
        int end = offset + length;

        for (int i = offset; i < end; i++) {
            for (Map.Entry<String, byte[]> entry : PATTERNS.entrySet()) {
                byte[] patternBytes = entry.getValue();
                if (i + patternBytes.length > end) {
                    continue;
                }

                boolean match = true;
                for (int j = 0; j < patternBytes.length; j++) {
                    if (content[i + j] != patternBytes[j]) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    matchedFolders.add(entry.getKey());
                }
            }
        }
        return matchedFolders;
    }

    private static short readShort(byte[] content, int offset) {
        return (short) (((content[offset] & 0xFF) << 8) |
                (content[offset + 1] & 0xFF));
    }

    private static int readInt(byte[] content, int offset) {
        return ((content[offset]     & 0xFF) << 24) |
                ((content[offset + 1] & 0xFF) << 16) |
                ((content[offset + 2] & 0xFF) << 8)  |
                (content[offset + 3] & 0xFF);
    }

    // Get the file length from the BRES header
    private static int getFileLengthFromHeader(byte[] content, int base) {
        // Read the Byte Order Mark (BOM) to determine byte order
        short bom = readShort(content, base + 4);

        //If BOM isn't 0xFEFF, then it isn't big endian. Just use the chunk size as a fallback
        if (bom != (short) 0xFEFF) {
            return Integer.MAX_VALUE;
        }

        // File length is at offset 0x08
        return readInt(content, base + 8);
    }
}