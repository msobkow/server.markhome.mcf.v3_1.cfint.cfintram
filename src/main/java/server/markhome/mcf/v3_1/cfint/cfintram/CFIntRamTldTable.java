
// Description: Java 25 in-memory RAM DbIO implementation for Tld.

/*
 *	server.markhome.mcf.CFInt
 *
 *	Copyright (c) 2016-2026 Mark Stephen Sobkow
 *	
 *	Mark's Code Fractal 3.1 CFInt - Internet Essentials
 *	
 *	This file is part of Mark's Code Fractal CFInt.
 *	
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *	
 *	http://www.apache.org/licenses/LICENSE-2.0
 *	
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
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
import server.markhome.mcf.v3_1.cflib.keyhash.*;

import server.markhome.mcf.v3_1.cfsec.cfsec.*;
import server.markhome.mcf.v3_1.cfint.cfint.*;
import server.markhome.mcf.v3_1.cfsec.cfsec.buff.*;
import server.markhome.mcf.v3_1.cfint.cfint.buff.*;
import server.markhome.mcf.v3_1.cfsec.cfsecobj.*;
import server.markhome.mcf.v3_1.cfint.cfintobj.*;

/*
 *	CFIntRamTldTable in-memory RAM DbIO implementation
 *	for Tld.
 */
public class CFIntRamTldTable
	implements ICFIntTldTable
{
	private ICFIntSchema schema;
	private Map< $implCommaIJavaOptAtomType$,
				CFIntBuffTld > dictByPKey
		= new HashMap< $implCommaIJavaOptAtomType$,
				CFIntBuffTld >();
	private Map< CFIntBuffTldByTenantIdxKey,
				Map< $implCommaIJavaOptAtomType$,
					CFIntBuffTld >> dictByTenantIdx
		= new HashMap< CFIntBuffTldByTenantIdxKey,
				Map< $implCommaIJavaOptAtomType$,
					CFIntBuffTld >>();
	private Map< CFIntBuffTldByNameIdxKey,
			CFIntBuffTld > dictByNameIdx
		= new HashMap< CFIntBuffTldByNameIdxKey,
			CFIntBuffTld >();

	public CFIntRamTldTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public CFIntBuffTld ensureRec(ICFIntTld rec) {
		return (((CFIntBuffTldFactoryService)(schema.getCFIntBuffFactory().getFactoryTld())).ensureRec(rec));
	}

	@Override
	public ICFIntTld createTld( ICFSecAuthorization Authorization,
		ICFIntTld iBuff )
	{
		final String S_ProcName = "createTld";
		
		CFIntBuffTld Buff = (CFIntBuffTld)ensureRec(iBuff);
		$implCommaIJavaOptAtomType$ pkey;
		pkey = schema.nextTldIdGen();
		Buff.setRequiredId( pkey );
		CFIntBuffTldByTenantIdxKey keyTenantIdx = (CFIntBuffTldByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffTldByNameIdxKey keyNameIdx = (CFIntBuffTldByNameIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByNameIdxKey();
		keyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByNameIdx.containsKey( keyNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"TldNameIdx",
				"TldNameIdx",
				keyNameIdx );
		}

		// Validate foreign keys

		{
			boolean allNull = true;
			allNull = false;
			if( ! allNull ) {
				if( null == schema.getTableTenant().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTenantId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Container",
						"Container",
						"TldTenant",
						"TldTenant",
						"Tenant",
						"Tenant",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< $implCommaIJavaOptAtomType$, CFIntBuffTld > subdictTenantIdx;
		if( dictByTenantIdx.containsKey( keyTenantIdx ) ) {
			subdictTenantIdx = dictByTenantIdx.get( keyTenantIdx );
		}
		else {
			subdictTenantIdx = new HashMap< $implCommaIJavaOptAtomType$, CFIntBuffTld >();
			dictByTenantIdx.put( keyTenantIdx, subdictTenantIdx );
		}
		subdictTenantIdx.put( pkey, Buff );

		dictByNameIdx.put( keyNameIdx, Buff );

		if (Buff == null) {
			return( null );
		}
		else {
			int classCode = Buff.getClassCode();
			if (classCode == ICFIntTld.CLASS_CODE) {
				CFIntBuffTld retbuff = ((CFIntBuffTld)(schema.getCFIntBuffFactory().getFactoryTld().newRec()));
				retbuff.set(Buff);
				return( retbuff );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), S_ProcName, "-create-buff-cloning-", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntTld readDerived( ICFSecAuthorization Authorization,
		$implCommaIJavaOptAtomType$ PKey )
	{
		final String S_ProcName = "CFIntRamTld.readDerived";
		ICFIntTld buff;
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
	public ICFIntTld lockDerived( ICFSecAuthorization Authorization,
		$implCommaIJavaOptAtomType$ PKey )
	{
		final String S_ProcName = "CFIntRamTld.lockDerived";
		ICFIntTld buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTld[] readAllDerived( ICFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamTld.readAllDerived";
		ICFIntTld[] retList = new ICFIntTld[ dictByPKey.values().size() ];
		Iterator< CFIntBuffTld > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	@Override
	public ICFIntTld[] readDerivedByTenantIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamTld.readDerivedByTenantIdx";
		CFIntBuffTldByTenantIdxKey key = (CFIntBuffTldByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByTenantIdxKey();

		key.setRequiredTenantId( TenantId );
		ICFIntTld[] recArray;
		if( dictByTenantIdx.containsKey( key ) ) {
			Map< $implCommaIJavaOptAtomType$, CFIntBuffTld > subdictTenantIdx
				= dictByTenantIdx.get( key );
			recArray = new ICFIntTld[ subdictTenantIdx.size() ];
			Iterator< CFIntBuffTld > iter = subdictTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< $implCommaIJavaOptAtomType$, CFIntBuffTld > subdictTenantIdx
				= new HashMap< $implCommaIJavaOptAtomType$, CFIntBuffTld >();
			dictByTenantIdx.put( key, subdictTenantIdx );
			recArray = new ICFIntTld[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntTld readDerivedByNameIdx( ICFSecAuthorization Authorization,
		String Name )
	{
		final String S_ProcName = "CFIntRamTld.readDerivedByNameIdx";
		CFIntBuffTldByNameIdxKey key = (CFIntBuffTldByNameIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByNameIdxKey();

		key.setRequiredName( Name );
		ICFIntTld buff;
		if( dictByNameIdx.containsKey( key ) ) {
			buff = dictByNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTld readDerivedByIdIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamTld.readDerivedByIdIdx() ";
		ICFIntTld buff;
		if( dictByPKey.containsKey( Id ) ) {
			buff = dictByPKey.get( Id );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTld readRec( ICFSecAuthorization Authorization,
		$implCommaIJavaOptAtomType$ PKey )
	{
		final String S_ProcName = "CFIntRamTld.readRec";
		ICFIntTld buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntTld.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTld lockRec( ICFSecAuthorization Authorization,
		$implCommaIJavaOptAtomType$ PKey )
	{
		final String S_ProcName = "lockRec";
		ICFIntTld buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntTld.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTld[] readAllRec( ICFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamTld.readAllRec";
		ICFIntTld buff;
		ArrayList<ICFIntTld> filteredList = new ArrayList<ICFIntTld>();
		ICFIntTld[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntTld.CLASS_CODE ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new ICFIntTld[0] ) );
	}

	@Override
	public ICFIntTld readRecByIdIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamTld.readRecByIdIdx() ";
		ICFIntTld buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntTld.CLASS_CODE ) ) {
			return( (ICFIntTld)buff );
		}
		else {
			return( null );
		}
	}

	@Override
	public ICFIntTld[] readRecByTenantIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamTld.readRecByTenantIdx() ";
		ICFIntTld buff;
		ArrayList<ICFIntTld> filteredList = new ArrayList<ICFIntTld>();
		ICFIntTld[] buffList = readDerivedByTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntTld.CLASS_CODE ) ) {
				filteredList.add( (ICFIntTld)buff );
			}
		}
		return( filteredList.toArray( new ICFIntTld[0] ) );
	}

	@Override
	public ICFIntTld readRecByNameIdx( ICFSecAuthorization Authorization,
		String Name )
	{
		final String S_ProcName = "CFIntRamTld.readRecByNameIdx() ";
		ICFIntTld buff = readDerivedByNameIdx( Authorization,
			Name );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntTld.CLASS_CODE ) ) {
			return( (ICFIntTld)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntTld updateTld( ICFSecAuthorization Authorization,
		ICFIntTld iBuff )
	{
		CFIntBuffTld Buff = (CFIntBuffTld)ensureRec(iBuff);
		$implCommaIJavaOptAtomType$ pkey = ($implCommaIJavaOptAtomType$)Buff.getPKey();
		CFIntBuffTld existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateTld",
				"Existing record not found",
				"Existing record not found",
				"Tld",
				"Tld",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateTld",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntBuffTldByTenantIdxKey existingKeyTenantIdx = (CFIntBuffTldByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByTenantIdxKey();
		existingKeyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffTldByTenantIdxKey newKeyTenantIdx = (CFIntBuffTldByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByTenantIdxKey();
		newKeyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffTldByNameIdxKey existingKeyNameIdx = (CFIntBuffTldByNameIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByNameIdxKey();
		existingKeyNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffTldByNameIdxKey newKeyNameIdx = (CFIntBuffTldByNameIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByNameIdxKey();
		newKeyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyNameIdx.equals( newKeyNameIdx ) ) {
			if( dictByNameIdx.containsKey( newKeyNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateTld",
					"TldNameIdx",
					"TldNameIdx",
					newKeyNameIdx );
			}
		}

		// Validate foreign keys

		{
			boolean allNull = true;

			if( allNull ) {
				if( null == schema.getTableTenant().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTenantId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateTld",
						"Container",
						"Container",
						"TldTenant",
						"TldTenant",
						"Tenant",
						"Tenant",
						null );
				}
			}
		}

		// Update is valid

		Map< $implCommaIJavaOptAtomType$, CFIntBuffTld > subdict;

		dictByPKey.remove( pkey );
		dictByPKey.put( pkey, Buff );

		subdict = dictByTenantIdx.get( existingKeyTenantIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByTenantIdx.containsKey( newKeyTenantIdx ) ) {
			subdict = dictByTenantIdx.get( newKeyTenantIdx );
		}
		else {
			subdict = new HashMap< $implCommaIJavaOptAtomType$, CFIntBuffTld >();
			dictByTenantIdx.put( newKeyTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByNameIdx.remove( existingKeyNameIdx );
		dictByNameIdx.put( newKeyNameIdx, Buff );

		return(Buff);
	}

	@Override
	public void deleteTld( ICFSecAuthorization Authorization,
		ICFIntTld iBuff )
	{
		final String S_ProcName = "CFIntRamTldTable.deleteTld() ";
		CFIntBuffTld Buff = (CFIntBuffTld)ensureRec(iBuff);
		int classCode;
		$implCommaIJavaOptAtomType$ pkey = ($implCommaIJavaOptAtomType$)(Buff.getPKey());
		CFIntBuffTld existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteTld",
				pkey );
		}
					schema.getTableTopDomain().deleteTopDomainByTldIdx( Authorization,
						existing.getRequiredId() );
		CFIntBuffTldByTenantIdxKey keyTenantIdx = (CFIntBuffTldByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffTldByNameIdxKey keyNameIdx = (CFIntBuffTldByNameIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByNameIdxKey();
		keyNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< $implCommaIJavaOptAtomType$, CFIntBuffTld > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByTenantIdx.get( keyTenantIdx );
		subdict.remove( pkey );

		dictByNameIdx.remove( keyNameIdx );

	}
	@Override
	public void deleteTldByIdIdx( ICFSecAuthorization Authorization,
		$implCommaIJavaOptAtomType$ argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntBuffTld cur;
		LinkedList<CFIntBuffTld> matchSet = new LinkedList<CFIntBuffTld>();
		Iterator<CFIntBuffTld> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffTld> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffTld)(schema.getTableTld().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteTld( Authorization, cur );
		}
	}

	@Override
	public void deleteTldByTenantIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 argTenantId )
	{
		CFIntBuffTldByTenantIdxKey key = (CFIntBuffTldByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteTldByTenantIdx( Authorization, key );
	}

	@Override
	public void deleteTldByTenantIdx( ICFSecAuthorization Authorization,
		ICFIntTldByTenantIdxKey argKey )
	{
		CFIntBuffTld cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffTld> matchSet = new LinkedList<CFIntBuffTld>();
		Iterator<CFIntBuffTld> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffTld> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffTld)(schema.getTableTld().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteTld( Authorization, cur );
		}
	}

	@Override
	public void deleteTldByNameIdx( ICFSecAuthorization Authorization,
		String argName )
	{
		CFIntBuffTldByNameIdxKey key = (CFIntBuffTldByNameIdxKey)schema.getCFIntBuffFactory().getFactoryTld().newByNameIdxKey();
		key.setRequiredName( argName );
		deleteTldByNameIdx( Authorization, key );
	}

	@Override
	public void deleteTldByNameIdx( ICFSecAuthorization Authorization,
		ICFIntTldByNameIdxKey argKey )
	{
		CFIntBuffTld cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffTld> matchSet = new LinkedList<CFIntBuffTld>();
		Iterator<CFIntBuffTld> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffTld> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffTld)(schema.getTableTld().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteTld( Authorization, cur );
		}
	}
}
