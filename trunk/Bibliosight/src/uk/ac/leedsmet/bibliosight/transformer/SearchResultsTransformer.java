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

package uk.ac.leedsmet.bibliosight.transformer;

import com.thomsonreuters.wokmws.cxf.woksearchlite.EditionDesc;
import com.thomsonreuters.wokmws.cxf.woksearchlite.LabelValuesPair;
import com.thomsonreuters.wokmws.cxf.woksearchlite.LiteRecord;
import com.thomsonreuters.wokmws.cxf.woksearchlite.QueryField;
import com.thomsonreuters.wokmws.cxf.woksearchlite.QueryParameters;
import com.thomsonreuters.wokmws.cxf.woksearchlite.RetrieveParameters;
import com.thomsonreuters.wokmws.cxf.woksearchlite.SearchResults;
import com.thomsonreuters.wokmws.cxf.woksearchlite.TimeSpan;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.ac.leedsmet.bibliosight.BibliosightClientException;

/**
 * A class to transform WS Lite query data and search results into an XML document
 *
 * @author Mike Taylor
 */
public class SearchResultsTransformer
{
    /**
     * Namespace URI for XML output
     */
    private static final String BIBLIOSIGHT_NAMESPACE_URI = "http://www.leedsmet.ac.uk/inn/repository/bibliosight/";

    /**
     * Namespace prefix for XML output
     */
    private static final String BIBLIOSIGHT_NAMESPACE_PREFIX = "bibliosight";

    /**
     * Date/time of query execution
     */
    private String executionDate_;

    /**
     * Query parameters used in query execution
     */
    private QueryParameters queryParameters_;

    /**
     * Retrieve parameters used in query execution
     */
    private RetrieveParameters retrieveParameters_;

    /**
     * Results data from query execution
     */
    private SearchResults searchResults_;

    /**
     * XML document used to generate output
     */
    private Document outputDocument_;

    /**
     * Create a new instance of the class and initialise the output document
     *
     * @throws BibliosightClientException
     */
    public SearchResultsTransformer() throws BibliosightClientException
    {
        initOutputDocument();
    }

    /**
     * Initialise the output document with Bibliosight namespace settings
     * @throws BibliosightClientException
     */
    private void initOutputDocument() throws BibliosightClientException
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation implementation = builder.getDOMImplementation();
            outputDocument_ = implementation.createDocument(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":bibliosight", null);
        }
        catch (ParserConfigurationException ex)
        {
            throw new BibliosightClientException("Could not create the output document", ex);
        }
    }

    /**
     * Returns execution date property
     * @return
     */
    public String getExecutionDate()
    {
        return executionDate_;
    }

    /**
     * Sets execution date property
     * @param executionDate
     */
    public void setExecutionDate(String executionDate)
    {
        this.executionDate_ = executionDate;
    }

    /**
     * Returns query parameters property
     * @return
     */
    public QueryParameters getQueryParameters()
    {
        return queryParameters_;
    }

    /**
     * Sets query parameters property
     * @param queryParameters
     */
    public void setQueryParameters(QueryParameters queryParameters)
    {
        this.queryParameters_ = queryParameters;
    }

    /**
     * Returns retrieve parameters property
     * @return
     */
    public RetrieveParameters getRetrieveParameters()
    {
        return retrieveParameters_;
    }

    /**
     * Sets retrieve parameters property
     * @param retrieveParameters
     */
    public void setRetrieveParameters(RetrieveParameters retrieveParameters)
    {
        this.retrieveParameters_ = retrieveParameters;
    }

    /**
     * Returns search results property
     * @return
     */
    public SearchResults getSearchResults()
    {
        return searchResults_;
    }

    /**
     * Sets search results property
     * @param searchResults
     */
    public void setSearchResults(SearchResults searchResults)
    {
        this.searchResults_ = searchResults;
    }

    /**
     * Returns a DOM element containing the number of records searched in the
     * search request
     * @return
     */
    private Element getNumberOfItemsSearchedElement()
    {
        Element numberOfItemsSearchedElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":numberOfItemsSearched");
        numberOfItemsSearchedElement.appendChild(
            outputDocument_.createTextNode(
                String.valueOf(searchResults_.getRecordsSearched())
            )
        );

        return numberOfItemsSearchedElement;
    }

    /**
     * Returns a DOM element containing the number of records found in the
     * search
     * @return
     */
    private Element getNumberOfItemsFoundElement()
    {
        Element numberOfItemsFoundElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":numberOfItemsFound");
        numberOfItemsFoundElement.appendChild(
            outputDocument_.createTextNode(
                String.valueOf(searchResults_.getRecordsFound())
            )
        );

        return numberOfItemsFoundElement;
    }

    /**
     * Returns a DOM element containing the number of records listed in the
     * search result object
     * @return
     */
    private Element getNumberOfItemsListedElement()
    {
        Element numberOfItemsListedElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":numberOfItemsListed");
        numberOfItemsListedElement.appendChild(
            outputDocument_.createTextNode(
                String.valueOf(searchResults_.getRecords().size())
            )
        );

        return numberOfItemsListedElement;
    }

    /**
     * Returns a DOM element containing the value of the execution date property
     * @return
     */
    private Element getDateCreatedElement()
    {
        Element dateCreatedElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":dateCreated");
        dateCreatedElement.appendChild(
            outputDocument_.createTextNode(executionDate_)
        );

        return dateCreatedElement;
    }

    /**
     * Returns a DOM element containing the data in the query parameters
     * property
     * @return
     */
    private Element getQueryParametersElement()
    {
        Element queryParametersElement  = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":queryParameters");
        Element databaseIdElement       = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":databaseId");
        Element editionsElement         = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":editions");
        Element symbolicTimeSpanElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":symbolicTimeSpan");
        Element timeSpanElement         = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":timeSpan");
        Element userQueryElement        = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":userQuery");

        Attr editionCountAttr   = outputDocument_.createAttributeNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":count");
        Attr queryLanguageAttr  = outputDocument_.createAttributeNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":language");

        String databaseId           = null;
        List<EditionDesc> editions  = null;
        Integer editionCount        = null;
        String symbolicTimeSpan     = null;
        TimeSpan timeSpan           = null;
        String userQuery            = null;
        String queryLanguage        = null;

        databaseId          = String.valueOf(queryParameters_.getDatabaseID());
        editions            = queryParameters_.getEditions();
        editionCount        = editions.size();
        symbolicTimeSpan    = String.valueOf(queryParameters_.getSymbolicTimeSpan());
        timeSpan            = queryParameters_.getTimeSpan();
        userQuery           = String.valueOf(queryParameters_.getUserQuery());
        queryLanguage       = String.valueOf(queryParameters_.getQueryLanguage());


        databaseIdElement.appendChild(
            outputDocument_.createTextNode(databaseId)
        );

        for (EditionDesc edition : editions)
        {
            Element editionElement  = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":edition");
            Attr collectionAttr     = outputDocument_.createAttributeNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":collection");

            String editionValue     = String.valueOf(edition.getEdition());
            String collectionValue  = String.valueOf(edition.getCollection());

            editionElement.appendChild(
                outputDocument_.createTextNode(editionValue)
            );

            collectionAttr.setNodeValue(collectionValue);
            editionElement.setAttributeNode(collectionAttr);

            editionsElement.appendChild(editionElement);
        }
        editionCountAttr.setNodeValue(editionCount.toString());
        editionsElement.setAttributeNode(editionCountAttr);

        symbolicTimeSpanElement.appendChild(
            outputDocument_.createTextNode(symbolicTimeSpan)
        );

        if (timeSpan != null)
        {
            Element timeSpanBeginElement    = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":begin");
            Element timeSpanEndElement      = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":end");

            String timeSpanBegin    = String.valueOf(timeSpan.getBegin());
            String timeSpanEnd      = String.valueOf(timeSpan.getEnd());

            timeSpanBeginElement.appendChild(
                outputDocument_.createTextNode(timeSpanBegin)
            );
            timeSpanEndElement.appendChild(
                outputDocument_.createTextNode(timeSpanEnd)
            );

            timeSpanElement.appendChild(timeSpanBeginElement);
            timeSpanElement.appendChild(timeSpanEndElement);
        }

        userQueryElement.appendChild(
            outputDocument_.createTextNode(userQuery)
        );
        
        queryLanguageAttr.setNodeValue(queryLanguage);
        userQueryElement.setAttributeNode(queryLanguageAttr);

        queryParametersElement.appendChild(databaseIdElement);
        queryParametersElement.appendChild(editionsElement);
        queryParametersElement.appendChild(symbolicTimeSpanElement);
        queryParametersElement.appendChild(timeSpanElement);
        queryParametersElement.appendChild(userQueryElement);

        return queryParametersElement;
    }

    /**
     * Returns a DOM element containing the data in the retrieve parameters
     * property
     * @return
     */
    private Element getRetrieveParametersElement()
    {
        Element retrieveParametersElement   = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":retrieveParameters");
        Element fieldsElement               = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":fields");
        Element countElement                = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":count");
        Element firstRecordElement          = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":firstRecord");

        Attr fieldCountAttr = outputDocument_.createAttributeNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":count");

        List<QueryField> queryFields    = null;
        Integer queryFieldCount         = null;
        Integer maxRecordReturnCount    = null;
        Integer firstRecord             = null;

        queryFields             = retrieveParameters_.getFields();
        queryFieldCount         = queryFields.size();
        maxRecordReturnCount    = retrieveParameters_.getCount();
        firstRecord             = retrieveParameters_.getFirstRecord();

        for (QueryField queryField : queryFields)
        {
            Element fieldElement    = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":field");
            Element nameElement     = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":name");
            Element sortElement     = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":sort");

            nameElement.appendChild(outputDocument_.createTextNode(queryField.getName()));
            sortElement.appendChild(outputDocument_.createTextNode(queryField.getSort()));
            fieldElement.appendChild(nameElement);
            fieldElement.appendChild(sortElement);

            fieldsElement.appendChild(fieldElement);
        }
        fieldCountAttr.setNodeValue(queryFieldCount.toString());
        fieldsElement.setAttributeNode(fieldCountAttr);

        countElement.appendChild(
            outputDocument_.createTextNode(String.valueOf(maxRecordReturnCount))
        );

        firstRecordElement.appendChild(
            outputDocument_.createTextNode(String.valueOf(firstRecord))
        );
        
        retrieveParametersElement.appendChild(fieldsElement);
        retrieveParametersElement.appendChild(countElement);
        retrieveParametersElement.appendChild(firstRecordElement);

        return retrieveParametersElement;
    }

    /**
     * Returns a DOM element containing the both the query parameters and
     * retrieve parameters properties
     * @return
     */
    private Element getSearchRequestElement()
    {
        Element searchRequestElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":searchRequest");
        searchRequestElement.appendChild(getQueryParametersElement());
        searchRequestElement.appendChild(getRetrieveParametersElement());
        return searchRequestElement;
    }

    /**
     * Returns a DOM element containing the data in a given search result record
     * @param liteRecord A single search result record
     * @return
     */
    private Element getItemElement(LiteRecord liteRecord)
    {
        Element itemElement     = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":item");
        Element titlesElement   = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":titles");
        Element authorsElement  = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":authors");
        Element sourceElement   = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":source");
        Element keywordsElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":keywords");
        Element utElement       = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":ut");

        Attr titleCountAttr     = outputDocument_.createAttributeNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":count");
        Attr authorCountAttr    = outputDocument_.createAttributeNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":count");
        Attr keywordCountAttr   = outputDocument_.createAttributeNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":count");

        List<LabelValuesPair> titles        = null;
        List<LabelValuesPair> authors       = null;
        List<LabelValuesPair> sourceData    = null;
        List<LabelValuesPair> keywords      = null;
        String ut                           = null;
        
        titles      = liteRecord.getTitle();
        authors     = liteRecord.getAuthors();
        sourceData  = liteRecord.getSource();
        keywords    = liteRecord.getKeywords();
        ut          = liteRecord.getUT();

        {
            Integer titleCount = 0;

            for (LabelValuesPair pair : titles)
            {
                for (String value : pair.getValues())
                {
                    Element titleElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":title");
                    titleElement.appendChild(
                        outputDocument_.createTextNode(value)
                    );

                    titlesElement.appendChild(titleElement);

                    titleCount++;
                }
            }

            titleCountAttr.setNodeValue(titleCount.toString());
            titlesElement.setAttributeNode(titleCountAttr);
        }

        {
            Integer authorCount = 0;

            for (LabelValuesPair pair : authors)
            {
                for (String value : pair.getValues())
                {
                    Element authorElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":author");
                    authorElement.appendChild(
                        outputDocument_.createTextNode(value)
                    );

                    authorsElement.appendChild(authorElement);

                    authorCount++;
                }
            }

            authorCountAttr.setNodeValue(authorCount.toString());
            authorsElement.setAttributeNode(authorCountAttr);
        }

        {
            String sourceBookSeriesTitle    = null;
            String sourceTitle              = null;
            String sourceVolume             = null;
            String sourceIssue              = null;
            String sourcePages              = null;
            String sourcePublishedDate      = null;
            String sourcePublishedYear      = null;

            for (LabelValuesPair pair : sourceData)
            {
                String label = pair.getLabel();
                String value = pair.getValues().get(0);
                
                if (label.equalsIgnoreCase("BookSeriesTitle"))
                {
                    sourceBookSeriesTitle = value;
                }
                else if (label.equalsIgnoreCase("Issue"))
                {
                    sourceIssue = value;
                }
                else if (label.equalsIgnoreCase("Pages"))
                {
                    sourcePages = value;
                }
                else if (label.equalsIgnoreCase("Published.BiblioDate"))
                {
                    sourcePublishedDate = value;
                }
                else if (label.equalsIgnoreCase("Published.BiblioYear"))
                {
                    sourcePublishedYear = value;
                }
                else if (label.equalsIgnoreCase("SourceTitle"))
                {
                    sourceTitle = value;
                }
                else if (label.equalsIgnoreCase("Volume"))
                {
                    sourceVolume = value;
                }
            }

            if (sourceBookSeriesTitle != null)
            {
                Element bookSeriesTitleElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":bookSeriesTitle");
                sourceElement.appendChild(bookSeriesTitleElement);
                bookSeriesTitleElement.appendChild(outputDocument_.createTextNode(sourceBookSeriesTitle));
            }

            if (sourceTitle != null)
            {
                Element sourceTitleElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":title");
                sourceElement.appendChild(sourceTitleElement);
                sourceTitleElement.appendChild(outputDocument_.createTextNode(sourceTitle));
            }

            if (sourceVolume != null)
            {
                Element sourceVolumeElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":volume");
                sourceElement.appendChild(sourceVolumeElement);
                sourceVolumeElement.appendChild(outputDocument_.createTextNode(sourceVolume));
            }

            if (sourceIssue != null)
            {
                Element sourceIssueElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":issue");
                sourceElement.appendChild(sourceIssueElement);
                sourceIssueElement.appendChild(outputDocument_.createTextNode(sourceIssue));
            }

            if (sourcePages != null)
            {
                Element sourcePagesElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":pages");
                sourceElement.appendChild(sourcePagesElement);
                sourcePagesElement.appendChild(outputDocument_.createTextNode(sourcePages));
            }

            if (sourcePublishedDate != null || sourcePublishedYear != null)
            {
                Element sourcePublishedElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":published");
                sourceElement.appendChild(sourcePublishedElement);

                if (sourcePublishedDate != null)
                {
                    Element sourcePublishedDateElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":date");
                    sourcePublishedElement.appendChild(sourcePublishedDateElement);
                    sourcePublishedDateElement.appendChild(outputDocument_.createTextNode(sourcePublishedDate));
                }

                if (sourcePublishedYear != null)
                {
                    Element sourcePublishedYearElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":year");
                    sourcePublishedElement.appendChild(sourcePublishedYearElement);
                    sourcePublishedYearElement.appendChild(outputDocument_.createTextNode(sourcePublishedYear));
                }
            }
        }

        {
            Integer keywordCount = 0;

            for (LabelValuesPair pair : keywords)
            {
                for (String value : pair.getValues())
                {
                    Element keywordElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":keyword");
                    keywordElement.appendChild(
                        outputDocument_.createTextNode(value)
                    );

                    keywordsElement.appendChild(keywordElement);

                    keywordCount++;
                }
            }

            keywordCountAttr.setNodeValue(keywordCount.toString());
            keywordsElement.setAttributeNode(keywordCountAttr);
        }

        {
            utElement.appendChild(
                outputDocument_.createTextNode(ut)
            );
        }

        itemElement.appendChild(titlesElement);
        itemElement.appendChild(authorsElement);
        itemElement.appendChild(sourceElement);
        itemElement.appendChild(keywordsElement);
        itemElement.appendChild(utElement);

        return itemElement;
    }

    /**
     * Returns a DOM element containing the data from all search result records
     * contained within the search results property
     * @return
     */
    private Element getItemsElement()
    {
        Element itemsElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":items");

        for (LiteRecord item : searchResults_.getRecords())
        {
            itemsElement.appendChild( getItemElement(item));
        }

        return itemsElement;
    }

    /**
     * Returns a DOM element containing all the relevant data in the object
     * properties
     * @return
     */
    private Element getSearchResponseElement()
    {
        Element searchResponseElement = outputDocument_.createElementNS(BIBLIOSIGHT_NAMESPACE_URI, BIBLIOSIGHT_NAMESPACE_PREFIX + ":searchResponse");

        searchResponseElement.appendChild(getNumberOfItemsSearchedElement());
        searchResponseElement.appendChild(getNumberOfItemsFoundElement());
        searchResponseElement.appendChild(getNumberOfItemsListedElement());
        searchResponseElement.appendChild(getDateCreatedElement());
        searchResponseElement.appendChild(getItemsElement());
        searchResponseElement.appendChild(getSearchRequestElement());

        return searchResponseElement;
    }

    /**
     * Returns a DOM document containing all the relevant data in the object
     * @return
     * @throws BibliosightClientException
     */
    public Document getResultsAsDocument() throws BibliosightClientException
    {
        try
        {
            initOutputDocument();
            Element searchResponseElement = getSearchResponseElement();
            outputDocument_.getFirstChild().appendChild(searchResponseElement);

            return outputDocument_;
        }
        catch (DOMException ex)
        {
            throw new BibliosightClientException("Document could not be generated from search results", ex);
        }
    }
}