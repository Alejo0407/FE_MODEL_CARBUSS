package com.pe.amd.modelo.beans;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
public abstract class BeanManager {
	
	public final static String SQL_INSERT_USUARIO = "INSERT INTO sunat.usuarios (id, pass, dni,correo, nombres, apellidos, rango)VALUES (?,?,?,?,?,?,?);" ;
	public final static String SQL_INSERT_CONTINGENCIA = "INSERT INTO sunat.contingencia (periodo, motivo, fecemision, tipodocum, serie, numero, "
			+ "tipdocucli, numdocucli, nombrecli, valventagra, valventaexo, valventaina, ISC, IGV, otroscargos, "
			+ "totalcomp, Tipocompnota, seriemod, numeromod) VALUES (?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
	public final static String SQL_INSERT_DETALLE = "INSERT INTO sunat.detdocumentos (transaccion, sec, codigo, denominacion, unidad, "
			+ "valunitario, cantidad, igv, codigv, valtotal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	public final static String SQL_INSERT_CABECERA= "INSERT INTO sunat.cabdocumentos (transaccion, periodo, tipodocumento, serie, "
			+ "numero, fechaemision, fechavencimiento, tipocliente, numcliente, nomcliente, direccion, departamento, provincia, distrito, email, "
			+ "valventaafe, valventaina, valventaexo, ISC, codisc, IGV, codigv, OTROS, totaldoc, Tipodocmod, serieno, numerono, serieelec, numeroelec, "
			+ "homologado, fechomologado, archivo, nom_archivo,mensaje_homologado,"
			+ " archivo_homologado,nom_archivo_homologado,resumen,anulado) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
			+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?,?,?,?,?);";
	public final static String SQL_INSERT_CORRELACION = "INSERT INTO sunat.correlacion (tipo_doc,serie,correlativo) VALUES  (?,?,?);";
	public final static String SQL_INSERT_CORRELACION_CONTINGENCIA = "INSERT INTO sunat.correlacioncontingencia (fecha,correlativo,archivo,nom_archivo) "
			+ "VALUES (?,?,?,?)";
	public final static String SQL_INSERT_RESUMEN ="INSERT INTO sunat.resumenes_diarios " + 
			"	(fecha,correlativo,archivo, " + 
			"	nom_archivo,ticket,archivo_sunat,nom_archivo_sunat,tipo ) " +
			" VALUES (?,?,?,?,?,?,?,?);";
	
	
	
	public final static int COD_FACTURA = 1;
	public final static int COD_BOLETA = 3;
	public final static int COD_NOTA_CREDITO = 7;
	public final static int COD_NOTA_DEBITO= 8;
	public final static int COD_GUIA_REMISION= 9;
	public final static int COD_COMPROBANTE_PERCEPCION = 40;
	public final static int COD_COMPROBANTE_RETENCION = 20;
	public final static int COD_COMUNICACION_BAJA = 0;
	
	public final static String COD_RESUMEN_DIARIO = "RC";
	public final static String COD_RESUMEN_BAJA = "RA";
	
	public static Usuario getUsuario( ResultSet tabla ) throws SQLException {
		Usuario usr = new Usuario();
		usr.setRango(tabla.getInt("rango"));
		usr.setApellidos(tabla.getString("apellidos"));
		usr.setCorreo(tabla.getString("correo"));
		usr.setDni(tabla.getString("dni"));
		usr.setId(tabla.getString("id"));
		usr.setNombres(tabla.getString("nombres"));
		usr.setPass(tabla.getString("pass"));
		return usr;
	}
	
	public static ResumenDiario getResumenDiario(ResultSet tabla) throws SQLException {
		ResumenDiario rd = new ResumenDiario();
		rd.setArchivo(tabla.getBlob("archivo"));
		rd.setArhivo_sunat(tabla.getBlob("archivo_sunat"));
		rd.setCorrelativo(tabla.getString("correlativo"));
		rd.setFecha(tabla.getString("fecha"));
		rd.setNom_archivo(tabla.getString("nom_archivo"));
		rd.setNom_archivo_sunat(tabla.getString("nom_archivo_sunat"));
		rd.setTicket(tabla.getString("ticket"));
		rd.setFecha_referencia(tabla.getString("fecha_referencia"));
		rd.setTipo(tabla.getString("tipo"));
		return rd;
	}
	
	public static Empresa getEmpresa(ResultSet tabla) throws SQLException{
		Empresa emp = Empresa.getEmpresa();
		emp.setDepartamento(tabla.getString("departamento"));
		emp.setDireccion(tabla.getString("direccion"));
		emp.setDistrito(tabla.getString("distrito"));
		emp.setFax(tabla.getString("fax"));
		emp.setMail_empresa(tabla.getString("mail_empresa"));
		emp.setNombre(tabla.getString("nombre"));
		emp.setNombre_comercial(tabla.getString("nombre_comercial"));
		emp.setProvincia(tabla.getString("provincia"));
		emp.setRuc(tabla.getString("ruc"));
		emp.setTelefono(tabla.getString("telefono"));
		emp.setUbigeo(tabla.getString("ubigeo"));
		emp.setUrbanizacion(tabla.getString("urbanizacion"));
		emp.setWeb(tabla.getString("web"));	
		emp.setCe(tabla.getBlob("ce"));
		emp.setNce(tabla.getString("nce"));
		emp.setPin(tabla.getString("pin"));
		emp.setPin_revocar(tabla.getString("pin_revocar"));
		emp.setAlias(tabla.getString("alias"));
		emp.setUsrSecundario(tabla.getString("usr_secundario"));
		emp.setPass(tabla.getString("pass"));
		return emp;
	}
	
	public static Correlacion getCorrelacion(ResultSet tabla) throws SQLException {
		Correlacion doc = new Correlacion();
		
		doc.setCorrelativo(tabla.getInt("correlativo"));
		doc.setSerie(tabla.getString("serie"));
		doc.setTipo_doc(tabla.getInt("tipo_doc"));
		
		return doc;
	}
	
	public static Contingencia getContingencia(ResultSet tabla) throws SQLException {
		Contingencia cont = new Contingencia();
		cont.setFecemision(tabla.getString("fecemision"));
		cont.setIgv(tabla.getDouble("igv"));
		cont.setIsc(tabla.getDouble("isc"));
		cont.setMotivo(tabla.getString("motivo"));
		cont.setNombrecli(tabla.getString("nombrecli"));
		cont.setNumdocucli(tabla.getString("numdocucli"));
		cont.setNumero(tabla.getString("numero"));
		cont.setNumeromod(tabla.getString("numeromod"));
		cont.setOtroscargos(tabla.getDouble("otroscargos"));
		cont.setPeriodo(tabla.getString("periodo"));
		cont.setSerie(tabla.getString("serie"));
		cont.setSeriemod(tabla.getString("seriemod"));
		cont.setTipocompnota(tabla.getString("tipocompnota"));
		cont.setTipodocucli(tabla.getString("tipdocucli"));
		cont.setTipodocum(tabla.getString("tipodocum"));
		cont.setTotalcomp(tabla.getDouble("totalcomp"));
		cont.setValventaexo(tabla.getDouble("valventaexo"));
		cont.setValventagra(tabla.getDouble("valventagra"));
		cont.setValventaina(tabla.getDouble("valventaina"));
		return cont;
	}
	
	public static CorrelacionContingencia getCorrelacionContingencia(ResultSet tabla) throws SQLException {
		CorrelacionContingencia cor = new CorrelacionContingencia();
		
		cor.setArchivo(tabla.getBlob("archivo"));
		cor.setCorrelativo(tabla.getInt("correlativo"));
		cor.setFecha(tabla.getString("fecha"));
		cor.setNom_archivo(tabla.getString("nom_archivo"));
		
		return cor;
	}
	
	public static Detdocumentos getDetdocumentos(ResultSet tabla) throws SQLException{
		Detdocumentos det = new Detdocumentos();
		
		det.setCantidad(tabla.getDouble("cantidad"));
		det.setCodigo(tabla.getString("codigo"));
		det.setCodigv(tabla.getString("codigv"));
		det.setDenominacion(tabla.getString("denominacion"));
		det.setIgv(tabla.getDouble("igv"));
		det.setSec(tabla.getString("sec"));
		det.setTransaccion(tabla.getString("transaccion"));
		det.setUnidad(tabla.getString("unidad"));
		det.setValtotal(tabla.getDouble("valtotal"));
		det.setValunitario(tabla.getDouble("valunitario"));
		
		return det;
	}
	
	public static Cabdocumentos getCabdocumentos(ResultSet tabla) throws SQLException {
		Cabdocumentos cab = new Cabdocumentos();
		
		cab.setTransaccion(tabla.getString("transaccion"));
		cab.setPeriodo(tabla.getString("periodo"));
		cab.setTipodocumento(tabla.getString("tipodocumento"));
		cab.setSerie(tabla.getString("serie"));
		cab.setNumero(tabla.getString("numero"));
		cab.setFechaemision(tabla.getString("fechaemision"));
		cab.setFechavencimiento(tabla.getString("fechavencimiento"));
		cab.setTipocliente(tabla.getString("tipocliente"));
		cab.setNumcliente(tabla.getString("numcliente"));
		cab.setNomcliente(tabla.getString("nomcliente"));
		cab.setDireccion(tabla.getString("direccion"));
		cab.setDepartamento(tabla.getString("departamento"));
		cab.setProvincia(tabla.getString("provincia"));
		cab.setDistrito(tabla.getString("distrito"));
		cab.setEmail(tabla.getString("email"));
		cab.setValventaafe(tabla.getDouble("valventaafe"));
		cab.setValventaina(tabla.getDouble("valventaina"));
		cab.setValventaexo(tabla.getDouble("valventaexo"));
		cab.setIsc(tabla.getDouble("isc"));
		cab.setCodisc(tabla.getString("codisc"));
		cab.setIgv(tabla.getDouble("igv"));
		cab.setCodigv(tabla.getString("codigv"));
		cab.setOtros(tabla.getDouble("otros"));
		cab.setTotaldoc(tabla.getDouble("totaldoc"));
		cab.setTipodocmod(tabla.getString("tipodocmod"));
		cab.setSerieno(tabla.getString("serieno"));
		cab.setNumerono(tabla.getString("numerono"));
		cab.setSerieelec(tabla.getString("serieelec"));
		cab.setNumeroelec(tabla.getString("numeroelec"));
		cab.setHomologado(tabla.getInt("homologado"));
		cab.setFechahomologado(tabla.getString("fechomologado"));
		cab.setArchivo(tabla.getBlob("archivo"));
		cab.setNom_archivo(tabla.getString("nom_archivo"));
		cab.setMensaje_homologado(tabla.getString("mensaje_homologado"));
		cab.setArchivo_homologado(tabla.getBlob("archivo_homologado"));
		cab.setNom_archivo_homologado(tabla.getString("nom_archivo_homologado"));
		cab.setResumen(tabla.getInt("resumen"));
		cab.setAnulado(tabla.getInt("anulado"));
		return cab;
	}

	public static Sistema getSistema(ResultSet tabla) throws SQLException{
		Sistema sistema = Sistema.getSistema();
		
		sistema.setUrlServidor(tabla.getString("direccion"));
		sistema.setUrlProduccion(tabla.getString("url_sunat_produccion"));
		sistema.setUrlRemision(tabla.getString("url_sunat_remision"));
		sistema.setUrlRetencion(tabla.getString("url_sunat_retencion"));
		sistema.setUrlValidarCE(tabla.getString("url_sunat_validez_ce"));
		sistema.setUrlConsultaCDR(tabla.getString("url_sunat_cdr"));
		sistema.setValidarBoletas(tabla.getInt("verificar_boletas"));
		
		return sistema;
	}
	public static int insertUsuario(PreparedStatement pst, Usuario usr) throws SQLException {
		
		pst.setString(1, usr.getId());
		pst.setString(2, usr.getPass());
		pst.setString(3, usr.getDni());
		pst.setString(4, usr.getCorreo());
		pst.setString(5, usr.getNombres());
		pst.setString(6, usr.getApellidos());
		if(usr.getRango() == null) pst.setNull(7, Types.INTEGER);
		else pst.setInt(7, usr.getRango());
		
		return pst.executeUpdate();
	}
	
	public static int insertCorrelacion(PreparedStatement pst, Correlacion cor) throws SQLException {
		pst.setInt(1, cor.getTipo_doc());
		pst.setString(2, cor.getSerie());
		pst.setInt(3, cor.getCorrelativo());
		
		return pst.executeUpdate();
	}
	
	public static int insertCorrelacionContingencia(PreparedStatement pst, CorrelacionContingencia cor) throws SQLException{
		pst.setString(1, cor.getFecha());
		pst.setInt(2, cor.getCorrelativo());
		if(cor.getArchivo() == null) pst.setNull(3, Types.BLOB);
		else  pst.setBlob(3, cor.getArchivo());
		pst.setString(4, cor.getNom_archivo());
		
		return pst.executeUpdate();
	}
	
	public static int insertContingencia(PreparedStatement pst, Contingencia cont) throws SQLException {
		
		int i = 1;
		pst.setString(i, cont.getPeriodo());i++;
		pst.setString(i, cont.getMotivo());i++;
		pst.setString(i, cont.getFecemision());i++;
		pst.setString(i, cont.getTipodocum());i++;
		pst.setString(i, cont.getSerie());i++;
		pst.setString(i, cont.getNumero());i++;
		pst.setString(i, cont.getTipodocucli());i++;
		pst.setString(i, cont.getNumdocucli());i++;
		pst.setString(i, cont.getNombrecli());i++;
		
		if(cont.getValventagra() == null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cont.getValventagra() );
		i++;
		if(cont.getValventaexo() == null)pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cont.getValventaexo());
		i++;
		if(cont.getValventaina() == null)pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cont.getValventaina());
		i++;
		if(cont.getIsc() == null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cont.getIsc() );
		i++;
		if(cont.getIgv() == null)pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cont.getIgv());
		i++;
		if(cont.getOtroscargos() == null)pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cont.getOtroscargos());
		i++;
		if(cont.getTotalcomp() == null)pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cont.getTotalcomp());
		i++;
		pst.setString(i, cont.getTipocompnota());i++;
		pst.setString(i, cont.getSeriemod());i++;
		pst.setString(i, cont.getNumeromod());i++;
		
		return pst.executeUpdate();
	}
	
	public static int insertDetalle(PreparedStatement pst, Detdocumentos det) throws SQLException {
		
		int i = 1;
		pst.setString(i, det.getTransaccion());i++;
		pst.setString(i, det.getSec());i++;
		pst.setString(i, det.getCodigo());i++;
		pst.setString(i, det.getDenominacion());i++;
		pst.setString(i, det.getUnidad());i++;
		if(det.getValunitario() == null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, det.getValunitario());
		i++;
		if(det.getCantidad()== null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, det.getCantidad());
		i++;
		if(det.getIgv() == null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, det.getIgv());
		i++;
		pst.setString(i, det.getCodigv());i++;
		if(det.getValtotal() == null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, det.getValtotal());
		
		return pst.executeUpdate();
	}
	
	public static int insertCabecera(PreparedStatement pst, Cabdocumentos cab) throws SQLException{
		int i = 1;
		
		pst.setString(i, cab.getTransaccion());i++;
		pst.setString(i, cab.getPeriodo());i++;
		pst.setString(i, cab.getTipodocumento());i++;
		pst.setString(i, cab.getSerie());i++;
		pst.setString(i, cab.getNumero());i++;
		pst.setString(i, cab.getFechaemision());i++;
		pst.setString(i, cab.getFechavencimiento());i++;
		pst.setString(i, cab.getTipocliente());i++;
		pst.setString(i, cab.getNumcliente());i++;
		pst.setString(i, cab.getNomcliente());i++;
		pst.setString(i, cab.getDireccion());i++;
		pst.setString(i, cab.getDepartamento());i++;
		pst.setString(i, cab.getProvincia());i++;
		pst.setString(i, cab.getDistrito());i++;
		pst.setString(i, cab.getEmail());i++;
		if(cab.getValventaafe() == null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cab.getValventaafe());
		i++;
		if(cab.getValventaina() == null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cab.getValventaina());
		i++;
		if(cab.getValventaexo() == null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cab.getValventaexo());
		i++;
		if(cab.getIsc()== null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cab.getIsc());
		i++;
		pst.setString(i, cab.getCodisc());i++;
		if(cab.getIgv()== null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cab.getIgv());
		i++;
		pst.setString(i, cab.getCodigv());i++;
		if(cab.getOtros() == null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cab.getOtros());
		i++;
		if(cab.getTotaldoc() == null) pst.setNull(i, Types.DECIMAL);
		else pst.setDouble(i, cab.getTotaldoc());
		i++;
		pst.setString(i, cab.getTipodocmod());i++;
		pst.setString(i, cab.getSerieno());i++;
		pst.setString(i, cab.getNumerono());i++;
		pst.setString(i, cab.getSerieelec());i++;
		pst.setString(i, cab.getNumeroelec());i++;
		if(cab.getHomologado() == null) pst.setNull(i, Types.DECIMAL);
		else pst.setInt(i, cab.getHomologado());
		i++;
		pst.setString(i, cab.getFechahomologado());i++;
		if(cab.getArchivo() == null)pst.setNull(i, Types.BLOB);
		else pst.setBlob(i, cab.getArchivo());
		i++;
	
		pst.setString(i, cab.getNombre_archivo());i++;
		pst.setString(i, cab.getMensaje_homologado());i++;
		if(cab.getArchivo_homologado() == null)pst.setNull(i, Types.BLOB);
		else pst.setBlob(i, cab.getArchivo_homologado());
		i++;
		pst.setString(i, cab.getNom_archivo_homologado());i++;
		if(cab.getResumen() == null)pst.setInt(i, 0);
		else pst.setInt(i, cab.getResumen());
		i++;
		if(cab.getAnulado() == null) pst.setInt(i, 0);
		else pst.setInt(i, cab.getAnulado());
		
		return pst.executeUpdate();
	}
	public static int insertResumenDiario(PreparedStatement pst, ResumenDiario rd) throws SQLException {
		int i = 1;
		pst.setString(i, rd.getFecha());i++;
		pst.setString(i, rd.getCorrelativo());i++;
		if(rd.getArchivo() == null) pst.setNull(i, Types.BLOB);
		else pst.setBlob(i, rd.getArchivo());
		i++;
		pst.setString(i, rd.getNom_archivo());i++;
		pst.setString(i, rd.getTicket());i++;
		if(rd.getArhivo_sunat() == null) pst.setNull(i, Types.BLOB);
		else pst.setBlob(i, rd.getArhivo_sunat());
		i++;
		pst.setString(i, rd.getNom_archivo_sunat());i++;
		pst.setString(i, rd.getTipo());
		return pst.executeUpdate();
	}
}
