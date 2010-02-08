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

package uk.ac.leedsmet.bibliosight.controller;

import uk.ac.leedsmet.bibliosight.model.AbstractModel;
import uk.ac.leedsmet.bibliosight.view.AbstractViewPanel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;

// Based on the general concept found here here: http://java.sun.com/developer/technicalArticles/javase/mvc/
/**
 * Provides the basic functionality for a controller that manages the
 * communication of changes and event triggers between data models and views.
 *
 * @author Mike Taylor
 */
public abstract class AbstractController implements PropertyChangeListener
{

    /**
     * List of views registered with this controller
     */
    private ArrayList<AbstractViewPanel> registeredViews;

    /**
     * List of models registered with this controller
     */
    private ArrayList<AbstractModel> registeredModels;


    /** Creates a new instance of Controller */
    public AbstractController()
    {
        registeredViews = new ArrayList<AbstractViewPanel>();
        registeredModels = new ArrayList<AbstractModel>();
    }

    /**
     * Registers a model with this controller.
     * @param model The model to be added
     */
    public void addModel(AbstractModel model)
    {
        registeredModels.add(model);
        model.addPropertyChangeListener(this);
    }

    /**
     * Removes a model from the registered models list for this controller.
     * @param model The model to be removed
     */
    public void removeModel(AbstractModel model)
    {
        registeredModels.remove(model);
        model.removePropertyChangeListener(this);
    }


    /**
     * Registers a view with this controller.
     * @param model The model to be added
     */
    public void addView(AbstractViewPanel view)
    {
        registeredViews.add(view);
    }

    /**
     * Removes a view from the registered views list for this controller.
     * @param model The model to be removed
     */
    public void removeView(AbstractViewPanel view)
    {
        registeredViews.remove(view);
    }

    /**
     * This method is used to implement the PropertyChangeListener interface. Any model
     * changes will be sent to this controller through the use of this method.
     * @param evt An object that describes the model's property change.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {

        for (AbstractViewPanel view: registeredViews)
        {
            view.modelPropertyChange(evt);
        }
    }


    /**
     * Attempts to send property changes to any models registered with this
     * controller.
     *
     * Each registered model is checked to see if it contains a setter for
     * the specified property name. For any models that have such a setter,
     * the setter is called with the supplied new property value. Cases where
     * a setter is not found in a model are ignored.
     *
     * @param propertyName The name of the property
     * @param newValue An object that represents the new value of the property.
     */
    protected void setModelProperty(String propertyName, Object newValue)
    {
        for (AbstractModel model: registeredModels)
        {
            try
            {

                Method method = model.getClass().getMethod(
                    "set"+propertyName,
                    new Class[] {
                        newValue.getClass()
                    }
                );

                method.invoke(model, newValue);

            }
            catch (Exception ex)
            {
                //  Handle exception
            }
        }
    }

    /**
     * Attempts to trigger a particular the execution of a particular method
     * within any models registered with this controller.
     *
     * Each registered model is checked to see if it contains a method matchign
     * the specified method name. For any models that have such a method,
     * the method is called. Cases where a method is not found in a model are
     * ignored.
     *
     * @param methodName The name of the method
     * 
     */
    protected void triggerModelMethod(String methodName)
    {
        for (AbstractModel model: registeredModels)
        {
            try
            {

                Method method = model.getClass().getMethod("trigger" + methodName);
                method.invoke(model);

            }
            catch (Exception ex)
            {
                //  Handle exception
            }
        }
    }
    
}

