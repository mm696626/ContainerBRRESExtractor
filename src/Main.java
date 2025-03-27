// Container BRRES Extractor by Matt McCullough
// This is to help extracting model data from what the VGResource calls container BRRES files

import ui.ContainerBRRESExtractorUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        ContainerBRRESExtractorUI containerBRRESExtractorUI = new ContainerBRRESExtractorUI();
        containerBRRESExtractorUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        containerBRRESExtractorUI.pack();
        containerBRRESExtractorUI.setVisible(true);
    }
}