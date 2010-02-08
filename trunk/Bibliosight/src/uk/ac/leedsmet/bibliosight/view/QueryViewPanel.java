/*
 * Copyright (c) 2010, Leeds Metropolitan University
 *
 * This file is part of Bibliosight.
 *
 * Bibliosight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bibliosight is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bibliosight. If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * QueryViewPanel.java
 *
 * Created on 03-Dec-2009, 15:05:13
 */

package uk.ac.leedsmet.bibliosight.view;

import com.thomsonreuters.wokmws.cxf.woksearchlite.EditionDesc;
import com.thomsonreuters.wokmws.cxf.woksearchlite.QueryField;
import com.thomsonreuters.wokmws.cxf.woksearchlite.TimeSpan;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import uk.ac.leedsmet.bibliosight.controller.DefaultController;
import uk.ac.leedsmet.bibliosight.controller.DefaultController.DateMode;
import uk.ac.leedsmet.bibliosight.controller.DefaultController.SymbolicTimeSpan;
import uk.ac.leedsmet.bibliosight.utilities.ProxyOptionsDialog;

/**
 * The main view panel for entering search data in the Bibliosight client
 * @author Mike Taylor
 */
public class QueryViewPanel extends AbstractViewPanel {

    private static final String MSG_RESULTS_XML_IS_EMPTY = "The results XML is currently empty.";

    /**
     * Controller used by the view to communicate changes
     */
    private DefaultController controller_;

    /**
     * Top level container the output view panel
     */
    private JFrame outputFrame_;

    /**
     * View panel for displaying output data
     */
    private OutputViewPanel outputViewPanel_;

    /**
     * Local copy of the proxy host name that can be passed to the proxy options dialog
     */
    private String currentProxyHost_;

    /**
     * Local copy of the proxy host port that can be passed to the proxy options dialog
     */
    private Integer currentProxyPort_;

    /**
     * Create a new query view panel
     * @param controller Controller to connect the view panel with the data model
     */
    public QueryViewPanel(DefaultController controller)
    {
        this.controller_ = controller;

        // Use the same controller to connect the output view panel with the data model
        outputViewPanel_ = new OutputViewPanel(controller);
        controller.addView(outputViewPanel_);

        outputFrame_ = new JFrame("Bibliosight: Output");
        outputFrame_.setContentPane(outputViewPanel_);
        outputFrame_.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        outputFrame_.pack();
        
        outputFrame_.setVisible(true);

        initComponents();
        outputFrame_.setLocation(this.getX() + 32, 32);
    }

    /**
     * Returns the list of currently selected editions
     * @return the selected editions as a list of EditionDesc objects
     */
    private List<EditionDesc> getSelectedEditions()
    {
        List<EditionDesc> selectedEditions = new ArrayList<EditionDesc>();

        if (editionsAhciCheckbox.isSelected())
        {
            EditionDesc edition = new EditionDesc();
            edition.setEdition("AHCI");
            edition.setCollection("WOS");
            selectedEditions.add(edition);
        }

        if (editionsIstpCheckbox.isSelected())
        {
            EditionDesc edition = new EditionDesc();
            edition.setEdition("ISTP");
            edition.setCollection("WOS");
            selectedEditions.add(edition);
        }

        if (editionsSciCheckbox.isSelected())
        {
            EditionDesc edition = new EditionDesc();
            edition.setEdition("SCI");
            edition.setCollection("WOS");
            selectedEditions.add(edition);
        }

        if (editionsSsciCheckbox.isSelected())
        {
            EditionDesc edition = new EditionDesc();
            edition.setEdition("SSCI");
            edition.setCollection("WOS");
            selectedEditions.add(edition);
        }

        return selectedEditions;
    }

    /**
     * Returns the list of currently selected sort fields
     * @return the selected sort fields as a list of QueryField objects
     */
    private List<QueryField> getSelectedSortFields()
    {
        List<QueryField> sortFields = new ArrayList<QueryField>();
        QueryField selectedQueryField = new QueryField();

        Object fieldName = sortByFieldComboBox.getSelectedItem();
        Object fieldOrder = sortByOrderComboBox.getSelectedItem();
        String fieldOrderCode = null;

        if (fieldOrder.toString().equals("Ascending"))
        {
            fieldOrderCode = "A";
        }
        else if (fieldOrder.toString().equals("Descending"))
        {
            fieldOrderCode = "D";
        }

        selectedQueryField.setName(fieldName.toString());
        selectedQueryField.setSort(fieldOrderCode);

        sortFields.add(selectedQueryField);

        return sortFields;
    }

    /**
     * Returns the currently selected symbolic time span
     * @return the selected symbolic time span or null if there is no selection
     */
    private SymbolicTimeSpan getSelectedSymbolicTimeSpan()
    {
        SymbolicTimeSpan selectedSymbolicTimeSpan = null;

        if (symbolicTimeSpanWeekRadioButton.isSelected())
        {
            selectedSymbolicTimeSpan = SymbolicTimeSpan.ONE_WEEK;
        }
        else if(symbolicTimeSpanTwoWeekRadioButton.isSelected())
        {
            selectedSymbolicTimeSpan = SymbolicTimeSpan.TWO_WEEK;
        }
        else if (symbolicTimeSpanFourWeekRadioButton.isSelected())
        {
            selectedSymbolicTimeSpan = SymbolicTimeSpan.FOUR_WEEK;
        }

        return selectedSymbolicTimeSpan;
    }

    /**
     * Returns the current time span (date range) data
     * @return a entered time span as a TimeSpan object
     */
    private TimeSpan getTimeSpan()
    {
        TimeSpan timeSpan = new TimeSpan();

        timeSpan.setBegin(timeSpanBeginFormattedTextField.getText());
        timeSpan.setEnd(timeSpanEndFormattedTextField.getText());

        return timeSpan;
    }

    /**
     * Saves the current contents of the results output to a file
     */
    private void saveResultsOutputToFile()
    {
        outputViewPanel_.appendToLogText("Saving results XML...");

        try
        {
            // A date/timestamp is a reasonably useful default for the filename
            Date saveDate = new Date();
            SimpleDateFormat saveDateFormat = new SimpleDateFormat();
            Calendar saveCalendar = Calendar.getInstance();
            String saveDateValue = null;

            saveDateFormat.setCalendar(saveCalendar);
            saveDateFormat.applyPattern("yyyy-MM-dd_HHmm");
            saveDateValue = saveDateFormat.format(saveDate);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("query_" + saveDateValue + ".xml"));
            Integer fileChooserResult = fileChooser.showSaveDialog(this);

            switch (fileChooserResult)
            {
                case JFileChooser.APPROVE_OPTION:
                    File file = fileChooser.getSelectedFile();
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(outputViewPanel_.getResultsOutputText());
                    fileWriter.close();
            }
        }
        catch (IOException ex)
        {
            outputViewPanel_.appendToLogText("Results XML could not be saved");

            JOptionPane.showMessageDialog(this, "Results XML could not be saved: " + ex.getMessage(), "Error message", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        timeSpanButtonGroup = new javax.swing.ButtonGroup();
        symbolicTimeSpanButtonGroup = new javax.swing.ButtonGroup();
        queryOptionsPanel = new javax.swing.JPanel();
        queryTextField = new javax.swing.JTextField();
        queryLabel = new javax.swing.JLabel();
        queryDatePanel = new javax.swing.JPanel();
        dateModeRangeRadioButton = new javax.swing.JRadioButton();
        timeSpanBeginLabel = new javax.swing.JLabel();
        timeSpanBeginFormattedTextField = new javax.swing.JFormattedTextField();
        timeSpanEndLabel = new javax.swing.JLabel();
        timeSpanEndFormattedTextField = new javax.swing.JFormattedTextField();
        dateModeRecentRadioButton = new javax.swing.JRadioButton();
        symbolicTimeSpanLabel = new javax.swing.JLabel();
        symbolicTimeSpanWeekRadioButton = new javax.swing.JRadioButton();
        symbolicTimeSpanTwoWeekRadioButton = new javax.swing.JRadioButton();
        symbolicTimeSpanFourWeekRadioButton = new javax.swing.JRadioButton();
        queryDatabasePanel = new javax.swing.JPanel();
        databaseIdLabel = new javax.swing.JLabel();
        databaseIdTextField = new javax.swing.JTextField();
        editionsLabel = new javax.swing.JLabel();
        editionsSciCheckbox = new javax.swing.JCheckBox();
        editionsSsciCheckbox = new javax.swing.JCheckBox();
        editionsAhciCheckbox = new javax.swing.JCheckBox();
        editionsIstpCheckbox = new javax.swing.JCheckBox();
        retrieveOptionsPanel = new javax.swing.JPanel();
        sortByLabel = new javax.swing.JLabel();
        sortByFieldComboBox = new javax.swing.JComboBox();
        sortByOrderComboBox = new javax.swing.JComboBox();
        startRecordLabel = new javax.swing.JLabel();
        startRecordFormattedTextField = new javax.swing.JFormattedTextField();
        maxRecordsLabel = new javax.swing.JLabel();
        maxRecordsFormattedTextField = new javax.swing.JFormattedTextField();
        proxySettingsButton = new javax.swing.JButton();
        saveResultsButton = new javax.swing.JButton();
        performSearchButton = new javax.swing.JButton();
        viewResultsButton = new javax.swing.JButton();

        queryOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Query options"));

        queryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryTextFieldActionPerformed(evt);
            }
        });
        queryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                queryTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                queryTextFieldFocusLost(evt);
            }
        });

        queryLabel.setText("Query");

        queryDatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Date"));

        timeSpanButtonGroup.add(dateModeRangeRadioButton);
        dateModeRangeRadioButton.setSelected(true);
        dateModeRangeRadioButton.setText("Date range (formatted as YYYY-MM-DD)");
        dateModeRangeRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                dateModeRangeRadioButtonItemStateChanged(evt);
            }
        });

        timeSpanBeginLabel.setText("Start date");

        timeSpanBeginFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("yyyy-MM-dd"))));
        timeSpanBeginFormattedTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeSpanBeginFormattedTextFieldActionPerformed(evt);
            }
        });
        timeSpanBeginFormattedTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                timeSpanBeginFormattedTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                timeSpanBeginFormattedTextFieldFocusLost(evt);
            }
        });

        timeSpanEndLabel.setText("End date");

        timeSpanEndFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("yyyy-MM-dd"))));
        timeSpanEndFormattedTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeSpanEndFormattedTextFieldActionPerformed(evt);
            }
        });
        timeSpanEndFormattedTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                timeSpanEndFormattedTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                timeSpanEndFormattedTextFieldFocusLost(evt);
            }
        });

        timeSpanButtonGroup.add(dateModeRecentRadioButton);
        dateModeRecentRadioButton.setText("Recent updates");
        dateModeRecentRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                dateModeRecentRadioButtonItemStateChanged(evt);
            }
        });

        symbolicTimeSpanLabel.setText("Within the last");
        symbolicTimeSpanLabel.setEnabled(false);

        symbolicTimeSpanButtonGroup.add(symbolicTimeSpanWeekRadioButton);
        symbolicTimeSpanWeekRadioButton.setText("week");
        symbolicTimeSpanWeekRadioButton.setEnabled(false);
        symbolicTimeSpanWeekRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                symbolicTimeSpanWeekRadioButtonItemStateChanged(evt);
            }
        });

        symbolicTimeSpanButtonGroup.add(symbolicTimeSpanTwoWeekRadioButton);
        symbolicTimeSpanTwoWeekRadioButton.setText("2 weeks");
        symbolicTimeSpanTwoWeekRadioButton.setEnabled(false);
        symbolicTimeSpanTwoWeekRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                symbolicTimeSpanTwoWeekRadioButtonItemStateChanged(evt);
            }
        });

        symbolicTimeSpanButtonGroup.add(symbolicTimeSpanFourWeekRadioButton);
        symbolicTimeSpanFourWeekRadioButton.setText("4 weeks");
        symbolicTimeSpanFourWeekRadioButton.setEnabled(false);
        symbolicTimeSpanFourWeekRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                symbolicTimeSpanFourWeekRadioButtonItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout queryDatePanelLayout = new javax.swing.GroupLayout(queryDatePanel);
        queryDatePanel.setLayout(queryDatePanelLayout);
        queryDatePanelLayout.setHorizontalGroup(
            queryDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryDatePanelLayout.createSequentialGroup()
                .addGroup(queryDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateModeRangeRadioButton)
                    .addGroup(queryDatePanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(timeSpanBeginLabel)
                        .addGap(15, 15, 15)
                        .addComponent(timeSpanBeginFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(timeSpanEndLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeSpanEndFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dateModeRecentRadioButton)
                    .addGroup(queryDatePanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(symbolicTimeSpanLabel)
                        .addGap(11, 11, 11)
                        .addComponent(symbolicTimeSpanWeekRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(symbolicTimeSpanTwoWeekRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(symbolicTimeSpanFourWeekRadioButton)))
                .addContainerGap())
        );
        queryDatePanelLayout.setVerticalGroup(
            queryDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryDatePanelLayout.createSequentialGroup()
                .addComponent(dateModeRangeRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(queryDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeSpanBeginLabel)
                    .addComponent(timeSpanBeginFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeSpanEndLabel)
                    .addComponent(timeSpanEndFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateModeRecentRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(queryDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(symbolicTimeSpanLabel)
                    .addComponent(symbolicTimeSpanWeekRadioButton)
                    .addComponent(symbolicTimeSpanTwoWeekRadioButton)
                    .addComponent(symbolicTimeSpanFourWeekRadioButton)))
        );

        queryDatabasePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Database"));

        databaseIdLabel.setText("Database ID");

        databaseIdTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databaseIdTextFieldActionPerformed(evt);
            }
        });
        databaseIdTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                databaseIdTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                databaseIdTextFieldFocusLost(evt);
            }
        });

        editionsLabel.setText("Editions");

        editionsSciCheckbox.setSelected(true);
        editionsSciCheckbox.setText("SCI");
        editionsSciCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                editionsSciCheckboxItemStateChanged(evt);
            }
        });

        editionsSsciCheckbox.setSelected(true);
        editionsSsciCheckbox.setText("SSCI");
        editionsSsciCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                editionsSsciCheckboxItemStateChanged(evt);
            }
        });

        editionsAhciCheckbox.setSelected(true);
        editionsAhciCheckbox.setText("AHCI");
        editionsAhciCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                editionsAhciCheckboxItemStateChanged(evt);
            }
        });

        editionsIstpCheckbox.setSelected(true);
        editionsIstpCheckbox.setText("ISTP");
        editionsIstpCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                editionsIstpCheckboxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout queryDatabasePanelLayout = new javax.swing.GroupLayout(queryDatabasePanel);
        queryDatabasePanel.setLayout(queryDatabasePanelLayout);
        queryDatabasePanelLayout.setHorizontalGroup(
            queryDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryDatabasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(queryDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(editionsLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(databaseIdLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(queryDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(queryDatabasePanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(databaseIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, queryDatabasePanelLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(editionsAhciCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(editionsIstpCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(editionsSciCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(editionsSsciCheckbox)))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        queryDatabasePanelLayout.setVerticalGroup(
            queryDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryDatabasePanelLayout.createSequentialGroup()
                .addGroup(queryDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(databaseIdLabel))
                .addGroup(queryDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(queryDatabasePanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(queryDatabasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(editionsAhciCheckbox)
                            .addComponent(editionsIstpCheckbox)
                            .addComponent(editionsSciCheckbox)
                            .addComponent(editionsSsciCheckbox))
                        .addGap(46, 46, 46))
                    .addGroup(queryDatabasePanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(editionsLabel)
                        .addContainerGap())))
        );

        javax.swing.GroupLayout queryOptionsPanelLayout = new javax.swing.GroupLayout(queryOptionsPanel);
        queryOptionsPanel.setLayout(queryOptionsPanelLayout);
        queryOptionsPanelLayout.setHorizontalGroup(
            queryOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(queryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(queryTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(queryDatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(queryDatabasePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        queryOptionsPanelLayout.setVerticalGroup(
            queryOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryOptionsPanelLayout.createSequentialGroup()
                .addGroup(queryOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(queryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queryLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(queryDatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(queryDatabasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        retrieveOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Retrieve options"));
        retrieveOptionsPanel.setPreferredSize(new java.awt.Dimension(479, 104));

        sortByLabel.setText("Sort by");

        sortByFieldComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Date" }));
        sortByFieldComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sortByFieldComboBoxItemStateChanged(evt);
            }
        });

        sortByOrderComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ascending", "Descending" }));
        sortByOrderComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sortByOrderComboBoxItemStateChanged(evt);
            }
        });

        startRecordLabel.setText("Start Record");

        startRecordFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        startRecordFormattedTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startRecordFormattedTextFieldActionPerformed(evt);
            }
        });
        startRecordFormattedTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                startRecordFormattedTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                startRecordFormattedTextFieldFocusLost(evt);
            }
        });

        maxRecordsLabel.setText("Maximum records to retrieve (1 - 100)");

        maxRecordsFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        maxRecordsFormattedTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxRecordsFormattedTextFieldActionPerformed(evt);
            }
        });
        maxRecordsFormattedTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                maxRecordsFormattedTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                maxRecordsFormattedTextFieldFocusLost(evt);
            }
        });

        javax.swing.GroupLayout retrieveOptionsPanelLayout = new javax.swing.GroupLayout(retrieveOptionsPanel);
        retrieveOptionsPanel.setLayout(retrieveOptionsPanelLayout);
        retrieveOptionsPanelLayout.setHorizontalGroup(
            retrieveOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(retrieveOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(retrieveOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, retrieveOptionsPanelLayout.createSequentialGroup()
                        .addComponent(sortByLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByFieldComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(startRecordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                    .addComponent(maxRecordsLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(retrieveOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxRecordsFormattedTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sortByOrderComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startRecordFormattedTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        retrieveOptionsPanelLayout.setVerticalGroup(
            retrieveOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, retrieveOptionsPanelLayout.createSequentialGroup()
                .addGroup(retrieveOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startRecordLabel)
                    .addComponent(startRecordFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(retrieveOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxRecordsFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxRecordsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(retrieveOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sortByLabel)
                    .addComponent(sortByOrderComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sortByFieldComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(104, 104, 104))
        );

        proxySettingsButton.setText("Proxy settings…");
        proxySettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proxySettingsButtonActionPerformed(evt);
            }
        });

        saveResultsButton.setText("Save results…");
        saveResultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveResultsButtonActionPerformed(evt);
            }
        });

        performSearchButton.setText("Perform search request");
        performSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performSearchButtonActionPerformed(evt);
            }
        });

        viewResultsButton.setText("View results");
        viewResultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewResultsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(retrieveOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(proxySettingsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(viewResultsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveResultsButton))
                    .addComponent(performSearchButton)
                    .addComponent(queryOptionsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(queryOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(retrieveOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxySettingsButton)
                    .addComponent(viewResultsButton)
                    .addComponent(saveResultsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(performSearchButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void dateModeRangeRadioButtonItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_dateModeRangeRadioButtonItemStateChanged
    {//GEN-HEADEREND:event_dateModeRangeRadioButtonItemStateChanged
        switch (evt.getStateChange())
        {
            case ItemEvent.SELECTED :
                timeSpanBeginLabel.setEnabled(true);
                timeSpanBeginFormattedTextField.setEnabled(true);
                timeSpanEndLabel.setEnabled(true);
                timeSpanEndFormattedTextField.setEnabled(true);
                controller_.changeDateMode(DateMode.RANGE);

                outputViewPanel_.appendToLogText("Date mode set to 'Date range'");

                break;

            case ItemEvent.DESELECTED :
                timeSpanBeginLabel.setEnabled(false);
                timeSpanBeginFormattedTextField.setEnabled(false);
                timeSpanEndLabel.setEnabled(false);
                timeSpanEndFormattedTextField.setEnabled(false);
                break;
        }
    }//GEN-LAST:event_dateModeRangeRadioButtonItemStateChanged

    private void dateModeRecentRadioButtonItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_dateModeRecentRadioButtonItemStateChanged
    {//GEN-HEADEREND:event_dateModeRecentRadioButtonItemStateChanged
        switch (evt.getStateChange())
        {
            case ItemEvent.SELECTED :
                symbolicTimeSpanLabel.setEnabled(true);
                symbolicTimeSpanWeekRadioButton.setEnabled(true);
                symbolicTimeSpanTwoWeekRadioButton.setEnabled(true);
                symbolicTimeSpanFourWeekRadioButton.setEnabled(true);
                controller_.changeDateMode(DateMode.RECENT);

                outputViewPanel_.appendToLogText("Date mode set to 'Recent date'");

                break;

            case ItemEvent.DESELECTED :
                symbolicTimeSpanLabel.setEnabled(false);
                symbolicTimeSpanWeekRadioButton.setEnabled(false);
                symbolicTimeSpanTwoWeekRadioButton.setEnabled(false);
                symbolicTimeSpanFourWeekRadioButton.setEnabled(false);
                break;
        }
    }//GEN-LAST:event_dateModeRecentRadioButtonItemStateChanged

    private void proxySettingsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_proxySettingsButtonActionPerformed
    {//GEN-HEADEREND:event_proxySettingsButtonActionPerformed
        ProxyOptionsDialog proxyOptionsDialog = new ProxyOptionsDialog((Frame)this.getTopLevelAncestor());
        proxyOptionsDialog.setProxyHost(currentProxyHost_);

        if (currentProxyPort_ != null)
        {
            proxyOptionsDialog.setProxyPort(currentProxyPort_);
        }
        proxyOptionsDialog.setVisible(true);

        String returnedProxyHost = proxyOptionsDialog.getProxyHost();
        Integer returnedProxyPort = proxyOptionsDialog.getProxyPort();

        if (returnedProxyHost != null)
        {
            controller_.changeProxyHost(returnedProxyHost);
        }
        if (returnedProxyPort != null)
        {
            controller_.changeProxyPort(returnedProxyPort);
        }
    }//GEN-LAST:event_proxySettingsButtonActionPerformed

    private void viewResultsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewResultsButtonActionPerformed
    {//GEN-HEADEREND:event_viewResultsButtonActionPerformed
        outputViewPanel_.selectResultsTab();
        outputFrame_.toFront();

        if (!outputFrame_.isVisible())
        {
            outputFrame_.setVisible(true);
        }
    }//GEN-LAST:event_viewResultsButtonActionPerformed

    private void databaseIdTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_databaseIdTextFieldActionPerformed
    {//GEN-HEADEREND:event_databaseIdTextFieldActionPerformed
        try {
            controller_.changeDatabaseId(databaseIdTextField.getText());
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_databaseIdTextFieldActionPerformed

    private void databaseIdTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_databaseIdTextFieldFocusLost
    {//GEN-HEADEREND:event_databaseIdTextFieldFocusLost
        try {
            controller_.changeDatabaseId(databaseIdTextField.getText());
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_databaseIdTextFieldFocusLost

    private void editionsAhciCheckboxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_editionsAhciCheckboxItemStateChanged
    {//GEN-HEADEREND:event_editionsAhciCheckboxItemStateChanged
        List<EditionDesc> selectedEditions = getSelectedEditions();
        try {
            controller_.changeEditions(selectedEditions);
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_editionsAhciCheckboxItemStateChanged

    private void editionsIstpCheckboxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_editionsIstpCheckboxItemStateChanged
    {//GEN-HEADEREND:event_editionsIstpCheckboxItemStateChanged
        List<EditionDesc> selectedEditions = getSelectedEditions();
        try {
            controller_.changeEditions(selectedEditions);
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_editionsIstpCheckboxItemStateChanged

    private void editionsSciCheckboxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_editionsSciCheckboxItemStateChanged
    {//GEN-HEADEREND:event_editionsSciCheckboxItemStateChanged
        List<EditionDesc> selectedEditions = getSelectedEditions();
        try {
            controller_.changeEditions(selectedEditions);
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_editionsSciCheckboxItemStateChanged

    private void editionsSsciCheckboxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_editionsSsciCheckboxItemStateChanged
    {//GEN-HEADEREND:event_editionsSsciCheckboxItemStateChanged
        List<EditionDesc> selectedEditions = getSelectedEditions();
        try {
            controller_.changeEditions(selectedEditions);
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_editionsSsciCheckboxItemStateChanged

    private void startRecordFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_startRecordFormattedTextFieldActionPerformed
    {//GEN-HEADEREND:event_startRecordFormattedTextFieldActionPerformed
        try {
            controller_.changeFirstRecord(Integer.parseInt(startRecordFormattedTextField.getText()));
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_startRecordFormattedTextFieldActionPerformed

    private void startRecordFormattedTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_startRecordFormattedTextFieldFocusLost
    {//GEN-HEADEREND:event_startRecordFormattedTextFieldFocusLost
        try {
            controller_.changeFirstRecord(Integer.parseInt(startRecordFormattedTextField.getText()));
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_startRecordFormattedTextFieldFocusLost

    private void maxRecordsFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_maxRecordsFormattedTextFieldActionPerformed
    {//GEN-HEADEREND:event_maxRecordsFormattedTextFieldActionPerformed
        try {
            controller_.changeMaxResultCount(Integer.parseInt(maxRecordsFormattedTextField.getText()));
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_maxRecordsFormattedTextFieldActionPerformed

    private void maxRecordsFormattedTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_maxRecordsFormattedTextFieldFocusLost
    {//GEN-HEADEREND:event_maxRecordsFormattedTextFieldFocusLost
        try {
            controller_.changeMaxResultCount(Integer.parseInt(maxRecordsFormattedTextField.getText()));
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_maxRecordsFormattedTextFieldFocusLost

    private void sortByFieldComboBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_sortByFieldComboBoxItemStateChanged
    {//GEN-HEADEREND:event_sortByFieldComboBoxItemStateChanged
        List<QueryField> sortFields = getSelectedSortFields();

        try
        {
            controller_.changeSortFields((List<QueryField>)sortFields);
        }
        catch (Exception ex)
        {
            Logger.getLogger(QueryViewPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_sortByFieldComboBoxItemStateChanged

    private void sortByOrderComboBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_sortByOrderComboBoxItemStateChanged
    {//GEN-HEADEREND:event_sortByOrderComboBoxItemStateChanged
        List<QueryField> sortFields = getSelectedSortFields();

        try
        {
            controller_.changeSortFields((List<QueryField>)sortFields);
        }
        catch (Exception ex)
        {
            Logger.getLogger(QueryViewPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_sortByOrderComboBoxItemStateChanged

    private void symbolicTimeSpanWeekRadioButtonItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_symbolicTimeSpanWeekRadioButtonItemStateChanged
    {//GEN-HEADEREND:event_symbolicTimeSpanWeekRadioButtonItemStateChanged
        SymbolicTimeSpan selectedSymbolicTimeSpan = getSelectedSymbolicTimeSpan();
        try {
            controller_.changeSymbolicTimeSpan(selectedSymbolicTimeSpan);
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_symbolicTimeSpanWeekRadioButtonItemStateChanged

    private void symbolicTimeSpanTwoWeekRadioButtonItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_symbolicTimeSpanTwoWeekRadioButtonItemStateChanged
    {//GEN-HEADEREND:event_symbolicTimeSpanTwoWeekRadioButtonItemStateChanged
        SymbolicTimeSpan selectedSymbolicTimeSpan = getSelectedSymbolicTimeSpan();
        try {
            controller_.changeSymbolicTimeSpan(selectedSymbolicTimeSpan);
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_symbolicTimeSpanTwoWeekRadioButtonItemStateChanged

    private void symbolicTimeSpanFourWeekRadioButtonItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_symbolicTimeSpanFourWeekRadioButtonItemStateChanged
    {//GEN-HEADEREND:event_symbolicTimeSpanFourWeekRadioButtonItemStateChanged
        SymbolicTimeSpan selectedSymbolicTimeSpan = getSelectedSymbolicTimeSpan();
        try {
            controller_.changeSymbolicTimeSpan(selectedSymbolicTimeSpan);
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_symbolicTimeSpanFourWeekRadioButtonItemStateChanged

    private void timeSpanBeginFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_timeSpanBeginFormattedTextFieldActionPerformed
    {//GEN-HEADEREND:event_timeSpanBeginFormattedTextFieldActionPerformed
        TimeSpan timeSpan = getTimeSpan();

        try {
            controller_.changeTimeSpan(timeSpan);
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_timeSpanBeginFormattedTextFieldActionPerformed

    private void timeSpanBeginFormattedTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_timeSpanBeginFormattedTextFieldFocusLost
    {//GEN-HEADEREND:event_timeSpanBeginFormattedTextFieldFocusLost
        TimeSpan timeSpan = getTimeSpan();

        try {
            controller_.changeTimeSpan(timeSpan);
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_timeSpanBeginFormattedTextFieldFocusLost

    private void timeSpanEndFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_timeSpanEndFormattedTextFieldActionPerformed
    {//GEN-HEADEREND:event_timeSpanEndFormattedTextFieldActionPerformed
        TimeSpan timeSpan = getTimeSpan();

        try {
            controller_.changeTimeSpan(timeSpan);
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_timeSpanEndFormattedTextFieldActionPerformed

    private void timeSpanEndFormattedTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_timeSpanEndFormattedTextFieldFocusLost
    {//GEN-HEADEREND:event_timeSpanEndFormattedTextFieldFocusLost
        TimeSpan timeSpan = getTimeSpan();

        try {
            controller_.changeTimeSpan(timeSpan);
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_timeSpanEndFormattedTextFieldFocusLost

    private void queryTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_queryTextFieldActionPerformed
    {//GEN-HEADEREND:event_queryTextFieldActionPerformed
        try {
            controller_.changeUserQuery(queryTextField.getText());
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_queryTextFieldActionPerformed

    private void queryTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_queryTextFieldFocusLost
    {//GEN-HEADEREND:event_queryTextFieldFocusLost
        try {
            controller_.changeUserQuery(queryTextField.getText());
        } catch (Exception ex) {
            //  Handle exception
        }
    }//GEN-LAST:event_queryTextFieldFocusLost

    private void performSearchButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_performSearchButtonActionPerformed
    {//GEN-HEADEREND:event_performSearchButtonActionPerformed
        try {
            controller_.executeWsLiteQuery();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_performSearchButtonActionPerformed

    private void queryTextFieldFocusGained(java.awt.event.FocusEvent evt)//GEN-FIRST:event_queryTextFieldFocusGained
    {//GEN-HEADEREND:event_queryTextFieldFocusGained
        queryTextField.selectAll();
    }//GEN-LAST:event_queryTextFieldFocusGained

    private void timeSpanBeginFormattedTextFieldFocusGained(java.awt.event.FocusEvent evt)//GEN-FIRST:event_timeSpanBeginFormattedTextFieldFocusGained
    {//GEN-HEADEREND:event_timeSpanBeginFormattedTextFieldFocusGained
        timeSpanBeginFormattedTextField.selectAll();
    }//GEN-LAST:event_timeSpanBeginFormattedTextFieldFocusGained

    private void timeSpanEndFormattedTextFieldFocusGained(java.awt.event.FocusEvent evt)//GEN-FIRST:event_timeSpanEndFormattedTextFieldFocusGained
    {//GEN-HEADEREND:event_timeSpanEndFormattedTextFieldFocusGained
        timeSpanEndFormattedTextField.selectAll();
    }//GEN-LAST:event_timeSpanEndFormattedTextFieldFocusGained

    private void databaseIdTextFieldFocusGained(java.awt.event.FocusEvent evt)//GEN-FIRST:event_databaseIdTextFieldFocusGained
    {//GEN-HEADEREND:event_databaseIdTextFieldFocusGained
        databaseIdTextField.selectAll();
    }//GEN-LAST:event_databaseIdTextFieldFocusGained

    private void startRecordFormattedTextFieldFocusGained(java.awt.event.FocusEvent evt)//GEN-FIRST:event_startRecordFormattedTextFieldFocusGained
    {//GEN-HEADEREND:event_startRecordFormattedTextFieldFocusGained
        startRecordFormattedTextField.selectAll();
    }//GEN-LAST:event_startRecordFormattedTextFieldFocusGained

    private void maxRecordsFormattedTextFieldFocusGained(java.awt.event.FocusEvent evt)//GEN-FIRST:event_maxRecordsFormattedTextFieldFocusGained
    {//GEN-HEADEREND:event_maxRecordsFormattedTextFieldFocusGained
        maxRecordsFormattedTextField.selectAll();
    }//GEN-LAST:event_maxRecordsFormattedTextFieldFocusGained

    private void saveResultsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveResultsButtonActionPerformed
    {//GEN-HEADEREND:event_saveResultsButtonActionPerformed
        if (outputViewPanel_.getResultsOutputText().length() > 0)
        {
            saveResultsOutputToFile();
        }
        else
        {
            JOptionPane.showMessageDialog(this, MSG_RESULTS_XML_IS_EMPTY, "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_saveResultsButtonActionPerformed

    /**
     * Updates the view with value changes from model properties
     * @param evt
     */
    @Override
    public void modelPropertyChange(PropertyChangeEvent evt)
    {
        try
        {
            if (evt.getPropertyName().equals(DefaultController.WS_LITE_SEARCH_DATABASE_ID_PROPERTY))
            {
                String newStringValue = evt.getNewValue().toString();

                if (!databaseIdTextField.getText().equals(newStringValue))
                {
                    databaseIdTextField.setText(newStringValue);
                }
            }
            else if (evt.getPropertyName().equals(DefaultController.WS_LITE_SEARCH_DATE_MODE_PROPERTY))
            {
                //DateMode newDateModeValue = null;
                DateMode newDateModeValue = (DateMode)evt.getNewValue();

                switch (newDateModeValue)
                {
                    case RANGE :
                        if (!dateModeRangeRadioButton.isSelected())
                        {
                            dateModeRangeRadioButton.setSelected(true);
                        }
                        break;

                    case RECENT :
                        if (!dateModeRecentRadioButton.isSelected())
                        {
                            dateModeRecentRadioButton.setSelected(true);
                        }
                        break;

                    default:
                        throw new PropertyVetoException("Value change rejected", evt);
                }
            }
            else if (evt.getPropertyName().equals(DefaultController.WS_LITE_SEARCH_EDITIONS_PROPERTY))
            {
                List<EditionDesc> newEditionValues = (List<EditionDesc>) evt.getNewValue();

                Boolean shouldSelectAhci = false;
                Boolean shouldSelectIstp = false;
                Boolean shouldSelectSci = false;
                Boolean shouldSelectSsci = false;

                for (EditionDesc edition : newEditionValues)
                {
                    String editionName = edition.getEdition();

                    if (editionName.equalsIgnoreCase("AHCI"))
                    {
                        shouldSelectAhci = true;
                    }
                    else if (editionName.equalsIgnoreCase("ISTP"))
                    {
                        shouldSelectIstp = true;
                    }
                    else if (editionName.equalsIgnoreCase("SCI"))
                    {
                        shouldSelectSci = true;
                    }
                    else if (editionName.equalsIgnoreCase("SSCI"))
                    {
                        shouldSelectSsci = true;
                    }
                }

                editionsAhciCheckbox.setSelected(shouldSelectAhci);
                editionsIstpCheckbox.setSelected(shouldSelectIstp);
                editionsSciCheckbox.setSelected(shouldSelectSci);
                editionsSsciCheckbox.setSelected(shouldSelectSsci);
            }
            else if (evt.getPropertyName().equals(DefaultController.WS_LITE_SEARCH_FIRST_RECORD_PROPERTY))
            {
                Integer newIntegerValue = (Integer)evt.getNewValue();

                try
                {
                    if (Integer.parseInt(startRecordFormattedTextField.getText()) != newIntegerValue)
                        startRecordFormattedTextField.setText(newIntegerValue.toString());
                }
                catch (NumberFormatException ex)
                {
                    startRecordFormattedTextField.setText(newIntegerValue.toString());
                }
            }
            else if (evt.getPropertyName().equals(DefaultController.WS_LITE_SEARCH_MAX_RESULT_COUNT_PROPERTY))
            {
                Integer newIntegerValue = (Integer)evt.getNewValue();

                try
                {
                    if (Integer.parseInt(maxRecordsFormattedTextField.getText()) != newIntegerValue)
                        maxRecordsFormattedTextField.setText(newIntegerValue.toString());
                }
                catch (NumberFormatException ex)
                {
                    maxRecordsFormattedTextField.setText(newIntegerValue.toString());
                }
            }
            else if (evt.getPropertyName().equals(DefaultController.WS_LITE_SEARCH_PROXY_HOST_PROPERTY))
            {
                String newStringValue = evt.getNewValue().toString();

                currentProxyHost_ = newStringValue;
            }
            else if (evt.getPropertyName().equals(DefaultController.WS_LITE_SEARCH_PROXY_PORT_PROPERTY))
            {
                Integer newIntegerValue = (Integer)evt.getNewValue();

                currentProxyPort_ = newIntegerValue;
            }
            else if (evt.getPropertyName().equals(DefaultController.WS_LITE_SEARCH_SORT_FIELDS_PROPERTY))
            {
                List<QueryField> newQueryFieldValues = (List<QueryField>)evt.getNewValue();

                String fieldName = newQueryFieldValues.get(0).getName();
                String fieldSort = newQueryFieldValues.get(0).getSort();
                String fieldSortText = null;

                if (fieldSort.equalsIgnoreCase("A"))
                {
                    fieldSortText = "Ascending";
                }
                else if (fieldSort.equalsIgnoreCase("D"))
                {
                    fieldSortText = "Descending";
                }

                if (!sortByFieldComboBox.getSelectedItem().equals(fieldName))
                {
                    sortByFieldComboBox.setSelectedItem(fieldName);
                }

                if (!sortByOrderComboBox.getSelectedItem().equals(fieldSortText))
                {
                    sortByOrderComboBox.setSelectedItem(fieldSortText);
                }
            }
            else if (evt.getPropertyName().equals(DefaultController.WS_LITE_SEARCH_SYMBOLIC_TIME_SPAN_PROPERTY))
            {
                SymbolicTimeSpan newSymbolicTimeSpanValue = (SymbolicTimeSpan) evt.getNewValue();

                switch (newSymbolicTimeSpanValue)
                {
                    case ONE_WEEK:
                        if (!symbolicTimeSpanWeekRadioButton.isSelected())
                        {
                            symbolicTimeSpanWeekRadioButton.setSelected(true);
                        }
                        break;

                    case TWO_WEEK:

                        if (!symbolicTimeSpanTwoWeekRadioButton.isSelected())
                        {
                            symbolicTimeSpanTwoWeekRadioButton.setSelected(true);
                        }
                        break;

                    case FOUR_WEEK:
                        if (!symbolicTimeSpanFourWeekRadioButton.isSelected())
                        {
                            symbolicTimeSpanFourWeekRadioButton.setSelected(true);
                        }
                        break;

                    default:
                        throw new PropertyVetoException("Value change rejected", evt);
                }
            }
            else if (evt.getPropertyName().equals(DefaultController.WS_LITE_SEARCH_TIME_SPAN_PROPERTY))
            {
                TimeSpan newTimeSpanValue = (TimeSpan) evt.getNewValue();

                if (!timeSpanBeginFormattedTextField.getText().equals(newTimeSpanValue.getBegin()))
                {
                    timeSpanBeginFormattedTextField.setValue(newTimeSpanValue.getBegin());
                }

                if (!timeSpanEndFormattedTextField.getText().equals(newTimeSpanValue.getEnd()))
                {
                    timeSpanEndFormattedTextField.setValue(newTimeSpanValue.getEnd());
                }
            }
            else if (evt.getPropertyName().equals(DefaultController.WS_LITE_SEARCH_USER_QUERY_PROPERTY))
            {
                String newStringValue = evt.getNewValue().toString();

                if (!queryTextField.getText().equals(newStringValue))
                {
                    queryTextField.setText(newStringValue);
                }
            }
        }
        catch (PropertyVetoException ex)
        {
            ex.printStackTrace();
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel databaseIdLabel;
    private javax.swing.JTextField databaseIdTextField;
    private javax.swing.JRadioButton dateModeRangeRadioButton;
    private javax.swing.JRadioButton dateModeRecentRadioButton;
    private javax.swing.JCheckBox editionsAhciCheckbox;
    private javax.swing.JCheckBox editionsIstpCheckbox;
    private javax.swing.JLabel editionsLabel;
    private javax.swing.JCheckBox editionsSciCheckbox;
    private javax.swing.JCheckBox editionsSsciCheckbox;
    private javax.swing.JFormattedTextField maxRecordsFormattedTextField;
    private javax.swing.JLabel maxRecordsLabel;
    private javax.swing.JButton performSearchButton;
    private javax.swing.JButton proxySettingsButton;
    private javax.swing.JPanel queryDatabasePanel;
    private javax.swing.JPanel queryDatePanel;
    private javax.swing.JLabel queryLabel;
    private javax.swing.JPanel queryOptionsPanel;
    private javax.swing.JTextField queryTextField;
    private javax.swing.JPanel retrieveOptionsPanel;
    private javax.swing.JButton saveResultsButton;
    private javax.swing.JComboBox sortByFieldComboBox;
    private javax.swing.JLabel sortByLabel;
    private javax.swing.JComboBox sortByOrderComboBox;
    private javax.swing.JFormattedTextField startRecordFormattedTextField;
    private javax.swing.JLabel startRecordLabel;
    private javax.swing.ButtonGroup symbolicTimeSpanButtonGroup;
    private javax.swing.JRadioButton symbolicTimeSpanFourWeekRadioButton;
    private javax.swing.JLabel symbolicTimeSpanLabel;
    private javax.swing.JRadioButton symbolicTimeSpanTwoWeekRadioButton;
    private javax.swing.JRadioButton symbolicTimeSpanWeekRadioButton;
    private javax.swing.JFormattedTextField timeSpanBeginFormattedTextField;
    private javax.swing.JLabel timeSpanBeginLabel;
    private javax.swing.ButtonGroup timeSpanButtonGroup;
    private javax.swing.JFormattedTextField timeSpanEndFormattedTextField;
    private javax.swing.JLabel timeSpanEndLabel;
    private javax.swing.JButton viewResultsButton;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[])
    {
        QueryViewPanel test = new QueryViewPanel(null);

        JFrame displayFrame = new JFrame("Display (View 1)");
        displayFrame.setResizable(false);
        displayFrame.getContentPane().add(test, BorderLayout.CENTER);
        displayFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        displayFrame.pack();

        displayFrame.setVisible(true);
    }
}
