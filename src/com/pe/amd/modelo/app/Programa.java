package com.pe.amd.modelo.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.pe.amd.modelo.app.in.Lector;
import com.pe.amd.modelo.app.out.Mensajero;
import com.pe.amd.modelo.beans.BeanManager;
import com.pe.amd.modelo.beans.Cabdocumentos;
import com.pe.amd.modelo.beans.Contingencia;
import com.pe.amd.modelo.beans.Correlacion;
import com.pe.amd.modelo.beans.Detdocumentos;
import com.pe.amd.modelo.beans.Empresa;
import com.pe.amd.modelo.beans.ResumenDiario;
import com.pe.amd.modelo.beans.Sistema;
import com.pe.amd.modelo.beans.Usuario;
import com.pe.amd.modelo.db.Consulta;

public class Programa {
	private Connection csunat;
	private Connection corigen;
	private Consulta consulta;
	private Generador generador;
	
	public static Sistema sistema = null;
	
	private boolean retorno;
	/**
	 * Crea el Objeto Programa para el manejo
	 * 
	 * @param csunat es la conexion que apunta a la base de datos del sistema
	 * @param origen es la conexion que apunta a la base de datos de origen
	 * @param retorno es el indicador si se desea devolver datos al usuario
	 *  , por ejemplo si se monta como un servicio debe ir false, y retornara nulos
	 *  , caso contrario retornara, en el caso de generarFacturas, una consulta con 
	 *  los documentos respectivos
	 * @throws Exception 
	 * 
	 */
	public Programa(Connection csunat, Connection corigen,boolean retorno) throws Exception {
		setCsunat(csunat);
		setCorigen(corigen);
		setRetorno(retorno);
		
		this.consulta = new Consulta(csunat);
		this.generador = new Generador(consulta);
		
		try {
			if(Programa.sistema == null)
				Programa.sistema = consulta.getVariablesSistema();
		}catch(Exception e) {
			Programa.sistema = null;
			this.close();
			throw e;
		}
	}
	//MIGRACIONES
	public List<Contingencia> migrarContingencia(Date fecha_migracion) throws SQLException {
		List<Contingencia> lista = null;
		try {
			getCsunat().setAutoCommit(false);
			lista = consulta.migrarContingencia(
					getCorigen(),this.getDateAsString(fecha_migracion));
			getCsunat().commit();
			getCsunat().setAutoCommit(true);
		}catch(Exception e) {
			getCsunat().rollback();
			throw e;
		}
		return lista;
	}
	
	public List<Cabdocumentos> migrarFacturas(Date fecha_migracion,boolean corregido) throws SQLException{
		List<Cabdocumentos> lista = null;
		try {
			getCsunat().setAutoCommit(false);
			lista = consulta.migrarFacturas(getCorigen(),this.getDateAsString(fecha_migracion),corregido);
			getCsunat().commit();
			getCsunat().setAutoCommit(true);
		}catch(Exception e) {
			getCsunat().rollback();
			throw e;
		}
		return lista;
		
	}
	public List<Cabdocumentos> migrarBoletas(Date fecha_migracion,boolean corregido) throws SQLException {
		List<Cabdocumentos> lista = null;
		try {
			getCsunat().setAutoCommit(false);
			lista = consulta.migrarBoletas(getCorigen(),this.getDateAsString(fecha_migracion),corregido);
			getCsunat().commit();
			getCsunat().setAutoCommit(true);
		}catch(Exception e) {
			getCsunat().rollback();
			throw e;
		}
		return lista;
		
	}
	public void migrarNotasCredito(Date fecha_migracion,boolean corregido) {}
	
	public void migrarNotasDebito(Date fecha_migracion,boolean corregido) {}
	
	//Generaciones
	/**
	 * Devuelve un arreglo de Objetos con la infromacion del archivo de contingencia
	 * ej:
	 * 	Object[0] -> contiene el nombre del archivo
	 * 	Object[1] -> contiene un array de bytes con la data del zip del contingencia
	 * 	Object[2] -> contiene el Mime del archivo
	 * Retorna nulo si es que se dio alguna excepcion
	 * 
	 * @param fecha
	 * @return
	 * @throws Exception 
	 * @throws NullPointerException
	 * @throws SQLException
	 * @throws IOException
	 */
	public Object[] generarContingencia(Date fecha_generacion) throws Exception {
		Object[] ret = null;
		try {
			ret = generador.generarContingencia(fecha_generacion);
		}catch (NullPointerException | SQLException | IOException e) {
			System.err.println("Error en la generacion del documento de contingencia..." 
				+ e.getMessage());
			throw e;
			
		}
		return ret;
	}
	public List<Cabdocumentos> generarFacturas(Date fecha_generacion) throws NullPointerException, SQLException, IOException, ParserConfigurationException, TransformerException{
		List<Cabdocumentos> documentos = null;
		try {
			generador.generarFacturas(this.getDateAsString(fecha_generacion));
			if(this.isRetorno())
				documentos = consulta.getDocumentos(this.getDateAsString(fecha_generacion)
						, BeanManager.COD_FACTURA, true);
		}catch(NullPointerException | SQLException 	| IOException 
				| ParserConfigurationException | TransformerException e) {
			System.err.println("Error en la generacion de Facturas...." + e.getMessage());
			throw e;
		}
		return documentos;
	}
	public List<Cabdocumentos> generarBoletas(Date fecha_generacion) throws NullPointerException, SQLException, IOException, ParserConfigurationException, TransformerException {
		List<Cabdocumentos> documentos = null;
		try {
			generador.generarBoletas(this.getDateAsString(fecha_generacion));
			if(this.isRetorno())
				documentos = consulta.getDocumentos(this.getDateAsString(fecha_generacion)
						, BeanManager.COD_BOLETA, true);
		}catch(NullPointerException | SQLException 	| IOException 
				| ParserConfigurationException | TransformerException e) {
			System.err.println("Error en la generacion de Boletas...." + e.getMessage());
			throw e;
		}
		return documentos;
	}
	public void generarNotasCredito(Date fecha_generacion) {}
	
	public void generarNotasDebito(Date fecha_migracion) {}
	
	/**
	 * Este metodo debe ser invocado una vez al día, por medio de seguridad, devuelve un arreglo de Objetos
	 * con la info del resumen diario
	 * ej:
	 * 	Object[0] -> El ticket generado
	 * 	Object[1] -> El xml del resumen diario
	 * 	Object[2] -> el nombre del archivo
	 * 	Object[3] -> su tipo MIME (ej. text/xml)
	 * 	Object[4] -> La respuesta de la SUNAT, se espera un xml
	 * 	Object[5] -> el nombre del archivo
	 * 	Object[6] -> su tipo MIME (ej. text/xml)
	 * 	Object[7] -> El correlativo actual
	 * @param fecha_datos -> Es la fecha de los datos a enviar en el resumen
	 * @return
	 * @throws Exception 
	 */
	public Object[] generarResumenDiario(Date fecha_datos) throws Exception {
		Object[] ret = null;
		try {
			ret = generador.generarResumenDiario(this.getDateAsString(fecha_datos));
		}catch(Exception e) {
			System.err.println("Error en la generacion del resumen diario....");
			throw e;
		}

		return ret;
	}
	/**
	 * Este metodo debe ser invocado una vez al día, por medio de seguridad, devuelve un arreglo de Objetos
	 * con la info del resumen diario
	 * ej:
	 * 	Object[0] -> El ticket generado
	 * 	Object[1] -> El xml del resumen diario
	 * 	Object[2] -> el nombre del archivo
	 * 	Object[3] -> su tipo MIME (ej. text/xml)
	 * 	Object[4] -> La respuesta de la SUNAT, se espera un xml
	 * 	Object[5] -> el nombre del archivo
	 * 	Object[6] -> su tipo MIME (ej. text/xml)
	 * 	Object[7] -> El correlativo actual
	 * @param fecha_datos -> Es la fecha de los datos a enviar en el resumen
	 * @return
	 * @throws Exception 
	 */
	public Object[] generarResumenDiario(String fecha_datos) throws Exception {
		Object[] ret = null;
		try {
			ret = generador.generarResumenDiario((fecha_datos));
		}catch(Exception e) {
			System.err.println("Error en la generacion del resumen diario....");
			throw e;
		}

		return ret;
	}
	
	/**
	 * Este metodo debe ser invocado una vez al día, por medio de seguridad, devuelve un arreglo de Objetos
	 * con la info del resumen diario
	 * ej:
	 * 	Object[0] -> El ticket generado
	 * 	Object[1] -> El xml del resumen diario
	 * 	Object[2] -> el nombre del archivo
	 * 	Object[3] -> su tipo MIME (ej. text/xml)
	 * 	Object[4] -> La respuesta de la SUNAT, se espera un xml
	 * 	Object[5] -> el nombre del archivo
	 * 	Object[6] -> su tipo MIME (ej. text/xml)
	 * 	Object[7] -> El correlativo actual
	 * @param fecha_datos -> Es la fecha de los datos a enviar en el resumen
	 * @return
	 * @throws Exception 
	 */
	public Object[] generarResumenBajas(String[] transacciones,List<String> razones) throws Exception{
		
		if(transacciones.length == 0)
			throw new IllegalArgumentException("Debe de haber al menos una transaccion");
		
		List<Cabdocumentos> datos = new ArrayList<>();
		
		for(int i = 0 ; i < transacciones.length ; i++) {
			HashMap<String,String> parametro = new HashMap<>();
			parametro.put("transaccion", transacciones[i]);
			datos.addAll(consulta.getDocumentos(parametro));
		}
		Object[] ret = null;
		try {
			ret = generador.generarResumenBajas(datos,razones);
		}catch(Exception e) {
			System.err.println("Error en la generacion del resumen diario....");
			throw e;
		}
		
		return ret;
	}
	/**
	 * Este metodo debe ser invocado una vez al día, por medio de seguridad, devuelve un arreglo de Objetos
	 * con la info del resumen diario
	 * ej:
	 * 	Object[0] -> El ticket generado
	 * 	Object[1] -> El xml del resumen diario
	 * 	Object[2] -> el nombre del archivo
	 * 	Object[3] -> su tipo MIME (ej. text/xml)
	 * 	Object[4] -> La respuesta de la SUNAT, se espera un xml
	 * 	Object[5] -> el nombre del archivo
	 * 	Object[6] -> su tipo MIME (ej. text/xml)
	 * 	Object[7] -> El correlativo actual
	 * @param fecha_datos -> Es la fecha de los datos a enviar en el resumen
	 * @return
	 * @throws Exception 
	 */
	public Object[] generarResumenBajas(List<Cabdocumentos> datos,List<String> razones) throws Exception{
		
		if(datos.size() == 0)
			throw new IllegalArgumentException("Debe de haber al menos un dato");
		Object[] ret = null;
		try {
			ret = generador.generarResumenBajas(datos,razones);
		}catch(Exception e) {
			System.err.println("Error en la generacion del resumen diario....");
			throw e;
		}
		
		return ret;
	}
	
	public List<Cabdocumentos> getDocumentos(HashMap<String,String> parametros) throws SQLException{	
		return consulta.getDocumentos(parametros);
	}
	public List<Cabdocumentos> getDocumentos(HashMap<String,String> parametros,List<String> orderBy) throws SQLException{	
		return consulta.getDocumentos(parametros,orderBy);
	}
	public List<Cabdocumentos> getDocumentos(String[] condiciones,String[] orderBy) throws SQLException{	
		return consulta.getDocumentos(condiciones,orderBy);
	}
	
	/**
	 * Devuelve los documentos en un rango determinado,
	 * si tipo es null se devuelven todos los documentos
	 * @param fechaInicio
	 * @param fechaFin
	 * @param tipo -> debe ser del tipo BeanManager
	 * @return
	 * @throws SQLException 
	 */
	public List<Cabdocumentos> getDocumentos(Date fechaInicio, Date fechaFin, Integer tipo) throws SQLException{
		return consulta.getDocumentos(this.getDateAsString(fechaInicio)
				,this.getDateAsString(fechaFin),tipo);
	}
	
	
	public Usuario loggin(String usr, String pass) throws SQLException {
		if(usr == null || pass == null)
			throw new NullPointerException("Identificador o Constraseña nulas");
		Usuario ret = null;
		ret = consulta.loggin(usr, Base64.getEncoder().encodeToString(pass.getBytes()));
		return ret;
	}
	
	public Empresa getDatosEmpresa() throws SQLException{
		Empresa empresa = null;
		empresa = consulta.getEmpresa();
		return empresa;
	}
	public void actualizarDatosEmpresa(HashMap<String,String> datos, InputStream content, long lenght) 
			throws SQLException, NullPointerException {
		try {
			consulta.updateEmpresa(datos, content, (int) lenght);
		}
		catch(SQLException | NullPointerException e) {
			System.err.println("Error en la actualizacion de datos de la emrpesa... "+ e.getMessage());
			throw e;
		}
		finally {
			try{ if(csunat != null)csunat.close(); } catch(Exception e) {}
		}
	}
	public List<Cabdocumentos> getDatosResumenenDiario(Date fecha_referencia) throws Exception {
		List<Cabdocumentos> ret = null;
		ret = consulta.getDocumentos(this.getDateAsString(fecha_referencia)
				, BeanManager.COD_BOLETA , true, true);
		return ret;
	}
	public List<Usuario> getUsuarios() throws SQLException {
		List<Usuario> ret = consulta.listarUsuarios();
		return ret;
	}
	public void crearUsuario(String ident, String pass, String dni, String correo, 
			String nombres, String apellidos, Integer rango) throws NullPointerException,NamingException,SQLException {
		try {
			new Consulta(csunat).crearUsuario(ident,Base64.getEncoder().encodeToString(pass.getBytes())
					,dni,correo,nombres,apellidos,rango);
		}
		catch(SQLException | NullPointerException e) {
			System.err.println("Entro en la creacion del Usuario: " + e.getMessage());
			throw e;
		}
	}
	/**
	 * Retorna el Estado de un resumen diario especifico com un array de Objetos
	 *  ej.
	 *  Object[0] -> String : con el mensaje de error, o Integer con el estado del documento (0,99,98,127)
	 *  Object[1] -> El archivo (xml -> fault-code, xml -> con estado 98 o zip con el cdr)
	 *  Object[2] -> el tipo mime
	 *  Object[3] -> Nombre del archivo
	 *  Object[4] -> Indica si hubo error en la BD o no (null si no hubo error)
	 * @param selectedTicket
	 * @param date
	 * @param correlativo}
	 * @param tipo : es el tipo de resumen, RA -> Bajas y RC -> Boletas
	 * @param refencia True si la fecha incluida es por referencia, false si la fecha es la de generacion
	 * @return
	 * @throws Exception
	 */
	public Object[] getStatus(String selectedTicket, Date date, 
			String correlativo,String tipo,boolean referencia) throws Exception {
		Object[] ret = null;
		try {
			ret = this.getStatus(selectedTicket, this.getDateAsString(date), correlativo,tipo ,referencia);
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		return ret;
	}
	/**
	 * Retorna el Estado de un resumen diario especifico com un array de Objetos
	 *  ej.
	 *  Object[0] -> String : con el mensaje de error, o Integer con el estado del documento (0,99,98,127)
	 *  Object[1] -> El archivo (xml -> fault-code, xml -> con estado 98 o zip con el cdr)
	 *  Object[2] -> el tipo mime
	 *  Object[3] -> Nombre del archivo
	 *  Object[4] -> Indica si hubo error en la BD o no (null si no hubo error)
	 * @param selectedTicket
	 * @param date
	 * @param correlativo
	 * @param tipo : es el tipo de resumen, RA -> Bajas y RC -> Boletas
	 * @param refencia True si la fecha incluida es por referencia, false si la fecha es la de generacion
	 * @return
	 * @throws Exception
	 */
	public Object[] getStatus(String selectedTicket, String date
				, String correlativo, String tipo,boolean referencia) throws Exception {
		Object[] ret = null;
		try {
			ret = new Object[5];
			Consulta consulta = new Consulta(csunat);
			Empresa emp = new Consulta(csunat).getEmpresa();
			
			Mensajero msj = new Mensajero(Programa.sistema.getUrlProduccion(),null);
			File in = null;
			int intentos = 10;
			while(intentos != 0) {
				try {
					in = msj.getStatus(selectedTicket,emp.getRuc(),emp.getUsrSecundario(),emp.getPass());
					break;
				}catch(Exception e) {intentos--;if(intentos == 0)throw e;}
			}
			
			Lector lector = new Lector(in);
			Object[] temporal = lector.decodeStatus();
			
			if(temporal == null) {
				ret[0] = lector.getMensaje();
				ret[1] = lector.getFileAsByteArray();
				ret[2] = "text/xml";
				ret[3] = "Fault-Response.xml";
			}
			else {
				if( (int) temporal[0] != 0 && (int) temporal[0] != 99 ) {
					ret[0] = String.valueOf((int)temporal[0]) + "-" + (String)temporal[1];
					ret[1] = lector.getFileAsByteArray();
					ret[2] = "text/xml";
					ret[3] = "Status-Response.xml";
				}else {
					ret[0] = (int)temporal[0] ;
					lector.decodeStatus((String)temporal[1],tipo+"-"+date);
					try {
						consulta.updateResumen((date), correlativo, 
								lector.getZip(),lector.getZipAsFileInputStream(),tipo,referencia);
					}catch(Exception e) {
						ret[4] = true;
					}
					lector.close();
					ret[1] = lector.getZipAsByteArray();
					ret[2] = "application/zip";
					ret[3] = "CDR.rar";
					
					try {
						ResumenDiario resumen = consulta.getResumen(date, tipo , correlativo, referencia).get(0);
						
						Lector decoder = new Lector((File)null);
						String[][] datos = decoder.decodeResumen(
								resumen.getArchivo().getBinaryStream(),tipo);
						
						HashMap<String,String> parametros = new HashMap<>();
						for(int i = 0 ;i < datos.length ; i++) {
							parametros.put("serieelec", datos[i][0]);
							parametros.put("numeroelec",datos[i][1]);
						}
						List<Cabdocumentos> lista = consulta.getDocumentos(parametros);
						
						if((int)temporal[0] == 0) {
							if(tipo.equals(BeanManager.COD_RESUMEN_DIARIO)) 
								for(int i = 0 ; i < lista.size(); i++) 
									consulta.updateCabecera(lista.get(i).getTransaccion(), 1);
							else if(tipo.equals(BeanManager.COD_RESUMEN_BAJA)) 
								for(int i = 0 ; i < lista.size() ; i++) 
									consulta.anularCabecera(lista.get(i).getTransaccion());
						}
						else {
							if(tipo.equals(BeanManager.COD_RESUMEN_DIARIO)) 
								for(int i = 0 ; i < lista.size(); i++) 
									consulta.updateCabecera(lista.get(i).getTransaccion(), 0);
							else if(tipo.equals(BeanManager.COD_RESUMEN_BAJA)) 
								for(int i = 0 ; i < lista.size() ; i++) 
									consulta.updateAnulado(lista.get(i).getTransaccion(),0);
						}
					}catch(Exception e) {
						ret[4] = true;
					}
					
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		return ret;
	}
	/**
	 * Retorna el Estado de un resumen diario especifico com un array de Objetos
	 *  ej.
	 *  Object[0] -> String : con el mensaje de error, o Integer con el estado del documento (0,99,98,127)
	 *  Object[1] -> El archivo (xml -> fault-code, xml -> con estado 98 o zip con el cdr)
	 *  Object[2] -> el tipo mime
	 *  Object[3] -> Nombre del archivo
	 *  Object[4] -> Indica si hubo error en la BD o no (null si no hubo error)
	 * @param selectedTicket
	 * @param date
	 * @param correlativo
	 * @return
	 * @throws Exception
	 */
	public Object[] getStatus(String selectedTicket, String tipo) throws Exception {
		Object[] ret = null;
		try {
			ret = new Object[5];
			Consulta consulta = new Consulta(csunat);
			Empresa emp = new Consulta(csunat).getEmpresa();
			
			Mensajero msj = new Mensajero(Programa.sistema.getUrlProduccion(),null);
			File in = null;
			int intentos = 10;
			while(intentos != 0) {
				try {
					in = msj.getStatus(selectedTicket,emp.getRuc(),emp.getUsrSecundario(),emp.getPass());
					break;
				}catch(Exception e) {intentos--;if(intentos == 0)throw e;}
			}
			
			Lector lector = new Lector(in);
			Object[] temporal = lector.decodeStatus();
			
			if(temporal == null) {
				ret[0] = lector.getMensaje();
				ret[1] = lector.getFileAsByteArray();
				ret[2] = "text/xml";
				ret[3] = "Fault-Response.xml";
			}
			else {
				if( (int) temporal[0] != 0 && (int) temporal[0] != 99 ) {
					ret[0] = String.valueOf((int)temporal[0]) + "-" + (String)temporal[1];
					ret[1] = lector.getFileAsByteArray();
					ret[2] = "text/xml";
					ret[3] = "Status-Response.xml";
				}else {
					ret[0] = (int)temporal[0] ;
					lector.decodeStatus((String)temporal[1],tipo);
					try {
						consulta.updateResumen(selectedTicket, 
								lector.getZip(),lector.getZipAsFileInputStream(),tipo);
					}catch(Exception e) {
						ret[4] = true;
					}
					lector.close();
					ret[1] = lector.getZipAsByteArray();
					ret[2] = "application/zip";
					ret[3] = "CDR.rar";
					

					try {
						ResumenDiario resumen = consulta.getResumen(selectedTicket, tipo).get(0);
						
						Lector decoder = new Lector((File)null);
						String[][] datos = decoder.decodeResumen(
								resumen.getArchivo().getBinaryStream(),tipo);
						
						HashMap<String,String> parametros = new HashMap<>();
						for(int i = 0 ;i < datos.length ; i++) {
							parametros.put("serieelec", datos[i][0]);
							parametros.put("numeroelec",datos[i][1]);
						}
						List<Cabdocumentos> lista = consulta.getDocumentos(parametros);
						
						if((int)temporal[0] == 0) {
							if(tipo.equals(BeanManager.COD_RESUMEN_DIARIO)) 
								for(int i = 0 ; i < lista.size(); i++) 
									consulta.updateCabecera(lista.get(i).getTransaccion(), 1);
							else if(tipo.equals(BeanManager.COD_RESUMEN_BAJA)) 
								for(int i = 0 ; i < lista.size() ; i++) 
									consulta.anularCabecera(lista.get(i).getTransaccion());
						}
						else {
							if(tipo.equals(BeanManager.COD_RESUMEN_DIARIO)) 
								for(int i = 0 ; i < lista.size(); i++) 
									consulta.updateCabecera(lista.get(i).getTransaccion(), 0);
							else if(tipo.equals(BeanManager.COD_RESUMEN_BAJA)) 
								for(int i = 0 ; i < lista.size() ; i++) 
									consulta.updateAnulado(lista.get(i).getTransaccion(),0);
						}
					}catch(Exception e) {
						ret[4] = true;
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		return ret;
	}
	public List<Detdocumentos> getDetalle(String transaccion) throws SQLException{
		return consulta.getDetalle(transaccion);
	}
	public List<ResumenDiario> getResumenesDiariosPorReferencia(Date fechaReferencia) throws SQLException{
		return consulta.getResumen(this.getDateAsString(fechaReferencia), BeanManager.COD_RESUMEN_DIARIO,true);
	}
	public List<ResumenDiario> getResumenesBajasPorReferencia(Date fechaReferencia) throws SQLException{
		return consulta.getResumen(this.getDateAsString(fechaReferencia), BeanManager.COD_RESUMEN_BAJA,true);
	}
	public List<ResumenDiario> getResumenesDiariosPorEmision(Date fechaGeneracion) throws SQLException{
		return consulta.getResumen(this.getDateAsString(fechaGeneracion), BeanManager.COD_RESUMEN_DIARIO,false);
	}
	public List<ResumenDiario> getResumenesBajasPorEmision(Date fechaGeneracion) throws SQLException{
		return consulta.getResumen(this.getDateAsString(fechaGeneracion), BeanManager.COD_RESUMEN_BAJA,false);
	}
	public Correlacion getCorrelacion(int cod) throws SQLException {
		return consulta.getCorrelacion(cod);
	}
	public int updateCorrelacion(Correlacion correlacion) throws SQLException {
		return consulta.updateCorrelacion(correlacion);
	}
	public Sistema getSistema() throws SQLException {
		return consulta.getVariablesSistema();
	}
	
	/**
	 * Marca un documento como anulado
	 * @param transaccion
	 * @param enviado
	 * @throws SQLException
	 */
	public void anularDocumento(String transaccion) throws SQLException {
		consulta.anularCabecera(transaccion);
		
	}
	public void anularDocumento(Cabdocumentos doc) throws SQLException {
		consulta.anularCabecera(doc.getTransaccion());
	}
	
	private String getDateAsString(Date fecha) {
		GregorianCalendar date = new GregorianCalendar();
		date.setTime(fecha);
			
		String anio = String.format("%04d", date.get(GregorianCalendar.YEAR)),
				mes = String.format("%02d", date.get(GregorianCalendar.MONTH)+1),
				dia = String.format("%02d", date.get(GregorianCalendar.DATE));
		return anio+mes+dia;
	}
	public void updateSistema() throws SQLException {
		consulta.updateSistema(Programa.sistema);
	}
	
	public void close() {
		try {this.getCsunat().close();} catch(Exception e) {}
		try {this.getCorigen().close();} catch(Exception e) {}
	}
	public Connection getCsunat() {
		return csunat;
	}
	public void setCsunat(Connection csunat) {
		this.csunat = csunat;
	}
	public Connection getCorigen() {
		return corigen;
	}
	public void setCorigen(Connection corigen) {
		this.corigen = corigen;
	}
	public boolean isRetorno() {
		return retorno;
	}
	public void setRetorno(boolean retorno) {
		this.retorno = retorno;
	}
}
