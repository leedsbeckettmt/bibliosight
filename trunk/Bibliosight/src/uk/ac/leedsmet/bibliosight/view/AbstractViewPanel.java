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

package uk.ac.leedsmet.bibliosight.view;

import java.beans.PropertyChangeEvent;
import javax.swing.JPanel;


// Based on the general concept found here here: http://java.sun.com/developer/technicalArticles/javase/mvc/
/**
 * Provides a simple abstraction for a GUI view by extending the JPanel class
 * with the addition of a method that can be used to pass on property changes
 * from a data model.
 *
 * @author Mike Taylor
 */
public abstract class AbstractViewPanel extends JPanel {

    /**
     * Called by a controller when it needs to pass along a property change
     * from a model.
     * @param evt The property change event from the model
     */
    public abstract void modelPropertyChange(PropertyChangeEvent evt);
}
