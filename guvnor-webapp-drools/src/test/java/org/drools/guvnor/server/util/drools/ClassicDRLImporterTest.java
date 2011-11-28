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

package org.drools.guvnor.server.util.drools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.server.contenthandler.drools.DRLFileContentHandler;
import org.drools.guvnor.server.util.ClassicDRLImporter;
import org.drools.guvnor.server.util.ClassicDRLImporter.Asset;
import org.junit.Test;

public class ClassicDRLImporterTest {

    @Test
    public void testStandardDRL() throws Exception {

        ClassicDRLImporter imp = new ClassicDRLImporter( getDrl( "sample_legacy.drl" ) );
        assertEquals( "foo",
                      imp.getPackageName() );
        assertEquals( 2,
                      imp.getAssets().size() );

        assertEquals( "blah",
                      imp.getAssets().get( 0 ).name );
        assertEquals( "cha",
                      imp.getAssets().get( 1 ).name );

        System.err.println( imp.getPackageHeader() );

        assertTrue( imp.getPackageHeader().indexOf( "import goo.wee" ) > -1 );
        assertTrue( imp.getPackageHeader().indexOf( "package" ) == -1 );

        assertFalse( imp.isDSLEnabled() );

        assertEqualsIgnoreWhitespace( "when Whee() then goo();",
                                      imp.getAssets().get( 0 ).content );
        assertEqualsIgnoreWhitespace( "when Sup() then ka();",
                                      imp.getAssets().get( 1 ).content );
        assertTrue( imp.getAssets().get( 0 ).content.indexOf( " Whee()" ) > -1 );

    }

    @Test
    public void testStandardWithQuotes() throws Exception {
        ClassicDRLImporter imp = new ClassicDRLImporter( getDrl( "sample_legacy_quotes.drl" ) );
        assertEquals( "foo",
                      imp.getPackageName() );
        assertEquals( 1,
                      imp.getAssets().size() );
        assertEquals(-1, imp.getAssets().get(0).name.indexOf("'"));
    }

    @Test
    public void testWithFunction() throws Exception {
        //        Pattern p = Pattern.compile("function\\s+.*\\s+(.*)\\(.*\\).*");
        //        Matcher m = p.matcher("function void fooBar() {");
        //        assertTrue(m.matches());
        //        System.err.println(m.group());
        //        assertEquals("fooBar", m.group(1));

        ClassicDRLImporter imp = new ClassicDRLImporter( getDrl( "sample_legacy_functions.drl" ) );
        assertFalse( imp.isDSLEnabled() );

        assertEquals( 7,
                      imp.getAssets().size() );
        assertEquals( AssetFormats.FUNCTION,
                      imp.getAssets().get( 0 ).format );
        assertEquals( AssetFormats.FUNCTION,
                      imp.getAssets().get( 1 ).format );

        assertEquals( "goo1",
                      imp.getAssets().get( 0 ).name );
        assertEqualsIgnoreWhitespace( "function void goo1() { //do something ! { yeah } }",
                                      imp.getAssets().get( 0 ).content );

        assertEquals( "goo2",
                      imp.getAssets().get( 1 ).name );
        assertEqualsIgnoreWhitespace( "function String goo2(String la) { //yeah man ! return \"whee\"; }",
                                      imp.getAssets().get( 1 ).content );

        assertEquals( "goo3",
                      imp.getAssets().get( 2 ).name );
        assertEqualsIgnoreWhitespace( "function String goo3() { return \"HELLO\"; }",
                                      imp.getAssets().get( 2 ).content );

        assertEquals( "goo4",
                      imp.getAssets().get( 3 ).name );
        assertEqualsIgnoreWhitespace( "function String goo4() { if( true ) { return \"HELLO\"; } }",
                                      imp.getAssets().get( 3 ).content );

        assertEquals( "goo6",
                      imp.getAssets().get( 4 ).name );
        assertEqualsIgnoreWhitespace( "function String goo6() { return \"HELLO\"; /* } */ /* } } } */ }",
                                      imp.getAssets().get( 4 ).content );

        assertEquals( AssetFormats.DRL,
                      imp.getAssets().get( 5 ).format );
        assertEquals( AssetFormats.DRL,
                      imp.getAssets().get( 6 ).format );
        assertNotNull( imp.getAssets().get( 6 ).content );

    }

    @Test
    public void testWithDSL() throws Exception {

        ClassicDRLImporter imp = new ClassicDRLImporter( getDrl( "sample_legacy_with_dsl.drl" ) );

        assertTrue( imp.isDSLEnabled() );
        assertEquals( 2,
                      imp.getAssets().size() );
        System.out.println( imp.getPackageHeader()  );
        
        assertEquals( "foo",
                      imp.getPackageName() );
        assertTrue( imp.getPackageHeader().contains( "import goo.wee" ));
        assertTrue( imp.getPackageHeader().contains( "global ka.cha" ));

        assertEqualsIgnoreWhitespace( "when ka chow then bam",
                                      imp.getAssets().get( 0 ).content );
        assertEqualsIgnoreWhitespace( "when ka chiga then ka chow",
                                      imp.getAssets().get( 1 ).content );

        Asset as = imp.getAssets().get( 0 );
        assertEquals( AssetFormats.DSL_TEMPLATE_RULE,
                      as.format );

    }

    @Test
    public void testComplexExample() throws Exception {
        ClassicDRLImporter imp = new ClassicDRLImporter( getDrl( "sample_complex.drl" ) );
        assertFalse( imp.isDSLEnabled() );
        assertEquals( 2,
                      imp.getAssets().size() );

        assertTrue( DRLFileContentHandler.isStandAloneRule( imp.getAssets().get( 0 ).content ) );
    }

    private InputStream getDrl(String file) throws IOException {
        return this.getClass().getResourceAsStream( file );
    }

    private void assertEqualsIgnoreWhitespace(final String expected,
                                              final String actual) {
        final String cleanExpected = expected.replaceAll( "\\s+",
                                                          "" );
        final String cleanActual = actual.replaceAll( "\\s+",
                                                      "" );

        assertEquals( cleanExpected,
                      cleanActual );
    }

    @Test
    public void testMergeHeader() {
        String header = "import foo.bar\nimport wee.waa\n\nglobal goo.ber baz\n";
        String toMerge = "import ninja\nimport foo.bar\nimport slack.bladder\n\nimport wee.waa";

        String result = ClassicDRLImporter.mergeLines( header,
                                                       toMerge );

        assertEquals( "import foo.bar\nimport wee.waa\n\nglobal goo.ber baz\n\nimport ninja\nimport slack.bladder",
                      result );

        assertEquals( "abc",
                      ClassicDRLImporter.mergeLines( "abc",
                                                     "" ) );

        assertEquals( "qed",
                      ClassicDRLImporter.mergeLines( "qed",
                                                     null ) );

        assertEquals( "xyz",
                      ClassicDRLImporter.mergeLines( "",
                                                     "xyz" ) );
        assertEquals( "xyz",
                      ClassicDRLImporter.mergeLines( null,
                                                     "xyz" ) );
    }

}
