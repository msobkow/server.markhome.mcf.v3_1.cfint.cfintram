
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
 *	CFIntRamTldTable in-memory RAM DbIO implementation
 *	for Tld.
 */
public class CFIntRamTldTable
	implements ICFIntTldTable
{
	private ICFIntSchema schema;
	private Map< CFLibDbKeyHash256,
				CFIntBuffTld > dictByPKey
		= new HashMap< CFLibDbKeyHash256,
				CFIntBuffTld >();
	private Map< CFIntBuffTldByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffTld >> dictByTenantIdx
		= new HashMap< CFIntBuffTldByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffTld >>();
	private Map< CFIntBuffTldByNameIdxKey,
			CFIntBuffTld > dictByNameIdx
		= new HashMap< CFIntBuffTldByNameIdxKey,
			CFIntBuffTld >();

	public CFIntRamTldTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public CFIntBuffTld ensureRec(ICFIntTld rec) {
		if (rec == null) {
			return( null );
		}
		else {
			int classCode = rec.getClassCode();
			if (classCode == ICFIntTld.CLASS_CODE) {
				return( ((CFIntBuffTldDefaultFactory)(schema.getFactoryTld())).ensureRec((ICFIntTld)rec) );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), "ensureRec", "rec", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntTld createTld( ICFSecAuthorization Authorization,
		ICFIntTld iBuff )
	{
		final String S_ProcName = "createTld";
		
		CFIntBuffTld Buff = (CFIntBuffTld)ensureRec(iBuff);
		CFLibDbKeyHash256 pkey;
		pkey = schema.nextTldIdGen();
		Buff.setRequiredId( pkey );
		CFIntBuffTldByTenantIdxKey keyTenantIdx = (CFIntBuffTldByTenantIdxKey)schema.getFactoryTld().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffTldByNameIdxKey keyNameIdx = (CFIntBuffTldByNameIdxKey)schema.getFactoryTld().newByNameIdxKey();
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

		Map< CFLibDbKeyHash256, CFIntBuffTld > subdictTenantIdx;
		if( dictByTenantIdx.containsKey( keyTenantIdx ) ) {
			subdictTenantIdx = dictByTenantIdx.get( keyTenantIdx );
		}
		else {
			subdictTenantIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffTld >();
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
				CFIntBuffTld retbuff = ((CFIntBuffTld)(schema.getFactoryTld().newRec()));
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
		CFLibDbKeyHash256 PKey )
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
		CFLibDbKeyHash256 PKey )
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
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamTld.readDerivedByTenantIdx";
		CFIntBuffTldByTenantIdxKey key = (CFIntBuffTldByTenantIdxKey)schema.getFactoryTld().newByTenantIdxKey();

		key.setRequiredTenantId( TenantId );
		ICFIntTld[] recArray;
		if( dictByTenantIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffTld > subdictTenantIdx
				= dictByTenantIdx.get( key );
			recArray = new ICFIntTld[ subdictTenantIdx.size() ];
			Iterator< CFIntBuffTld > iter = subdictTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffTld > subdictTenantIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffTld >();
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
		CFIntBuffTldByNameIdxKey key = (CFIntBuffTldByNameIdxKey)schema.getFactoryTld().newByNameIdxKey();

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
		CFLibDbKeyHash256 Id )
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
		CFLibDbKeyHash256 PKey )
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
		CFLibDbKeyHash256 PKey )
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
		CFLibDbKeyHash256 Id )
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
		CFLibDbKeyHash256 TenantId )
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
		CFLibDbKeyHash256 pkey = Buff.getPKey();
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
		CFIntBuffTldByTenantIdxKey existingKeyTenantIdx = (CFIntBuffTldByTenantIdxKey)schema.getFactoryTld().newByTenantIdxKey();
		existingKeyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffTldByTenantIdxKey newKeyTenantIdx = (CFIntBuffTldByTenantIdxKey)schema.getFactoryTld().newByTenantIdxKey();
		newKeyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffTldByNameIdxKey existingKeyNameIdx = (CFIntBuffTldByNameIdxKey)schema.getFactoryTld().newByNameIdxKey();
		existingKeyNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffTldByNameIdxKey newKeyNameIdx = (CFIntBuffTldByNameIdxKey)schema.getFactoryTld().newByNameIdxKey();
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

		Map< CFLibDbKeyHash256, CFIntBuffTld > subdict;

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
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffTld >();
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
		CFLibDbKeyHash256 pkey = (CFLibDbKeyHash256)(Buff.getPKey());
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
		CFIntBuffTldByTenantIdxKey keyTenantIdx = (CFIntBuffTldByTenantIdxKey)schema.getFactoryTld().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffTldByNameIdxKey keyNameIdx = (CFIntBuffTldByNameIdxKey)schema.getFactoryTld().newByNameIdxKey();
		keyNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFLibDbKeyHash256, CFIntBuffTld > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByTenantIdx.get( keyTenantIdx );
		subdict.remove( pkey );

		dictByNameIdx.remove( keyNameIdx );

	}
	@Override
	public void deleteTldByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argKey )
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
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntBuffTldByTenantIdxKey key = (CFIntBuffTldByTenantIdxKey)schema.getFactoryTld().newByTenantIdxKey();
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
		CFIntBuffTldByNameIdxKey key = (CFIntBuffTldByNameIdxKey)schema.getFactoryTld().newByNameIdxKey();
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
