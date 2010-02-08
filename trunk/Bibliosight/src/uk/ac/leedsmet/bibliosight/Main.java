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

    public Main() {
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

        queryModel.setDatabaseId("WOS");
        queryModel.setDateMode(DefaultController.DateMode.RANGE);
        queryModel.setFirstRecord(1);
        queryModel.setMaxResultCount(100);
        queryModel.setProxyHost("wwwcache.leedsmet.ac.uk");
        queryModel.setProxyPort(3128);
        
        TimeSpan timeSpan = new TimeSpan();
        timeSpan.setBegin("2008-01-01");
        timeSpan.setEnd("2008-12-31");

        QueryField initialQueryField = new QueryField();
        initialQueryField.setName("Date");
        initialQueryField.setSort("D");
        ArrayList initialQueryFields = new ArrayList();
        initialQueryFields.add(initialQueryField);
        queryModel.setSortFields(initialQueryFields);

        queryModel.setUserQuery("AD=(Leeds Met* Univ*)");

    }

    public static void main(String[] args)
    {
        Main main = new Main();
    }
}
