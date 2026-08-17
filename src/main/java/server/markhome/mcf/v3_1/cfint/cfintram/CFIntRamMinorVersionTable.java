
// Description: Java 25 in-memory RAM DbIO implementation for MinorVersion.

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
 *	CFIntRamMinorVersionTable in-memory RAM DbIO implementation
 *	for MinorVersion.
 */
public class CFIntRamMinorVersionTable
	implements ICFIntMinorVersionTable
{
	private ICFIntSchema schema;
	private Map< ICFLibKeyHash256,
				CFIntBuffMinorVersion > dictByPKey
		= new HashMap< ICFLibKeyHash256,
				CFIntBuffMinorVersion >();
	private Map< CFIntBuffMinorVersionByTenantIdxKey,
				Map< ICFLibKeyHash256,
					CFIntBuffMinorVersion >> dictByTenantIdx
		= new HashMap< CFIntBuffMinorVersionByTenantIdxKey,
				Map< ICFLibKeyHash256,
					CFIntBuffMinorVersion >>();
	private Map< CFIntBuffMinorVersionByMajorVerIdxKey,
				Map< ICFLibKeyHash256,
					CFIntBuffMinorVersion >> dictByMajorVerIdx
		= new HashMap< CFIntBuffMinorVersionByMajorVerIdxKey,
				Map< ICFLibKeyHash256,
					CFIntBuffMinorVersion >>();
	private Map< CFIntBuffMinorVersionByNameIdxKey,
			CFIntBuffMinorVersion > dictByNameIdx
		= new HashMap< CFIntBuffMinorVersionByNameIdxKey,
			CFIntBuffMinorVersion >();

	public CFIntRamMinorVersionTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public CFIntBuffMinorVersion ensureRec(ICFIntMinorVersion rec) {
		return (((CFIntBuffMinorVersionFactoryService)(schema.getCFIntBuffFactory().getFactoryMinorVersion())).ensureRec(rec));
	}

	@Override
	public ICFIntMinorVersion createMinorVersion( ICFSecAuthorization Authorization,
		ICFIntMinorVersion iBuff )
	{
		final String S_ProcName = "createMinorVersion";
		
		CFIntBuffMinorVersion Buff = (CFIntBuffMinorVersion)ensureRec(iBuff);
		ICFLibKeyHash256 pkey;
		pkey = schema.nextMinorVersionIdGen();
		Buff.setRequiredId( pkey );
		CFIntBuffMinorVersionByTenantIdxKey keyTenantIdx = (CFIntBuffMinorVersionByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffMinorVersionByMajorVerIdxKey keyMajorVerIdx = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByMajorVerIdxKey();
		keyMajorVerIdx.setRequiredMajorVersionId( Buff.getRequiredMajorVersionId() );

		CFIntBuffMinorVersionByNameIdxKey keyNameIdx = (CFIntBuffMinorVersionByNameIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByNameIdxKey();
		keyNameIdx.setRequiredMajorVersionId( Buff.getRequiredMajorVersionId() );
		keyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByNameIdx.containsKey( keyNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"MinorVersionNameIdx",
				"MinorVersionNameIdx",
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
						"Owner",
						"Owner",
						"Tenant",
						"Tenant",
						"Tenant",
						"Tenant",
						null );
				}
			}
		}

		{
			boolean allNull = true;
			allNull = false;
			if( ! allNull ) {
				if( null == schema.getTableMajorVersion().readDerivedByIdIdx( Authorization,
						Buff.getRequiredMajorVersionId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Container",
						"Container",
						"ParentMajorVersion",
						"ParentMajorVersion",
						"MajorVersion",
						"MajorVersion",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< ICFLibKeyHash256, CFIntBuffMinorVersion > subdictTenantIdx;
		if( dictByTenantIdx.containsKey( keyTenantIdx ) ) {
			subdictTenantIdx = dictByTenantIdx.get( keyTenantIdx );
		}
		else {
			subdictTenantIdx = new HashMap< ICFLibKeyHash256, CFIntBuffMinorVersion >();
			dictByTenantIdx.put( keyTenantIdx, subdictTenantIdx );
		}
		subdictTenantIdx.put( pkey, Buff );

		Map< ICFLibKeyHash256, CFIntBuffMinorVersion > subdictMajorVerIdx;
		if( dictByMajorVerIdx.containsKey( keyMajorVerIdx ) ) {
			subdictMajorVerIdx = dictByMajorVerIdx.get( keyMajorVerIdx );
		}
		else {
			subdictMajorVerIdx = new HashMap< ICFLibKeyHash256, CFIntBuffMinorVersion >();
			dictByMajorVerIdx.put( keyMajorVerIdx, subdictMajorVerIdx );
		}
		subdictMajorVerIdx.put( pkey, Buff );

		dictByNameIdx.put( keyNameIdx, Buff );

		if (Buff == null) {
			return( null );
		}
		else {
			int classCode = Buff.getClassCode();
			if (classCode == ICFIntMinorVersion.CLASS_CODE) {
				CFIntBuffMinorVersion retbuff = ((CFIntBuffMinorVersion)(schema.getCFIntBuffFactory().getFactoryMinorVersion().newRec()));
				retbuff.set(Buff);
				return( retbuff );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), S_ProcName, "-create-buff-cloning-", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntMinorVersion readDerived( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readDerived";
		ICFIntMinorVersion buff;
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
	public ICFIntMinorVersion lockDerived( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamMinorVersion.lockDerived";
		ICFIntMinorVersion buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntMinorVersion[] readAllDerived( ICFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamMinorVersion.readAllDerived";
		ICFIntMinorVersion[] retList = new ICFIntMinorVersion[ dictByPKey.values().size() ];
		Iterator< CFIntBuffMinorVersion > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	@Override
	public ICFIntMinorVersion[] readDerivedByTenantIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readDerivedByTenantIdx";
		CFIntBuffMinorVersionByTenantIdxKey key = (CFIntBuffMinorVersionByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByTenantIdxKey();

		key.setRequiredTenantId( TenantId );
		ICFIntMinorVersion[] recArray;
		if( dictByTenantIdx.containsKey( key ) ) {
			Map< ICFLibKeyHash256, CFIntBuffMinorVersion > subdictTenantIdx
				= dictByTenantIdx.get( key );
			recArray = new ICFIntMinorVersion[ subdictTenantIdx.size() ];
			Iterator< CFIntBuffMinorVersion > iter = subdictTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< ICFLibKeyHash256, CFIntBuffMinorVersion > subdictTenantIdx
				= new HashMap< ICFLibKeyHash256, CFIntBuffMinorVersion >();
			dictByTenantIdx.put( key, subdictTenantIdx );
			recArray = new ICFIntMinorVersion[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntMinorVersion[] readDerivedByMajorVerIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 MajorVersionId )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readDerivedByMajorVerIdx";
		CFIntBuffMinorVersionByMajorVerIdxKey key = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByMajorVerIdxKey();

		key.setRequiredMajorVersionId( MajorVersionId );
		ICFIntMinorVersion[] recArray;
		if( dictByMajorVerIdx.containsKey( key ) ) {
			Map< ICFLibKeyHash256, CFIntBuffMinorVersion > subdictMajorVerIdx
				= dictByMajorVerIdx.get( key );
			recArray = new ICFIntMinorVersion[ subdictMajorVerIdx.size() ];
			Iterator< CFIntBuffMinorVersion > iter = subdictMajorVerIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< ICFLibKeyHash256, CFIntBuffMinorVersion > subdictMajorVerIdx
				= new HashMap< ICFLibKeyHash256, CFIntBuffMinorVersion >();
			dictByMajorVerIdx.put( key, subdictMajorVerIdx );
			recArray = new ICFIntMinorVersion[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntMinorVersion readDerivedByNameIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 MajorVersionId,
		String Name )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readDerivedByNameIdx";
		CFIntBuffMinorVersionByNameIdxKey key = (CFIntBuffMinorVersionByNameIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByNameIdxKey();

		key.setRequiredMajorVersionId( MajorVersionId );
		key.setRequiredName( Name );
		ICFIntMinorVersion buff;
		if( dictByNameIdx.containsKey( key ) ) {
			buff = dictByNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntMinorVersion readDerivedByIdIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readDerivedByIdIdx() ";
		ICFIntMinorVersion buff;
		if( dictByPKey.containsKey( Id ) ) {
			buff = dictByPKey.get( Id );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntMinorVersion readRec( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readRec";
		ICFIntMinorVersion buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntMinorVersion.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntMinorVersion lockRec( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 PKey )
	{
		final String S_ProcName = "lockRec";
		ICFIntMinorVersion buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntMinorVersion.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntMinorVersion[] readAllRec( ICFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readAllRec";
		ICFIntMinorVersion buff;
		ArrayList<ICFIntMinorVersion> filteredList = new ArrayList<ICFIntMinorVersion>();
		ICFIntMinorVersion[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntMinorVersion.CLASS_CODE ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new ICFIntMinorVersion[0] ) );
	}

	@Override
	public ICFIntMinorVersion readRecByIdIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readRecByIdIdx() ";
		ICFIntMinorVersion buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntMinorVersion.CLASS_CODE ) ) {
			return( (ICFIntMinorVersion)buff );
		}
		else {
			return( null );
		}
	}

	@Override
	public ICFIntMinorVersion[] readRecByTenantIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readRecByTenantIdx() ";
		ICFIntMinorVersion buff;
		ArrayList<ICFIntMinorVersion> filteredList = new ArrayList<ICFIntMinorVersion>();
		ICFIntMinorVersion[] buffList = readDerivedByTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntMinorVersion.CLASS_CODE ) ) {
				filteredList.add( (ICFIntMinorVersion)buff );
			}
		}
		return( filteredList.toArray( new ICFIntMinorVersion[0] ) );
	}

	@Override
	public ICFIntMinorVersion[] readRecByMajorVerIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 MajorVersionId )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readRecByMajorVerIdx() ";
		ICFIntMinorVersion buff;
		ArrayList<ICFIntMinorVersion> filteredList = new ArrayList<ICFIntMinorVersion>();
		ICFIntMinorVersion[] buffList = readDerivedByMajorVerIdx( Authorization,
			MajorVersionId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntMinorVersion.CLASS_CODE ) ) {
				filteredList.add( (ICFIntMinorVersion)buff );
			}
		}
		return( filteredList.toArray( new ICFIntMinorVersion[0] ) );
	}

	@Override
	public ICFIntMinorVersion readRecByNameIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 MajorVersionId,
		String Name )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readRecByNameIdx() ";
		ICFIntMinorVersion buff = readDerivedByNameIdx( Authorization,
			MajorVersionId,
			Name );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntMinorVersion.CLASS_CODE ) ) {
			return( (ICFIntMinorVersion)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntMinorVersion updateMinorVersion( ICFSecAuthorization Authorization,
		ICFIntMinorVersion iBuff )
	{
		CFIntBuffMinorVersion Buff = (CFIntBuffMinorVersion)ensureRec(iBuff);
		ICFLibKeyHash256 pkey = (ICFLibKeyHash256)Buff.getPKey();
		CFIntBuffMinorVersion existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateMinorVersion",
				"Existing record not found",
				"Existing record not found",
				"MinorVersion",
				"MinorVersion",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateMinorVersion",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntBuffMinorVersionByTenantIdxKey existingKeyTenantIdx = (CFIntBuffMinorVersionByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByTenantIdxKey();
		existingKeyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffMinorVersionByTenantIdxKey newKeyTenantIdx = (CFIntBuffMinorVersionByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByTenantIdxKey();
		newKeyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffMinorVersionByMajorVerIdxKey existingKeyMajorVerIdx = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByMajorVerIdxKey();
		existingKeyMajorVerIdx.setRequiredMajorVersionId( existing.getRequiredMajorVersionId() );

		CFIntBuffMinorVersionByMajorVerIdxKey newKeyMajorVerIdx = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByMajorVerIdxKey();
		newKeyMajorVerIdx.setRequiredMajorVersionId( Buff.getRequiredMajorVersionId() );

		CFIntBuffMinorVersionByNameIdxKey existingKeyNameIdx = (CFIntBuffMinorVersionByNameIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByNameIdxKey();
		existingKeyNameIdx.setRequiredMajorVersionId( existing.getRequiredMajorVersionId() );
		existingKeyNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffMinorVersionByNameIdxKey newKeyNameIdx = (CFIntBuffMinorVersionByNameIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByNameIdxKey();
		newKeyNameIdx.setRequiredMajorVersionId( Buff.getRequiredMajorVersionId() );
		newKeyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyNameIdx.equals( newKeyNameIdx ) ) {
			if( dictByNameIdx.containsKey( newKeyNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateMinorVersion",
					"MinorVersionNameIdx",
					"MinorVersionNameIdx",
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
						"updateMinorVersion",
						"Owner",
						"Owner",
						"Tenant",
						"Tenant",
						"Tenant",
						"Tenant",
						null );
				}
			}
		}

		{
			boolean allNull = true;

			if( allNull ) {
				if( null == schema.getTableMajorVersion().readDerivedByIdIdx( Authorization,
						Buff.getRequiredMajorVersionId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateMinorVersion",
						"Container",
						"Container",
						"ParentMajorVersion",
						"ParentMajorVersion",
						"MajorVersion",
						"MajorVersion",
						null );
				}
			}
		}

		// Update is valid

		Map< ICFLibKeyHash256, CFIntBuffMinorVersion > subdict;

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
			subdict = new HashMap< ICFLibKeyHash256, CFIntBuffMinorVersion >();
			dictByTenantIdx.put( newKeyTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		subdict = dictByMajorVerIdx.get( existingKeyMajorVerIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByMajorVerIdx.containsKey( newKeyMajorVerIdx ) ) {
			subdict = dictByMajorVerIdx.get( newKeyMajorVerIdx );
		}
		else {
			subdict = new HashMap< ICFLibKeyHash256, CFIntBuffMinorVersion >();
			dictByMajorVerIdx.put( newKeyMajorVerIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByNameIdx.remove( existingKeyNameIdx );
		dictByNameIdx.put( newKeyNameIdx, Buff );

		return(Buff);
	}

	@Override
	public void deleteMinorVersion( ICFSecAuthorization Authorization,
		ICFIntMinorVersion iBuff )
	{
		final String S_ProcName = "CFIntRamMinorVersionTable.deleteMinorVersion() ";
		CFIntBuffMinorVersion Buff = (CFIntBuffMinorVersion)ensureRec(iBuff);
		int classCode;
		ICFLibKeyHash256 pkey = (ICFLibKeyHash256)(Buff.getPKey());
		CFIntBuffMinorVersion existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteMinorVersion",
				pkey );
		}
		CFIntBuffMinorVersionByTenantIdxKey keyTenantIdx = (CFIntBuffMinorVersionByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffMinorVersionByMajorVerIdxKey keyMajorVerIdx = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByMajorVerIdxKey();
		keyMajorVerIdx.setRequiredMajorVersionId( existing.getRequiredMajorVersionId() );

		CFIntBuffMinorVersionByNameIdxKey keyNameIdx = (CFIntBuffMinorVersionByNameIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByNameIdxKey();
		keyNameIdx.setRequiredMajorVersionId( existing.getRequiredMajorVersionId() );
		keyNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< ICFLibKeyHash256, CFIntBuffMinorVersion > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByTenantIdx.get( keyTenantIdx );
		subdict.remove( pkey );

		subdict = dictByMajorVerIdx.get( keyMajorVerIdx );
		subdict.remove( pkey );

		dictByNameIdx.remove( keyNameIdx );

	}
	@Override
	public void deleteMinorVersionByIdIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntBuffMinorVersion cur;
		LinkedList<CFIntBuffMinorVersion> matchSet = new LinkedList<CFIntBuffMinorVersion>();
		Iterator<CFIntBuffMinorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffMinorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffMinorVersion)(schema.getTableMinorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteMinorVersion( Authorization, cur );
		}
	}

	@Override
	public void deleteMinorVersionByTenantIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 argTenantId )
	{
		CFIntBuffMinorVersionByTenantIdxKey key = (CFIntBuffMinorVersionByTenantIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteMinorVersionByTenantIdx( Authorization, key );
	}

	@Override
	public void deleteMinorVersionByTenantIdx( ICFSecAuthorization Authorization,
		ICFIntMinorVersionByTenantIdxKey argKey )
	{
		CFIntBuffMinorVersion cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffMinorVersion> matchSet = new LinkedList<CFIntBuffMinorVersion>();
		Iterator<CFIntBuffMinorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffMinorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffMinorVersion)(schema.getTableMinorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteMinorVersion( Authorization, cur );
		}
	}

	@Override
	public void deleteMinorVersionByMajorVerIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 argMajorVersionId )
	{
		CFIntBuffMinorVersionByMajorVerIdxKey key = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByMajorVerIdxKey();
		key.setRequiredMajorVersionId( argMajorVersionId );
		deleteMinorVersionByMajorVerIdx( Authorization, key );
	}

	@Override
	public void deleteMinorVersionByMajorVerIdx( ICFSecAuthorization Authorization,
		ICFIntMinorVersionByMajorVerIdxKey argKey )
	{
		CFIntBuffMinorVersion cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffMinorVersion> matchSet = new LinkedList<CFIntBuffMinorVersion>();
		Iterator<CFIntBuffMinorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffMinorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffMinorVersion)(schema.getTableMinorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteMinorVersion( Authorization, cur );
		}
	}

	@Override
	public void deleteMinorVersionByNameIdx( ICFSecAuthorization Authorization,
		ICFLibKeyHash256 argMajorVersionId,
		String argName )
	{
		CFIntBuffMinorVersionByNameIdxKey key = (CFIntBuffMinorVersionByNameIdxKey)schema.getCFIntBuffFactory().getFactoryMinorVersion().newByNameIdxKey();
		key.setRequiredMajorVersionId( argMajorVersionId );
		key.setRequiredName( argName );
		deleteMinorVersionByNameIdx( Authorization, key );
	}

	@Override
	public void deleteMinorVersionByNameIdx( ICFSecAuthorization Authorization,
		ICFIntMinorVersionByNameIdxKey argKey )
	{
		CFIntBuffMinorVersion cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffMinorVersion> matchSet = new LinkedList<CFIntBuffMinorVersion>();
		Iterator<CFIntBuffMinorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffMinorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffMinorVersion)(schema.getTableMinorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteMinorVersion( Authorization, cur );
		}
	}
}
