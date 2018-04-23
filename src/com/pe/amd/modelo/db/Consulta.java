package com.pe.amd.modelo.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.pe.amd.modelo.app.in.Lector;
import com.pe.amd.modelo.beans.BeanManager;
import com.pe.amd.modelo.beans.Cabdocumentos;
import com.pe.amd.modelo.beans.Contingencia;
import com.pe.amd.modelo.beans.Correlacion;
import com.pe.amd.modelo.beans.CorrelacionContingencia;
import com.pe.amd.modelo.beans.Detdocumentos;
import com.pe.amd.modelo.beans.Empresa;
import com.pe.amd.modelo.beans.ResumenDiario;
import com.pe.amd.modelo.beans.Sistema;
import com.pe.amd.modelo.beans.Usuario;

public class Consulta {
	
	private Connection connection;//Representa la conexion a la BD del sistema
	private Migrador migrador;
	public Consulta(Connection connection) {
		setConnection(connection);
		migrador = new Migrador();
	}
	//USUARIOS
	public void crearUsuario(String ident, String pass, String dni, String correo, 
			String nombres, String apellidos,Integer rango) throws SQLException{
		// TODO Auto-generated method stub
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement(BeanManager.SQL_INSERT_USUARIO);
			Usuario usr = new Usuario();
			usr.setApellidos(apellidos);
			usr.setCorreo(correo);
			usr.setDni(dni);
			usr.setId(ident);
			usr.setNombres(nombres);
			usr.setPass(pass);
			usr.setRango(rango);
			BeanManager.insertUsuario(pst, usr);
		}catch(SQLException e) {throw e;} 
		finally {
			try{if(pst != null)pst.close();}catch(Exception e) {}
		}
	}
	public List<Usuario> listarUsuarios() throws SQLException{
		// TODO Auto-generated method stub
		Statement st = null;
		ArrayList<Usuario> usrs = null;
		try {
			st = connection.createStatement();
			st.executeQuery("select * from sunat.usuarios");
			usrs = new ArrayList<>();
			while(st.getResultSet().next()) {
				usrs.add(BeanManager.getUsuario(st.getResultSet()));
			}
			st.getResultSet().close();
			st.close();
		}catch(SQLException e) {usrs = null; throw e;}
		finally {
			try{if(st != null)st.close();}catch(Exception e) {}
		}
		return usrs;
	}
	//GETS INFO
	public Empresa getEmpresa() throws SQLException {
		Statement st = null;
		Empresa empresa = null;
		try {
			st = connection.createStatement();
			st.executeQuery("select * from empresa");
			while(st.getResultSet().next()) {
				empresa = BeanManager.getEmpresa(st.getResultSet());
			}
			st.getResultSet().close();
			st.close();
		}catch(SQLException e) {throw e;}
		finally {
			try{if(st != null)st.close();}catch(Exception e) {}
		}
		return empresa;
	}
	public List<Contingencia> getContingencia(String fecha) throws SQLException{
		Statement st = null;
		ArrayList<Contingencia> lista = null;
		try {
			st = connection.createStatement();
			st.executeQuery("SELECT * FROM sunat.contingencia WHERE fecemision = '"+fecha+"';");
			lista = new ArrayList<>();
			while(st.getResultSet().next()) {
				lista.add(BeanManager.getContingencia(st.getResultSet()));
			}
			st.getResultSet().close();
			st.close();
		}catch(SQLException e) {lista = null; throw e;}
		finally {
			try{if(st != null)st.close();}catch(Exception e) {}
		}
		
		return lista;
	}
	public List<Cabdocumentos> getDocumentos(String fecha, int codFactura, boolean muestra) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement pst = null;
		List<Cabdocumentos> lista = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM sunat.cabdocumentos"
					+ " WHERE fechaemision = ? AND tipodocumento = ? AND anulado = 0"+ 
					( muestra== false ?" AND homologado = 0":"")+ ";");
			pst.setString(1, fecha);
			pst.setString(2, String.format("%02d", codFactura));
			
			pst.executeQuery();
			lista = new ArrayList<Cabdocumentos>();
			while(pst.getResultSet().next()) {
				lista.add(BeanManager.getCabdocumentos(pst.getResultSet()));
			}
			pst.getResultSet().close();
			pst.close();
		}catch(SQLException e) {throw e;}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		
		return lista;
	}
	public List<Cabdocumentos> getDocumentos(String fecha, int codFactura, boolean muestra,boolean resumen) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement pst = null;
		List<Cabdocumentos> lista = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM sunat.cabdocumentos"
					+ " WHERE fechaemision = ? AND tipodocumento = ? AND anulado = 0" 
					+( muestra== false ?" AND homologado = 0":"")
					+ ( resumen == true ? " AND resumen = 0 ":"")+";");
			pst.setString(1, fecha);
			pst.setString(2, String.format("%02d", codFactura));
			
			pst.executeQuery();
			lista = new ArrayList<Cabdocumentos>();
			while(pst.getResultSet().next()) {
				lista.add(BeanManager.getCabdocumentos(pst.getResultSet()));
			}
			pst.getResultSet().close();
			pst.close();
		}catch(SQLException e) {throw e;}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		
		return lista;
	}
	public List<Cabdocumentos> getDocumentos(HashMap<String, String> parametros) throws SQLException {
		
		List<Cabdocumentos> documentos = null;
		String variables = "";
		
		int i = 1;
		for(String key : parametros.keySet()) {
			variables +=" " + key + " = ?";
			if(i != parametros.keySet().size())
				variables += " AND ";
			else
				variables += " ;";
			i++;
		}
		if(parametros.size() != 0)
			variables = " WHERE "+ variables;
		
		try(PreparedStatement pst = connection.prepareStatement("SELECT * FROM sunat.cabdocumentos"
				+ variables)){
			i = 1;
			for(String key : parametros.keySet()) {
				pst.setString(i, parametros.get(key));
				i++;
			}
			pst.executeQuery();
			
			documentos = new ArrayList<>();
			while(pst.getResultSet().next()) {
				documentos.add(BeanManager.getCabdocumentos(pst.getResultSet()));
			}
			pst.getResultSet().close();
		} catch (SQLException e) {throw e;}
		
		System.out.println("INFO-MESSAGE: SQL -> PARAMETROS...'SELECT * FROM sunat.cabdocumentos"+variables+"'");
		
		return documentos;
	}
	public List<Cabdocumentos> getDocumentos(HashMap<String, String> parametros, List<String> orderBy) throws SQLException {
		List<Cabdocumentos> documentos = null;
		
		String variables = "";
		String order = "";
		
		int i = 1;
		for(String key : parametros.keySet()) {
			variables +=" " + key + " = ?";
			if(i != parametros.keySet().size())
				variables += " AND ";
			
			i++;
		}
		if(parametros.size() != 0)
			variables = " WHERE "+ variables;
		
		
		for(String orderParam:orderBy) {
			order += orderParam ;
			if(!orderParam.equals(orderBy.get(orderBy.size()-1))) {
				order += ",";
			}
			else
				order = ";";
		}
		
		try(PreparedStatement pst = connection.prepareStatement("SELECT * FROM sunat.cabdocumentos"
				+ variables + " " + order)){

			System.out.println("INFO-MESSAGE: "
					+ "SQL -> PARAMETROS...'SELECT * FROM sunat.cabdocumentos"+variables+ " " + order);
			
			i = 1;
			for(String key : parametros.keySet()) {
				pst.setString(i, parametros.get(key));
				i++;
			}
			try(ResultSet resultados = pst.executeQuery()){
				documentos = new ArrayList<>();
				while(pst.getResultSet().next()) 
					documentos.add(BeanManager.getCabdocumentos(pst.getResultSet()));
				
			}
		} 
		return documentos;
	}
	public List<Cabdocumentos> getDocumentos(String fechaInicio, String fechaFin,Integer codFactura) throws SQLException {
		List<Cabdocumentos> documentos = null;
		try (PreparedStatement pst = connection.prepareStatement("SELECT * FROM sunat.cabdocumentos "
				+ "WHERE tipodocumento = ? AND anulado = 0 AND ( fechaemision BETWEEN ? AND ?)")){
			pst.setString(1, String.format("%02d", codFactura));
			pst.setString(2, fechaInicio);
			pst.setString(3, fechaFin);
			pst.executeQuery();
			
			documentos = new ArrayList<>();
			while(pst.getResultSet().next()) {
				documentos.add(BeanManager.getCabdocumentos(pst.getResultSet()));
			}
			pst.getResultSet().close();
		}
		
		return documentos;
	}
	public List<Cabdocumentos> getDocumentos(String[] condiciones, String[] orderBy) throws SQLException {
		List<Cabdocumentos> documentos = null;
		String parametros = "", orden = "";
		
		if(condiciones.length != 0) {
			parametros = " WHERE ";
			for(int i = 0 ; i < condiciones.length - 1 ; i++) {
				parametros += condiciones[i] + " AND ";
			}
			parametros += condiciones[condiciones.length - 1];
		}
		if(orderBy.length != 0) {
			orden = " ORDER BY ";
			for(int i = 0 ; i < orderBy.length - 1 ; i++) {
				orden += orderBy[i] + " , ";
			}
			orden += orderBy[orderBy.length - 1] + " ; ";
		}
		try(Statement st = this.connection.createStatement(
				ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_SENSITIVE)){
			System.out.println("SQL RESULTANTE... -> "
					+"SELECT * FROM cabdocumentos"+parametros + orden);
			try(ResultSet resultados = st.executeQuery("SELECT * FROM cabdocumentos"+parametros + orden)){
				documentos = new ArrayList<>();
				while(resultados.next())
					documentos.add(BeanManager.getCabdocumentos(resultados));
			}
		}
		return documentos;
	}
	public List<Detdocumentos> getDetalle(String transaccion) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement pst = null;
		List<Detdocumentos> lista = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM sunat.detdocumentos"
					+ " WHERE transaccion = ? ORDER BY sec;");
			pst.setString(1, transaccion);
			
			pst.executeQuery();
			lista = new ArrayList<Detdocumentos>();
			while(pst.getResultSet().next()) {
				lista.add(BeanManager.getDetdocumentos(pst.getResultSet()));
			}
			pst.getResultSet().close();
			pst.close();
		}catch(SQLException e) {throw e;}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		
		return lista;
	}
	
	
	public CorrelacionContingencia getCorrelacionContingencia(String fecha) throws SQLException {
		Statement st = null;
		PreparedStatement pst = null;
		CorrelacionContingencia correlacion = null;
		try {
			st = connection.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_SCROLL_SENSITIVE);
			st.executeQuery("SELECT * FROM sunat.correlacioncontingencia WHERE fecha = '"+fecha+"';");
			st.getResultSet().last();
			if(st.getResultSet().getRow() != 1) {
				correlacion = new CorrelacionContingencia();
				correlacion.setFecha(fecha);
				correlacion.setCorrelativo(0);
				pst = connection.prepareStatement(BeanManager.SQL_INSERT_CORRELACION_CONTINGENCIA);
				BeanManager.insertCorrelacionContingencia(pst, correlacion);
				pst.close();
			}else {
				correlacion = BeanManager.getCorrelacionContingencia(st.getResultSet());
			}
			st.getResultSet().close();
			st.close();
		}
		catch(SQLException e) {	throw e;}
		finally {
			try{if(st != null)st.close();}catch(Exception e) {}
			try{if(pst != null)pst.close();}catch(Exception e) {}
		}
		return correlacion;
	}
	
	public String getCorrelacionResumen(String fecha, String tipo) throws SQLException {
		// TODO Auto-generated method stub
		Statement st = null;
		String correlacion = "1";
		try {
			st = getConnection().createStatement();
			st.executeQuery("SELECT COUNT(*) FROM sunat.resumenes_diarios "
					+ "WHERE fecha = '"+fecha+"' AND tipo = '"+tipo+"';");
			st.getResultSet().next();
			
			correlacion = String.valueOf(st.getResultSet().getInt(1)+1);
		}catch(SQLException e) {throw e;}
		finally {
			try{if(st != null)st.close();}catch(Exception e) {}
		}
		
		return correlacion;
	}
	
	public Correlacion getCorrelacion(int codFactura) throws SQLException {
		PreparedStatement pst = null;
		Correlacion cor  = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM sunat.correlacion"
					+ " WHERE tipo_doc = ?;");
			pst.setInt(1, codFactura);
			pst.executeQuery();
			
			while(pst.getResultSet().next()) {
				cor = BeanManager.getCorrelacion(pst.getResultSet());
			}
			pst.getResultSet().close();
			pst.close();
		}catch(SQLException e) { throw e;}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		
		return cor;
	}
	
	public List<ResumenDiario> getResumen(String fecha_referencia, String tipo, boolean referencia) throws SQLException {
		PreparedStatement pst = null;
		List<ResumenDiario> datos = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM sunat.resumenes_diarios "
					+ "WHERE " + (referencia?"fecha_referencia":"fecha")+" = ? AND tipo = ?;");
			pst.setString(1, fecha_referencia);
			pst.setString(2, tipo);
			pst.executeQuery();
			datos = new ArrayList<>();
			while(pst.getResultSet().next()) {
				datos.add(BeanManager.getResumenDiario(pst.getResultSet()));
			}
			pst.getResultSet().close();
			pst.close();
		}catch(SQLException  e) {throw e;}
		finally {try {if(pst != null)pst.close();}catch(Exception e) {}}

		return datos;
	}
	public List<ResumenDiario> getResumen(String ticket, String tipo) throws SQLException {
		PreparedStatement pst = null;
		List<ResumenDiario> datos = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM sunat.resumenes_diarios "
					+ "WHERE ticket = ? AND tipo = ? ;");
			pst.setString(1, ticket);
			pst.setString(2, tipo);
			
			pst.executeQuery();
			datos = new ArrayList<>();
			while(pst.getResultSet().next()) {
				datos.add(BeanManager.getResumenDiario(pst.getResultSet()));
			}
			pst.getResultSet().close();
			pst.close();
		}catch(SQLException  e) {throw e;}
		finally {try {if(pst != null)pst.close();}catch(Exception e) {}}

		return datos;
	}
	public List<ResumenDiario> getResumen(String date, String tipo
			, String correlativo, boolean referencia) throws SQLException {
		PreparedStatement pst = null;
		List<ResumenDiario> datos = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM sunat.resumenes_diarios "
					+ "WHERE " + (referencia?"fecha_referencia":"fecha")
					+ " = ? AND tipo = ? AND correlativo = ?;");
			pst.setString(1, date);
			pst.setString(2, tipo);
			pst.setString(3, correlativo);
			pst.executeQuery();
			datos = new ArrayList<>();
			while(pst.getResultSet().next()) {
				datos.add(BeanManager.getResumenDiario(pst.getResultSet()));
			}
			pst.getResultSet().close();
			pst.close();
		}catch(SQLException  e) {throw e;}
		finally {try {if(pst != null)pst.close();}catch(Exception e) {}}

		return datos;
	}
	
	
	
	//MIGRACIONES
	public List<Contingencia> migrarContingencia(Connection origen, String fecha) throws SQLException {	
		return migrador.migrarContingencia(origen, fecha, getConnection());
	}
	public List<Cabdocumentos> migrarFacturas(Connection corigen, String fecha, boolean corregido)  throws SQLException{
		return migrador.migrarFacturas(corigen, fecha, corregido, getConnection());
	}
	public List<Cabdocumentos> migrarBoletas(Connection corigen, String fecha, boolean corregido)  throws SQLException{
		return migrador.migrarBoletas(corigen, fecha, corregido, getConnection());
	}
	
	//LOGGIN
	public Usuario loggin(String ident, String pass) throws SQLException {
		Usuario ret = null;
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("SELECT * FROM sunat.usuarios WHERE id = ? AND pass = ?",
					ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			pst.setString(1, ident);
			pst.setString(2, pass);
			pst.executeQuery();
			while(pst.getResultSet().next())
				ret = BeanManager.getUsuario(pst.getResultSet());
			
			pst.getResultSet().close();
			pst.close();
		}
		catch(SQLException e) {throw e;}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		return ret;
	}
	
	//UPDATES
	public int updateCorrelacionContingencia(String fecha, FileInputStream fileAsFileInputStream, File file) throws SQLException{
		PreparedStatement pst = null;
		int i = -999;
		try {
			pst = connection.prepareStatement("UPDATE sunat.correlacioncontingencia"
					+ " SET archivo = ? , "
					+ " correlativo = (correlativo)+1, "
					+ " nom_archivo = ? "
					+ " WHERE fecha = ?;");
			pst.setBinaryStream(1, fileAsFileInputStream, (int)file.length());
			pst.setString(2, file.getName());
			pst.setString(3, fecha);
			i = pst.executeUpdate();
			
		}catch(SQLException e) {throw e;}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		return i;
	}
	public int updateCabecera(String serie, String numero, int codFactura, String transaccion,
			FileInputStream fileAsFileInputStream,File f) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement pst = null;
		int i = -999;
		try {
			pst = connection.prepareStatement("UPDATE sunat.cabdocumentos"
					+ " SET archivo = ? , "
					+ " nom_archivo = ? ,"
					+ "serieelec = ? , "
					+ "numeroelec = ? "
					+ " WHERE ( transaccion = ? AND tipodocumento = ? ) ;");
			
			pst.setBinaryStream(1, fileAsFileInputStream, (int)f.length());
			pst.setString(2, f.getName());
			pst.setString(3, serie);
			pst.setString(4, numero);
			pst.setString(5, transaccion);
			pst.setString(6, String.format("%02d", codFactura));
			i = pst.executeUpdate();
			
		}catch(SQLException e) {throw e;}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		return i;
	}
	public int updateCabecera(String serie, String numero, int codFactura, String transaccion,
			FileInputStream doc_xml, File doc, int homologado, String msj_homologado
			, FileInputStream resp_file, File resp) throws SQLException {
		// TODO Auto-generated method stub
		
		PreparedStatement pst = null;
		int i = -999;
		try {
			GregorianCalendar date = new GregorianCalendar();
			String anio = String.format("%04d", date.get(GregorianCalendar.YEAR)),
					mes = String.format("%02d", date.get(GregorianCalendar.MONTH)+1),
					dia = String.format("%02d", date.get(GregorianCalendar.DATE));
			
			pst = connection.prepareStatement("UPDATE sunat.cabdocumentos"
					+ " SET archivo = ? , "
					+ " nom_archivo = ? ,"
					+ " serieelec = ? , "
					+ " numeroelec = ? ,"
					+ " homologado = ?,"
					+ " fechomologado = ?,"
					+ " mensaje_homologado = ?,"
					+ " archivo_homologado = ?,"
					+ " nom_archivo_homologado = ? "
					+ " WHERE ( transaccion = ? AND tipodocumento = ? ) ;");
			if(doc_xml != null) {
				pst.setBinaryStream(1, doc_xml, (int)doc.length());
				pst.setString(2, doc.getName());
			}
			else {
				pst.setNull(1, Types.BLOB);
				pst.setString(2, null);
			}
			pst.setString(3, serie);
			pst.setString(4, numero);
			pst.setInt(5, homologado);
			pst.setString(6, anio+mes+dia);
			pst.setString(7,msj_homologado );
			if(resp_file != null) {
				pst.setBinaryStream(8, resp_file , (int)resp.length());
				pst.setString(9, resp.getName());
			}
			else {
				pst.setNull(8, Types.BLOB);
				pst.setString(9, null);
			}
			pst.setString(10, transaccion);
			pst.setString(11, String.format("%02d", codFactura));
			
			i = pst.executeUpdate();
			
			try {doc_xml.close();}catch(Exception e) {}
			try {resp_file.close();}catch(Exception e) {}
			
		}catch(SQLException e) {throw e;}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		return i;
	}
	public void updateCabecera(String transaccion, int i) throws SQLException {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("UPDATE sunat.cabdocumentos "
					+ "SET resumen = ? WHERE transaccion = ?");
			pst.setInt(1, i);
			pst.setString(2, transaccion);
			pst.executeUpdate();
		}catch(SQLException e) {throw e;}
		finally{
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		
	}
	public void updateAnulado(String transaccion, int i) throws SQLException {
		PreparedStatement pst = null;
		try {
			pst = getConnection().prepareStatement("UPDATE sunat.cabdocumentos "
					+ "SET anulado = ? WHERE transaccion = ?");
			pst.setInt(1, i);
			pst.setString(2, transaccion);
			pst.executeUpdate();
		}catch(SQLException e) {throw e;}
		finally{
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		
	}
	public int updateCorrelacion(Correlacion cor) throws SQLException{
		// TODO Auto-generated method stub
		PreparedStatement pst = null;
		int i = -999;
		try {
			pst = connection.prepareStatement("UPDATE sunat.correlacion"
					+ " SET serie = ? , "
					+ " correlativo = ? "
					+ " WHERE tipo_doc = ? ;");
			pst.setString(1, cor.getSerie());
			pst.setInt(2, cor.getCorrelativo());
			pst.setInt(3, cor.getTipo_doc());
			i = pst.executeUpdate();
			
		}catch(SQLException e) {throw e;}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		return i;
	}
	public void updateResumen(String fecha, String correlativo, 
			File zip, FileInputStream zipAsFileInputStream,String tipo,boolean referencia) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("UPDATE sunat.resumenes_diarios"
					+ " SET archivo_sunat = ? , "
					+ " nom_archivo_sunat = ? "
					+ " WHERE "+(referencia?"fecha_referencia":"fecha")
					+ "= ? AND correlativo = ? AND tipo = ?;");
			if(zipAsFileInputStream != null) {
				
				pst.setBinaryStream(1, zipAsFileInputStream, (int)zip.length());
				pst.setString(2, zip.getName());
				pst.setString(3, fecha);
				pst.setString(4, correlativo);
				pst.setString(5, tipo);
				pst.executeUpdate();
				
			}
		}catch(SQLException e) {throw e;}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
	}
	public void updateResumen(String selectedTicket, File zip
			, FileInputStream zipAsFileInputStream,String tipo) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("UPDATE sunat.resumenes_diarios"
					+ " SET archivo_sunat = ? , "
					+ " nom_archivo_sunat = ? "
					+ " WHERE ticket = ? AND tipo = ?;");
			if(zipAsFileInputStream != null) {
				
				pst.setBinaryStream(1, zipAsFileInputStream, (int)zip.length());
				pst.setString(2, zip.getName());
				pst.setString(3, selectedTicket);
				pst.setString(4, tipo);
				pst.executeUpdate();
				
			}
		}catch(SQLException e) {throw e;}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
	}
	public int updateEmpresa(HashMap<String, String> datos, InputStream content, int size) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement pst = null;
		int k = -555;
		try {
			String variables = "";
			for(String name : datos.keySet()) {
				variables += (name + " = ? ,");
			}
			if(content != null) 
				variables += "ce = ? ;";
			else
				variables = variables.substring(0,variables.length()-1)+" ;";
			
			String sql = "UPDATE sunat.empresa "
					+ "SET "+variables ;

			
			System.out.println(sql);
			
			pst = connection.prepareStatement(sql);
			
			int i = 1;
			for(String name : datos.keySet()) {
				pst.setString(i, datos.get(name));
				i++;
			}
			if(content != null)
				pst.setBinaryStream(i, content, size);
			k = pst.executeUpdate();
		}catch(SQLException e) {
			throw e;
		}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		return k;
	}
	public int updateSistema(Sistema sistema) throws SQLException {
		PreparedStatement pst = null;
		int k = -555;
		try {
			pst = getConnection().prepareStatement("UPDATE sunat.sistema "
					+ "SET direccion = ? , \r\n" + 
					"	url_sunat_produccion = ? , \r\n" + 
					"	url_sunat_remision = ?, \r\n" + 
					"	url_sunat_validez_ce = ? , \r\n" + 
					"	url_sunat_cdr = ? , \r\n" + 
					"	url_sunat_retencion = ? , \r\n" + 
					"	verificar_boletas = ? ;");
			pst.setString(1, sistema.getUrlServidor());
			pst.setString(2, sistema.getUrlProduccion());
			pst.setString(3, sistema.getUrlRemision());
			pst.setString(4, sistema.getUrlValidarCE());
			pst.setString(5, sistema.getUrlConsultaCDR());
			pst.setString(6, sistema.getUrlRetencion());
			pst.setInt(7, sistema.getValidarBoletas());
			k = pst.executeUpdate();
		}catch(SQLException e) {
			throw e;
		}
		finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		return k;
	}

	public void anularCabecera(String transaccion) throws SQLException {
		try(PreparedStatement pst = connection.prepareStatement("UPDATE sunat.cabdocumentos "
				+ "SET anulado = 1 WHERE transaccion = ?")){
			pst.setString(1, transaccion);
			pst.executeUpdate();
		}
	}
	
	public boolean validarSerieYNumero(String serie, String numero) throws SQLException {
		try(PreparedStatement pst = connection.prepareStatement("SELECT * FROM sunat.cabdocumentos "
				+ "WHERE serieelec = ? AND numeroelec = ? ;")){
			pst.setString(1, serie);
			pst.setString(2, numero);
			pst.executeQuery();
			
			pst.getResultSet().last();
			
			boolean b;
			if(pst.getResultSet().getRow() != 0)
				b = false;
			else
				b = true;
			
			return b;
		}
	}
	public boolean addCdrRechazo(String serie, String numero, String serie2, String numero2,
			FileInputStream cdr, File zip) throws SQLException {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("INSERT INTO sunat.cdr_rechazos"
					+ " SET eserie = ?, enumero = ?, oserie = ?, onumero = ?, cdr = ?;");
			pst.setString(1, serie);
			pst.setString(2, numero);
			pst.setString(3, serie2);
			pst.setString(4, numero2);
			pst.setBinaryStream(5, cdr, (int)zip.length());
			pst.execute();
			pst.close();
			
			try{cdr.close();}catch(Exception e1) {}
			
			return true;
		}catch(SQLException e) {
			throw e;
		}finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		
	}
	public void addResumenDiario(String fecha,String referencia, String correlativo, Lector lector, String ticket, Lector lector2) throws SQLException, FileNotFoundException {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("INSERT INTO sunat.resumenes_diarios "
					+ "SET fecha = ?, correlativo = ?, archivo = ?, nom_archivo = ?"
					+ " , ticket = ?, archivo_sunat = ?, nom_archivo_sunat = ?, fecha_referencia = ? , tipo = ?;");
			
			pst.setString(1, fecha);
			pst.setString(2, correlativo);
			pst.setBinaryStream(3, lector.getFileAsFileInputStream(), (int)lector.getFile().length());
			pst.setString(4, lector.getFile().getName());
			pst.setString(5, ticket);
			
			if(lector2.getFile() != null) {
				pst.setBinaryStream(6, lector2.getFileAsFileInputStream(), (int)lector.getFile().length());
				pst.setString(7, lector2.getFile().getName());
			}else {

				pst.setNull(6, Types.BLOB);
				pst.setString(7, null);
			}
			pst.setString(8, referencia);
			pst.setString(9, BeanManager.COD_RESUMEN_DIARIO);
			pst.execute();
			pst.close();
		}catch(SQLException e) {
			throw e;
		}finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
	}
	public void addResumen(String fecha,String referencia, String correlativo, 
			Lector lector, String ticket, Lector lector2,String tipo) throws SQLException, FileNotFoundException {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement("INSERT INTO sunat.resumenes_diarios "
					+ "SET fecha = ?, correlativo = ?, archivo = ?, nom_archivo = ?"
					+ " , ticket = ?, archivo_sunat = ?, nom_archivo_sunat = ?, fecha_referencia = ? , tipo = ?;");
			
			pst.setString(1, fecha);
			pst.setString(2, correlativo);
			pst.setBinaryStream(3, lector.getFileAsFileInputStream(), (int)lector.getFile().length());
			pst.setString(4, lector.getFile().getName());
			pst.setString(5, ticket);
			
			if(lector2.getFile() != null) {
				pst.setBinaryStream(6, lector2.getFileAsFileInputStream(), (int)lector.getFile().length());
				pst.setString(7, lector2.getFile().getName());
			}else {

				pst.setNull(6, Types.BLOB);
				pst.setString(7, null);
			}
			pst.setString(8, referencia);
			pst.setString(9, tipo);
			pst.execute();
			pst.close();
		}catch(SQLException e) {
			throw e;
		}finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
	}
	public void addResumen(ResumenDiario rd) throws SQLException, FileNotFoundException {
		PreparedStatement pst = null;
		try {
			pst = connection.prepareStatement(BeanManager.SQL_INSERT_RESUMEN);	
			BeanManager.insertResumenDiario(pst, rd);
			pst.close();
		}catch(SQLException e) {
			throw e;
		}finally {
			try {if(pst != null)pst.close();}catch(Exception e) {}
		}
		
		
	}
	
	public Connection getConnection() {return connection;}
	public void setConnection(Connection connection) {this.connection = connection;}
	
	public Sistema getVariablesSistema() throws SQLException {
		Sistema ret = null;
		Statement st = null;
		try {
			st = getConnection().createStatement();
			st.executeQuery("SELECT * FROM sunat.sistema; ");
			st.getResultSet().next();
			ret = BeanManager.getSistema(st.getResultSet());
			st.close();
		} catch (SQLException e) {
			try {st.close();}catch(Exception e1) {}
			throw e;
		}
		return ret;
	}
	
	
	
	
	
	
}
