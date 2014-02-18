/*
* Copyright 2013 Mikhail Khodonov
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*
* $Id: SQLModifierTest.java 4 2013-05-28 12:48:13Z khomisha $
*/

package org.homedns.mkh.sqlmodifier;

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.akiban.sql.StandardException;

/**
 * Unit test for SQLModifier
 *
 */
@RunWith( Parameterized.class )
public class SQLModifierTest {
	private String _sQuery;
	private String _sAddingWhere;

	public SQLModifierTest( String sQuery, String sAddingWhere ) {
		_sQuery = sQuery;
		_sAddingWhere = sAddingWhere;
	}

	@Test
	public void testParseQuery( ) throws StandardException {
		SQLModifier parser = new SQLModifier( );
		parser.parseQuery( _sQuery );
		assertTrue( _sQuery, parser.isParsed( ) );
	}

	@Test
	public void testModifyQuery( ) throws StandardException {
		SQLModifier parser = new SQLModifier( );
		parser.parseQuery( _sQuery );
		System.out.println( parser.modifyQuery( _sAddingWhere ) );
	}

	@Parameterized.Parameters
	public static List< String[] > getTestData( ) {
	    return(
	    	Arrays.asList(
		    	new String[][] {
		    		{
		    			"SELECT TRO_EVENT.TRE_ID, D_EVENT.EVT_CODE,  D_EVENT.EVT_NAME, D_EVENT.EVT_IS_SECURITY, " +
		    			"TRO_OBJECT.TRO_UID, D_TRO_STATE.STA_NAME, TRO_EVENT.TRE_DATE, TRO_EVENT.TRE_MSG  FROM  " +
		    			"TRO_EVENT, D_TRO_STATE, TRO_OBJECT, D_EVENT WHERE  " +
	    				"D_TRO_STATE.STA_ID = TRO_OBJECT.STA_ID AND TRO_OBJECT.TRO_ID = TRO_EVENT.TRO_ID AND " +
	    				"D_EVENT.EVT_ID = TRO_EVENT.EVT_ID AND TRO_OBJECT.TRO_ID = ?" +
	    				" UNION " +
	    				"SELECT TRO_EVENT.TRE_ID, D_EVENT.EVT_CODE,  D_EVENT.EVT_NAME, D_EVENT.EVT_IS_SECURITY, " +
	    				"TRO_OBJECT.TRO_UID, D_TRO_STATE.STA_NAME, TRO_EVENT.TRE_DATE, TRO_EVENT.TRE_MSG  FROM  " +
	    				"TRO_EVENT, D_TRO_STATE, TRO_OBJECT, D_EVENT WHERE  " +
	    				"D_TRO_STATE.STA_ID = 1 AND D_TRO_STATE.STA_ID = TRO_OBJECT.STA_ID AND TRO_OBJECT.TRO_ID = TRO_EVENT.TRO_ID AND " +
	    				"D_EVENT.EVT_ID = TRO_EVENT.EVT_ID AND TRO_OBJECT.TRO_ID = ?",
	    				"D_EVENT.EVT_ID = 3"
	    			},
		    		{
	    				"SELECT TRO_EVENT.TRE_ID, D_EVENT.EVT_CODE,  D_EVENT.EVT_NAME, D_EVENT.EVT_IS_SECURITY, " +
	    				"TRO_OBJECT.TRO_UID, D_TRO_STATE.STA_NAME, TRO_EVENT.TRE_DATE, TRO_EVENT.TRE_MSG  FROM  " +
	    				"TRO_EVENT, D_TRO_STATE, TRO_OBJECT, D_EVENT WHERE  " +
	    				"D_TRO_STATE.STA_ID = TRO_OBJECT.STA_ID AND TRO_OBJECT.TRO_ID = TRO_EVENT.TRO_ID AND " +
	    				"D_EVENT.EVT_ID = TRO_EVENT.EVT_ID AND TRO_OBJECT.TRO_ID = ? ORDER BY D_EVENT.EVT_ID LIMIT 100",
	    				"D_EVENT.EVT_ID = 3"
		    		},
		    		{
		    			"select * from egm_meter_report order by sta_id", "egm_meter_report.EVT_ID = 3"
		    		}
		    	}
		    )
	    );
	}
}
