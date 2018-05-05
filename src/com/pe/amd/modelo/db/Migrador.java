package com.pe.amd.modelo.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.pe.amd.modelo.beans.BeanManager;
import com.pe.amd.modelo.beans.Cabdocumentos;
import com.pe.amd.modelo.beans.Contingencia;
import com.pe.amd.modelo.beans.Detdocumentos;

class Migrador {
	public List<Contingencia> migrarContingencia(Connection origen, String fecha, Connection connection) throws SQLException {	
		Statement st1 = null,st2 = null;
		PreparedStatement pst = null;
		ResultSet tabla = null;
		ArrayList<Contingencia> lista = null;
		try {
			String sql1 = "SELECT  m_sali.PERS_P_inCODPER, m_sali.TDOS_P_inCODTIP,m_sali.TIDS_P_inCODTIP, "+
	                "m_sali.USUA_P_inCODUSU,m_sali.TDOI_P_inCODTIP,m_sali.SALI_inSITSAL,m_sali.SALI_chSERDOC, "+
	                "m_sali.SALI_chNRODOC,m_sali.SALI_chFECSAL, m_sali.SALI_deTOTSAL,m_pers.Pers_chrucdniper, "+
	                "m_pers.Pers_chnomcom,m_tipo_docu_impr.TDOI_chNOMTIP "+
	                "FROM carbuss.m_sali "+
	                "INNER JOIN m_pers "+       
	                "ON m_sali.PERS_P_inCODPER=m_pers.Pers_P_incodper "+             
	                "INNER JOIN m_tipo_docu_impr "+
	                "ON m_sali.TDOI_P_inCODTIP=m_tipo_docu_impr.TDOI_P_inCODTIP "+    
	                "WHERE m_sali.sali_chfecsal=\""+fecha+"\" "+            
	                "ORDER BY m_sali.TDOI_P_inCODTIP,m_sali.SALI_chNRODOC ";
			
			String sql2 = "select * from sunat.contingencia where TRIM(fecemision) = \""+fecha+"\"";
			
			st1 = connection.createStatement();
			st1.executeQuery(sql2);
			if(st1.getResultSet().next()) {
				st1.getResultSet().close();
				st1.execute("delete from sunat.contingencia where TRIM(fecemision) = \""+fecha+"\"");
			}

			st1.close();
			
			st2 = origen.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			tabla = st2.executeQuery(sql1);
			pst = connection.prepareStatement(BeanManager.SQL_INSERT_CONTINGENCIA);
			
			lista = new ArrayList<>();
			while(tabla.next()) {
				
				Contingencia cont = new Contingencia();
				
				cont.setPeriodo(fecha.substring(0, 6));//AAAAMMDD
				cont.setMotivo("7");
				cont.setFecemision(tabla.getString("SALI_chFECSAL"));
				String tipodoc = tabla.getString("TDOI_chNOMTIP");
				if(tipodoc.equalsIgnoreCase("FACTURA"))
					tipodoc = "01";
				else if(tipodoc.equalsIgnoreCase("BOLETA"))
					tipodoc = "03";
				else
					tipodoc = "12";
				cont.setTipodocum(tipodoc);
				cont.setSerie(tabla.getString("SALI_chSERDOC"));
				cont.setNumero(String.format("%4s", tabla.getString("SALI_chNRODOC").replace(' ', '0')));
				
				String doc_cli = tabla.getString("Pers_chrucdniper");
				String tipo = "6";
				
				if(doc_cli.trim().length() != 11)
					tipo = "1";
				
				if(doc_cli.trim().equals("--")) {
					tipo = "0";
					doc_cli = "00000000";
				}
				cont.setTipodocucli(tipo);
				cont.setNumdocucli(doc_cli);
				
				cont.setNombrecli(tabla.getString("Pers_chnomcom"));
				cont.setValventaexo(tabla.getDouble("SALI_deTOTSAL"));
				cont.setTotalcomp(tabla.getDouble("SALI_deTOTSAL"));
				
				BeanManager.insertContingencia(pst, cont);
				lista.add(cont);
			}
			tabla.close();
			st2.close();
			pst.close();
		}
		catch(SQLException e) {lista = null; throw e;}
		finally {
			try {if(st1 != null )st1.close();}catch(Exception e) {}
			try {if(st2 != null )st1.close();}catch(Exception e) {}
			try {if(pst != null )st1.close();}catch(Exception e) {}
			try {if(tabla != null )st1.close();}catch(Exception e) {}
		}	
		return lista;
	}

	public List<Cabdocumentos> migrarFacturas(Connection corigen, String fecha, boolean corregido,Connection connection)  throws SQLException{
		ArrayList<Cabdocumentos> lista = null;
		Statement st = null,st2 = null;
		PreparedStatement pst1 = null,pst2 = null;
		ResultSet tabla = null,detalle = null;
		try {
			//Primero se Borra las lineas cono homologado = -1 --> Rechazado por Excepcion
			//Se pregunta al usuario si ha corregido los errores (homologado = -2) --> Rechazado
			String msj = "";
			st = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			st.executeQuery("SELECT transaccion FROM sunat.cabdocumentos "
					+ "WHERE tipodocumento = '01' "+ 
					(!corregido ?"AND homologado = -1 ;":"AND (homologado = -1 OR homologado = -2);"));
			while(st.getResultSet().next()) {
				msj += st.getResultSet().getString(1)+",";
			}
			st.getResultSet().close();
			if(!msj.equals("")) {
				st.execute("DELETE FROM sunat.cabdocumentos "
						+ "WHERE tipodocumento = '01' "+ 
						(corregido ?"AND homologado = -1 ;":"AND (homologado = -1 OR homologado = -2);"));
				st.execute("DELETE FROM sunat.detdocumentos "
						+ "WHERE transaccion IN ("+msj.substring(0,msj.length()-1) + ");");
			}
			
			st.close();
			
			//Se extrae la informacion
			lista = new ArrayList<>();
			String not_into = "";
			st = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			st.executeQuery("select * from sunat.cabdocumentos where TRIM(fechaemision) = \""+fecha+"\" and tipodocumento = '01';");
			
			//Guardo las transacciones que ya han sido aceptadas (homologado = 1)
			//O aquellas que todavia estan en proceso de homologacion (homologado = 0)
			//O que todavia no han sido modificadas tras su error (homologado = -2)
			while(st.getResultSet().next()) {
				lista.add(BeanManager.getCabdocumentos(st.getResultSet()));
				not_into +="'"+ lista.get(lista.size()-1).getTransaccion() + "',";
			}
			st.getResultSet().close();
			st.close();
			if(!not_into.isEmpty())
				not_into = " AND m_sali.sali_p_incodsal NOT IN (" + not_into.substring(0, not_into.length()-1) +") ";
			String sql1 = "SELECT  m_sali.sali_p_incodsal AS Transaccion,"
					+"SUBSTRING(TRIM(m_sali.SALI_chFECSAL),1,6) AS periodo,"
					+"m_sali.SALI_chFECSAL AS fechaemision,"
					+"IF(TRIM(m_tipo_docu_impr.TDOI_chNOMTIP)=\"FACTURA\",\"01\",\"03\") AS TIPODOCUMENTO,"
					+"m_sali.SALI_inSITSAL AS ANULADO,"
					+"m_sali.SALI_chSERDOC AS serie,"
					+"m_sali.SALI_chNRODOC AS numero,"
					+"IF(LENGTH(TRIM(m_pers.Pers_chrucdniper))=11,\"6\",\"1\") AS tipocliente,"
					+"m_pers.Pers_chrucdniper AS numcliente,"
					+"m_pers.Pers_chnomcom AS nomcliente,"
					+"m_pers.Pers_chdirper AS direccion,"
					+"m_pers.Pers_chemaper AS email,"
					+"m_sali.SALI_deTOTSAL AS valventaexo,"
					+"m_sali.SALI_deTOTSAL AS totaldoc"       
                	+" FROM "
                	+"carbuss.m_sali"
                	+" INNER JOIN m_pers"
                	+" ON m_sali.PERS_P_inCODPER=m_pers.Pers_P_incodper"
                	+" INNER JOIN m_tipo_docu_impr"
                	+" ON m_sali.TDOI_P_inCODTIP=m_tipo_docu_impr.TDOI_P_inCODTIP"
                	+" WHERE m_sali.sali_chfecsal=\""+fecha+"\" " +not_into +" "
                			+ "AND m_tipo_docu_impr.TDOI_chNOMTIP = 'FACTURA' AND m_sali.SALI_inSITSAL <> 4 "
                	+" ORDER BY m_sali.TDOI_P_inCODTIP,m_sali.SALI_chNRODOC";
			/*String sql2 = " SELECT v_sali.sali_p_incodsal AS transaccion,"
				       +" m_cata.cata_chcodext AS codigo,"
				       +" m_cata.cata_chnomcat AS denominacion,"
				       +" m_unid.unid_chSUNAT  AS unidad,"
				       +" v_sali.sald_decansal AS cantidad,"
				       +" v_sali.sald_depreuni AS valunitario,"
				       +" v_sali.sald_detotdet AS valtotal"
				       +" FROM v_sali"
				       +"  INNER JOIN m_cata"
				       +" ON v_sali.catd_p_incodcat=m_cata.cata_p_incodcat"
				       +" INNER JOIN v_cata_deta"
				       +" ON v_sali.catd_p_incodcat=v_cata_deta.CATD_P_inCODCAT"
				       +" INNER JOIN m_unid"
				       +" ON v_cata_deta.UNID_P_inCODUNI=m_unid.unid_p_incoduni"
	       			   +" INNER JOIN m_sali"
				       +" ON m_sali.SALI_P_inCODSAL = v_sali.sali_p_incodsal"
	       			   +" WHERE m_sali.sali_chfecsal=\""+fecha+"\""
	       			   +" ORDER BY transaccion; ";*/
			
			String sql2 = "SELECT v_sali.sali_p_incodsal AS transaccion,\r\n" + 
					"    m_cata.cata_chcodext AS codigo,\r\n" + 
					"    m_cata.cata_chnomcat AS denominacion,\r\n" + 
					"    m_unid.unid_chSUNAT  AS unidad,\r\n" + 
					"    v_sali.sald_decansal AS cantidad,\r\n" + 
					"    v_sali.sald_depreuni AS valunitario,\r\n" + 
					"    v_sali.sald_detotdet AS valtotal,\r\n" + 
					"    v_sali.estado,\r\n" + 
					"    v_cata_deta.CATD_P_inCODCAT,\r\n" + 
					"    v_cata_deta.CATA_P_inCODCAT\r\n" + 
					"    FROM v_sali\r\n" + 
					"    INNER JOIN v_cata_deta\r\n" + 
					"    ON v_sali.catd_p_incodcat=v_cata_deta.CATD_P_inCODCAT\r\n" + 
					"    INNER JOIN m_cata\r\n" + 
					"    ON v_cata_deta.CATA_P_inCODCAT=m_cata.cata_p_incodcat\r\n" + 
					"    INNER JOIN m_unid\r\n" + 
					"    ON v_cata_deta.UNID_P_inCODUNI=m_unid.unid_p_incoduni\r\n" + 
					"    INNER JOIN m_sali\r\n" + 
					"    ON m_sali.SALI_P_inCODSAL = v_sali.sali_p_incodsal\r\n" + 
					"    WHERE m_sali.sali_chfecsal = \""+fecha+"\" AND v_sali.estado = 1 "+
					"    ORDER BY transaccion; ";
			st = corigen.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			st2 = corigen.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			tabla = st.executeQuery(sql1);
			detalle = st2.executeQuery(sql2);
			pst1 = connection.prepareStatement(BeanManager.SQL_INSERT_CABECERA);
			pst2 = connection.prepareStatement(BeanManager.SQL_INSERT_DETALLE);
			
			if( tabla.last())
				System.out.println("Migrando...");
			tabla.beforeFirst();
			while(tabla.next()) {
				
				 
				if(tabla.getInt("ANULADO") == 4) {continue;}
				
				Cabdocumentos factura = new Cabdocumentos();
				factura.setTransaccion(tabla.getString("transaccion"));
				factura.setPeriodo(tabla.getString("periodo"));
				factura.setTipodocumento(tabla.getString("TIPODOCUMENTO"));
				factura.setSerie(tabla.getString("serie"));
				factura.setNumero(tabla.getString("numero"));
				factura.setFechaemision(tabla.getString("fechaemision"));
				factura.setTipocliente(tabla.getString("tipocliente"));
				factura.setNumcliente(tabla.getString("numcliente"));
				factura.setNomcliente( tabla.getString("nomcliente"));;
				factura.setDireccion(tabla.getString("direccion"));;
				factura.setEmail(tabla.getString("email"));
				factura.setValventaexo( tabla.getDouble("valventaexo"));
				factura.setCodigv("20");
				factura.setIgv(0.00);
				factura.setTotaldoc( tabla.getDouble("totaldoc"));
				factura.setHomologado(0);
				
				System.out.println("Factura: " + factura.getTransaccion() 
				+ "::" + factura.getSerie() + "-" + factura.getNumero()
				+ "::" + factura.getTipocliente() + "::" + factura.getNomcliente()
				+ "::" + factura.getNumcliente()+ "::" + factura.getTotaldoc());
				
				BeanManager.insertCabecera(pst1, factura);
				int sec = 0;
				boolean entro = false;
				Detdocumentos det = new Detdocumentos();
				while(detalle.next()) {
					if(detalle.getString("transaccion").equals(factura.getTransaccion())) {
						entro = true;
						det.setTransaccion(detalle.getString("transaccion"));
						det.setSec(String.format("%03d", ++sec));
						det.setCodigo(detalle.getString("codigo"));
						det.setDenominacion(detalle.getString("denominacion"));
						det.setUnidad(detalle.getString("unidad"));
						det.setValunitario( detalle.getDouble("valunitario"));
						det.setCantidad(detalle.getDouble("cantidad"));
						det.setIgv(0.00);
						det.setCodigv("20");
						det.setValtotal(detalle.getDouble("valtotal"));
						BeanManager.insertDetalle(pst2, det);
						
					}
					else if(entro)
						break;
				}
				detalle.beforeFirst();
				lista.add(factura);
			}
			tabla.last();
			if(tabla.getRow() > 0)
				System.out.println("Fin Migrando....");
			
			tabla.close();detalle.close();
			st.close();pst1.close();st2.close();pst2.close();
			
		}catch(SQLException e) { lista = null; throw e;}
		finally {
			try {if(tabla != null)tabla.close();}catch(Exception e) {}
			try {if(detalle != null)detalle.close();}catch(Exception e) {}
			try {if(pst1 != null)pst1.close();}catch(Exception e) {}
			try {if(pst2 != null)pst2.close();}catch(Exception e) {}
			try {if(st != null)st.close();}catch(Exception e) {}
			try {if(st2 != null)st2.close();}catch(Exception e) {}
		}
		return lista;
	}
	public List<Cabdocumentos> migrarBoletas(Connection corigen, String fecha, boolean corregido,Connection connection)  throws SQLException{
		ArrayList<Cabdocumentos> lista = null;
		Statement st = null,st2 = null;
		PreparedStatement pst1 = null,pst2 = null;
		ResultSet tabla = null,detalle = null;
		try {
			//Primero se Borra las lineas cono homologado = -1 --> Rechazado por Excepcion
			//Se pregunta al usuario si ha corregido los errores (homologado = -2) --> Rechazado
			String msj = "";
			st = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			st.executeQuery("SELECT transaccion FROM sunat.cabdocumentos "
					+ "WHERE tipodocumento = '03' "+ 
					(!corregido ?"AND homologado = -1 ;":"AND (homologado = -1 OR homologado = -2);"));
			while(st.getResultSet().next()) {
				msj += st.getResultSet().getString(1)+",";
			}
			st.getResultSet().close();
			if(!msj.equals("")) {
				st.execute("DELETE FROM sunat.cabdocumentos "
						+ "WHERE tipodocumento = '03' "+ 
						(corregido ?"AND homologado = -1 ;":"AND (homologado = -1 OR homologado = -2);"));
				st.execute("DELETE FROM sunat.detdocumentos "
						+ "WHERE transaccion IN ("+msj.substring(0,msj.length()-1) + ");");
			}
			
			st.close();
			
			lista = new ArrayList<>();
			String not_into = "";
			st = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			st.executeQuery("select * from sunat.cabdocumentos where TRIM(fechaemision) = \""+fecha+"\" and tipodocumento = '03'");
			
			while(st.getResultSet().next()) {
				lista.add(BeanManager.getCabdocumentos(st.getResultSet()));
				not_into +="'"+ lista.get(lista.size()-1).getTransaccion() + "',";
			}
			st.getResultSet().close();
			st.close();
			if(!not_into.isEmpty())
				not_into = " AND m_sali.sali_p_incodsal NOT IN (" + not_into.substring(0, not_into.length()-1) +") ";
			String sql1 = "SELECT  m_sali.sali_p_incodsal AS Transaccion,"
					+"SUBSTRING(TRIM(m_sali.SALI_chFECSAL),1,6) AS periodo,"
					+"m_sali.SALI_chFECSAL AS fechaemision,"
					+"IF(TRIM(m_tipo_docu_impr.TDOI_chNOMTIP)=\"FACTURA\",\"01\",\"03\") AS TIPODOCUMENTO,"
					+"m_sali.SALI_inSITSAL AS ANULADO,"
					+"m_sali.SALI_chSERDOC AS serie,"
					+"m_sali.SALI_chNRODOC AS numero,"
					+"IF(LENGTH(TRIM(m_pers.Pers_chrucdniper))=11,\"6\",\"1\") AS tipocliente,"
					+"m_pers.Pers_chrucdniper AS numcliente,"
					+"m_pers.Pers_chnomcom AS nomcliente,"
					+"m_pers.Pers_chdirper AS direccion,"
					+"m_pers.Pers_chemaper AS email,"
					+"m_sali.SALI_deTOTSAL AS valventaexo,"
					+"m_sali.SALI_deTOTSAL AS totaldoc"       
                	+" FROM "
                	+"carbuss.m_sali"
                	+" INNER JOIN m_pers"
                	+" ON m_sali.PERS_P_inCODPER=m_pers.Pers_P_incodper"
                	+" INNER JOIN m_tipo_docu_impr"
                	+" ON m_sali.TDOI_P_inCODTIP=m_tipo_docu_impr.TDOI_P_inCODTIP"
                	+" WHERE m_sali.sali_chfecsal=\""+fecha+"\" " +not_into +" "
                			+ "AND m_tipo_docu_impr.TDOI_chNOMTIP <> 'FACTURA' AND m_sali.SALI_inSITSAL <> 4 "
                	+" ORDER BY m_sali.TDOI_P_inCODTIP,m_sali.SALI_chNRODOC";
			/*String sql2 = " SELECT v_sali.sali_p_incodsal AS transaccion,"
				       +" m_cata.cata_chcodext AS codigo,"
				       +" m_cata.cata_chnomcat AS denominacion,"
				       +" m_unid.unid_chSUNAT  AS unidad,"
				       +" v_sali.sald_decansal AS cantidad,"
				       +" v_sali.sald_depreuni AS valunitario,"
				       +" v_sali.sald_detotdet AS valtotal"
				       +" FROM v_sali"
				       +"  INNER JOIN m_cata"
				       +" ON v_sali.catd_p_incodcat=m_cata.cata_p_incodcat"
				       +" INNER JOIN v_cata_deta"
				       +" ON v_sali.catd_p_incodcat=v_cata_deta.CATD_P_inCODCAT"
				       +" INNER JOIN m_unid"
				       +" ON v_cata_deta.UNID_P_inCODUNI=m_unid.unid_p_incoduni"
	       			   +" INNER JOIN m_sali"
				       +" ON m_sali.SALI_P_inCODSAL = v_sali.sali_p_incodsal"
	       			   +" WHERE m_sali.sali_chfecsal=\""+fecha+"\""
	       			   +" ORDER BY transaccion; ";*/
			String sql2 = "SELECT v_sali.sali_p_incodsal AS transaccion,\r\n" + 
					"    m_cata.cata_chcodext AS codigo,\r\n" + 
					"    m_cata.cata_chnomcat AS denominacion,\r\n" + 
					"    m_unid.unid_chSUNAT  AS unidad,\r\n" + 
					"    v_sali.sald_decansal AS cantidad,\r\n" + 
					"    v_sali.sald_depreuni AS valunitario,\r\n" + 
					"    v_sali.sald_detotdet AS valtotal,\r\n" + 
					"    v_sali.estado,\r\n" + 
					"    v_cata_deta.CATD_P_inCODCAT,\r\n" + 
					"    v_cata_deta.CATA_P_inCODCAT\r\n" + 
					"    FROM v_sali\r\n" + 
					"    INNER JOIN v_cata_deta\r\n" + 
					"    ON v_sali.catd_p_incodcat=v_cata_deta.CATD_P_inCODCAT\r\n" + 
					"    INNER JOIN m_cata\r\n" + 
					"    ON v_cata_deta.CATA_P_inCODCAT=m_cata.cata_p_incodcat\r\n" + 
					"    INNER JOIN m_unid\r\n" + 
					"    ON v_cata_deta.UNID_P_inCODUNI=m_unid.unid_p_incoduni\r\n" + 
					"    INNER JOIN m_sali\r\n" + 
					"    ON m_sali.SALI_P_inCODSAL = v_sali.sali_p_incodsal\r\n" + 
					"    WHERE m_sali.sali_chfecsal = \""+fecha+"\" AND v_sali.estado = 1 "+
					"    ORDER BY transaccion; ";
			st = corigen.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			st2 = corigen.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			tabla = st.executeQuery(sql1);
			detalle = st2.executeQuery(sql2);
			pst1 = connection.prepareStatement(BeanManager.SQL_INSERT_CABECERA);
			pst2 = connection.prepareStatement(BeanManager.SQL_INSERT_DETALLE);
			
			if( tabla.last())
				System.out.println("Migrando...");
			tabla.beforeFirst();
			
			while(tabla.next()) {
				if(tabla.getInt("ANULADO") == 4) {continue;}
				
				Cabdocumentos boleta = new Cabdocumentos();
				boleta.setTransaccion(tabla.getString("transaccion"));
				boleta.setPeriodo(tabla.getString("periodo"));
				boleta.setTipodocumento(tabla.getString("TIPODOCUMENTO"));
				boleta.setSerie(tabla.getString("serie"));
				boleta.setNumero(tabla.getString("numero"));
				boleta.setFechaemision(tabla.getString("fechaemision"));
				boleta.setTipocliente(tabla.getString("tipocliente"));
				boleta.setNumcliente(tabla.getString("numcliente"));
				boleta.setNomcliente( tabla.getString("nomcliente"));;
				boleta.setDireccion(tabla.getString("direccion"));;
				boleta.setEmail(tabla.getString("email"));
				boleta.setValventaexo( tabla.getDouble("valventaexo"));
				boleta.setCodigv("20");
				boleta.setIgv(0.00);
				boleta.setTotaldoc( tabla.getDouble("totaldoc"));
				boleta.setHomologado(0);
				
				System.out.println("Boleta: " + boleta.getTransaccion() 
				+ "::" + boleta.getSerie() + "-" + boleta.getNumero()
				+ "::" + boleta.getTipocliente() + "::" + boleta.getNomcliente()
				+ "::" + boleta.getNumcliente() + "::" + boleta.getTotaldoc());
				
				BeanManager.insertCabecera(pst1, boleta);
				int sec = 0;
				boolean entro = false;
				Detdocumentos det = new Detdocumentos();
				while(detalle.next()) {
					if(detalle.getString("transaccion").equals(boleta.getTransaccion())) {
						entro = true;
						det.setTransaccion(detalle.getString("transaccion"));
						det.setSec(String.format("%03d", ++sec));
						det.setCodigo(detalle.getString("codigo"));
						det.setDenominacion(detalle.getString("denominacion"));
						det.setUnidad(detalle.getString("unidad"));
						det.setValunitario( detalle.getDouble("valunitario"));
						det.setCantidad(detalle.getDouble("cantidad"));
						det.setIgv(0.00);
						det.setCodigv("20");
						det.setValtotal(detalle.getDouble("valtotal"));
						BeanManager.insertDetalle(pst2, det);
						
					}
					else if(entro)
						break;
				}
				detalle.beforeFirst();
				lista.add(boleta);
			}
			tabla.last();
			if(tabla.getRow() > 0)
				System.out.println("Fin Migrando....");
			
			tabla.close();detalle.close();
			st.close();pst1.close();st2.close();pst2.close();
			
		}catch(SQLException e) { lista = null; throw e;}
		finally {
			try {if(tabla != null)tabla.close();}catch(Exception e) {}
			try {if(detalle != null)detalle.close();}catch(Exception e) {}
			try {if(pst1 != null)pst1.close();}catch(Exception e) {}
			try {if(pst2 != null)pst2.close();}catch(Exception e) {}
			try {if(st != null)st.close();}catch(Exception e) {}
			try {if(st2 != null)st2.close();}catch(Exception e) {}
		}
		return lista;
	}
}
