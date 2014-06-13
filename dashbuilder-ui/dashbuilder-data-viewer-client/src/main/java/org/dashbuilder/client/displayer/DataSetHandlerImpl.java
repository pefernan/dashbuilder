    /**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.client.displayer;

import org.dashbuilder.client.dataset.DataSetLookupClient;
import org.dashbuilder.client.dataset.DataSetMetadataCallback;
import org.dashbuilder.client.dataset.DataSetReadyCallback;
import org.dashbuilder.model.dataset.DataSetLookup;
import org.dashbuilder.model.dataset.DataSetMetadata;
import org.dashbuilder.model.dataset.DataSetOpType;
import org.dashbuilder.model.dataset.group.ColumnGroup;
import org.dashbuilder.model.dataset.group.DataSetGroup;
import org.dashbuilder.model.dataset.sort.DataSetSort;

public class DataSetHandlerImpl implements DataSetHandler {

    protected DataSetLookupClient dataSetLookupClient;
    protected DataSetMetadata dataSetMetadata;
    protected DataSetLookup lookupBase;
    protected DataSetLookup lookupCurrent;

    public DataSetHandlerImpl(DataSetLookupClient dataSetLookupClient, DataSetLookup lookup) {
        this.dataSetLookupClient = dataSetLookupClient;
        this.lookupBase = lookup;
        this.lookupCurrent = lookup.cloneInstance();

        try {
            // Fetch the data set metadata
            dataSetLookupClient.fetchMetadata(lookupBase, new DataSetMetadataCallback() {
                public void callback(DataSetMetadata metadata) {
                    dataSetMetadata = metadata;
                }
                public void notFound() {
                    throw new RuntimeException("DataSet not found. UUID=" + lookupBase.getDataSetUUID());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DataSetMetadata getDataSetMetadata() {
        return dataSetMetadata;
    }

    public void resetAllOperations() {
        this.lookupCurrent = lookupBase.cloneInstance();
    }

    public void limitDataSetRows(int offset, int rows) {
        lookupCurrent.setRowOffset(offset);
        lookupCurrent.setNumberOfRows(rows);
    }

    public DataSetGroup getGroupOperation(String columnId) {
        for (DataSetGroup op : lookupCurrent.getOperationList(DataSetGroup.class)) {
            if (op.getColumnGroup() == null) continue;

            if (op.getColumnGroup().getColumnId().equals(columnId)) {
                return op;
            }
            if (op.getColumnGroup().getSourceId().equals(columnId)) {
                return op;
            }
        }
        return null;
    }

    public boolean addGroupOperation(DataSetGroup op) {
        ColumnGroup cg = op.getColumnGroup();
        if (cg == null) {
            throw new RuntimeException("Group ops requires to specify a pivot column.");
        }
        if (getGroupOperation(cg.getSourceId()) == null) {
            lookupCurrent.addOperation(0, op);
            return true;
        }
        return false;
    }

    public boolean removeFirstGroupOperation(ColumnGroup columnGroup) {
        for (DataSetGroup next : lookupCurrent.getOperationList(DataSetGroup.class)) {
            ColumnGroup cg = next.getColumnGroup();
            if (cg == null) continue;

            if (cg.equals(columnGroup)) {
                lookupCurrent.getOperationList().remove(next);
                return true;
            }
        }
        return false;
    }

    public void setSortOperation(DataSetSort op) {
        cleaSortOperations();
        lookupCurrent.addOperation(op);
    }

    public boolean cleaSortOperations() {
        int n = lookupCurrent.removeOperations(DataSetOpType.SORT);
        return n > 0;
    }

    public void lookupDataSet(DataSetReadyCallback callback) throws Exception {
        dataSetLookupClient.lookupDataSet(lookupCurrent, callback);
    }
}