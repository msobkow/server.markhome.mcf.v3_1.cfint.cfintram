
// Description: Java 25 in-memory RAM DbIO implementation for License.

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
 *	CFIntRamLicenseTable in-memory RAM DbIO implementation
 *	for License.
 */
public class CFIntRamLicenseTable
	implements ICFIntLicenseTable
{
	private ICFIntSchema schema;
	private Map< CFLibDbKeyHash256,
				CFIntBuffLicense > dictByPKey
		= new HashMap< CFLibDbKeyHash256,
				CFIntBuffLicense >();
	private Map< CFIntBuffLicenseByLicnTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffLicense >> dictByLicnTenantIdx
		= new HashMap< CFIntBuffLicenseByLicnTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffLicense >>();
	private Map< CFIntBuffLicenseByDomainIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffLicense >> dictByDomainIdx
		= new HashMap< CFIntBuffLicenseByDomainIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffLicense >>();
	private Map< CFIntBuffLicenseByUNameIdxKey,
			CFIntBuffLicense > dictByUNameIdx
		= new HashMap< CFIntBuffLicenseByUNameIdxKey,
			CFIntBuffLicense >();

	public CFIntRamLicenseTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public CFIntBuffLicense ensureRec(ICFIntLicense rec) {
		if (rec == null) {
			return( null );
		}
		else {
			int classCode = rec.getClassCode();
			if (classCode == ICFIntLicense.CLASS_CODE) {
				return( ((CFIntBuffLicenseDefaultFactory)(schema.getFactoryLicense())).ensureRec((ICFIntLicense)rec) );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), "ensureRec", "rec", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntLicense createLicense( ICFSecAuthorization Authorization,
		ICFIntLicense iBuff )
	{
		final String S_ProcName = "createLicense";
		
		CFIntBuffLicense Buff = (CFIntBuffLicense)ensureRec(iBuff);
		CFLibDbKeyHash256 pkey;
		pkey = schema.nextLicenseIdGen();
		Buff.setRequiredId( pkey );
		CFIntBuffLicenseByLicnTenantIdxKey keyLicnTenantIdx = (CFIntBuffLicenseByLicnTenantIdxKey)schema.getFactoryLicense().newByLicnTenantIdxKey();
		keyLicnTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffLicenseByDomainIdxKey keyDomainIdx = (CFIntBuffLicenseByDomainIdxKey)schema.getFactoryLicense().newByDomainIdxKey();
		keyDomainIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );

		CFIntBuffLicenseByUNameIdxKey keyUNameIdx = (CFIntBuffLicenseByUNameIdxKey)schema.getFactoryLicense().newByUNameIdxKey();
		keyUNameIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );
		keyUNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByUNameIdx.containsKey( keyUNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"LicenseUNameIdx",
				"LicenseUNameIdx",
				keyUNameIdx );
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
						"Owner",
						"Owner",
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
						"TopDomain",
						"TopDomain",
						"TopDomain",
						"TopDomain",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictLicnTenantIdx;
		if( dictByLicnTenantIdx.containsKey( keyLicnTenantIdx ) ) {
			subdictLicnTenantIdx = dictByLicnTenantIdx.get( keyLicnTenantIdx );
		}
		else {
			subdictLicnTenantIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByLicnTenantIdx.put( keyLicnTenantIdx, subdictLicnTenantIdx );
		}
		subdictLicnTenantIdx.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictDomainIdx;
		if( dictByDomainIdx.containsKey( keyDomainIdx ) ) {
			subdictDomainIdx = dictByDomainIdx.get( keyDomainIdx );
		}
		else {
			subdictDomainIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByDomainIdx.put( keyDomainIdx, subdictDomainIdx );
		}
		subdictDomainIdx.put( pkey, Buff );

		dictByUNameIdx.put( keyUNameIdx, Buff );

		if (Buff == null) {
			return( null );
		}
		else {
			int classCode = Buff.getClassCode();
			if (classCode == ICFIntLicense.CLASS_CODE) {
				CFIntBuffLicense retbuff = ((CFIntBuffLicense)(schema.getFactoryLicense().newRec()));
				retbuff.set(Buff);
				return( retbuff );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), S_ProcName, "-create-buff-cloning-", (Integer)classCode, "Classcode not recognized: " + Integer.toString(classCode));
			}
		}
	}

	@Override
	public ICFIntLicense readDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamLicense.readDerived";
		ICFIntLicense buff;
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
	public ICFIntLicense lockDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamLicense.lockDerived";
		ICFIntLicense buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntLicense[] readAllDerived( ICFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamLicense.readAllDerived";
		ICFIntLicense[] retList = new ICFIntLicense[ dictByPKey.values().size() ];
		Iterator< CFIntBuffLicense > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	@Override
	public ICFIntLicense[] readDerivedByLicnTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByLicnTenantIdx";
		CFIntBuffLicenseByLicnTenantIdxKey key = (CFIntBuffLicenseByLicnTenantIdxKey)schema.getFactoryLicense().newByLicnTenantIdxKey();

		key.setRequiredTenantId( TenantId );
		ICFIntLicense[] recArray;
		if( dictByLicnTenantIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictLicnTenantIdx
				= dictByLicnTenantIdx.get( key );
			recArray = new ICFIntLicense[ subdictLicnTenantIdx.size() ];
			Iterator< CFIntBuffLicense > iter = subdictLicnTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictLicnTenantIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByLicnTenantIdx.put( key, subdictLicnTenantIdx );
			recArray = new ICFIntLicense[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntLicense[] readDerivedByDomainIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByDomainIdx";
		CFIntBuffLicenseByDomainIdxKey key = (CFIntBuffLicenseByDomainIdxKey)schema.getFactoryLicense().newByDomainIdxKey();

		key.setRequiredTopDomainId( TopDomainId );
		ICFIntLicense[] recArray;
		if( dictByDomainIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictDomainIdx
				= dictByDomainIdx.get( key );
			recArray = new ICFIntLicense[ subdictDomainIdx.size() ];
			Iterator< CFIntBuffLicense > iter = subdictDomainIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictDomainIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByDomainIdx.put( key, subdictDomainIdx );
			recArray = new ICFIntLicense[0];
		}
		return( recArray );
	}

	@Override
	public ICFIntLicense readDerivedByUNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId,
		String Name )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByUNameIdx";
		CFIntBuffLicenseByUNameIdxKey key = (CFIntBuffLicenseByUNameIdxKey)schema.getFactoryLicense().newByUNameIdxKey();

		key.setRequiredTopDomainId( TopDomainId );
		key.setRequiredName( Name );
		ICFIntLicense buff;
		if( dictByUNameIdx.containsKey( key ) ) {
			buff = dictByUNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntLicense readDerivedByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByIdIdx() ";
		ICFIntLicense buff;
		if( dictByPKey.containsKey( Id ) ) {
			buff = dictByPKey.get( Id );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntLicense readRec( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamLicense.readRec";
		ICFIntLicense buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntLicense.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntLicense lockRec( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "lockRec";
		ICFIntLicense buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntLicense.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	@Override
	public ICFIntLicense[] readAllRec( ICFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamLicense.readAllRec";
		ICFIntLicense buff;
		ArrayList<ICFIntLicense> filteredList = new ArrayList<ICFIntLicense>();
		ICFIntLicense[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntLicense.CLASS_CODE ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new ICFIntLicense[0] ) );
	}

	@Override
	public ICFIntLicense readRecByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamLicense.readRecByIdIdx() ";
		ICFIntLicense buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntLicense.CLASS_CODE ) ) {
			return( (ICFIntLicense)buff );
		}
		else {
			return( null );
		}
	}

	@Override
	public ICFIntLicense[] readRecByLicnTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamLicense.readRecByLicnTenantIdx() ";
		ICFIntLicense buff;
		ArrayList<ICFIntLicense> filteredList = new ArrayList<ICFIntLicense>();
		ICFIntLicense[] buffList = readDerivedByLicnTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntLicense.CLASS_CODE ) ) {
				filteredList.add( (ICFIntLicense)buff );
			}
		}
		return( filteredList.toArray( new ICFIntLicense[0] ) );
	}

	@Override
	public ICFIntLicense[] readRecByDomainIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId )
	{
		final String S_ProcName = "CFIntRamLicense.readRecByDomainIdx() ";
		ICFIntLicense buff;
		ArrayList<ICFIntLicense> filteredList = new ArrayList<ICFIntLicense>();
		ICFIntLicense[] buffList = readDerivedByDomainIdx( Authorization,
			TopDomainId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntLicense.CLASS_CODE ) ) {
				filteredList.add( (ICFIntLicense)buff );
			}
		}
		return( filteredList.toArray( new ICFIntLicense[0] ) );
	}

	@Override
	public ICFIntLicense readRecByUNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId,
		String Name )
	{
		final String S_ProcName = "CFIntRamLicense.readRecByUNameIdx() ";
		ICFIntLicense buff = readDerivedByUNameIdx( Authorization,
			TopDomainId,
			Name );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntLicense.CLASS_CODE ) ) {
			return( (ICFIntLicense)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntLicense updateLicense( ICFSecAuthorization Authorization,
		ICFIntLicense iBuff )
	{
		CFIntBuffLicense Buff = (CFIntBuffLicense)ensureRec(iBuff);
		CFLibDbKeyHash256 pkey = Buff.getPKey();
		CFIntBuffLicense existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateLicense",
				"Existing record not found",
				"Existing record not found",
				"License",
				"License",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateLicense",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntBuffLicenseByLicnTenantIdxKey existingKeyLicnTenantIdx = (CFIntBuffLicenseByLicnTenantIdxKey)schema.getFactoryLicense().newByLicnTenantIdxKey();
		existingKeyLicnTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffLicenseByLicnTenantIdxKey newKeyLicnTenantIdx = (CFIntBuffLicenseByLicnTenantIdxKey)schema.getFactoryLicense().newByLicnTenantIdxKey();
		newKeyLicnTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffLicenseByDomainIdxKey existingKeyDomainIdx = (CFIntBuffLicenseByDomainIdxKey)schema.getFactoryLicense().newByDomainIdxKey();
		existingKeyDomainIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );

		CFIntBuffLicenseByDomainIdxKey newKeyDomainIdx = (CFIntBuffLicenseByDomainIdxKey)schema.getFactoryLicense().newByDomainIdxKey();
		newKeyDomainIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );

		CFIntBuffLicenseByUNameIdxKey existingKeyUNameIdx = (CFIntBuffLicenseByUNameIdxKey)schema.getFactoryLicense().newByUNameIdxKey();
		existingKeyUNameIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );
		existingKeyUNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffLicenseByUNameIdxKey newKeyUNameIdx = (CFIntBuffLicenseByUNameIdxKey)schema.getFactoryLicense().newByUNameIdxKey();
		newKeyUNameIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );
		newKeyUNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyUNameIdx.equals( newKeyUNameIdx ) ) {
			if( dictByUNameIdx.containsKey( newKeyUNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateLicense",
					"LicenseUNameIdx",
					"LicenseUNameIdx",
					newKeyUNameIdx );
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
						"updateLicense",
						"Owner",
						"Owner",
						"Owner",
						"Owner",
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
						"updateLicense",
						"Container",
						"Container",
						"TopDomain",
						"TopDomain",
						"TopDomain",
						"TopDomain",
						null );
				}
			}
		}

		// Update is valid

		Map< CFLibDbKeyHash256, CFIntBuffLicense > subdict;

		dictByPKey.remove( pkey );
		dictByPKey.put( pkey, Buff );

		subdict = dictByLicnTenantIdx.get( existingKeyLicnTenantIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByLicnTenantIdx.containsKey( newKeyLicnTenantIdx ) ) {
			subdict = dictByLicnTenantIdx.get( newKeyLicnTenantIdx );
		}
		else {
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByLicnTenantIdx.put( newKeyLicnTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		subdict = dictByDomainIdx.get( existingKeyDomainIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByDomainIdx.containsKey( newKeyDomainIdx ) ) {
			subdict = dictByDomainIdx.get( newKeyDomainIdx );
		}
		else {
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByDomainIdx.put( newKeyDomainIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByUNameIdx.remove( existingKeyUNameIdx );
		dictByUNameIdx.put( newKeyUNameIdx, Buff );

		return(Buff);
	}

	@Override
	public void deleteLicense( ICFSecAuthorization Authorization,
		ICFIntLicense iBuff )
	{
		final String S_ProcName = "CFIntRamLicenseTable.deleteLicense() ";
		CFIntBuffLicense Buff = (CFIntBuffLicense)ensureRec(iBuff);
		int classCode;
		CFLibDbKeyHash256 pkey = (CFLibDbKeyHash256)(Buff.getPKey());
		CFIntBuffLicense existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteLicense",
				pkey );
		}
		CFIntBuffLicenseByLicnTenantIdxKey keyLicnTenantIdx = (CFIntBuffLicenseByLicnTenantIdxKey)schema.getFactoryLicense().newByLicnTenantIdxKey();
		keyLicnTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffLicenseByDomainIdxKey keyDomainIdx = (CFIntBuffLicenseByDomainIdxKey)schema.getFactoryLicense().newByDomainIdxKey();
		keyDomainIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );

		CFIntBuffLicenseByUNameIdxKey keyUNameIdx = (CFIntBuffLicenseByUNameIdxKey)schema.getFactoryLicense().newByUNameIdxKey();
		keyUNameIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );
		keyUNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFLibDbKeyHash256, CFIntBuffLicense > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByLicnTenantIdx.get( keyLicnTenantIdx );
		subdict.remove( pkey );

		subdict = dictByDomainIdx.get( keyDomainIdx );
		subdict.remove( pkey );

		dictByUNameIdx.remove( keyUNameIdx );

	}
	@Override
	public void deleteLicenseByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntBuffLicense cur;
		LinkedList<CFIntBuffLicense> matchSet = new LinkedList<CFIntBuffLicense>();
		Iterator<CFIntBuffLicense> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffLicense> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffLicense)(schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteLicense( Authorization, cur );
		}
	}

	@Override
	public void deleteLicenseByLicnTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntBuffLicenseByLicnTenantIdxKey key = (CFIntBuffLicenseByLicnTenantIdxKey)schema.getFactoryLicense().newByLicnTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteLicenseByLicnTenantIdx( Authorization, key );
	}

	@Override
	public void deleteLicenseByLicnTenantIdx( ICFSecAuthorization Authorization,
		ICFIntLicenseByLicnTenantIdxKey argKey )
	{
		CFIntBuffLicense cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffLicense> matchSet = new LinkedList<CFIntBuffLicense>();
		Iterator<CFIntBuffLicense> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffLicense> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffLicense)(schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteLicense( Authorization, cur );
		}
	}

	@Override
	public void deleteLicenseByDomainIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopDomainId )
	{
		CFIntBuffLicenseByDomainIdxKey key = (CFIntBuffLicenseByDomainIdxKey)schema.getFactoryLicense().newByDomainIdxKey();
		key.setRequiredTopDomainId( argTopDomainId );
		deleteLicenseByDomainIdx( Authorization, key );
	}

	@Override
	public void deleteLicenseByDomainIdx( ICFSecAuthorization Authorization,
		ICFIntLicenseByDomainIdxKey argKey )
	{
		CFIntBuffLicense cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffLicense> matchSet = new LinkedList<CFIntBuffLicense>();
		Iterator<CFIntBuffLicense> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffLicense> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffLicense)(schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteLicense( Authorization, cur );
		}
	}

	@Override
	public void deleteLicenseByUNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopDomainId,
		String argName )
	{
		CFIntBuffLicenseByUNameIdxKey key = (CFIntBuffLicenseByUNameIdxKey)schema.getFactoryLicense().newByUNameIdxKey();
		key.setRequiredTopDomainId( argTopDomainId );
		key.setRequiredName( argName );
		deleteLicenseByUNameIdx( Authorization, key );
	}

	@Override
	public void deleteLicenseByUNameIdx( ICFSecAuthorization Authorization,
		ICFIntLicenseByUNameIdxKey argKey )
	{
		CFIntBuffLicense cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntBuffLicense> matchSet = new LinkedList<CFIntBuffLicense>();
		Iterator<CFIntBuffLicense> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntBuffLicense> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = (CFIntBuffLicense)(schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() ));
			deleteLicense( Authorization, cur );
		}
	}
}
