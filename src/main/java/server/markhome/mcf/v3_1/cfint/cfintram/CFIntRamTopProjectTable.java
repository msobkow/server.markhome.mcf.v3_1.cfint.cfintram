
// Description: Java 25 in-memory RAM DbIO implementation for TopProject.

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
 *	CFIntRamTopProjectTable in-memory RAM DbIO implementation
 *	for TopProject.
 */
public class CFIntRamTopProjectTable
	implements ICFIntTopProjectTable
{
	private ICFIntSchema schema;
	private Map< CFLibDbKeyHash256,
				CFIntBuffTopProject > dictByPKey
		= new HashMap< CFLibDbKeyHash256,
				CFIntBuffTopProject >();
	private Map< CFIntBuffTopProjectByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffTopProject >> dictByTenantIdx
		= new HashMap< CFIntBuffTopProjectByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffTopProject >>();
	private Map< CFIntBuffTopProjectByTopDomainIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffTopProject >> dictByTopDomainIdx
		= new HashMap< CFIntBuffTopProjectByTopDomainIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffTopProject >>();
	private Map< CFIntBuffTopProjectByNameIdxKey,
			CFIntBuffTopProject > dictByNameIdx
		= new HashMap< CFIntBuffTopProjectByNameIdxKey,
			CFIntBuffTopProject >();

	public CFIntRamTopProjectTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public CFIntBuffTopProject ensureRec(ICFIntTopProject rec) {
		if (rec == null) {
			return( null );
		}
		else {
			int classCode = rec.getClassCode();
			if (classCode == ICFIntTopProject.CLASS_CODE) {
				return( ((CFIntBuffTopProjectDefaultFactory)(schema.getFactoryTopProject())).ensureRec((ICFIntTopProject)rec) );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), "ensureRec", "rec", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntTopProject createTopProject( ICFSecAuthorization Authorization,
		ICFIntTopProject iBuff )
	{
		final String S_ProcName = "createTopProject";
		
		CFIntBuffTopProject Buff = (CFIntBuffTopProject)ensureRec(iBuff);
		CFLibDbKeyHash256 pkey;
		pkey = schema.nextTopProjectIdGen();
		Buff.setRequiredId( pkey );
		CFIntBuffTopProjectByTenantIdxKey keyTenantIdx = (CFIntBuffTopProjectByTenantIdxKey)schema.getFactoryTopProject().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffTopProjectByTopDomainIdxKey keyTopDomainIdx = (CFIntBuffTopProjectByTopDomainIdxKey)schema.getFactoryTopProject().newByTopDomainIdxKey();
		keyTopDomainIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );

		CFIntBuffTopProjectByNameIdxKey keyNameIdx = (CFIntBuffTopProjectByNameIdxKey)schema.getFactoryTopProject().newByNameIdxKey();
		keyNameIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );
		keyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByNameIdx.containsKey( keyNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"TopProjectNameIdx",
				"TopProjectNameIdx",
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
				if( null == schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTopDomainId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Container",
						"Container",
						"ParentTopDomain",
						"ParentTopDomain",
						"TopDomain",
						"TopDomain",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffTopProject > subdictTenantIdx;
		if( dictByTenantIdx.containsKey( keyTenantIdx ) ) {
			subdictTenantIdx = dictByTenantIdx.get( keyTenantIdx );
		}
		else {
			subdictTenantIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffTopProject >();
			dictByTenantIdx.put( keyTenantIdx, subdictTenantIdx );
		}
		subdictTenantIdx.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffTopProject > subdictTopDomainIdx;
		if( dictByTopDomainIdx.containsKey( keyTopDomainIdx ) ) {
			subdictTopDomainIdx = dictByTopDomainIdx.get( keyTopDomainIdx );
		}
		else {
			subdictTopDomainIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffTopProject >();
			dictByTopDomainIdx.put( keyTopDomainIdx, subdictTopDomainIdx );
		}
		subdictTopDomainIdx.put( pkey, Buff );

		dictByNameIdx.put( keyNameIdx, Buff );

		if (Buff == null) {
			return( null );
		}
		else {
			int classCode = Buff.getClassCode();
			if (classCode == ICFIntTopProject.CLASS_CODE) {
				CFIntBuffTopProject retbuff = ((CFIntBuffTopProject)(schema.getFactoryTopProject().newRec()));
				retbuff.set(Buff);
				return( retbuff );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), S_ProcName, "-create-buff-cloning-", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntTopProject readDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamTopProject.readDerived";
		ICFIntTopProject buff;
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
	public ICFIntTopProject lockDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamTopProject.lockDerived";
		ICFIntTopProject buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTopProject[] readAllDerived( ICFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamTopProject.readAllDerived";
		ICFIntTopProject[] retList = new ICFIntTopProject[ dictByPKey.values().size() ];
		Iterator< CFIntBuffTopProject > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	@Override
	public ICFIntTopProject[] readDerivedByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamTopProject.readDerivedByTenantIdx";
		CFIntBuffTopProjectByTenantIdxKey key = (CFIntBuffTopProjectByTenantIdxKey)schema.getFactoryTopProject().newByTenantIdxKey();

		key.setRequiredTenantId( TenantId );
		ICFIntTopProject[] recArray;
		if( dictByTenantIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffTopProject > subdictTenantIdx
				= dictByTenantIdx.get( key );
			recArray = new ICFIntTopProject[ subdictTenantIdx.size() ];
			Iterator< CFIntBuffTopProject > iter = subdictTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffTopProject > subdictTenantIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffTopProject >();
			dictByTenantIdx.put( key, subdictTenantIdx );
			recArray = new ICFIntTopProject[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntTopProject[] readDerivedByTopDomainIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId )
	{
		final String S_ProcName = "CFIntRamTopProject.readDerivedByTopDomainIdx";
		CFIntBuffTopProjectByTopDomainIdxKey key = (CFIntBuffTopProjectByTopDomainIdxKey)schema.getFactoryTopProject().newByTopDomainIdxKey();

		key.setRequiredTopDomainId( TopDomainId );
		ICFIntTopProject[] recArray;
		if( dictByTopDomainIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffTopProject > subdictTopDomainIdx
				= dictByTopDomainIdx.get( key );
			recArray = new ICFIntTopProject[ subdictTopDomainIdx.size() ];
			Iterator< CFIntBuffTopProject > iter = subdictTopDomainIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffTopProject > subdictTopDomainIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffTopProject >();
			dictByTopDomainIdx.put( key, subdictTopDomainIdx );
			recArray = new ICFIntTopProject[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntTopProject readDerivedByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId,
		String Name )
	{
		final String S_ProcName = "CFIntRamTopProject.readDerivedByNameIdx";
		CFIntBuffTopProjectByNameIdxKey key = (CFIntBuffTopProjectByNameIdxKey)schema.getFactoryTopProject().newByNameIdxKey();

		key.setRequiredTopDomainId( TopDomainId );
		key.setRequiredName( Name );
		ICFIntTopProject buff;
		if( dictByNameIdx.containsKey( key ) ) {
			buff = dictByNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTopProject readDerivedByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamTopProject.readDerivedByIdIdx() ";
		ICFIntTopProject buff;
		if( dictByPKey.containsKey( Id ) ) {
			buff = dictByPKey.get( Id );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTopProject readRec( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamTopProject.readRec";
		ICFIntTopProject buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntTopProject.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTopProject lockRec( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "lockRec";
		ICFIntTopProject buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntTopProject.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntTopProject[] readAllRec( ICFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamTopProject.readAllRec";
		ICFIntTopProject buff;
		ArrayList<ICFIntTopProject> filteredList = new ArrayList<ICFIntTopProject>();
		ICFIntTopProject[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntTopProject.CLASS_CODE ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new ICFIntTopProject[0] ) );
	}

	@Override
	public ICFIntTopProject readRecByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamTopProject.readRecByIdIdx() ";
		ICFIntTopProject buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntTopProject.CLASS_CODE ) ) {
			return( (ICFIntTopProject)buff );
		}
		else {
			return( null );
		}
	}

	@Override
	public ICFIntTopProject[] readRecByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamTopProject.readRecByTenantIdx() ";
		ICFIntTopProject buff;
		ArrayList<ICFIntTopProject> filteredList = new ArrayList<ICFIntTopProject>();
		ICFIntTopProject[] buffList = readDerivedByTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntTopProject.CLASS_CODE ) ) {
				filteredList.add( (ICFIntTopProject)buff );
			}
		}
		return( filteredList.toArray( new ICFIntTopProject[0] ) );
	}

	@Override
	public ICFIntTopProject[] readRecByTopDomainIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId )
	{
		final String S_ProcName = "CFIntRamTopProject.readRecByTopDomainIdx() ";
		ICFIntTopProject buff;
		ArrayList<ICFIntTopProject> filteredList = new ArrayList<ICFIntTopProject>();
		ICFIntTopProject[] buffList = readDerivedByTopDomainIdx( Authorization,
			TopDomainId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntTopProject.CLASS_CODE ) ) {
				filteredList.add( (ICFIntTopProject)buff );
			}
		}
		return( filteredList.toArray( new ICFIntTopProject[0] ) );
	}

	@Override
	public ICFIntTopProject readRecByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId,
		String Name )
	{
		final String S_ProcName = "CFIntRamTopProject.readRecByNameIdx() ";
		ICFIntTopProject buff = readDerivedByNameIdx( Authorization,
			TopDomainId,
			Name );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntTopProject.CLASS_CODE ) ) {
			return( (ICFIntTopProject)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntTopProject updateTopProject( ICFSecAuthorization Authorization,
		ICFIntTopProject iBuff )
	{
		CFIntBuffTopProject Buff = (CFIntBuffTopProject)ensureRec(iBuff);
		CFLibDbKeyHash256 pkey = Buff.getPKey();
		CFIntBuffTopProject existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateTopProject",
				"Existing record not found",
				"Existing record not found",
				"TopProject",
				"TopProject",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateTopProject",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntBuffTopProjectByTenantIdxKey existingKeyTenantIdx = (CFIntBuffTopProjectByTenantIdxKey)schema.getFactoryTopProject().newByTenantIdxKey();
		existingKeyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffTopProjectByTenantIdxKey newKeyTenantIdx = (CFIntBuffTopProjectByTenantIdxKey)schema.getFactoryTopProject().newByTenantIdxKey();
		newKeyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffTopProjectByTopDomainIdxKey existingKeyTopDomainIdx = (CFIntBuffTopProjectByTopDomainIdxKey)schema.getFactoryTopProject().newByTopDomainIdxKey();
		existingKeyTopDomainIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );

		CFIntBuffTopProjectByTopDomainIdxKey newKeyTopDomainIdx = (CFIntBuffTopProjectByTopDomainIdxKey)schema.getFactoryTopProject().newByTopDomainIdxKey();
		newKeyTopDomainIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );

		CFIntBuffTopProjectByNameIdxKey existingKeyNameIdx = (CFIntBuffTopProjectByNameIdxKey)schema.getFactoryTopProject().newByNameIdxKey();
		existingKeyNameIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );
		existingKeyNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffTopProjectByNameIdxKey newKeyNameIdx = (CFIntBuffTopProjectByNameIdxKey)schema.getFactoryTopProject().newByNameIdxKey();
		newKeyNameIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );
		newKeyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyNameIdx.equals( newKeyNameIdx ) ) {
			if( dictByNameIdx.containsKey( newKeyNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateTopProject",
					"TopProjectNameIdx",
					"TopProjectNameIdx",
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
						"updateTopProject",
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
				if( null == schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTopDomainId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateTopProject",
						"Container",
						"Container",
						"ParentTopDomain",
						"ParentTopDomain",
						"TopDomain",
						"TopDomain",
						null );
				}
			}
		}

		// Update is valid

		Map< CFLibDbKeyHash256, CFIntBuffTopProject > subdict;

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
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffTopProject >();
			dictByTenantIdx.put( newKeyTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		subdict = dictByTopDomainIdx.get( existingKeyTopDomainIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByTopDomainIdx.containsKey( newKeyTopDomainIdx ) ) {
			subdict = dictByTopDomainIdx.get( newKeyTopDomainIdx );
		}
		else {
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffTopProject >();
			dictByTopDomainIdx.put( newKeyTopDomainIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByNameIdx.remove( existingKeyNameIdx );
		dictByNameIdx.put( newKeyNameIdx, Buff );

		return(Buff);
	}

	@Override
	public void deleteTopProject( ICFSecAuthorization Authorization,
		ICFIntTopProject iBuff )
	{
		final String S_ProcName = "CFIntRamTopProjectTable.deleteTopProject() ";
		CFIntBuffTopProject Buff = (CFIntBuffTopProject)ensureRec(iBuff);
		int classCode;
		CFLibDbKeyHash256 pkey = (CFLibDbKeyHash256)(Buff.getPKey());
		CFIntBuffTopProject existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteTopProject",
				pkey );
		}
					schema.getTableSubProject().deleteSubProjectByTopProjectIdx( Authorization,
						existing.getRequiredId() );
		CFIntBuffTopProjectByTenantIdxKey keyTenantIdx = (CFIntBuffTopProjectByTenantIdxKey)schema.getFactoryTopProject().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffTopProjectByTopDomainIdxKey keyTopDomainIdx = (CFIntBuffTopProjectByTopDomainIdxKey)schema.getFactoryTopProject().newByTopDomainIdxKey();
		keyTopDomainIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );

		CFIntBuffTopProjectByNameIdxKey keyNameIdx = (CFIntBuffTopProjectByNameIdxKey)schema.getFactoryTopProject().newByNameIdxKey();
		keyNameIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );
		keyNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFLibDbKeyHash256, CFIntBuffTopProject > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByTenantIdx.get( keyTenantIdx );
		subdict.remove( pkey );

		subdict = dictByTopDomainIdx.get( keyTopDomainIdx );
		subdict.remove( pkey );

		dictByNameIdx.remove( keyNameIdx );

	}
	@Override
	public void deleteTopProjectByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntBuffTopProject cur;
		LinkedList<CFIntBuffTopProject> matchSet = new LinkedList<CFIntBuffTopProject>();
		Iterator<CFIntBuffTopProject> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffTopProject> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffTopProject)(schema.getTableTopProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteTopProject( Authorization, cur );
		}
	}

	@Override
	public void deleteTopProjectByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntBuffTopProjectByTenantIdxKey key = (CFIntBuffTopProjectByTenantIdxKey)schema.getFactoryTopProject().newByTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteTopProjectByTenantIdx( Authorization, key );
	}

	@Override
	public void deleteTopProjectByTenantIdx( ICFSecAuthorization Authorization,
		ICFIntTopProjectByTenantIdxKey argKey )
	{
		CFIntBuffTopProject cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffTopProject> matchSet = new LinkedList<CFIntBuffTopProject>();
		Iterator<CFIntBuffTopProject> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffTopProject> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffTopProject)(schema.getTableTopProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteTopProject( Authorization, cur );
		}
	}

	@Override
	public void deleteTopProjectByTopDomainIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopDomainId )
	{
		CFIntBuffTopProjectByTopDomainIdxKey key = (CFIntBuffTopProjectByTopDomainIdxKey)schema.getFactoryTopProject().newByTopDomainIdxKey();
		key.setRequiredTopDomainId( argTopDomainId );
		deleteTopProjectByTopDomainIdx( Authorization, key );
	}

	@Override
	public void deleteTopProjectByTopDomainIdx( ICFSecAuthorization Authorization,
		ICFIntTopProjectByTopDomainIdxKey argKey )
	{
		CFIntBuffTopProject cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffTopProject> matchSet = new LinkedList<CFIntBuffTopProject>();
		Iterator<CFIntBuffTopProject> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffTopProject> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffTopProject)(schema.getTableTopProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteTopProject( Authorization, cur );
		}
	}

	@Override
	public void deleteTopProjectByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopDomainId,
		String argName )
	{
		CFIntBuffTopProjectByNameIdxKey key = (CFIntBuffTopProjectByNameIdxKey)schema.getFactoryTopProject().newByNameIdxKey();
		key.setRequiredTopDomainId( argTopDomainId );
		key.setRequiredName( argName );
		deleteTopProjectByNameIdx( Authorization, key );
	}

	@Override
	public void deleteTopProjectByNameIdx( ICFSecAuthorization Authorization,
		ICFIntTopProjectByNameIdxKey argKey )
	{
		CFIntBuffTopProject cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffTopProject> matchSet = new LinkedList<CFIntBuffTopProject>();
		Iterator<CFIntBuffTopProject> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffTopProject> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffTopProject)(schema.getTableTopProject().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteTopProject( Authorization, cur );
		}
	}
}
