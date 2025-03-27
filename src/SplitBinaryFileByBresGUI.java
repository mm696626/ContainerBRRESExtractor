import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class SplitBinaryFileByBresGUI {

    public static void main(String[] args) {
        // Ask the user for the input file
        File inputFile = chooseInputFile();
        if (inputFile == null) {
            System.out.println("No input file selected. Exiting...");
            return;
        }

        // Ask the user for the output directory
        File outputDir = chooseOutputDirectory();
        if (outputDir == null) {
            System.out.println("No output directory selected. Exiting...");
            return;
        }

        // Byte sequence to search for ("bres")
        byte[] searchPattern = "bres".getBytes();

        try {
            // Read the binary file
            byte[] fileContent = readBinaryFile(inputFile);

            // Find all "bres" offsets in the binary content
            List<Integer> bresOffsets = findBresOffsets(fileContent, searchPattern);

            // Split the file into individual files based on "bres" offsets
            splitBinaryFileByOffsets(fileContent, bresOffsets, inputFile, outputDir);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to open a file chooser and select an input file
    private static File chooseInputFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select the Input Binary File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    // Method to open a file chooser and select an output directory
    private static File chooseOutputDirectory() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setDialogTitle("Select the Output Directory");
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = dirChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return dirChooser.getSelectedFile();
        }
        return null;
    }

    // Method to read binary file content
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

    // Method to find all offsets of the "bres" byte pattern in the binary data
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
                i += pattern.length - 1;  // Move past the found "bres" sequence
            }
        }
        return offsets;
    }

    // Method to split the binary file into multiple files based on the "bres" offsets
    private static void splitBinaryFileByOffsets(byte[] content, List<Integer> offsets, File inputFile, File outputDir) throws IOException {
        // Ensure that there is at least one offset, otherwise no file will be created
        if (offsets.isEmpty()) {
            System.out.println("No occurrences of 'bres' found in the file.");
            return;
        }

        // Add the end of file as a final "offset" for the last segment
        offsets.add(content.length);

        // Extract the original file extension
        String fileExtension = getFileExtension(inputFile);

        // Binary representations for each string
        Map<String, byte[]> patterns = new HashMap<>();
        patterns.put("AnmChr", "AnmChr(NW4R)".getBytes());
        patterns.put("AnmShp", "AnmShp(NW4R)".getBytes());
        patterns.put("AnmScn", "AnmScn(NW4R)".getBytes());
        patterns.put("AnmClr", "AnmClr(NW4R)".getBytes());
        patterns.put("AnmVis", "AnmVis(NW4R)".getBytes());
        patterns.put("Textures", "Textures(NW4R)".getBytes());
        patterns.put("3DModels", "3DModels(NW4R)".getBytes());

        // Create new files starting from each "bres" offset
        for (int i = 0; i < offsets.size() - 1; i++) {
            int start = offsets.get(i);
            int end = offsets.get(i + 1);
            byte[] splitContent = Arrays.copyOfRange(content, start, end);

            // Create a temporary file to hold the split content
            File outputFile = new File(outputDir, "output_" + i + fileExtension);

            // Write the split binary content to a new file
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(splitContent);
            }

            // Now check if any of the patterns exist in the split file content
            List<String> matchedFolders = new ArrayList<>();
            for (Map.Entry<String, byte[]> entry : patterns.entrySet()) {
                byte[] patternBytes = entry.getValue();
                if (containsPattern(splitContent, patternBytes)) {
                    matchedFolders.add(entry.getKey());
                }
            }

            // Copy the file to all matching folders
            if (!matchedFolders.isEmpty()) {
                for (String folderName : matchedFolders) {
                    // Create a new folder named after the matching string (without "NW4R")
                    File folder = new File(outputDir, folderName);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    // Copy the file to the new folder
                    File newFile = new File(folder, outputFile.getName());
                    try {
                        Files.copy(outputFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Copied: " + outputFile.getAbsolutePath() + " -> " + newFile.getAbsolutePath());
                    } catch (IOException e) {
                        System.out.println("Failed to copy: " + outputFile.getAbsolutePath());
                        e.printStackTrace();
                    }
                }
            } else {
                // If no match, leave the file in the main output directory
                System.out.println("Created: " + outputFile.getAbsolutePath());
            }
        }
    }

    // Method to check if a byte pattern exists in the content
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

    // Method to extract the file extension from the input file
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex); // Includes the dot (e.g., ".dat")
        }
        return ""; // No extension
    }
}