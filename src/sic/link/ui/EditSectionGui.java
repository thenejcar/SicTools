package sic.link.ui;


import sic.link.LinkerError;
import sic.link.section.ExtDef;
import sic.link.section.ExtRef;
import sic.link.section.Section;
import sic.link.section.Sections;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditSectionGui {

    private Sections sections;
    private Section selectedSection = null;
    private List<String> symbols;
    private List<Boolean> symbolTypes;
    private String selectedSymbol = null;
    private boolean selectedDef = false;

    // left and right panels - for section list and symbol list
    private JPanel leftPanel;
    private JPanel rightPanel;
    // their list modes
    DefaultListModel<String> sectionModel;
    DefaultListModel<String> symbolModel;
    JList<String> sectionList;
    JList<String> symbolList;

    // edit panels - one visible at a time
    private JPanel editSectionPanel;
    private JPanel editSymbolPanel;
    // components for editSection
    JLabel sectionTitle;
    JButton upButton;
    JButton downButton;
    JTextField secName;
    JButton secDelete;
    JButton secApply;
    // components for editSection
    JLabel symbolTitle;
    JTextField symName;
    JButton symDelete;
    JButton symApply;

    JButton proceed;

    public EditSectionGui(Sections sections) {
        this.sections = sections;
    }

    public void sectionEdit(SectionEditListener listener) {
        JFrame frame = new JFrame("Section editor");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // initialize the components

        // for sections and symbols list
        leftPanel = new JPanel();
        rightPanel = new JPanel();
        sectionModel = new DefaultListModel<>();
        symbolModel = new DefaultListModel<>();

        // edit panels
        editSectionPanel = new JPanel();
        editSymbolPanel = new JPanel();

        sectionTitle = new JLabel("Selected section : sec name");
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        upButton = new JButton("Up");
        downButton = new JButton("Down");
        secName = new JTextField("Name", 8);
        secDelete = new JButton("Remove");
        secApply = new JButton("Rename");

        symbolTitle = new JLabel("selected Symbol : sym name");
        symbolTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        symName = new JTextField("Name", 8);
        symDelete = new JButton("Remove");
        symApply = new JButton("Rename");

        sectionList = new JList<>(sectionModel);
        symbolList = new JList<>(symbolModel);

        // final button
        proceed = new JButton("Continue");



        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(new JLabel("Sections"));
        leftPanel.add(new JScrollPane(sectionList));

        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(new JLabel("Symbols"));
        rightPanel.add(new JScrollPane(symbolList));

        // fill edit section panel
        editSectionPanel.setLayout(new GridLayout(4,1));
        JPanel secTitleContainer = new JPanel();
        secTitleContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        secTitleContainer.add(sectionTitle);
        editSectionPanel.add(secTitleContainer);
        JPanel updown = new JPanel();
        updown.add(upButton);
        updown.add(downButton);
        editSectionPanel.add(updown);
        JPanel secNamePanel = new JPanel();
        secNamePanel.add(new JLabel("Name:"));
        secNamePanel.add(secName);
        editSectionPanel.add(secNamePanel);
        JPanel secRenameDelete = new JPanel();
        secRenameDelete.add(secApply);
        secRenameDelete.add(secDelete);
        editSectionPanel.add(secRenameDelete);
        editSectionPanel.setVisible(false);

        // fill edit symbol panel
        editSymbolPanel.setLayout(new GridLayout(3,1));
        JPanel symTitleContainer = new JPanel();
        symTitleContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        symTitleContainer.add(symbolTitle);
        editSymbolPanel.add(symTitleContainer);
        JPanel symNamePanel = new JPanel();
        symNamePanel.add(new JLabel("Name"));
        symNamePanel.add(symName);
        editSymbolPanel.add(symNamePanel);
        JPanel symRenameDelete = new JPanel();
        symRenameDelete.add(symApply);
        symRenameDelete.add(symDelete);
        editSymbolPanel.add(symRenameDelete);
        editSymbolPanel.setVisible(false);

        // fill sections panel
        fillSections();

        sectionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (sectionList.getSelectedIndex() != -1) {

                    selectedSection = sections.getSections().get(sectionList.getSelectedIndex());

                    editSectionPanel.setVisible(true);
                    editSymbolPanel.setVisible(false);
                    sectionTitle.setText("Selected section: " + selectedSection.getName());
                    secName.setText(selectedSection.getName());

                    // fill symbols panel
                    fillSymbols();
                }
            }
        });
        symbolList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (symbolList.getSelectedIndex() != -1) {

                    selectedSymbol = symbols.get(symbolList.getSelectedIndex());
                    selectedDef = symbolTypes.get(symbolList.getSelectedIndex());

                    editSymbolPanel.setVisible(true);
                    editSectionPanel.setVisible(false);
                    if (selectedDef)
                        symbolTitle.setText("Selected definition: " + selectedSymbol);
                    else
                        symbolTitle.setText("Selected reference: " + selectedSymbol);

                    symName.setText(selectedSymbol);
                }
            }
        });

        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = sectionList.getSelectedIndex();
                if (index > 0 && selectedSection != null) {
                    try {
                        sections.move(selectedSection.getName(), index-1);
                        fillSections();
                        sectionList.setSelectedIndex(index-1);
                    } catch (LinkerError linkerError) {
                        LinkerGui.showError(linkerError.getMessage());
                    }
                }
            }
        });

        downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = sectionList.getSelectedIndex();
                if (index >= 0 && index < sections.getSections().size()-1 && selectedSection != null) {
                    try {
                        sections.move(selectedSection.getName(), sectionList.getSelectedIndex()+1);
                        fillSections();
                        sectionList.setSelectedIndex(index+1);
                    } catch (LinkerError linkerError) {
                        LinkerGui.showError(linkerError.getMessage());
                    }
                }
            }
        });

        secApply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (secName.getText().length() > 6) {
                    LinkerGui.showError("Please enter a name that has 6 or less characters.");
                } else if (!selectedSection.getName().equals(secName.getText())) {
                    try {
                        sections.rename(selectedSection.getName(), secName.getText());
                        fillSections();
                        sectionTitle.setText("Selected section: " + selectedSection.getName());
                    } catch (LinkerError linkerError) {
                        LinkerGui.showError(linkerError.getMessage());
                    }
                }
            }
        });

        symApply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (symName.getText().length() > 6) {
                    LinkerGui.showError("Please enter a name that has 6 or less characters.");
                } else if (!selectedSymbol.equals(symName.getText())) {
                    try {
                        if (selectedDef)
                            sections.renameDef(selectedSection.getName(), selectedSymbol, symName.getText());
                        else
                            sections.renameRef(selectedSection.getName(), selectedSymbol, symName.getText());

                        fillSymbols();
                        selectedSymbol = symName.getText();
                        if (selectedDef)
                            symbolTitle.setText("Selected definition: " + selectedSymbol);
                        else
                            symbolTitle.setText("Selected reference: " + selectedSymbol);

                    } catch (LinkerError linkerError) {
                        LinkerGui.showError(linkerError.getMessage());
                    }
                }
            }
        });

        secDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int dialogResult = JOptionPane.showConfirmDialog (null, "Would you like to remove section " + selectedSection.getName() + " from linking?","Confirmation", JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    try {
                        sections.remove(selectedSection.getName());
                        fillSections();
                        editSectionPanel.setVisible(false);
                    } catch (LinkerError linkerError) {
                        LinkerGui.showError(linkerError.getMessage());
                    }
                }
            }
        });

        symDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dialogResult = JOptionPane.showConfirmDialog (null, "Would you like to remove symbol " + selectedSymbol + " from section " + selectedSection.getName() + "?","Confirmation", JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    try {
                        if (selectedDef)
                            sections.removeDef(selectedSection.getName(), selectedSymbol);
                        else
                            sections.removeRef(selectedSection.getName(), selectedSymbol);

                        fillSymbols();
                        editSymbolPanel.setVisible(false);
                    } catch (LinkerError linkerError) {
                        LinkerGui.showError(linkerError.getMessage());
                    }
                }
            }
        });

        proceed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();

                listener.onEdited(sections, "0");
            }
        });

        JPanel scrollpanels = new JPanel();
        scrollpanels.setLayout(new GridLayout(1, 2));
        scrollpanels.add(leftPanel);
        scrollpanels.add(rightPanel);

        JPanel editPanel = new JPanel();
        editPanel.add(editSectionPanel);
        editPanel.add(editSymbolPanel);

        frame.add(new JPanel(), BorderLayout.NORTH);
        frame.add(scrollpanels, BorderLayout.CENTER);
        frame.add(new JPanel(), BorderLayout.WEST);
        frame.add(editPanel, BorderLayout.EAST);
        frame.add(proceed, BorderLayout.SOUTH);

        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    private void fillSections() {
        sectionModel.clear();
        int i=0;
        for (Section s : sections.getSections()) {
            sectionModel.addElement(i + " - " + s.getName());
            i++;
        }
    }

    private void fillSymbols() {
        symbolModel.clear();
        symbols = new ArrayList<>();
        symbolTypes = new ArrayList<>();

        for (ExtDef d : selectedSection.getExtDefs()) {
            symbols.add(d.getName());
            symbolTypes.add(true);
            symbolModel.addElement(d.getName() + " (definition)");
        }
        for (ExtRef r : selectedSection.getExtRefs()) {
            symbols.add(r.getName());
            symbolTypes.add(false);
            symbolModel.addElement(r.getName() + " (reference)");

        }
    }
}
