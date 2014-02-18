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
* $Id: SQLModifier.java 5 2013-07-16 14:00:34Z khomisha $
*/

package org.homedns.mkh.sqlmodifier;

import java.util.ArrayList;
import java.util.List;
import com.akiban.sql.StandardException;
import com.akiban.sql.parser.*;
import com.akiban.sql.unparser.NodeToString;

/**
 * SQL parser. Doesn't support postgresql specific stored procedures calls, e.g.
 * <p>
 * <code> select * from get_person( ?, ? ) </code> or
 * <code> select set_log_params </code>
 * </p>
 */
public class SQLModifier {
	public static final String AND = "AND";
	public static final String OR = "OR";
	
	private com.akiban.sql.parser.SQLParser _parser;
	private List< SelectNode > _nodes = new ArrayList< SelectNode >( );
	private CursorNode _cn;
	
	public SQLModifier( ) {
		_parser = new com.akiban.sql.parser.SQLParser( );
	}
	
	/**
	 * Parses SQL query.
	 * 
	 * @param sQuery
	 *            the sql query to parse
	 * 
	 * @throws StandardException
	 */
	public void parseQuery( String sQuery ) throws StandardException {
		try {
			StatementNode node = _parser.parseStatement( sQuery );
			if( node instanceof CursorNode ) {
				_nodes.clear( );
				_cn = ( CursorNode )node;
				_cn.accept( new HasNodeVisitor( CursorNode.class ) {
					@Override
					public Visitable visit( Visitable node ) {
						onVisit( node );
						return( node );
					}

					@Override
					public boolean stopTraversal( ) {
						return( false );
					}

					@Override
					public boolean visitChildrenFirst( Visitable node ) {
						return( false );
					}

					@Override
					public boolean skipChildren( Visitable node ) {
						return( false );
					}
				} );
			} else {
				throw new IllegalArgumentException( "wrong input query: "
						+ sQuery );
			}
		} catch( StandardException e ) {
			throw new StandardException( sQuery, e );
		}
	}

	/**
	 * Returns true if sql query was parsed and false otherwise.
	 * 
	 * @return true if sql query was parsed and false otherwise.
	 */
	public boolean isParsed( ) {
		return( !_nodes.isEmpty( ) );
	}
	
	/**
	 * Modifies query via adding WHERE conditions with AND as operator between
	 * existing condition and adding Adding condition format:
	 * <code>condition<sub>1</sub> logical_operator<sub>1</sub> condition<sub>2</sub>... logical_operator<sub>i</sub> condition<sub>i+1</sub></code>
	 * 
	 * @param sAddWhere
	 *            additional conditions for the WHERE clause
	 * 
	 * @return the modified query.
	 * 
	 * @throws StandardException
	 */
	public String modifyQuery( String sAddingWhere ) throws StandardException {
		return( modifyQuery( AND, sAddingWhere ) );
	}
	
	/**
	 * Modifies query via adding WHERE conditions. Adding condition format:
	 * <code>condition<sub>1</sub> logical_operator<sub>1</sub> condition<sub>2</sub>... logical_operator<sub>i</sub> condition<sub>i+1</sub></code>
	 * 
	 * @param sOperator
	 *            the operator between existing condition and adding
	 * @param sAddWhere
	 *            the additional conditions for the WHERE clause
	 * 
	 * @return the modified query.
	 * 
	 * @throws StandardException
	 */
	public String modifyQuery( String sOperator, String sAddingWhere )
			throws StandardException {
		NodeToString node2String = new NodeToString( );
		try {
			for( SelectNode sn : _nodes ) {
				ValueNode vn = sn.getWhereClause( );
				if( vn != null ) {
					sAddingWhere = node2String.toString( vn ) + " " + sOperator
							+ " " + sAddingWhere;
				}
				vn = getWhereNode( sAddingWhere );
				sn.setWhereClause( vn );
			}
			return( node2String.toString( _cn ) );
		} catch( StandardException e ) {
			throw new StandardException( "adding condition: " + sAddingWhere, e );
		}
	}

	/**
	 * Checks sql query.
	 * 
	 * @param sQuery
	 *            the sql query to check
	 * 
	 * @throws StandardException
	 */
	public void checkSyntaxQuery( String sQuery ) throws StandardException {
		try {
			_parser.parseStatement( sQuery );
		} catch( StandardException e ) {
			throw new StandardException( sQuery, e );
		}
	}

	/**
	 * On visit sets select nodes.
	 * 
	 * @param node
	 *            the node to visit
	 */
	protected void onVisit( Visitable node ) {
		if( node instanceof SelectNode ) {
			_nodes.add( ( SelectNode )node );
		}
	}
	
	/**
	 * Returns value node which represents WHERE conditions.
	 * 
	 * @param sCondition
	 *            the WHERE condition in following format:
	 *            <code>condition<sub>1</sub> logical_operator<sub>1</sub> condition<sub>2</sub>... logical_operator<sub>i</sub> condition<sub>i+1</sub></code>
	 * 
	 * @return the WHERE clause value node.
	 * 
	 * @throws StandardException
	 */
	private ValueNode getWhereNode( String sCondition ) throws StandardException {
		String sQuery = "select * from t where " + sCondition;
		StatementNode node = _parser.parseStatement( sQuery );
		CursorNode cn = ( CursorNode )node;
		SelectNode sn = ( SelectNode )cn.getResultSetNode( );
		return( sn.getWhereClause( ) );
	}
}
