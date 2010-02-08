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

import com.thomsonreuters.wokmws.cxf.woksearchlite.EditionDesc;
import com.thomsonreuters.wokmws.cxf.woksearchlite.QueryField;
import com.thomsonreuters.wokmws.cxf.woksearchlite.TimeSpan;
import java.util.List;



/**
 * The default controller implementation for connecting the Bibliosight data
 * model(s) with the view(s)
 *
 * @author Mike Taylor
 */
public class DefaultController extends AbstractController
{
    /**
     * Date mode options
     */
    public enum DateMode
    {
        RANGE,
        RECENT
    }

    /**
     * Symbolic time span options
     */
    public enum SymbolicTimeSpan
    {
        ONE_WEEK ("1week"),
        TWO_WEEK ("2week"),
        FOUR_WEEK ("4week");

        private final String value;

        SymbolicTimeSpan(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }
    }

    // Properties that are expected to be in one or more of the registered models
    public static final String WS_LITE_SEARCH_DATABASE_ID_PROPERTY = "DatabaseId";
    public static final String WS_LITE_SEARCH_DATE_MODE_PROPERTY = "DateMode";
    public static final String WS_LITE_SEARCH_EDITIONS_PROPERTY = "Editions";
    public static final String WS_LITE_SEARCH_FIRST_RECORD_PROPERTY = "FirstRecord";
    public static final String WS_LITE_SEARCH_MAX_RESULT_COUNT_PROPERTY = "MaxResultCount";
    public static final String WS_LITE_SEARCH_PROXY_HOST_PROPERTY = "ProxyHost";
    public static final String WS_LITE_SEARCH_PROXY_PORT_PROPERTY = "ProxyPort";
    public static final String WS_LITE_SEARCH_SORT_FIELDS_PROPERTY = "SortFields";
    public static final String WS_LITE_SEARCH_SYMBOLIC_TIME_SPAN_PROPERTY = "SymbolicTimeSpan";
    public static final String WS_LITE_SEARCH_TIME_SPAN_PROPERTY = "TimeSpan";
    public static final String WS_LITE_SEARCH_USER_QUERY_PROPERTY = "UserQuery";
    public static final String WS_LITE_SEARCH_RESULT_OUTPUT_PROPERTY = "ResultOutput";
    public static final String WS_LITE_SEARCH_LOG_PROPERTY = "Log";

    // Method names that are expected to be in one or more of the registered models
    public static final String WS_LITE_SEARCH_EXECUTE_QUERY_METHOD = "ExecuteWsLiteQuery";

    /**
     * Change the database Id in the model
     * @param newDatabaseId
     */
    public void changeDatabaseId(String newDatabaseId)
    {
        setModelProperty(WS_LITE_SEARCH_DATABASE_ID_PROPERTY, newDatabaseId);
    }

    /**
     * Change the date type in the model
     * @param newDateType
     */
    public void changeDateMode(DateMode newDateType)
    {
        setModelProperty(WS_LITE_SEARCH_DATE_MODE_PROPERTY, newDateType);
    }

    /**
     * Change the editions in the model
     * @param newEditions
     */
    public void changeEditions(List<EditionDesc> newEditions)
    {
        setModelProperty(WS_LITE_SEARCH_EDITIONS_PROPERTY, newEditions);
    }

    /**
     * Change the first record in the model
     * @param newFirstRecord
     */
    public void changeFirstRecord(Integer newFirstRecord)
    {
        setModelProperty(WS_LITE_SEARCH_FIRST_RECORD_PROPERTY, newFirstRecord);
    }

    /**
     * Change the log in the model
     * @param newLog
     */
    public void changeLog(String newLog)
    {
        setModelProperty(WS_LITE_SEARCH_LOG_PROPERTY, newLog);
    }

    /**
     * Change the maximum result count in the model
     * @param newMaxResultCount
     */
    public void changeMaxResultCount(Integer newMaxResultCount)
    {
        setModelProperty(WS_LITE_SEARCH_MAX_RESULT_COUNT_PROPERTY, newMaxResultCount);
    }

    /**
     * Change the proxy host name in the model
     * @param newProxyHost
     */
    public void changeProxyHost(String newProxyHost)
    {
        setModelProperty(WS_LITE_SEARCH_PROXY_HOST_PROPERTY, newProxyHost);
    }

    /**
     * Change the proxy host port in the model
     * @param newProxyPort
     */
    public void changeProxyPort(Integer newProxyPort)
    {
        setModelProperty(WS_LITE_SEARCH_PROXY_PORT_PROPERTY, newProxyPort);
    }

    /**
     * Change the result output in the model
     * @param newResultOutput
     */
    public void changeResultOutput(String newResultOutput)
    {
        setModelProperty(WS_LITE_SEARCH_RESULT_OUTPUT_PROPERTY, newResultOutput);
    }

    /**
     * Change the sort fields in the model
     * @param newSortFields
     */
    public void changeSortFields(List<QueryField> newSortFields)
    {
        setModelProperty(WS_LITE_SEARCH_SORT_FIELDS_PROPERTY, newSortFields);
    }

    /**
     * Change the symbolic time span in the model
     * @param newSymbolicTimeSpan
     */
    public void changeSymbolicTimeSpan(SymbolicTimeSpan newSymbolicTimeSpan)
    {
        setModelProperty(WS_LITE_SEARCH_SYMBOLIC_TIME_SPAN_PROPERTY, newSymbolicTimeSpan);
    }

    /**
     * Change the time span in the model
     * @param newTimeSpan
     */
    public void changeTimeSpan(TimeSpan newTimeSpan)
    {
        setModelProperty(WS_LITE_SEARCH_TIME_SPAN_PROPERTY, newTimeSpan);
    }

    /**
     * Change the user query in the model
     * @param newUserQuery
     */
    public void changeUserQuery(String newUserQuery)
    {
        setModelProperty(WS_LITE_SEARCH_USER_QUERY_PROPERTY, newUserQuery);
    }

    /**
     * Triggers the WS Lite query execution in the model
     */
    public void executeWsLiteQuery()
    {
        triggerModelMethod(WS_LITE_SEARCH_EXECUTE_QUERY_METHOD);
    }
}
