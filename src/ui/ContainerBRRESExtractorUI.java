package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ContainerBRRESExtractorUI extends JFrame implements ActionListener {

    //where to get the file from and where to save split files to
    private String inputFilePath = "";
    private String outputFolderPath = "";

    private JButton browseInputFile, browseOutputFolder, extract;

    private JLabel inputFileLabel, outputFolderLabel;
    private JTextField inputFileField, outputFolderField;

    GridBagConstraints gridBagConstraints = null;

    public ContainerBRRESExtractorUI()
    {
        setTitle("Container BRRES Extractor");

        inputFileLabel = new JLabel("Input File");
        outputFolderLabel = new JLabel("Output Folder");
        inputFileField = new JTextField(10);
        inputFileField.setEditable(false);
        outputFolderField = new JTextField(10);
        outputFolderField.setEditable(false);

        browseInputFile = new JButton("Browse for Input File");
        browseInputFile.addActionListener(this);

        browseOutputFolder = new JButton("Browse for Output Folder");
        browseOutputFolder.addActionListener(this);

        extract = new JButton("Extract");
        extract.addActionListener(this);

        setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();

        //input
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        add(inputFileLabel, gridBagConstraints);

        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=0;
        add(inputFileField, gridBagConstraints);

        gridBagConstraints.gridx=2;
        gridBagConstraints.gridy=0;
        add(browseInputFile, gridBagConstraints);

        //output
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=1;
        add(outputFolderLabel, gridBagConstraints);

        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=1;
        add(outputFolderField, gridBagConstraints);

        gridBagConstraints.gridx=2;
        gridBagConstraints.gridy=1;
        add(browseOutputFolder, gridBagConstraints);

        gridBagConstraints.gridx=2;
        gridBagConstraints.gridy=2;
        add(extract, gridBagConstraints);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
