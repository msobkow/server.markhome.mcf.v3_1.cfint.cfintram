
// Description: Java 25 in-memory RAM DbIO implementation for MajorVersion.

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
 *	CFIntRamMajorVersionTable in-memory RAM DbIO implementation
 *	for MajorVersion.
 */
public class CFIntRamMajorVersionTable
	implements ICFIntMajorVersionTable
{
	private ICFIntSchema schema;
	private Map< CFLibDbKeyHash256,
				CFIntBuffMajorVersion > dictByPKey
		= new HashMap< CFLibDbKeyHash256,
				CFIntBuffMajorVersion >();
	private Map< CFIntBuffMajorVersionByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffMajorVersion >> dictByTenantIdx
		= new HashMap< CFIntBuffMajorVersionByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffMajorVersion >>();
	private Map< CFIntBuffMajorVersionBySubProjectIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffMajorVersion >> dictBySubProjectIdx
		= new HashMap< CFIntBuffMajorVersionBySubProjectIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffMajorVersion >>();
	private Map< CFIntBuffMajorVersionByNameIdxKey,
			CFIntBuffMajorVersion > dictByNameIdx
		= new HashMap< CFIntBuffMajorVersionByNameIdxKey,
			CFIntBuffMajorVersion >();

	public CFIntRamMajorVersionTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public CFIntBuffMajorVersion ensureRec(ICFIntMajorVersion rec) {
		if (rec == null) {
			return( null );
		}
		else {
			int classCode = rec.getClassCode();
			if (classCode == ICFIntMajorVersion.CLASS_CODE) {
				return( ((CFIntBuffMajorVersionDefaultFactory)(schema.getFactoryMajorVersion())).ensureRec((ICFIntMajorVersion)rec) );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), "ensureRec", "rec", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntMajorVersion createMajorVersion( ICFSecAuthorization Authorization,
		ICFIntMajorVersion iBuff )
	{
		final String S_ProcName = "createMajorVersion";
		
		CFIntBuffMajorVersion Buff = (CFIntBuffMajorVersion)ensureRec(iBuff);
		CFLibDbKeyHash256 pkey;
		pkey = schema.nextMajorVersionIdGen();
		Buff.setRequiredId( pkey );
		CFIntBuffMajorVersionByTenantIdxKey keyTenantIdx = (CFIntBuffMajorVersionByTenantIdxKey)schema.getFactoryMajorVersion().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffMajorVersionBySubProjectIdxKey keySubProjectIdx = (CFIntBuffMajorVersionBySubProjectIdxKey)schema.getFactoryMajorVersion().newBySubProjectIdxKey();
		keySubProjectIdx.setRequiredSubProjectId( Buff.getRequiredSubProjectId() );

		CFIntBuffMajorVersionByNameIdxKey keyNameIdx = (CFIntBuffMajorVersionByNameIdxKey)schema.getFactoryMajorVersion().newByNameIdxKey();
		keyNameIdx.setRequiredSubProjectId( Buff.getRequiredSubProjectId() );
		keyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByNameIdx.containsKey( keyNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"MajorVersionNameIdx",
				"MajorVersionNameIdx",
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
				if( null == schema.getTableSubProject().readDerivedByIdIdx( Authorization,
						Buff.getRequiredSubProjectId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Container",
						"Container",
						"ParentSubProject",
						"ParentSubProject",
						"SubProject",
						"SubProject",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffMajorVersion > subdictTenantIdx;
		if( dictByTenantIdx.containsKey( keyTenantIdx ) ) {
			subdictTenantIdx = dictByTenantIdx.get( keyTenantIdx );
		}
		else {
			subdictTenantIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffMajorVersion >();
			dictByTenantIdx.put( keyTenantIdx, subdictTenantIdx );
		}
		subdictTenantIdx.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffMajorVersion > subdictSubProjectIdx;
		if( dictBySubProjectIdx.containsKey( keySubProjectIdx ) ) {
			subdictSubProjectIdx = dictBySubProjectIdx.get( keySubProjectIdx );
		}
		else {
			subdictSubProjectIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffMajorVersion >();
			dictBySubProjectIdx.put( keySubProjectIdx, subdictSubProjectIdx );
		}
		subdictSubProjectIdx.put( pkey, Buff );

		dictByNameIdx.put( keyNameIdx, Buff );

		if (Buff == null) {
			return( null );
		}
		else {
			int classCode = Buff.getClassCode();
			if (classCode == ICFIntMajorVersion.CLASS_CODE) {
				CFIntBuffMajorVersion retbuff = ((CFIntBuffMajorVersion)(schema.getFactoryMajorVersion().newRec()));
				retbuff.set(Buff);
				return( retbuff );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), S_ProcName, "-create-buff-cloning-", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntMajorVersion readDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamMajorVersion.readDerived";
		ICFIntMajorVersion buff;
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
	public ICFIntMajorVersion lockDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamMajorVersion.lockDerived";
		ICFIntMajorVersion buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntMajorVersion[] readAllDerived( ICFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamMajorVersion.readAllDerived";
		ICFIntMajorVersion[] retList = new ICFIntMajorVersion[ dictByPKey.values().size() ];
		Iterator< CFIntBuffMajorVersion > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	@Override
	public ICFIntMajorVersion[] readDerivedByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamMajorVersion.readDerivedByTenantIdx";
		CFIntBuffMajorVersionByTenantIdxKey key = (CFIntBuffMajorVersionByTenantIdxKey)schema.getFactoryMajorVersion().newByTenantIdxKey();

		key.setRequiredTenantId( TenantId );
		ICFIntMajorVersion[] recArray;
		if( dictByTenantIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffMajorVersion > subdictTenantIdx
				= dictByTenantIdx.get( key );
			recArray = new ICFIntMajorVersion[ subdictTenantIdx.size() ];
			Iterator< CFIntBuffMajorVersion > iter = subdictTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffMajorVersion > subdictTenantIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffMajorVersion >();
			dictByTenantIdx.put( key, subdictTenantIdx );
			recArray = new ICFIntMajorVersion[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntMajorVersion[] readDerivedBySubProjectIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 SubProjectId )
	{
		final String S_ProcName = "CFIntRamMajorVersion.readDerivedBySubProjectIdx";
		CFIntBuffMajorVersionBySubProjectIdxKey key = (CFIntBuffMajorVersionBySubProjectIdxKey)schema.getFactoryMajorVersion().newBySubProjectIdxKey();

		key.setRequiredSubProjectId( SubProjectId );
		ICFIntMajorVersion[] recArray;
		if( dictBySubProjectIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffMajorVersion > subdictSubProjectIdx
				= dictBySubProjectIdx.get( key );
			recArray = new ICFIntMajorVersion[ subdictSubProjectIdx.size() ];
			Iterator< CFIntBuffMajorVersion > iter = subdictSubProjectIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffMajorVersion > subdictSubProjectIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffMajorVersion >();
			dictBySubProjectIdx.put( key, subdictSubProjectIdx );
			recArray = new ICFIntMajorVersion[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntMajorVersion readDerivedByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 SubProjectId,
		String Name )
	{
		final String S_ProcName = "CFIntRamMajorVersion.readDerivedByNameIdx";
		CFIntBuffMajorVersionByNameIdxKey key = (CFIntBuffMajorVersionByNameIdxKey)schema.getFactoryMajorVersion().newByNameIdxKey();

		key.setRequiredSubProjectId( SubProjectId );
		key.setRequiredName( Name );
		ICFIntMajorVersion buff;
		if( dictByNameIdx.containsKey( key ) ) {
			buff = dictByNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntMajorVersion readDerivedByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamMajorVersion.readDerivedByIdIdx() ";
		ICFIntMajorVersion buff;
		if( dictByPKey.containsKey( Id ) ) {
			buff = dictByPKey.get( Id );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntMajorVersion readRec( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamMajorVersion.readRec";
		ICFIntMajorVersion buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntMajorVersion.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntMajorVersion lockRec( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "lockRec";
		ICFIntMajorVersion buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntMajorVersion.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntMajorVersion[] readAllRec( ICFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamMajorVersion.readAllRec";
		ICFIntMajorVersion buff;
		ArrayList<ICFIntMajorVersion> filteredList = new ArrayList<ICFIntMajorVersion>();
		ICFIntMajorVersion[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntMajorVersion.CLASS_CODE ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new ICFIntMajorVersion[0] ) );
	}

	@Override
	public ICFIntMajorVersion readRecByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamMajorVersion.readRecByIdIdx() ";
		ICFIntMajorVersion buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntMajorVersion.CLASS_CODE ) ) {
			return( (ICFIntMajorVersion)buff );
		}
		else {
			return( null );
		}
	}

	@Override
	public ICFIntMajorVersion[] readRecByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamMajorVersion.readRecByTenantIdx() ";
		ICFIntMajorVersion buff;
		ArrayList<ICFIntMajorVersion> filteredList = new ArrayList<ICFIntMajorVersion>();
		ICFIntMajorVersion[] buffList = readDerivedByTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntMajorVersion.CLASS_CODE ) ) {
				filteredList.add( (ICFIntMajorVersion)buff );
			}
		}
		return( filteredList.toArray( new ICFIntMajorVersion[0] ) );
	}

	@Override
	public ICFIntMajorVersion[] readRecBySubProjectIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 SubProjectId )
	{
		final String S_ProcName = "CFIntRamMajorVersion.readRecBySubProjectIdx() ";
		ICFIntMajorVersion buff;
		ArrayList<ICFIntMajorVersion> filteredList = new ArrayList<ICFIntMajorVersion>();
		ICFIntMajorVersion[] buffList = readDerivedBySubProjectIdx( Authorization,
			SubProjectId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntMajorVersion.CLASS_CODE ) ) {
				filteredList.add( (ICFIntMajorVersion)buff );
			}
		}
		return( filteredList.toArray( new ICFIntMajorVersion[0] ) );
	}

	@Override
	public ICFIntMajorVersion readRecByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 SubProjectId,
		String Name )
	{
		final String S_ProcName = "CFIntRamMajorVersion.readRecByNameIdx() ";
		ICFIntMajorVersion buff = readDerivedByNameIdx( Authorization,
			SubProjectId,
			Name );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntMajorVersion.CLASS_CODE ) ) {
			return( (ICFIntMajorVersion)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntMajorVersion updateMajorVersion( ICFSecAuthorization Authorization,
		ICFIntMajorVersion iBuff )
	{
		CFIntBuffMajorVersion Buff = (CFIntBuffMajorVersion)ensureRec(iBuff);
		CFLibDbKeyHash256 pkey = Buff.getPKey();
		CFIntBuffMajorVersion existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateMajorVersion",
				"Existing record not found",
				"Existing record not found",
				"MajorVersion",
				"MajorVersion",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateMajorVersion",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntBuffMajorVersionByTenantIdxKey existingKeyTenantIdx = (CFIntBuffMajorVersionByTenantIdxKey)schema.getFactoryMajorVersion().newByTenantIdxKey();
		existingKeyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffMajorVersionByTenantIdxKey newKeyTenantIdx = (CFIntBuffMajorVersionByTenantIdxKey)schema.getFactoryMajorVersion().newByTenantIdxKey();
		newKeyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffMajorVersionBySubProjectIdxKey existingKeySubProjectIdx = (CFIntBuffMajorVersionBySubProjectIdxKey)schema.getFactoryMajorVersion().newBySubProjectIdxKey();
		existingKeySubProjectIdx.setRequiredSubProjectId( existing.getRequiredSubProjectId() );

		CFIntBuffMajorVersionBySubProjectIdxKey newKeySubProjectIdx = (CFIntBuffMajorVersionBySubProjectIdxKey)schema.getFactoryMajorVersion().newBySubProjectIdxKey();
		newKeySubProjectIdx.setRequiredSubProjectId( Buff.getRequiredSubProjectId() );

		CFIntBuffMajorVersionByNameIdxKey existingKeyNameIdx = (CFIntBuffMajorVersionByNameIdxKey)schema.getFactoryMajorVersion().newByNameIdxKey();
		existingKeyNameIdx.setRequiredSubProjectId( existing.getRequiredSubProjectId() );
		existingKeyNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffMajorVersionByNameIdxKey newKeyNameIdx = (CFIntBuffMajorVersionByNameIdxKey)schema.getFactoryMajorVersion().newByNameIdxKey();
		newKeyNameIdx.setRequiredSubProjectId( Buff.getRequiredSubProjectId() );
		newKeyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyNameIdx.equals( newKeyNameIdx ) ) {
			if( dictByNameIdx.containsKey( newKeyNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateMajorVersion",
					"MajorVersionNameIdx",
					"MajorVersionNameIdx",
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
						"updateMajorVersion",
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
				if( null == schema.getTableSubProject().readDerivedByIdIdx( Authorization,
						Buff.getRequiredSubProjectId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateMajorVersion",
						"Container",
						"Container",
						"ParentSubProject",
						"ParentSubProject",
						"SubProject",
						"SubProject",
						null );
				}
			}
		}

		// Update is valid

		Map< CFLibDbKeyHash256, CFIntBuffMajorVersion > subdict;

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
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffMajorVersion >();
			dictByTenantIdx.put( newKeyTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		subdict = dictBySubProjectIdx.get( existingKeySubProjectIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictBySubProjectIdx.containsKey( newKeySubProjectIdx ) ) {
			subdict = dictBySubProjectIdx.get( newKeySubProjectIdx );
		}
		else {
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffMajorVersion >();
			dictBySubProjectIdx.put( newKeySubProjectIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByNameIdx.remove( existingKeyNameIdx );
		dictByNameIdx.put( newKeyNameIdx, Buff );

		return(Buff);
	}

	@Override
	public void deleteMajorVersion( ICFSecAuthorization Authorization,
		ICFIntMajorVersion iBuff )
	{
		final String S_ProcName = "CFIntRamMajorVersionTable.deleteMajorVersion() ";
		CFIntBuffMajorVersion Buff = (CFIntBuffMajorVersion)ensureRec(iBuff);
		int classCode;
		CFLibDbKeyHash256 pkey = (CFLibDbKeyHash256)(Buff.getPKey());
		CFIntBuffMajorVersion existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteMajorVersion",
				pkey );
		}
					schema.getTableMinorVersion().deleteMinorVersionByMajorVerIdx( Authorization,
						existing.getRequiredId() );
		CFIntBuffMajorVersionByTenantIdxKey keyTenantIdx = (CFIntBuffMajorVersionByTenantIdxKey)schema.getFactoryMajorVersion().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffMajorVersionBySubProjectIdxKey keySubProjectIdx = (CFIntBuffMajorVersionBySubProjectIdxKey)schema.getFactoryMajorVersion().newBySubProjectIdxKey();
		keySubProjectIdx.setRequiredSubProjectId( existing.getRequiredSubProjectId() );

		CFIntBuffMajorVersionByNameIdxKey keyNameIdx = (CFIntBuffMajorVersionByNameIdxKey)schema.getFactoryMajorVersion().newByNameIdxKey();
		keyNameIdx.setRequiredSubProjectId( existing.getRequiredSubProjectId() );
		keyNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFLibDbKeyHash256, CFIntBuffMajorVersion > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByTenantIdx.get( keyTenantIdx );
		subdict.remove( pkey );

		subdict = dictBySubProjectIdx.get( keySubProjectIdx );
		subdict.remove( pkey );

		dictByNameIdx.remove( keyNameIdx );

	}
	@Override
	public void deleteMajorVersionByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntBuffMajorVersion cur;
		LinkedList<CFIntBuffMajorVersion> matchSet = new LinkedList<CFIntBuffMajorVersion>();
		Iterator<CFIntBuffMajorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffMajorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffMajorVersion)(schema.getTableMajorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteMajorVersion( Authorization, cur );
		}
	}

	@Override
	public void deleteMajorVersionByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntBuffMajorVersionByTenantIdxKey key = (CFIntBuffMajorVersionByTenantIdxKey)schema.getFactoryMajorVersion().newByTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteMajorVersionByTenantIdx( Authorization, key );
	}

	@Override
	public void deleteMajorVersionByTenantIdx( ICFSecAuthorization Authorization,
		ICFIntMajorVersionByTenantIdxKey argKey )
	{
		CFIntBuffMajorVersion cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffMajorVersion> matchSet = new LinkedList<CFIntBuffMajorVersion>();
		Iterator<CFIntBuffMajorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffMajorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffMajorVersion)(schema.getTableMajorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteMajorVersion( Authorization, cur );
		}
	}

	@Override
	public void deleteMajorVersionBySubProjectIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argSubProjectId )
	{
		CFIntBuffMajorVersionBySubProjectIdxKey key = (CFIntBuffMajorVersionBySubProjectIdxKey)schema.getFactoryMajorVersion().newBySubProjectIdxKey();
		key.setRequiredSubProjectId( argSubProjectId );
		deleteMajorVersionBySubProjectIdx( Authorization, key );
	}

	@Override
	public void deleteMajorVersionBySubProjectIdx( ICFSecAuthorization Authorization,
		ICFIntMajorVersionBySubProjectIdxKey argKey )
	{
		CFIntBuffMajorVersion cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffMajorVersion> matchSet = new LinkedList<CFIntBuffMajorVersion>();
		Iterator<CFIntBuffMajorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffMajorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffMajorVersion)(schema.getTableMajorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteMajorVersion( Authorization, cur );
		}
	}

	@Override
	public void deleteMajorVersionByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argSubProjectId,
		String argName )
	{
		CFIntBuffMajorVersionByNameIdxKey key = (CFIntBuffMajorVersionByNameIdxKey)schema.getFactoryMajorVersion().newByNameIdxKey();
		key.setRequiredSubProjectId( argSubProjectId );
		key.setRequiredName( argName );
		deleteMajorVersionByNameIdx( Authorization, key );
	}

	@Override
	public void deleteMajorVersionByNameIdx( ICFSecAuthorization Authorization,
		ICFIntMajorVersionByNameIdxKey argKey )
	{
		CFIntBuffMajorVersion cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffMajorVersion> matchSet = new LinkedList<CFIntBuffMajorVersion>();
		Iterator<CFIntBuffMajorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffMajorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffMajorVersion)(schema.getTableMajorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteMajorVersion( Authorization, cur );
		}
	}
}
