
// Description: Java 25 in-memory RAM DbIO implementation for TopDomain.

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
 *	CFIntRamTopDomainTable in-memory RAM DbIO implementation
 *	for TopDomain.
 */
public class CFIntRamTopDomainTable
	implements ICFIntTopDomainTable
{
	private ICFIntSchema schema;
	private Map< CFLibDbKeyHash256,
				CFIntBuffTopDomain > dictByPKey
		= new HashMap< CFLibDbKeyHash256,
				CFIntBuffTopDomain >();
	private Map< CFIntBuffTopDomainByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffTopDomain >> dictByTenantIdx
		= new HashMap< CFIntBuffTopDomainByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffTopDomain >>();
	private Map< CFIntBuffTopDomainByTldIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffTopDomain >> dictByTldIdx
		= new HashMap< CFIntBuffTopDomainByTldIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffTopDomain >>();
	private Map< CFIntBuffTopDomainByNameIdxKey,
			CFIntBuffTopDomain > dictByNameIdx
		= new HashMap< CFIntBuffTopDomainByNameIdxKey,
			CFIntBuffTopDomain >();

	public CFIntRamTopDomainTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public CFIntBuffTopDomain ensureRec(ICFIntTopDomain rec) {
		if (rec == null) {
			return( null );
		}
		else {
			int classCode = rec.getClassCode();
			if (classCode == ICFIntTopDomain.CLASS_CODE) {
				return( ((CFIntBuffTopDomainDefaultFactory)(schema.getFactoryTopDomain())).ensureRec((ICFIntTopDomain)rec) );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), "ensureRec", "rec", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntTopDomain createTopDomain( ICFSecAuthorization Authorization,
		ICFIntTopDomain iBuff )
	{
		final String S_ProcName = "createTopDomain";
		
		CFIntBuffTopDomain Buff = (CFIntBuffTopDomain)ensureRec(iBuff);
		CFLibDbKeyHash256 pkey;
		pkey = schema.nextTopDomainIdGen();
		Buff.setRequiredId( pkey );
		CFIntBuffTopDomainByTenantIdxKey keyTenantIdx = (CFIntBuffTopDomainByTenantIdxKey)schema.getFactoryTopDomain().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffTopDomainByTldIdxKey keyTldIdx = (CFIntBuffTopDomainByTldIdxKey)schema.getFactoryTopDomain().newByTldIdxKey();
		keyTldIdx.setRequiredTldId( Buff.getRequiredTldId() );

		CFIntBuffTopDomainByNameIdxKey keyNameIdx = (CFIntBuffTopDomainByNameIdxKey)schema.getFactoryTopDomain().newByNameIdxKey();
		keyNameIdx.setRequiredTldId( Buff.getRequiredTldId() );
		keyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByNameIdx.containsKey( keyNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"TopDomNameIdx",
				"TopDomNameIdx",
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
				if( null == schema.getTableTld().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTldId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Container",
						"Container",
						"ParentTld",
						"ParentTld",
						"Tld",
						"Tld",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffTopDomain > subdictTenantIdx;
		if( dictByTenantIdx.containsKey( keyTenantIdx ) ) {
			subdictTenantIdx = dictByTenantIdx.get( keyTenantIdx );
		}
		else {
			subdictTenantIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffTopDomain >();
			dictByTenantIdx.put( keyTenantIdx, subdictTenantIdx );
		}
		subdictTenantIdx.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffTopDomain > subdictTldIdx;
		if( dictByTldIdx.containsKey( keyTldIdx ) ) {
			subdictTldIdx = dictByTldIdx.get( keyTldIdx );
		}
		else {
			subdictTldIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffTopDomain >();
			dictByTldIdx.put( keyTldIdx, subdictTldIdx );
		}
		subdictTldIdx.put( pkey, Buff );

		dictByNameIdx.put( keyNameIdx, Buff );

		if (Buff == null) {
			return( null );
		}
		else {
			int classCode = Buff.getClassCode();
			if (classCode == ICFIntTopDomain.CLASS_CODE) {
				CFIntBuffTopDomain retbuff = ((CFIntBuffTopDomain)(schema.getFactoryTopDomain().newRec()));
				retbuff.set(Buff);
				return( retbuff );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), S_ProcName, "-create-buff-cloning-", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntTopDomain readDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamTopDomain.readDerived";
		ICFIntTopDomain buff;
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
	public ICFIntTopDomain lockDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamTopDomain.lockDerived";
		ICFIntTopDomain buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTopDomain[] readAllDerived( ICFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamTopDomain.readAllDerived";
		ICFIntTopDomain[] retList = new ICFIntTopDomain[ dictByPKey.values().size() ];
		Iterator< CFIntBuffTopDomain > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	@Override
	public ICFIntTopDomain[] readDerivedByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamTopDomain.readDerivedByTenantIdx";
		CFIntBuffTopDomainByTenantIdxKey key = (CFIntBuffTopDomainByTenantIdxKey)schema.getFactoryTopDomain().newByTenantIdxKey();

		key.setRequiredTenantId( TenantId );
		ICFIntTopDomain[] recArray;
		if( dictByTenantIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffTopDomain > subdictTenantIdx
				= dictByTenantIdx.get( key );
			recArray = new ICFIntTopDomain[ subdictTenantIdx.size() ];
			Iterator< CFIntBuffTopDomain > iter = subdictTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffTopDomain > subdictTenantIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffTopDomain >();
			dictByTenantIdx.put( key, subdictTenantIdx );
			recArray = new ICFIntTopDomain[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntTopDomain[] readDerivedByTldIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TldId )
	{
		final String S_ProcName = "CFIntRamTopDomain.readDerivedByTldIdx";
		CFIntBuffTopDomainByTldIdxKey key = (CFIntBuffTopDomainByTldIdxKey)schema.getFactoryTopDomain().newByTldIdxKey();

		key.setRequiredTldId( TldId );
		ICFIntTopDomain[] recArray;
		if( dictByTldIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffTopDomain > subdictTldIdx
				= dictByTldIdx.get( key );
			recArray = new ICFIntTopDomain[ subdictTldIdx.size() ];
			Iterator< CFIntBuffTopDomain > iter = subdictTldIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffTopDomain > subdictTldIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffTopDomain >();
			dictByTldIdx.put( key, subdictTldIdx );
			recArray = new ICFIntTopDomain[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntTopDomain readDerivedByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TldId,
		String Name )
	{
		final String S_ProcName = "CFIntRamTopDomain.readDerivedByNameIdx";
		CFIntBuffTopDomainByNameIdxKey key = (CFIntBuffTopDomainByNameIdxKey)schema.getFactoryTopDomain().newByNameIdxKey();

		key.setRequiredTldId( TldId );
		key.setRequiredName( Name );
		ICFIntTopDomain buff;
		if( dictByNameIdx.containsKey( key ) ) {
			buff = dictByNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTopDomain readDerivedByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamTopDomain.readDerivedByIdIdx() ";
		ICFIntTopDomain buff;
		if( dictByPKey.containsKey( Id ) ) {
			buff = dictByPKey.get( Id );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTopDomain readRec( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamTopDomain.readRec";
		ICFIntTopDomain buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntTopDomain.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTopDomain lockRec( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "lockRec";
		ICFIntTopDomain buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntTopDomain.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTopDomain[] readAllRec( ICFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamTopDomain.readAllRec";
		ICFIntTopDomain buff;
		ArrayList<ICFIntTopDomain> filteredList = new ArrayList<ICFIntTopDomain>();
		ICFIntTopDomain[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntTopDomain.CLASS_CODE ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new ICFIntTopDomain[0] ) );
	}

	@Override
	public ICFIntTopDomain readRecByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamTopDomain.readRecByIdIdx() ";
		ICFIntTopDomain buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntTopDomain.CLASS_CODE ) ) {
			return( (ICFIntTopDomain)buff );
		}
		else {
			return( null );
		}
	}

	@Override
	public ICFIntTopDomain[] readRecByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamTopDomain.readRecByTenantIdx() ";
		ICFIntTopDomain buff;
		ArrayList<ICFIntTopDomain> filteredList = new ArrayList<ICFIntTopDomain>();
		ICFIntTopDomain[] buffList = readDerivedByTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntTopDomain.CLASS_CODE ) ) {
				filteredList.add( (ICFIntTopDomain)buff );
			}
		}
		return( filteredList.toArray( new ICFIntTopDomain[0] ) );
	}

	@Override
	public ICFIntTopDomain[] readRecByTldIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TldId )
	{
		final String S_ProcName = "CFIntRamTopDomain.readRecByTldIdx() ";
		ICFIntTopDomain buff;
		ArrayList<ICFIntTopDomain> filteredList = new ArrayList<ICFIntTopDomain>();
		ICFIntTopDomain[] buffList = readDerivedByTldIdx( Authorization,
			TldId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntTopDomain.CLASS_CODE ) ) {
				filteredList.add( (ICFIntTopDomain)buff );
			}
		}
		return( filteredList.toArray( new ICFIntTopDomain[0] ) );
	}

	@Override
	public ICFIntTopDomain readRecByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TldId,
		String Name )
	{
		final String S_ProcName = "CFIntRamTopDomain.readRecByNameIdx() ";
		ICFIntTopDomain buff = readDerivedByNameIdx( Authorization,
			TldId,
			Name );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntTopDomain.CLASS_CODE ) ) {
			return( (ICFIntTopDomain)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntTopDomain updateTopDomain( ICFSecAuthorization Authorization,
		ICFIntTopDomain iBuff )
	{
		CFIntBuffTopDomain Buff = (CFIntBuffTopDomain)ensureRec(iBuff);
		CFLibDbKeyHash256 pkey = Buff.getPKey();
		CFIntBuffTopDomain existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateTopDomain",
				"Existing record not found",
				"Existing record not found",
				"TopDomain",
				"TopDomain",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateTopDomain",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntBuffTopDomainByTenantIdxKey existingKeyTenantIdx = (CFIntBuffTopDomainByTenantIdxKey)schema.getFactoryTopDomain().newByTenantIdxKey();
		existingKeyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffTopDomainByTenantIdxKey newKeyTenantIdx = (CFIntBuffTopDomainByTenantIdxKey)schema.getFactoryTopDomain().newByTenantIdxKey();
		newKeyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffTopDomainByTldIdxKey existingKeyTldIdx = (CFIntBuffTopDomainByTldIdxKey)schema.getFactoryTopDomain().newByTldIdxKey();
		existingKeyTldIdx.setRequiredTldId( existing.getRequiredTldId() );

		CFIntBuffTopDomainByTldIdxKey newKeyTldIdx = (CFIntBuffTopDomainByTldIdxKey)schema.getFactoryTopDomain().newByTldIdxKey();
		newKeyTldIdx.setRequiredTldId( Buff.getRequiredTldId() );

		CFIntBuffTopDomainByNameIdxKey existingKeyNameIdx = (CFIntBuffTopDomainByNameIdxKey)schema.getFactoryTopDomain().newByNameIdxKey();
		existingKeyNameIdx.setRequiredTldId( existing.getRequiredTldId() );
		existingKeyNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffTopDomainByNameIdxKey newKeyNameIdx = (CFIntBuffTopDomainByNameIdxKey)schema.getFactoryTopDomain().newByNameIdxKey();
		newKeyNameIdx.setRequiredTldId( Buff.getRequiredTldId() );
		newKeyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyNameIdx.equals( newKeyNameIdx ) ) {
			if( dictByNameIdx.containsKey( newKeyNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateTopDomain",
					"TopDomNameIdx",
					"TopDomNameIdx",
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
						"updateTopDomain",
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
				if( null == schema.getTableTld().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTldId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateTopDomain",
						"Container",
						"Container",
						"ParentTld",
						"ParentTld",
						"Tld",
						"Tld",
						null );
				}
			}
		}

		// Update is valid

		Map< CFLibDbKeyHash256, CFIntBuffTopDomain > subdict;

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
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffTopDomain >();
			dictByTenantIdx.put( newKeyTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		subdict = dictByTldIdx.get( existingKeyTldIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByTldIdx.containsKey( newKeyTldIdx ) ) {
			subdict = dictByTldIdx.get( newKeyTldIdx );
		}
		else {
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffTopDomain >();
			dictByTldIdx.put( newKeyTldIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByNameIdx.remove( existingKeyNameIdx );
		dictByNameIdx.put( newKeyNameIdx, Buff );

		return(Buff);
	}

	@Override
	public void deleteTopDomain( ICFSecAuthorization Authorization,
		ICFIntTopDomain iBuff )
	{
		final String S_ProcName = "CFIntRamTopDomainTable.deleteTopDomain() ";
		CFIntBuffTopDomain Buff = (CFIntBuffTopDomain)ensureRec(iBuff);
		int classCode;
		CFLibDbKeyHash256 pkey = (CFLibDbKeyHash256)(Buff.getPKey());
		CFIntBuffTopDomain existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteTopDomain",
				pkey );
		}
					schema.getTableTopProject().deleteTopProjectByTopDomainIdx( Authorization,
						existing.getRequiredId() );
		CFIntBuffTopDomainByTenantIdxKey keyTenantIdx = (CFIntBuffTopDomainByTenantIdxKey)schema.getFactoryTopDomain().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffTopDomainByTldIdxKey keyTldIdx = (CFIntBuffTopDomainByTldIdxKey)schema.getFactoryTopDomain().newByTldIdxKey();
		keyTldIdx.setRequiredTldId( existing.getRequiredTldId() );

		CFIntBuffTopDomainByNameIdxKey keyNameIdx = (CFIntBuffTopDomainByNameIdxKey)schema.getFactoryTopDomain().newByNameIdxKey();
		keyNameIdx.setRequiredTldId( existing.getRequiredTldId() );
		keyNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFLibDbKeyHash256, CFIntBuffTopDomain > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByTenantIdx.get( keyTenantIdx );
		subdict.remove( pkey );

		subdict = dictByTldIdx.get( keyTldIdx );
		subdict.remove( pkey );

		dictByNameIdx.remove( keyNameIdx );

	}
	@Override
	public void deleteTopDomainByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntBuffTopDomain cur;
		LinkedList<CFIntBuffTopDomain> matchSet = new LinkedList<CFIntBuffTopDomain>();
		Iterator<CFIntBuffTopDomain> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffTopDomain> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffTopDomain)(schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteTopDomain( Authorization, cur );
		}
	}

	@Override
	public void deleteTopDomainByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntBuffTopDomainByTenantIdxKey key = (CFIntBuffTopDomainByTenantIdxKey)schema.getFactoryTopDomain().newByTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteTopDomainByTenantIdx( Authorization, key );
	}

	@Override
	public void deleteTopDomainByTenantIdx( ICFSecAuthorization Authorization,
		ICFIntTopDomainByTenantIdxKey argKey )
	{
		CFIntBuffTopDomain cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffTopDomain> matchSet = new LinkedList<CFIntBuffTopDomain>();
		Iterator<CFIntBuffTopDomain> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffTopDomain> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffTopDomain)(schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteTopDomain( Authorization, cur );
		}
	}

	@Override
	public void deleteTopDomainByTldIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTldId )
	{
		CFIntBuffTopDomainByTldIdxKey key = (CFIntBuffTopDomainByTldIdxKey)schema.getFactoryTopDomain().newByTldIdxKey();
		key.setRequiredTldId( argTldId );
		deleteTopDomainByTldIdx( Authorization, key );
	}

	@Override
	public void deleteTopDomainByTldIdx( ICFSecAuthorization Authorization,
		ICFIntTopDomainByTldIdxKey argKey )
	{
		CFIntBuffTopDomain cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffTopDomain> matchSet = new LinkedList<CFIntBuffTopDomain>();
		Iterator<CFIntBuffTopDomain> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffTopDomain> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffTopDomain)(schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteTopDomain( Authorization, cur );
		}
	}

	@Override
	public void deleteTopDomainByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTldId,
		String argName )
	{
		CFIntBuffTopDomainByNameIdxKey key = (CFIntBuffTopDomainByNameIdxKey)schema.getFactoryTopDomain().newByNameIdxKey();
		key.setRequiredTldId( argTldId );
		key.setRequiredName( argName );
		deleteTopDomainByNameIdx( Authorization, key );
	}

	@Override
	public void deleteTopDomainByNameIdx( ICFSecAuthorization Authorization,
		ICFIntTopDomainByNameIdxKey argKey )
	{
		CFIntBuffTopDomain cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffTopDomain> matchSet = new LinkedList<CFIntBuffTopDomain>();
		Iterator<CFIntBuffTopDomain> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffTopDomain> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffTopDomain)(schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteTopDomain( Authorization, cur );
		}
	}
}
