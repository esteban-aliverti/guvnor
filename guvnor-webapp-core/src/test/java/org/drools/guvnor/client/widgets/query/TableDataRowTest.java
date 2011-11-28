/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.client.widgets.query;

import static org.junit.Assert.assertEquals;

import org.drools.guvnor.client.rpc.TableDataRow;
import org.junit.Test;

public class TableDataRowTest {

    @Test
    public void testRow() {
        TableDataRow row = new TableDataRow();
        row.id = "HJKHFKJHFDJS";
        row.format = "rule";
        row.values = new String[]{"name", "x"};
        
        assertEquals("name", row.getDisplayName());
        
        assertEquals(row.id + "," + row.format, row.getKeyValue());
        
        assertEquals(row.id, TableDataRow.getId( row.getKeyValue() ));
        assertEquals(row.format, TableDataRow.getFormat( row.getKeyValue()));
    }
    
}
