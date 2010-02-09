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

package uk.ac.leedsmet.bibliosight;

import com.thomsonreuters.wokmws.cxf.woksearchlite.EditionDesc;
import com.thomsonreuters.wokmws.cxf.woksearchlite.QueryField;
import com.thomsonreuters.wokmws.cxf.woksearchlite.TimeSpan;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import uk.ac.leedsmet.bibliosight.controller.DefaultController;
import uk.ac.leedsmet.bibliosight.model.QueryModel;
import uk.ac.leedsmet.bibliosight.view.QueryViewPanel;

/**
 *
 * @author Mike Taylor
 */
public class Main
{

    public Main()
    {
        QueryModel queryModel = new QueryModel();
        DefaultController controller = new DefaultController();
        QueryViewPanel queryViewPanel = new QueryViewPanel(controller);

        controller.addView(queryViewPanel);
        controller.addModel(queryModel);

        JFrame displayFrame = new JFrame("Bibliosight");
        displayFrame.setContentPane(queryViewPanel);
        displayFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        displayFrame.pack();
        displayFrame.setVisible(true);

        // Set initial database id
        queryModel.setDatabaseId("WOS");

        // Set initial date mode
        queryModel.setDateMode(DefaultController.DateMode.RANGE);

        // Set initial time span
        TimeSpan timeSpan = new TimeSpan();
        timeSpan.setBegin(new String("2008-01-01"));
        timeSpan.setEnd(new String("2008-12-31"));
        queryModel.setTimeSpan(timeSpan);

        // Set initial symbolic time span
        queryModel.setSymbolicTimeSpan(DefaultController.SymbolicTimeSpan.FOUR_WEEK);

        // Set initial record offset
        queryModel.setFirstRecord(1);

        // Set initial maximum result count
        queryModel.setMaxResultCount(100);

        // Set initial proxy
        //queryModel.setProxyHost("proxy.example.com");
        //queryModel.setProxyPort(8080);

        // Set initial editions selection
        ArrayList<EditionDesc> editions = new ArrayList<EditionDesc>();
        EditionDesc editionAhci = new EditionDesc();
        editionAhci.setEdition("AHCI");
        editionAhci.setCollection("WOS");
        EditionDesc editionIstp = new EditionDesc();
        editionIstp.setEdition("ISTP");
        editionIstp.setCollection("WOS");
        EditionDesc editionSci = new EditionDesc();
        editionSci.setEdition("SCI");
        editionSci.setCollection("WOS");
        EditionDesc editionSsci = new EditionDesc();
        editionSsci.setEdition("SSCI");
        editionSsci.setCollection("WOS");

        editions.add(editionAhci);
        editions.add(editionIstp);
        editions.add(editionSci);
        editions.add(editionSsci);

        queryModel.setEditions(editions);

        // Set initial sort fields
        QueryField initialQueryField = new QueryField();
        initialQueryField.setName("Date");
        initialQueryField.setSort("D");
        ArrayList initialQueryFields = new ArrayList();
        initialQueryFields.add(initialQueryField);
        queryModel.setSortFields(initialQueryFields);

        // Set initial user query
        //queryModel.setUserQuery("AD=(Leeds Met* Univ*)");
        queryModel.setUserQuery("TI=(Business)");

    }

    public static void main(String[] args)
    {
        Main main = new Main();
    }
}
