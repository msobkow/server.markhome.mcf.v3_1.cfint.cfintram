// Description: Java 25 implementation of an in-memory RAM CFInt schema.

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

import java.lang.reflect.*;
import java.net.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import server.markhome.mcf.v3_1.cflib.*;
import server.markhome.mcf.v3_1.cflib.dbutil.*;

import server.markhome.mcf.v3_1.cfsec.cfsec.*;
import server.markhome.mcf.v3_1.cfint.cfint.*;
import server.markhome.mcf.v3_1.cfsec.cfsecobj.*;
import server.markhome.mcf.v3_1.cfint.cfintobj.*;
import server.markhome.mcf.v3_1.cfsec.cfsec.buff.*;
import server.markhome.mcf.v3_1.cfint.cfint.buff.*;
import server.markhome.mcf.v3_1.cfint.cfintsaxloader.*;

import server.markhome.mcf.v3_1.cfsec.cfsec.buff.CFSecBuffSchema;
import server.markhome.mcf.v3_1.cfsec.cfsec.buff.CFSecBuffFactoryService;
//import server.markhome.mcf.v3_1.cfint.cfintpub.buff.CFIntPubBuffFactoryService;

public class CFIntRamSchema
	extends CFIntBuffSchema
	implements ICFIntSchema
{
	protected int nextMimeTypeIdGenValue = 1;
	protected int nextURLProtocolIdGenValue = 1;


	public CFIntRamSchema() {
		super();
		tableLicense = new CFIntRamLicenseTable( this );
		tableMajorVersion = new CFIntRamMajorVersionTable( this );
		tableMimeType = new CFIntRamMimeTypeTable( this );
		tableMinorVersion = new CFIntRamMinorVersionTable( this );
		tableSubProject = new CFIntRamSubProjectTable( this );
		tableTld = new CFIntRamTldTable( this );
		tableTopDomain = new CFIntRamTopDomainTable( this );
		tableTopProject = new CFIntRamTopProjectTable( this );
		tableURLProtocol = new CFIntRamURLProtocolTable( this );
	}

	@Override
	public ICFIntSchema newSchema() {
		throw new CFLibMustOverrideException( getClass(), "newSchema" );
	}

	@Override
	public int nextMimeTypeIdGen() {
		int next = nextMimeTypeIdGenValue++;
		return( next );
	}

	@Override
	public int nextURLProtocolIdGen() {
		int next = nextURLProtocolIdGenValue++;
		return( next );
	}

	@Override
	public CFLibDbKeyHash256 nextMajorVersionIdGen() {
		CFLibDbKeyHash256 retval = new CFLibDbKeyHash256(0);
		return( retval );
	}

	@Override
	public CFLibDbKeyHash256 nextMinorVersionIdGen() {
		CFLibDbKeyHash256 retval = new CFLibDbKeyHash256(0);
		return( retval );
	}

	@Override
	public CFLibDbKeyHash256 nextSubProjectIdGen() {
		CFLibDbKeyHash256 retval = new CFLibDbKeyHash256(0);
		return( retval );
	}

	@Override
	public CFLibDbKeyHash256 nextTldIdGen() {
		CFLibDbKeyHash256 retval = new CFLibDbKeyHash256(0);
		return( retval );
	}

	@Override
	public CFLibDbKeyHash256 nextTopDomainIdGen() {
		CFLibDbKeyHash256 retval = new CFLibDbKeyHash256(0);
		return( retval );
	}

	@Override
	public CFLibDbKeyHash256 nextTopProjectIdGen() {
		CFLibDbKeyHash256 retval = new CFLibDbKeyHash256(0);
		return( retval );
	}

	@Override
	public CFLibDbKeyHash256 nextLicenseIdGen() {
		CFLibDbKeyHash256 retval = new CFLibDbKeyHash256(0);
		return( retval );
	}

	public String fileImport( CFSecAuthorization Authorization,
		String fileName,
		String fileContent )
	{
		final String S_ProcName = "fileImport";
		if( ( fileName == null ) || ( fileName.length() <= 0 ) ) {
			throw new CFLibNullArgumentException( getClass(),
				S_ProcName,
				1,
				"fileName" );
		}
		if( ( fileContent == null ) || ( fileContent.length() <= 0 ) ) {
			throw new CFLibNullArgumentException( getClass(),
				S_ProcName,
				2,
				"fileContent" );
		}

		CFIntSaxLoader saxLoader = new CFIntSaxLoader();
		ICFIntSchemaObj schemaObj = new CFIntSchemaObj();
		schemaObj.setCFIntBackingStore( this );
		saxLoader.setSchemaObj( schemaObj );
		ICFSecClusterObj useCluster = schemaObj.getClusterTableObj().readClusterByIdIdx( Authorization.getSecClusterId() );
		ICFSecTenantObj useTenant = schemaObj.getTenantTableObj().readTenantByIdIdx( Authorization.getSecTenantId() );
		CFLibCachedMessageLog runlog = new CFLibCachedMessageLog();
		saxLoader.setLog( runlog );
		saxLoader.setUseCluster( useCluster );
		saxLoader.setUseTenant( useTenant );
		saxLoader.parseStringContents( fileContent );
		String logFileContent = runlog.getCacheContents();
		if( logFileContent == null ) {
			logFileContent = "";
		}

		return( logFileContent );
	}

		
	@Override
	public void wireTableTableInstances() {
		if (tableLicense == null || !(tableLicense instanceof CFIntRamLicenseTable)) {
			tableLicense = new CFIntRamLicenseTable(this);
		}
		if (tableMajorVersion == null || !(tableMajorVersion instanceof CFIntRamMajorVersionTable)) {
			tableMajorVersion = new CFIntRamMajorVersionTable(this);
		}
		if (tableMimeType == null || !(tableMimeType instanceof CFIntRamMimeTypeTable)) {
			tableMimeType = new CFIntRamMimeTypeTable(this);
		}
		if (tableMinorVersion == null || !(tableMinorVersion instanceof CFIntRamMinorVersionTable)) {
			tableMinorVersion = new CFIntRamMinorVersionTable(this);
		}
		if (tableSubProject == null || !(tableSubProject instanceof CFIntRamSubProjectTable)) {
			tableSubProject = new CFIntRamSubProjectTable(this);
		}
		if (tableTld == null || !(tableTld instanceof CFIntRamTldTable)) {
			tableTld = new CFIntRamTldTable(this);
		}
		if (tableTopDomain == null || !(tableTopDomain instanceof CFIntRamTopDomainTable)) {
			tableTopDomain = new CFIntRamTopDomainTable(this);
		}
		if (tableTopProject == null || !(tableTopProject instanceof CFIntRamTopProjectTable)) {
			tableTopProject = new CFIntRamTopProjectTable(this);
		}
		if (tableURLProtocol == null || !(tableURLProtocol instanceof CFIntRamURLProtocolTable)) {
			tableURLProtocol = new CFIntRamURLProtocolTable(this);
		}
	}
}
