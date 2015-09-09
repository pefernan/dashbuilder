package org.dashbuilder.dataprovider.backend.elasticsearch.rest.util;

import org.dashbuilder.dataprovider.backend.elasticsearch.ElasticSearchValueTypeMapper;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.ElasticSearchClient;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.exception.ElasticSearchClientGenericException;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.Query;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchHitResponse;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchRequest;
import org.dashbuilder.dataprovider.backend.elasticsearch.rest.model.SearchResponse;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.ElasticSearchDataSetDef;
import org.dashbuilder.dataset.group.AggregateFunctionType;
import org.dashbuilder.dataset.group.DataSetGroup;
import org.dashbuilder.dataset.group.GroupFunction;
import org.dashbuilder.dataset.impl.DataColumnImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * @since 0.3.0
 */
@ApplicationScoped
public class ElasticSearchDateUtils {

    @Inject
    protected ElasticSearchValueTypeMapper valueTypeMapper;

    public ElasticSearchDateUtils() {
    }

    public ElasticSearchDateUtils(ElasticSearchValueTypeMapper valueTypeMapper) {
        this.valueTypeMapper = valueTypeMapper;
    }

    /**
     * <p>Obtain the minimum date and maximum date values for the given column with identifier <code>dateColumnId</code>.</p>
     *
     * @param  client The client for performing the query to ELS
     * @param metadata The data set metadata
     * @param dateColumnId The column identifier for the date type column
     * @param query The query model, if any, for filtering the results
     * @return The minimum and maximum dates.
     */
    public Date[] calculateDateLimits(ElasticSearchClient client, DataSetMetadata metadata, String dateColumnId, Query query) throws ElasticSearchClientGenericException {

        ElasticSearchDataSetDef def = (ElasticSearchDataSetDef) metadata.getDefinition();

        // The resulting data set columns.
        String minDateColumnId = dateColumnId + "_min";
        String maxDateColumnId = dateColumnId + "_max";
        DataColumn minDateColumn = new DataColumnImpl(minDateColumnId, ColumnType.NUMBER);
        DataColumn maxDateColumn = new DataColumnImpl(maxDateColumnId, ColumnType.NUMBER);
        List<DataColumn> columns = new ArrayList<DataColumn>(2);
        columns.add(minDateColumn);
        columns.add(maxDateColumn);

        // Create the aggregation model to bulid the query to EL server.
        DataSetGroup aggregation = new DataSetGroup();
        GroupFunction minFunction = new GroupFunction(dateColumnId, minDateColumnId, AggregateFunctionType.MIN);
        GroupFunction maxFunction = new GroupFunction(dateColumnId, maxDateColumnId, AggregateFunctionType.MAX);
        aggregation.addGroupFunction(minFunction, maxFunction);

        SearchRequest request = new SearchRequest(metadata);
        request.setColumns(columns);
        request.setAggregations(Arrays.asList(aggregation));

        // Append the filter clauses
        if (query != null) request.setQuery(query);

        // Perform the query.
        SearchResponse searchResult = client.search(def, metadata, request);
        if (searchResult != null) {
            SearchHitResponse[] hits = searchResult.getHits();
            if (hits != null && hits.length == 1) {
                SearchHitResponse hit0 = hits[0];
                Map<String, Object> fields = hit0.getFields();
                if (fields != null && !fields.isEmpty()) {
                    Double min = (Double) fields.get(minDateColumnId);
                    Double max = (Double) fields.get(maxDateColumnId);
                    Date minValue = valueTypeMapper.parseDate(def, dateColumnId, min.longValue());
                    Date maxValue = valueTypeMapper.parseDate(def, dateColumnId, max.longValue());
                    return new Date[] {minValue, maxValue};
                }
            }
        }

        return null;
    }
    
}
