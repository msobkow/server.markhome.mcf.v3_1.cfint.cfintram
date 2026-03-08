
// Description: Java 25 in-memory RAM DbIO implementation for URLProtocol.

/*
 *	server.markhome.mcf.CFInt
 *
 *	Copyright (c) 2016-2026 Mark Stephen Sobkow
 *	
 *	Mark's Code Fractal 3.1 CFInt - Internet Essentials
 *	
 *	This file is part of Mark's Code Fractal CFInt.
 *	
 *	Mark's Code Fractal CFInt is available under dual commercial license from
 *	Mark Stephen Sobkow, or under the terms of the GNU Library General Public License,
 *	Version 3 or later.
 *	
 *	Mark's Code Fractal CFInt is free software: you can redistribute it and/or
 *	modify it under the terms of the GNU Library General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *	
 *	Mark's Code Fractal CFInt is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU Library General Public License
 *	along with Mark's Code Fractal CFInt.  If not, see <https://www.gnu.org/licenses/>.
 *	
 *	If you wish to modify and use this code without publishing your changes in order to
 *	tie it to proprietary code, please contact Mark Stephen Sobkow
 *	for a commercial license at mark.sobkow@gmail.com
 *	
 */

package server.markhome.mcf.v3_1.cfint.cfintram;

import java.math.*;
import java.sql.*;
import java.text.*;
import java.time.*;
import java.util.*;
import org.apache.commons.codec.binary.Base64;
import server.markhome.mcf.v3_1.cflib.*;
import server.markhome.mcf.v3_1.cflib.dbutil.*;

import server.markhome.mcf.v3_1.cfsec.cfsec.*;
import server.markhome.mcf.v3_1.cfint.cfint.*;
import server.markhome.mcf.v3_1.cfsec.cfsec.buff.*;
import server.markhome.mcf.v3_1.cfint.cfint.buff.*;
import server.markhome.mcf.v3_1.cfsec.cfsecobj.*;
import server.markhome.mcf.v3_1.cfint.cfintobj.*;

/*
 *	CFIntRamURLProtocolTable in-memory RAM DbIO implementation
 *	for URLProtocol.
 */
public class CFIntRamURLProtocolTable
	implements ICFIntURLProtocolTable
{
	private ICFIntSchema schema;
	private Map< Integer,
				CFIntBuffURLProtocol > dictByPKey
		= new HashMap< Integer,
				CFIntBuffURLProtocol >();
	private Map< CFIntBuffURLProtocolByUNameIdxKey,
			CFIntBuffURLProtocol > dictByUNameIdx
		= new HashMap< CFIntBuffURLProtocolByUNameIdxKey,
			CFIntBuffURLProtocol >();
	private Map< CFIntBuffURLProtocolByIsSecureIdxKey,
				Map< Integer,
					CFIntBuffURLProtocol >> dictByIsSecureIdx
		= new HashMap< CFIntBuffURLProtocolByIsSecureIdxKey,
				Map< Integer,
					CFIntBuffURLProtocol >>();

	public CFIntRamURLProtocolTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public CFIntBuffURLProtocol ensureRec(ICFIntURLProtocol rec) {
		if (rec == null) {
			return( null );
		}
		else {
			int classCode = rec.getClassCode();
			if (classCode == ICFIntURLProtocol.CLASS_CODE) {
				return( ((CFIntBuffURLProtocolDefaultFactory)(schema.getFactoryURLProtocol())).ensureRec((ICFIntURLProtocol)rec) );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), "ensureRec", "rec", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntURLProtocol createURLProtocol( ICFSecAuthorization Authorization,
		ICFIntURLProtocol iBuff )
	{
		final String S_ProcName = "createURLProtocol";
		
		CFIntBuffURLProtocol Buff = (CFIntBuffURLProtocol)ensureRec(iBuff);
		Integer pkey;
		pkey = schema.nextURLProtocolIdGen();
		Buff.setRequiredURLProtocolId( pkey );
		CFIntBuffURLProtocolByUNameIdxKey keyUNameIdx = (CFIntBuffURLProtocolByUNameIdxKey)schema.getFactoryURLProtocol().newByUNameIdxKey();
		keyUNameIdx.setRequiredName( Buff.getRequiredName() );

		CFIntBuffURLProtocolByIsSecureIdxKey keyIsSecureIdx = (CFIntBuffURLProtocolByIsSecureIdxKey)schema.getFactoryURLProtocol().newByIsSecureIdxKey();
		keyIsSecureIdx.setRequiredIsSecure( Buff.getRequiredIsSecure() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByUNameIdx.containsKey( keyUNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"URLProtocolUNameIdx",
				"URLProtocolUNameIdx",
				keyUNameIdx );
		}

		// Validate foreign keys

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		dictByUNameIdx.put( keyUNameIdx, Buff );

		Map< Integer, CFIntBuffURLProtocol > subdictIsSecureIdx;
		if( dictByIsSecureIdx.containsKey( keyIsSecureIdx ) ) {
			subdictIsSecureIdx = dictByIsSecureIdx.get( keyIsSecureIdx );
		}
		else {
			subdictIsSecureIdx = new HashMap< Integer, CFIntBuffURLProtocol >();
			dictByIsSecureIdx.put( keyIsSecureIdx, subdictIsSecureIdx );
		}
		subdictIsSecureIdx.put( pkey, Buff );

		if (Buff == null) {
			return( null );
		}
		else {
			int classCode = Buff.getClassCode();
			if (classCode == ICFIntURLProtocol.CLASS_CODE) {
				CFIntBuffURLProtocol retbuff = ((CFIntBuffURLProtocol)(schema.getFactoryURLProtocol().newRec()));
				retbuff.set(Buff);
				return( retbuff );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), S_ProcName, "-create-buff-cloning-", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntURLProtocol readDerived( ICFSecAuthorization Authorization,
		Integer PKey )
	{
		final String S_ProcName = "CFIntRamURLProtocol.readDerived";
		ICFIntURLProtocol buff;
		if( PKey == null ) {
			return( null );
		}
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntURLProtocol lockDerived( ICFSecAuthorization Authorization,
		Integer PKey )
	{
		final String S_ProcName = "CFIntRamURLProtocol.lockDerived";
		ICFIntURLProtocol buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntURLProtocol[] readAllDerived( ICFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamURLProtocol.readAllDerived";
		ICFIntURLProtocol[] retList = new ICFIntURLProtocol[ dictByPKey.values().size() ];
		Iterator< CFIntBuffURLProtocol > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	@Override
	public ICFIntURLProtocol readDerivedByUNameIdx( ICFSecAuthorization Authorization,
		String Name )
	{
		final String S_ProcName = "CFIntRamURLProtocol.readDerivedByUNameIdx";
		CFIntBuffURLProtocolByUNameIdxKey key = (CFIntBuffURLProtocolByUNameIdxKey)schema.getFactoryURLProtocol().newByUNameIdxKey();

		key.setRequiredName( Name );
		ICFIntURLProtocol buff;
		if( dictByUNameIdx.containsKey( key ) ) {
			buff = dictByUNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntURLProtocol[] readDerivedByIsSecureIdx( ICFSecAuthorization Authorization,
		boolean IsSecure )
	{
		final String S_ProcName = "CFIntRamURLProtocol.readDerivedByIsSecureIdx";
		CFIntBuffURLProtocolByIsSecureIdxKey key = (CFIntBuffURLProtocolByIsSecureIdxKey)schema.getFactoryURLProtocol().newByIsSecureIdxKey();

		key.setRequiredIsSecure( IsSecure );
		ICFIntURLProtocol[] recArray;
		if( dictByIsSecureIdx.containsKey( key ) ) {
			Map< Integer, CFIntBuffURLProtocol > subdictIsSecureIdx
				= dictByIsSecureIdx.get( key );
			recArray = new ICFIntURLProtocol[ subdictIsSecureIdx.size() ];
			Iterator< CFIntBuffURLProtocol > iter = subdictIsSecureIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< Integer, CFIntBuffURLProtocol > subdictIsSecureIdx
				= new HashMap< Integer, CFIntBuffURLProtocol >();
			dictByIsSecureIdx.put( key, subdictIsSecureIdx );
			recArray = new ICFIntURLProtocol[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntURLProtocol readDerivedByIdIdx( ICFSecAuthorization Authorization,
		int URLProtocolId )
	{
		final String S_ProcName = "CFIntRamURLProtocol.readDerivedByIdIdx() ";
		ICFIntURLProtocol buff;
		if( dictByPKey.containsKey( URLProtocolId ) ) {
			buff = dictByPKey.get( URLProtocolId );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntURLProtocol readRec( ICFSecAuthorization Authorization,
		Integer PKey )
	{
		final String S_ProcName = "CFIntRamURLProtocol.readRec";
		ICFIntURLProtocol buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntURLProtocol.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntURLProtocol lockRec( ICFSecAuthorization Authorization,
		Integer PKey )
	{
		final String S_ProcName = "lockRec";
		ICFIntURLProtocol buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntURLProtocol.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntURLProtocol[] readAllRec( ICFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamURLProtocol.readAllRec";
		ICFIntURLProtocol buff;
		ArrayList<ICFIntURLProtocol> filteredList = new ArrayList<ICFIntURLProtocol>();
		ICFIntURLProtocol[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntURLProtocol.CLASS_CODE ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new ICFIntURLProtocol[0] ) );
	}

	@Override
	public ICFIntURLProtocol readRecByIdIdx( ICFSecAuthorization Authorization,
		int URLProtocolId )
	{
		final String S_ProcName = "CFIntRamURLProtocol.readRecByIdIdx() ";
		ICFIntURLProtocol buff = readDerivedByIdIdx( Authorization,
			URLProtocolId );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntURLProtocol.CLASS_CODE ) ) {
			return( (ICFIntURLProtocol)buff );
		}
		else {
			return( null );
		}
	}

	@Override
	public ICFIntURLProtocol readRecByUNameIdx( ICFSecAuthorization Authorization,
		String Name )
	{
		final String S_ProcName = "CFIntRamURLProtocol.readRecByUNameIdx() ";
		ICFIntURLProtocol buff = readDerivedByUNameIdx( Authorization,
			Name );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntURLProtocol.CLASS_CODE ) ) {
			return( (ICFIntURLProtocol)buff );
		}
		else {
			return( null );
		}
	}

	@Override
	public ICFIntURLProtocol[] readRecByIsSecureIdx( ICFSecAuthorization Authorization,
		boolean IsSecure )
	{
		final String S_ProcName = "CFIntRamURLProtocol.readRecByIsSecureIdx() ";
		ICFIntURLProtocol buff;
		ArrayList<ICFIntURLProtocol> filteredList = new ArrayList<ICFIntURLProtocol>();
		ICFIntURLProtocol[] buffList = readDerivedByIsSecureIdx( Authorization,
			IsSecure );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntURLProtocol.CLASS_CODE ) ) {
				filteredList.add( (ICFIntURLProtocol)buff );
			}
		}
		return( filteredList.toArray( new ICFIntURLProtocol[0] ) );
	}

	public ICFIntURLProtocol updateURLProtocol( ICFSecAuthorization Authorization,
		ICFIntURLProtocol iBuff )
	{
		CFIntBuffURLProtocol Buff = (CFIntBuffURLProtocol)ensureRec(iBuff);
		Integer pkey = Buff.getPKey();
		CFIntBuffURLProtocol existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateURLProtocol",
				"Existing record not found",
				"Existing record not found",
				"URLProtocol",
				"URLProtocol",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateURLProtocol",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntBuffURLProtocolByUNameIdxKey existingKeyUNameIdx = (CFIntBuffURLProtocolByUNameIdxKey)schema.getFactoryURLProtocol().newByUNameIdxKey();
		existingKeyUNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffURLProtocolByUNameIdxKey newKeyUNameIdx = (CFIntBuffURLProtocolByUNameIdxKey)schema.getFactoryURLProtocol().newByUNameIdxKey();
		newKeyUNameIdx.setRequiredName( Buff.getRequiredName() );

		CFIntBuffURLProtocolByIsSecureIdxKey existingKeyIsSecureIdx = (CFIntBuffURLProtocolByIsSecureIdxKey)schema.getFactoryURLProtocol().newByIsSecureIdxKey();
		existingKeyIsSecureIdx.setRequiredIsSecure( existing.getRequiredIsSecure() );

		CFIntBuffURLProtocolByIsSecureIdxKey newKeyIsSecureIdx = (CFIntBuffURLProtocolByIsSecureIdxKey)schema.getFactoryURLProtocol().newByIsSecureIdxKey();
		newKeyIsSecureIdx.setRequiredIsSecure( Buff.getRequiredIsSecure() );

		// Check unique indexes

		if( ! existingKeyUNameIdx.equals( newKeyUNameIdx ) ) {
			if( dictByUNameIdx.containsKey( newKeyUNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateURLProtocol",
					"URLProtocolUNameIdx",
					"URLProtocolUNameIdx",
					newKeyUNameIdx );
			}
		}

		// Validate foreign keys

		// Update is valid

		Map< Integer, CFIntBuffURLProtocol > subdict;

		dictByPKey.remove( pkey );
		dictByPKey.put( pkey, Buff );

		dictByUNameIdx.remove( existingKeyUNameIdx );
		dictByUNameIdx.put( newKeyUNameIdx, Buff );

		subdict = dictByIsSecureIdx.get( existingKeyIsSecureIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByIsSecureIdx.containsKey( newKeyIsSecureIdx ) ) {
			subdict = dictByIsSecureIdx.get( newKeyIsSecureIdx );
		}
		else {
			subdict = new HashMap< Integer, CFIntBuffURLProtocol >();
			dictByIsSecureIdx.put( newKeyIsSecureIdx, subdict );
		}
		subdict.put( pkey, Buff );

		return(Buff);
	}

	@Override
	public void deleteURLProtocol( ICFSecAuthorization Authorization,
		ICFIntURLProtocol iBuff )
	{
		final String S_ProcName = "CFIntRamURLProtocolTable.deleteURLProtocol() ";
		CFIntBuffURLProtocol Buff = (CFIntBuffURLProtocol)ensureRec(iBuff);
		int classCode;
		Integer pkey = (Integer)(Buff.getPKey());
		CFIntBuffURLProtocol existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteURLProtocol",
				pkey );
		}
		CFIntBuffURLProtocolByUNameIdxKey keyUNameIdx = (CFIntBuffURLProtocolByUNameIdxKey)schema.getFactoryURLProtocol().newByUNameIdxKey();
		keyUNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffURLProtocolByIsSecureIdxKey keyIsSecureIdx = (CFIntBuffURLProtocolByIsSecureIdxKey)schema.getFactoryURLProtocol().newByIsSecureIdxKey();
		keyIsSecureIdx.setRequiredIsSecure( existing.getRequiredIsSecure() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< Integer, CFIntBuffURLProtocol > subdict;

		dictByPKey.remove( pkey );

		dictByUNameIdx.remove( keyUNameIdx );

		subdict = dictByIsSecureIdx.get( keyIsSecureIdx );
		subdict.remove( pkey );

	}
	@Override
	public void deleteURLProtocolByIdIdx( ICFSecAuthorization Authorization,
		Integer argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntBuffURLProtocol cur;
		LinkedList<CFIntBuffURLProtocol> matchSet = new LinkedList<CFIntBuffURLProtocol>();
		Iterator<CFIntBuffURLProtocol> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffURLProtocol> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffURLProtocol)(schema.getTableURLProtocol().readDerivedByIdIdx( Authorization,
				cur.getRequiredURLProtocolId() ));
			deleteURLProtocol( Authorization, cur );
		}
	}

	@Override
	public void deleteURLProtocolByUNameIdx( ICFSecAuthorization Authorization,
		String argName )
	{
		CFIntBuffURLProtocolByUNameIdxKey key = (CFIntBuffURLProtocolByUNameIdxKey)schema.getFactoryURLProtocol().newByUNameIdxKey();
		key.setRequiredName( argName );
		deleteURLProtocolByUNameIdx( Authorization, key );
	}

	@Override
	public void deleteURLProtocolByUNameIdx( ICFSecAuthorization Authorization,
		ICFIntURLProtocolByUNameIdxKey argKey )
	{
		CFIntBuffURLProtocol cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffURLProtocol> matchSet = new LinkedList<CFIntBuffURLProtocol>();
		Iterator<CFIntBuffURLProtocol> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffURLProtocol> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffURLProtocol)(schema.getTableURLProtocol().readDerivedByIdIdx( Authorization,
				cur.getRequiredURLProtocolId() ));
			deleteURLProtocol( Authorization, cur );
		}
	}

	@Override
	public void deleteURLProtocolByIsSecureIdx( ICFSecAuthorization Authorization,
		boolean argIsSecure )
	{
		CFIntBuffURLProtocolByIsSecureIdxKey key = (CFIntBuffURLProtocolByIsSecureIdxKey)schema.getFactoryURLProtocol().newByIsSecureIdxKey();
		key.setRequiredIsSecure( argIsSecure );
		deleteURLProtocolByIsSecureIdx( Authorization, key );
	}

	@Override
	public void deleteURLProtocolByIsSecureIdx( ICFSecAuthorization Authorization,
		ICFIntURLProtocolByIsSecureIdxKey argKey )
	{
		CFIntBuffURLProtocol cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffURLProtocol> matchSet = new LinkedList<CFIntBuffURLProtocol>();
		Iterator<CFIntBuffURLProtocol> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffURLProtocol> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffURLProtocol)(schema.getTableURLProtocol().readDerivedByIdIdx( Authorization,
				cur.getRequiredURLProtocolId() ));
			deleteURLProtocol( Authorization, cur );
		}
	}
}
