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

package uk.ac.leedsmet.bibliosight.model;

import com.thomsonreuters.wokmws.cxf.auth.WOKMWSAuthenticate;
import com.thomsonreuters.wokmws.cxf.auth.WOKMWSAuthenticateService;
import com.thomsonreuters.wokmws.cxf.woksearchlite.EditionDesc;
import com.thomsonreuters.wokmws.cxf.woksearchlite.QueryField;
import com.thomsonreuters.wokmws.cxf.woksearchlite.QueryParameters;
import com.thomsonreuters.wokmws.cxf.woksearchlite.RetrieveParameters;
import com.thomsonreuters.wokmws.cxf.woksearchlite.SearchResults;
import com.thomsonreuters.wokmws.cxf.woksearchlite.TimeSpan;
import com.thomsonreuters.wokmws.cxf.woksearchlite.WokSearchLite;
import com.thomsonreuters.wokmws.cxf.woksearchlite.WokSearchLiteService;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import uk.ac.leedsmet.bibliosight.BibliosightAuthenticationException;
import uk.ac.leedsmet.bibliosight.BibliosightClientException;
import uk.ac.leedsmet.bibliosight.BibliosightSearchException;
import uk.ac.leedsmet.bibliosight.controller.DefaultController;
import uk.ac.leedsmet.bibliosight.controller.DefaultController.DateMode;
import uk.ac.leedsmet.bibliosight.controller.DefaultController.SymbolicTimeSpan;
import uk.ac.leedsmet.bibliosight.transformer.SearchResultsTransformer;

/**
 * The main data model for the Bibliosight client
 * @author Mike Taylor
 */
public class QueryModel extends AbstractModel {

    private static final String NEW_LINE = System.getProperty("line.separator");

    /**
     * Product code for the database to be searched
     */
    private String databaseId_;

    /**
     * Option for the type of date search to be used
     */
    private DateMode dateMode_;

    /**
     * Database editions to be searched
     */
    private List<EditionDesc> editions_;

    /**
     * Index of the first record to be returned in the search result
     */
    private Integer firstRecord_;

    /**
     * Maximum number of records to be returned in the search result
     */
    private Integer maxResultCount_;

    /**
     * Proxy host name
     */
    private String proxyHost_;

    /**
     * Proxy host port
     */
    private Integer proxyPort_;

    /**
     * Field by which search results are sorted
     */
    private List<QueryField> sortFields_;

    /**
     * Time span option when searching for recent updates
     */
    private SymbolicTimeSpan symbolicTimeSpan_;

    /**
     * Time span option when searching arbitrary date ranges
     */
    private TimeSpan timeSpan_;

    /**
     * The search criteria
     */
    private String userQuery_;

    /**
     * A log of program feedback for the user
     */
    private String log_;

    /**
     * Stores the results of a query
     */
    private String resultOutput_;

    /**
     * Returns the minimum allowable value for the first record property
     * @return
     */
    public static Integer getMinFirstRecord()
    {
        return 1;
    }

    /**
     * Returns the minimum allowable value for the maximum result count property
     * @return
     */
    public static Integer getMinMaxResultCount()
    {
        return 1;
    }

    /**
     * Returns the maximum allowable value for the maximum result count property
     * @return
     */
    public static Integer getMaxMaxResultCount()
    {
        return 100;
    }

    /**
     * Returns the current database Id property
     * @return
     */
    public String getDatabaseId()
    {
        return databaseId_;
    }

    /**
     * Sets the database Id property
     * @param databaseId
     */
    public void setDatabaseId(String databaseId)
    {
        String oldDatabaseId = this.databaseId_;
        this.databaseId_ = databaseId;

        try
        {
            if (!databaseId.equals(oldDatabaseId))
            {
                appendToLog("Setting database Id to " + this.databaseId_);
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Warning: Database Id has been given a null value.");
        }

        firePropertyChange(DefaultController.WS_LITE_SEARCH_DATABASE_ID_PROPERTY, oldDatabaseId, databaseId);
    }

    /**
     * Returns the current date mode property
     * @return
     */
    public DateMode getDateMode()
    {
        return dateMode_;
    }

    /**
     * Sets the date mode property
     * @param dateMode
     */
    public void setDateMode(DateMode dateMode)
    {
        DateMode oldDateMode = this.dateMode_;
        this.dateMode_ = dateMode;

        firePropertyChange(DefaultController.WS_LITE_SEARCH_DATE_MODE_PROPERTY, oldDateMode, dateMode);
    }

    /**
     * Returns the current editions list property
     * @return
     */
    public List<EditionDesc> getEditions()
    {
        return editions_;
    }

    /**
     * Sets the editions list property
     * @param editions
     */
    public void setEditions(ArrayList<EditionDesc> editions)
    {
        List<EditionDesc> oldEditions = this.editions_;
        this.editions_ = editions;

        try
        {
            if (!editions.equals(oldEditions))
            {
                String selectedEditions = "";

                for (EditionDesc edition : this.editions_)
                {
                    if (selectedEditions.length() > 0)
                    {
                        selectedEditions += ", ";
                    }
                    selectedEditions += edition.getEdition();

                }

                appendToLog("Setting editions to " + selectedEditions);
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Warning: Editions has been given a null value.");
        }

        firePropertyChange(DefaultController.WS_LITE_SEARCH_EDITIONS_PROPERTY, oldEditions, editions);
    }

    /**
     * Returns the current first record property
     * @return
     */
    public Integer getFirstRecord()
    {
        return firstRecord_;
    }

    /**
     * Set the first record property
     * @param firstRecord
     */
    public void setFirstRecord(Integer firstRecord)
    {
        Integer minFirstRecord = getMinFirstRecord();

        Integer oldFirstRecord = this.firstRecord_;
        this.firstRecord_ = firstRecord;

        try
        {
            if (!firstRecord.equals(oldFirstRecord))
            {
                if (firstRecord.compareTo(minFirstRecord) < 0)
                {
                    // This will force the property to refresh in the view
                    oldFirstRecord = null;
                    firstRecord = minFirstRecord;
                    this.firstRecord_ = minFirstRecord;

                    appendToLog("Start record cannot be lower than " + minFirstRecord);
                }

                appendToLog("Setting start record to " + this.firstRecord_);
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Warning: Start record has been given a null value.");
        }

        firePropertyChange(DefaultController.WS_LITE_SEARCH_FIRST_RECORD_PROPERTY, oldFirstRecord, firstRecord);
    }

    /**
     * Returns the current maximum result count property
     * @return
     */
    public Integer getMaxResultCount()
    {
        return maxResultCount_;
    }

    /**
     * Sets the maximum result count property
     * @param maxResultCount
     */
    public void setMaxResultCount(Integer maxResultCount)
    {
        Integer minMaxResultCount = getMinMaxResultCount();
        Integer maxMaxResultCount = getMaxMaxResultCount();

        Integer oldMaxResultCount = this.maxResultCount_;
        this.maxResultCount_ = maxResultCount;

        try
        {
            if (!maxResultCount.equals(oldMaxResultCount))
            {
                if (maxResultCount.compareTo(minMaxResultCount) < 0)
                {
                    // This will force the property to refresh in the view
                    oldMaxResultCount = null;
                    maxResultCount = minMaxResultCount;
                    this.maxResultCount_ = minMaxResultCount;

                    appendToLog("Maximum records to retrieve cannot be lower than " + minMaxResultCount);
                }
                else if (maxResultCount.compareTo(maxMaxResultCount) > 0)
                {
                    // This will force the property to refresh in the view
                    oldMaxResultCount = null;
                    this.maxResultCount_ = maxMaxResultCount;
                    maxResultCount = maxMaxResultCount;

                    appendToLog("Maximum records to retrieve cannot be greater than " + maxMaxResultCount);
                }

                appendToLog("Setting maximum records to retrieve to " + this.maxResultCount_);
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Warning: Maximum records has been given a null value.");
        }

        firePropertyChange(DefaultController.WS_LITE_SEARCH_MAX_RESULT_COUNT_PROPERTY, oldMaxResultCount, maxResultCount);
    }

    /**
     * Returns the current proxy host name property
     * @return
     */
    public String getProxyHost()
    {
        return proxyHost_;
    }

    /**
     * Sets the proxy host name property
     * @param proxyHost
     */
    public void setProxyHost(String proxyHost)
    {
        String oldProxyHost = this.proxyHost_;
        this.proxyHost_ = proxyHost;

        try
        {
            if (!proxyHost.equals(oldProxyHost))
            {
                appendToLog("Setting proxy host name to " + this.proxyHost_);
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Warning: Proxy host name has been given a null value.");
        }

        firePropertyChange(DefaultController.WS_LITE_SEARCH_PROXY_HOST_PROPERTY, oldProxyHost, proxyHost);
    }

    /**
     * Returns the current proxy host port property
     * @return
     */
    public Integer getProxyPort()
    {
        return proxyPort_;
    }

    /**
     * Sets the proxy host port property
     * @param proxyPort
     */
    public void setProxyPort(Integer proxyPort)
    {
        Integer oldProxyPort = this.proxyPort_;
        this.proxyPort_ = proxyPort;

        try
        {
            if (!proxyPort.equals(oldProxyPort))
            {
                appendToLog("Setting proxy host port to " + this.proxyPort_);
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Warning: Proxy host port has been given a null value.");
        }

        firePropertyChange(DefaultController.WS_LITE_SEARCH_PROXY_PORT_PROPERTY, oldProxyPort, proxyPort);

        
    }

    /**
     * Returns the current sort fields list property
     * @return
     */
    public List<QueryField> getSortFields()
    {
        return sortFields_;
    }

    /**
     * Sets the sort fields list property
     * @param sortFields
     */
    public void setSortFields(ArrayList<QueryField> sortFields)
    {
        List<QueryField> oldSortFields = this.sortFields_;
        this.sortFields_ = sortFields;

        try
        {
            if (!sortFields.equals(oldSortFields))
            {
                String sortFieldDisplay = "";

                for (QueryField field : this.sortFields_)
                {
                    if (sortFieldDisplay.length() > 0)
                    {
                        sortFieldDisplay += ", ";
                    }
                    sortFieldDisplay += field.getName() + "(" + field.getSort() + ")";

                }

                appendToLog("Setting sort fields to " + sortFieldDisplay);
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Warning: Sort fields has been given a null value.");
        }

        firePropertyChange(DefaultController.WS_LITE_SEARCH_SORT_FIELDS_PROPERTY, oldSortFields, sortFields);
    }

    /**
     * Returns the current symbolic time span property
     * @return
     */
    public SymbolicTimeSpan getSymbolicTimeSpan()
    {
        return symbolicTimeSpan_;
    }

    /**
     * Sets the symbolic time span property
     * @param symbolicTimeSpan
     */
    public void setSymbolicTimeSpan(SymbolicTimeSpan symbolicTimeSpan)
    {
        SymbolicTimeSpan oldSymbolicTimeSpan = this.symbolicTimeSpan_;
        this.symbolicTimeSpan_ = symbolicTimeSpan;

        try
        {
            if (!symbolicTimeSpan.equals(oldSymbolicTimeSpan))
            {
                appendToLog("Setting recent date to " + this.symbolicTimeSpan_.getValue());
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Warning: Recent date has been given a null value.");
        }

        firePropertyChange(DefaultController.WS_LITE_SEARCH_SYMBOLIC_TIME_SPAN_PROPERTY, oldSymbolicTimeSpan, symbolicTimeSpan);
    }

    /**
     * Returns the current time span property
     * @return
     */
    public TimeSpan getTimeSpan()
    {
        return timeSpan_;
    }

    /**
     * Sets the time span property
     * @param timeSpan
     */
    public void setTimeSpan(TimeSpan timeSpan)
    {
        TimeSpan oldTimeSpan = this.timeSpan_;
        this.timeSpan_ = timeSpan;

        try
        {
            if (!timeSpan.equals(oldTimeSpan))
            {
                appendToLog("Setting date range to " + this.timeSpan_.getBegin() + " to " + this.timeSpan_.getEnd());
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Warning: Date range has been given a null value.");
        }

        firePropertyChange(DefaultController.WS_LITE_SEARCH_TIME_SPAN_PROPERTY, oldTimeSpan, timeSpan);
    }

    /**
     * Returns the current user query property
     * @return
     */
    public String getUserQuery()
    {
        return userQuery_;
    }

    /**
     * Sets the user query property
     * @param userQuery
     */
    public void setUserQuery(String userQuery)
    {
        String oldUserQuery = this.userQuery_;
        this.userQuery_ = userQuery;

        try
        {
            if (!userQuery.equals(oldUserQuery))
            {
                appendToLog("Setting user query to " + this.userQuery_);
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Warning: User query has been given a null value.");
        }

        firePropertyChange(DefaultController.WS_LITE_SEARCH_USER_QUERY_PROPERTY, oldUserQuery, userQuery);
    }

    /**
     * Returns the current log property
     * @return
     */
    public String getLog()
    {
        return log_;
    }

    /**
     * Sets the log property
     * @param log
     */
    public void setLog(String log)
    {
        String oldLog = this.log_;
        this.log_ = log;

        firePropertyChange(DefaultController.WS_LITE_SEARCH_LOG_PROPERTY, oldLog, log);
    }

    /**
     * Appends a string on a new line to the log property
     * @param string
     */
    public void appendToLog(String string)
    {
        if(log_ != null)
        {
            setLog(log_ + NEW_LINE + string);
        }
        else
        {
            setLog(string);
        }
    }

    /**
     * Returns the current results output property
     * @return
     */
    public String getResultOutput()
    {
        return resultOutput_;
    }

    /**
     * Sets the results output property
     * @param resultOutput
     */
    public void setResultOutput(String resultOutput)
    {
        String oldResultOutput = this.resultOutput_;
        this.resultOutput_ = resultOutput;

        firePropertyChange(DefaultController.WS_LITE_SEARCH_RESULT_OUTPUT_PROPERTY, oldResultOutput, resultOutput);
    }

    private void clearResultOutput()
    {
        this.resultOutput_ = "";
        firePropertyChange(DefaultController.WS_LITE_SEARCH_RESULT_OUTPUT_PROPERTY, null, this.resultOutput_);
    }

    /**
     * Returns the query data in the form of a QueryParameters object
     * @return
     */
    public QueryParameters getQueryParameters()
    {
        QueryParameters queryParameters = new QueryParameters();

        try
        {
            queryParameters.setDatabaseID(databaseId_);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        queryParameters.setQueryLanguage("en");

        try
        {
            switch (dateMode_)
            {
                case RANGE:
                    queryParameters.setTimeSpan(timeSpan_);
                    queryParameters.setSymbolicTimeSpan(null);
                    break;

                case RECENT:
                    queryParameters.setSymbolicTimeSpan(symbolicTimeSpan_.getValue());
                    queryParameters.setTimeSpan(null);
                    break;

                default:
                    // Nothing

            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Error: Date mode is null");
        }

        try
        {
            queryParameters.setUserQuery(userQuery_);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        try
        {
            for (EditionDesc edition : editions_)
            {
                queryParameters.getEditions().add(edition);
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Error: Editions list is null");
        }

        return queryParameters;
    }

    /**
     * Returns the retrieve data in the form of a RetrieveParameters object
     * @return
     */
    public RetrieveParameters getRetrieveParameters()
    {
        RetrieveParameters retrieveParameters = new RetrieveParameters();

        try
        {
            retrieveParameters.setCount(maxResultCount_);
        }
        catch (NullPointerException ex)
        {
            appendToLog("Error: Maximum records to retrieve is null");
        }
        try
        {
            retrieveParameters.setFirstRecord(firstRecord_);
        }
        catch (NullPointerException ex)
        {
            appendToLog("Error: Start record is null");
        }

        try
        {
            for (QueryField queryField : sortFields_)
            {
                retrieveParameters.getFields().add(queryField);
            }
        }
        catch (NullPointerException ex)
        {
            appendToLog("Error: Sort fields list is null");
        }


        return retrieveParameters;
    }

    /**
     * Trigger function for executeWsLiteQuery()
     */
    public void triggerExecuteWsLiteQuery()
    {
        executeWsLiteQuery();
    }

    /**
     * Query the Web of Science Web Services Lite service with the current
     * query/retrieve properties. Results are stored in the results output
     * property
     */
    private void executeWsLiteQuery()
    {
        appendToLog("Building query...");

        setSystemProxy();

        QName searchServiceName = new QName(
            "http://woksearchlite.cxf.wokmws.thomsonreuters.com",
            "WokSearchLiteService"
        );

        QName authServiceName = new QName(
            "http://auth.cxf.wokmws.thomsonreuters.com",
            "WOKMWSAuthenticateService"
        );

        QueryParameters queryParameters = getQueryParameters();
        RetrieveParameters retrieveParameters = getRetrieveParameters();

        SearchResults searchResults = new SearchResults();

        URL authWsdlLocation = getDefaultAuthenticationWsdlUrl();
        URL searchWsdlLocation = getDefaultSearchWsdlUrl();

        String sessionId = null;

        WOKMWSAuthenticateService authService = new WOKMWSAuthenticateService(authWsdlLocation, authServiceName);
        WOKMWSAuthenticate authPort = authService.getWOKMWSAuthenticatePort();

        WokSearchLiteService searchService = new WokSearchLiteService(searchWsdlLocation, searchServiceName);
        WokSearchLite searchPort = searchService.getWokSearchLitePort();

        try
        {
            sessionId = authenticateWithWsLite(authPort);
        }
        catch (BibliosightAuthenticationException ex)
        {
            Logger.getLogger(QueryModel.class.getName()).log(
                Level.SEVERE, "Authentication with the Web Services Lite service has failed. Search operation cannot continue.", ex
            );
            appendToLog("Error: " + ex.getMessage());
            clearResultOutput();
        }

        Boolean isSessionInitialised = initialiseSearchSession(searchPort, sessionId);

        if (isSessionInitialised)
        {
            try
            {
                searchResults = retrieveSearchResults(searchPort, queryParameters, retrieveParameters);
            }
            catch (BibliosightSearchException ex)
            {
                Logger.getLogger(QueryModel.class.getName()).log(
                    Level.SEVERE, "The search operation could not be completed.", ex
                );
                appendToLog("Error: " + ex.getMessage());
                clearResultOutput();
            }
        }

        try
        {
            closeSearchSession(authPort);
        }
        catch (BibliosightAuthenticationException ex)
        {
            Logger.getLogger(QueryModel.class.getName()).log(
                Level.WARNING, "The Web Services Lite session could not be closed.", ex
            );
            appendToLog("Warning: " + ex.getMessage());
        }

        try
        {
            if (searchResults.getRecordsFound() > 0)
            {
                // @todo Move some of this out to other method(s)
                try
                {
                    appendToLog("Tranforming search results into XML");
                    SearchResultsTransformer resultsTransformer = new SearchResultsTransformer();

                    String searchExecutionDate = getCurrentDateString("yyyy-MM-dd'T'HH:mm:ssZ");

                    resultsTransformer.setExecutionDate(searchExecutionDate);
                    resultsTransformer.setQueryParameters(queryParameters);
                    resultsTransformer.setRetrieveParameters(retrieveParameters);
                    resultsTransformer.setSearchResults(searchResults);

                    // Need to transform the generated XML Document into a String
                    DOMSource domSource = new DOMSource(resultsTransformer.getResultsAsDocument());

                    TransformerFactory tf = TransformerFactory.newInstance();

                    try
                    {
                        Transformer transformer = tf.newTransformer();

                        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        transformer.setOutputProperty
                            ("{http://xml.apache.org/xslt}indent-amount", "4");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                        StringWriter sw = new StringWriter();
                        StreamResult sr = new StreamResult(sw);

                        try
                        {
                            transformer.transform(domSource, sr);
                            String xml = sw.toString();

                            String oldResultOutput = resultOutput_;
                            resultOutput_ = xml;

                            firePropertyChange(DefaultController.WS_LITE_SEARCH_RESULT_OUTPUT_PROPERTY, oldResultOutput, xml);
                        }
                        catch (TransformerException ex)
                        {
                            throw new BibliosightClientException("The search results transformation could not be completed.", ex);
                        }

                    }
                    catch (TransformerConfigurationException ex)
                    {
                        throw new BibliosightClientException("The search results transformation could not be completed.", ex);
                    }

                }
                catch (BibliosightClientException ex)
                {
                    Logger.getLogger(QueryModel.class.getName()).log(
                        Level.SEVERE, "The search results transformation could not be completed.", ex
                    );
                    appendToLog("Error: " + ex.getMessage());
                    clearResultOutput();
                }
            }
        }
        catch (NullPointerException ex)
        {
            Logger.getLogger(QueryModel.class.getName()).log(
                Level.SEVERE, "The search results transformation could not be completed.", ex
            );
            appendToLog("Error: " + ex.getMessage());
            clearResultOutput();
        }
    }

    /**
     * Returns the default Url for the WS Lite Authentication Wsdl
     * @return
     */
    private URL getDefaultAuthenticationWsdlUrl()
    {
        URL authWsdlLocation = null;

        try
        {
            authWsdlLocation = new URL("http://search.isiknowledge.com/esti/wokmws/ws/WOKMWSAuthenticate?wsdl");
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(QueryModel.class.getName()).log(Level.SEVERE, "Can not initialize the default wsdl from http://search.isiknowledge.com/esti/wokmws/ws/WOKMWSAuthenticate?wsdl", ex);
        }

        return authWsdlLocation;
    }

    /**
     * Returns the default Url for the WS Lite Search Wsdl
     * @return
     */
    private URL getDefaultSearchWsdlUrl()
    {
        URL searchWsdlLocation = null;

        try
        {
            searchWsdlLocation = new URL("http://search.isiknowledge.com/esti/wokmws/ws/WokSearchLite?wsdl");
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(QueryModel.class.getName()).log(Level.SEVERE, "Can not initialize the default wsdl from http://search.isiknowledge.com/esti/wokmws/ws/WokSearchLite?wsdl", ex);
        }

        return searchWsdlLocation;
    }

    /**
     * Sets the system proxy using the proxy host name/port properties (if set)
     */
    private void setSystemProxy()
    {
        if (proxyHost_ != null && proxyPort_ != null)
        {
            System.setProperty("http.proxyHost", proxyHost_);
            System.setProperty("http.proxyPort", proxyPort_.toString());
        }
    }

    /**
     * Sends and authentication request to the WS Lite Authentication service,
     * returning a session Id if successful.
     * @param authPort
     * @return
     */
    private String authenticateWithWsLite(WOKMWSAuthenticate authPort)
        throws BibliosightAuthenticationException
    {
        String sessionId = null;

        try
        {
            appendToLog("Authenticating with Web Services Lite...");
            sessionId = authPort.authenticate();
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.QueryException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Authentication with Web Services Lite failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.SessionException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Authentication with Web Services Lite failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.AuthenticationException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Authentication with Web Services Lite failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.InvalidInputException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Authentication with Web Services Lite failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.ESTIWSException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Authentication with Web Services Lite failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.InternalServerException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Authentication with Web Services Lite failed", ex);
        }
        catch (SOAPFaultException ex)
        {
            throw new BibliosightAuthenticationException("Authentication with Web Services Lite failed", ex);
        }

        return sessionId;
    }

    /**
     * Initialises a search session with a WS Lite search service using the
     * specified session Id.
     * @param searchPort
     * @param sessionId
     * @return
     */
    private Boolean initialiseSearchSession(WokSearchLite searchPort, String sessionId)
    {
        Boolean isSessionInitialised = false;

        try
        {
            if (sessionId != null)
            {
                BindingProvider bindingProvider = (BindingProvider)searchPort;
                Map<String, Object> requestContext = bindingProvider.getRequestContext();

                requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

                Cookie cookie = new Cookie("SID", sessionId);
                Client client = ClientProxy.getClient(searchPort);
                HTTPConduit http = (HTTPConduit) client.getConduit();
                HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
                httpClientPolicy.setCookie(cookie.getName() + "=" + cookie.getValue());
                http.setClient(httpClientPolicy);

                appendToLog("Search session initialised successfully");

                isSessionInitialised = true;
            }
            else
            {
                appendToLog("Search session can not be initialised with a null session id");
            }
        }
        catch (Exception ex)
        {
            appendToLog("Search session initialisation failed: " + ex.getMessage());
        }

        return isSessionInitialised;
    }

    /**
     * Closes the session with the specified WS Lite search service
     * @param authPort
     * @return
     */
    private Boolean closeSearchSession(WOKMWSAuthenticate authPort)
        throws BibliosightAuthenticationException
    {
        Boolean isSessionClosed = false;

        try
        {
            appendToLog("Closing search session...");
            authPort.closeSession();
            isSessionClosed = true;
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.QueryException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Search session closure failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.SessionException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Search session closure failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.AuthenticationException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Search session closure failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.InvalidInputException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Search session closure failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.ESTIWSException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Search session closure failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.auth.InternalServerException_Exception ex)
        {
            throw new BibliosightAuthenticationException("Search session closure failed", ex);
        }

        return isSessionClosed;
    }

    /**
     * Queries Web of Science using the Web Services Lite service using the
     * supplied search parameters and returns a set of search results.
     * @param searchPort The Web Services Lite search service
     * @param queryParameters The query options for the search
     * @param retrieveParameters The retrieve options for the search
     * @return
     */
    private SearchResults retrieveSearchResults(
        WokSearchLite searchPort,
        QueryParameters queryParameters,
        RetrieveParameters retrieveParameters)
        throws BibliosightSearchException
    {
        SearchResults searchResults = null;

        try
        {
            appendToLog("Sending query request...");
            searchResults = searchPort.search(queryParameters, retrieveParameters);
        }
        catch (com.thomsonreuters.wokmws.cxf.woksearchlite.InternalServerException_Exception ex)
        {
            throw new BibliosightSearchException("Query execution failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.woksearchlite.ESTIWSException_Exception ex)
        {
            throw new BibliosightSearchException("Query execution failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.woksearchlite.AuthenticationException_Exception ex)
        {
            throw new BibliosightSearchException("Query execution failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.woksearchlite.QueryException_Exception ex)
        {
            throw new BibliosightSearchException("Query execution failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.woksearchlite.SessionException_Exception ex)
        {
            throw new BibliosightSearchException("Query execution failed", ex);
        }
        catch (com.thomsonreuters.wokmws.cxf.woksearchlite.InvalidInputException_Exception ex)
        {
            throw new BibliosightSearchException("Query execution failed", ex);
        }

        return searchResults;
    }

    /**
     * Returns the current date formatted as the supplied string pattern indicates
     * @param pattern
     * @return
     */
    private static String getCurrentDateString(String pattern)
    {
        Date searchExecutionDate = new Date();
        SimpleDateFormat searchExecutionDateFormat = new SimpleDateFormat();
        Calendar searchExecutionCalendar = Calendar.getInstance();

        searchExecutionDateFormat.setCalendar(searchExecutionCalendar);

        searchExecutionDateFormat.applyPattern(pattern);
        return searchExecutionDateFormat.format(searchExecutionDate);
    }
}
