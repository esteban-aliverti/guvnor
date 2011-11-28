/*
 * Copyright 2010 JBoss Inc
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

package org.drools.guvnor.server.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.drools.guvnor.client.rpc.DiscussionRecord;
import org.junit.Test;

public class DiscussionTest {

    @Test
    public void testPersist() throws InterruptedException {
        DiscussionRecord dr = new DiscussionRecord();
        dr.author = "mic";
        dr.note = "hey hey";

        DiscussionRecord dr2 = new DiscussionRecord();
        dr2.author = "chloe";
        dr2.note = "hey hey";

        Thread.sleep(100);

        Discussion d = new Discussion();
        List<DiscussionRecord> drs = new ArrayList<DiscussionRecord>();
        drs.add(dr);
        drs.add(dr2);
        String xml = d.toString(drs);
        System.err.println(xml);

        List<DiscussionRecord> res = d.fromString(xml);
        assertEquals(2, res.size());

        assertEquals("mic", res.get(0).author);

        assertEquals(dr.timestamp, res.get(0).timestamp);
        

        assertNotNull(d.fromString(null));
        assertNotNull(d.fromString(""));

        DiscussionRecord dr3 = new DiscussionRecord();
        dr3.author = "sam";
        dr3.note = "yeah !";
        res.add(dr3);

        assertTrue(d.toString(res).indexOf("sam") > -1);
        List<DiscussionRecord> d_ = d.fromString(d.toString(res));
        assertEquals(3, d_.size());
        assertEquals("sam", d_.get(2).author);


    }

}
