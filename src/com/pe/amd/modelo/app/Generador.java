package com.pe.amd.modelo.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.pe.amd.modelo.app.in.Lector;
import com.pe.amd.modelo.app.out.Compresor;
import com.pe.amd.modelo.app.out.Escritor;
import com.pe.amd.modelo.app.out.Mensajero;
import com.pe.amd.modelo.app.out.URLSunat;
import com.pe.amd.modelo.app.xml.XMLFactory;
import com.pe.amd.modelo.app.xml.XMLDocument;
import com.pe.amd.modelo.beans.BeanManager;
import com.pe.amd.modelo.beans.Cabdocumentos;
import com.pe.amd.modelo.beans.Contingencia;
import com.pe.amd.modelo.beans.Correlacion;
import com.pe.amd.modelo.beans.CorrelacionContingencia;
import com.pe.amd.modelo.beans.Detdocumentos;
import com.pe.amd.modelo.beans.Empresa;
import com.pe.amd.modelo.db.Consulta;

class Generador {
	private Consulta consulta;
	
	public Generador(Consulta consulta) {
		super();
		this.consulta = consulta;
	}
	
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
	 * @throws NullPointerException
	 * @throws SQLException
	 * @throws IOException
	 */
	public Object[] generarContingencia(Date fecha) throws NullPointerException, SQLException, IOException{
		if(fecha == null)
			throw new NullPointerException("Error en la fecha... null");
		GregorianCalendar date = new GregorianCalendar();
		date.setTime(fecha);
		
		String anio = String.format("%04d", date.get(GregorianCalendar.YEAR)),
				mes = String.format("%02d", date.get(GregorianCalendar.MONTH)+1),
				dia = String.format("%02d", date.get(GregorianCalendar.DATE));
		
		List<Contingencia> datos = consulta.getContingencia(anio+mes+dia);
		Empresa empresa = consulta.getEmpresa();
		CorrelacionContingencia correlacion = consulta.getCorrelacionContingencia(anio+mes+dia);
		Object[] info = null;
		try {
			
			if(datos.size() != 0) {
				String nombre = empresa.getRuc() + "-" + "RF" + "-" + dia+mes+anio+"-"+ (correlacion.getCorrelativo()+1);
				ArrayList<String> lineas = new ArrayList<>();
				DecimalFormat df = new DecimalFormat("#.00");
				
				for(Contingencia documento :datos) {
					lineas.add(documento.getMotivo()+"|"+(dia+"/"+mes+"/"+anio) + "|" + documento.getTipodocum() 
						+	"|" + documento.getSerie() + "|" + documento.getNumero() + "||" + documento.getTipodocucli()
						+ "|" + documento.getNumdocucli() + "|" + documento.getNombrecli() 
						+ "|" + ( documento.getValventagra()==null? "0.00":df.format(documento.getValventagra().doubleValue()) )
						+ "|" + ( documento.getValventaexo()==null? "0.00":df.format(documento.getValventaexo().doubleValue()) )
						+ "|" + ( documento.getValventaina()==null? "0.00":df.format(documento.getValventaina().doubleValue()) )
						+ "|" + ( documento.getIsc()==null? "0.00":df.format(documento.getIsc().doubleValue()) )
						+ "|" + ( documento.getIgv()==null? "0.00":df.format(documento.getIgv().doubleValue()) )
						+ "|" + ( documento.getOtroscargos()==null? "0.00":df.format(documento.getOtroscargos().doubleValue()) )
						+ "|" + ( documento.getTotalcomp()==null? "0.00":df.format(documento.getTotalcomp().doubleValue()) )
						+"||||"
						);
				}
				//GUARDADO DE INFORMACION
				File f = new File(nombre + ".txt");
				Escritor e = new Escritor(nombre +".txt",false);
				e.escribir(lineas);
				e.cerrar();
				Compresor c = new Compresor();
				c.comprimir(nombre+".txt",nombre);
				f.delete();
				f = new File(nombre+".zip");
				
				Lector in = new Lector(f);
				
				FileInputStream file =  in.getFileAsFileInputStream();
				System.out.println("RESULTADO: "+ consulta.updateCorrelacionContingencia(anio+mes+dia, file, f));
				file.close();
				
				info = new Object[3];
				info[0] = nombre;
				info[1] =  in.getFileAsByteArray();
				info[2] = "application/zip";
				
				in.deleteFiles();
			}
		}catch(NullPointerException e) {
			throw e;
		}
		return info;
	}
	
	/**
	 * 
	 * @param fecha
	 * @throws NullPointerException
	 * @throws SQLException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public boolean generarFacturas(String fecha) throws NullPointerException, SQLException,
			IOException, ParserConfigurationException, TransformerException {
		if(fecha == null)
			throw new NullPointerException("Error en la fecha... null");
		
		//Si no hubo excepcion en produccion es true, si no es false
		boolean retorno = true; 
		List<Cabdocumentos> cabecera = consulta.getDocumentos(fecha,BeanManager.COD_FACTURA,false);
		Empresa empresa = consulta.getEmpresa();
		Correlacion cor = consulta.getCorrelacion(BeanManager.COD_FACTURA);
		
		try {
			if(cabecera.size() != 0) {
				String nombre;
				List<Detdocumentos> det;
				XMLDocument xml;
				Lector in = new Lector((String)null);
				
				System.out.println("Generando...");
				
				for(Cabdocumentos cab:cabecera) {
					cor.aumentarCorrelacion();
					nombre = empresa.getRuc()+ "-01-F" +  cor.getSerie() + "-" + String.valueOf(cor.getCorrelativo());
					det = consulta.getDetalle(cab.getTransaccion());
					
					xml = XMLFactory.getXMLFacturaBoleta(cab,det,empresa,
							nombre, cor.getSerie(), String.valueOf(cor.getCorrelativo()),
							BeanManager.COD_FACTURA);
					
					File archivo = xml.generarDocumento();
					
					
					//Aqui se da el envio de documentos para el caso de las facturas
					Compresor comp = new Compresor();
					comp.comprimir(archivo.getName(), nombre);
					
					Mensajero msj = new Mensajero(Programa.sistema.getUrlProduccion(),new File(nombre+".zip"));
					File respuesta = null; //CONTENDRA EL XML QUE DEFINE LA RESPUESTA DE LA SUNAT AL ENVIO DE DOCUMENTOS
					int ans = 0;//sin enviar a sunat
					int counter = 10;
					
					while(counter != 0) {
						try {
							respuesta = msj.enviar(empresa.getRuc(), 
									empresa.getUsrSecundario(), empresa.getPass());
							in = new Lector(respuesta);
							ans = in.decodeRespuesta(nombre);//Primer Filtro
							if(ans == -1 ) //Error en Produccion
								cor.disminuirCorrelacion();
							counter = 0;
							
						}catch(Exception e) {
							counter--;
							//Significa que no se pudo enviar el documento
							//Se para el análisis, hay un riezgo que de continuar la numeracion tenga
							//un error de excepcion
							if(counter == 0) {
								throw new NullPointerException("El servicio de recepcion de la sunat para facturas "
										+ "ha fallado, la informacion del dia de generacion serán restaurada. Error-code:"
										+e.getMessage());
							}
						}
					}
					Lector l1;
					if(ans != -1) {//Archivo aceptado o con Observaciones o Rechazados (1 / -2)
						l1= new Lector(archivo);
						String serie = "F"+cor.getSerie();
						String numero = String.valueOf(cor.getCorrelativo());
						
						while(!consulta.validarSerieYNumero(serie, numero)) {
							cor.aumentarCorrelacion();
							serie = "F"+cor.getSerie();
							numero = String.valueOf(cor.getCorrelativo());
						}
						consulta.updateCabecera(
								"F"+cor.getSerie(),String.valueOf(cor.getCorrelativo()), //SERIE - NUMERO
								BeanManager.COD_FACTURA, cab.getTransaccion(),  //FACTURA - TRANSACCION
								l1.getFileAsFileInputStream(), archivo, //XML-FACTURA -> File xml
								ans, in.getMensaje(), //Tipo Homologado - Mensaje de sunat
								in.getZipAsFileInputStream(),in.getZip()); // XMl respuesta o zip con el cdr y su file respectivo
						if(ans == -2) {//rechazado
							consulta.addCdrRechazo("F"+cor.getSerie(),String.valueOf(cor.getCorrelativo()),
									cab.getSerie(),cab.getNumero(),in.getZipAsFileInputStream(),in.getZip());
						}
						
					}
					else{//Excepcion en produccion
						retorno = false;
						l1 = new Lector(archivo);
						consulta.updateCabecera(
								null,null, //SERIE - NUMERO
								BeanManager.COD_FACTURA, cab.getTransaccion(),  //FACTURA - TRANSACCION
								l1.getFileAsFileInputStream(), archivo, //XML-FACTURA -> File xml
								ans, in.getMensaje(), //Tipo Homologado - Mensaje de sunat
								in.getFileAsFileInputStream(),in.getFile()); // XMl respuesta o zip con el cdr y su file respectivo
					}
					
					in.close();
					l1.close();
					
					archivo.delete();
					new File(nombre+".zip").delete();
					
					if(in.getFile() != null) {in.getFile().delete();in.setFile(null);}
					if(in.getZip() != null) {in.getZip().delete();in.setZip(null);}
					
					String respuesta1 = "";
					if(ans == 1)
						respuesta1 = "Aceptado::"+"F"+cor.getSerie()+"-"+cor.getCorrelativo();
					else if(ans == -1)
						respuesta1 = "Error en produccion::"+ in.getMensaje();
					else if(ans == -2)
						respuesta1 = "Rechazado::"+"F"+cor.getSerie()+"-"+cor.getCorrelativo() + "::"+in.getMensaje();
					
					System.out.println("Generando Factura: " + cab.getSerie()+"-"+cab.getNumero()+"::"
							+respuesta1) ;
					consulta.updateCorrelacion(cor);
				}
				System.out.println("Fin Generando...");
			}
			
		}catch(NullPointerException | SQLException e) {
			throw e;
		}
		return retorno;
	}
	public boolean generarBoletas(String fecha) throws NullPointerException, SQLException,
		IOException, ParserConfigurationException, TransformerException{
		
		//Si no hubo excepcion en produccion es true, si no es false
		boolean retorno = true;
		List<Cabdocumentos> cabecera = consulta.getDocumentos(fecha,BeanManager.COD_BOLETA,false);
		Empresa empresa = consulta.getEmpresa();
		Correlacion cor = consulta.getCorrelacion(BeanManager.COD_BOLETA);
		
		try {
			if(cabecera.size() != 0) {
				String nombre;
				List<Detdocumentos> det;
				XMLDocument xml;
				Lector in = new Lector((String)null);
				boolean actualizar_correlacion = true;
				
				for(Cabdocumentos cab:cabecera) {
					//Genero
					cor.aumentarCorrelacion();
					
					nombre = empresa.getRuc()+ "-03-B" +  cor.getSerie() + "-" + String.valueOf(cor.getCorrelativo());
					det = consulta.getDetalle(cab.getTransaccion());
					
					xml = XMLFactory.getXMLFacturaBoleta(cab,det,empresa,
							nombre, cor.getSerie(), String.valueOf(cor.getCorrelativo()),
							BeanManager.COD_BOLETA);
					
					File archivo = xml.generarDocumento();
					
					//Aqui se da el envio de documentos para el caso de las facturas
					Compresor comp = new Compresor();
					comp.comprimir(archivo.getName(), nombre);
					
					int ans = 1;//sin enviar a sunat
					String respuesta1 = "";
					Lector l1 = null;
					if(Programa.sistema.getValidarBoletas() == 1) {
						Mensajero msj = new Mensajero(URLSunat.BetaFactura.getValor(),new File(nombre+".zip"));
						File respuesta = null; //CONTENDRA EL XML QUE DEFINE LA RESPUESTA DE LA SUNAT AL ENVIO DE DOCUMENTOS
						int counter = 10;
						while(counter != 0) {
							try {
								respuesta = msj.enviar(empresa.getRuc(), 
										empresa.getUsrSecundario(), empresa.getPass());
								in = new Lector(respuesta);
								ans = in.decodeRespuesta(nombre);//Primer Filtro
								if(ans == -1 || ans == -2) {//Error en Produccion o Error de Rechazo
									actualizar_correlacion = false; //Erro+r en produccion = La serie no se acepta
									cor.disminuirCorrelacion();
								}
								break;
							}catch(Exception e) {
								counter--;
								if(counter == 0) {
									throw new NullPointerException("El servicio de recepcion de la sunat para Boletas "
											+ "ha fallado, la informacion del dia de generacion serán restaurada. Error-code:"
											+e.getMessage());
								}
							}
						}
					}
					if(ans == 1) {//Archivo aceptado o con Observaciones o Rechazados (1 / -2)
						l1= new Lector(archivo);
						String serie = (actualizar_correlacion?"B"+cor.getSerie():null);
						String numero = (actualizar_correlacion?String.valueOf(cor.getCorrelativo()):null);
						
						if(actualizar_correlacion) {
							while(!consulta.validarSerieYNumero(serie, numero)) {
								cor.aumentarCorrelacion();
								serie = "B"+cor.getSerie();
								numero = String.valueOf(cor.getCorrelativo());
							}
						}
						if(Programa.sistema.getValidarBoletas() == 1)
							consulta.updateCabecera(
									serie,numero, //SERIE - NUMERO
									BeanManager.COD_BOLETA, cab.getTransaccion(),  //FACTURA - TRANSACCION
									l1.getFileAsFileInputStream(), archivo, //XML-FACTURA -> File xml
									ans, in.getMensaje(), //Tipo Homologado - Mensaje de sunat
									in.getZipAsFileInputStream(),in.getZip()); // XMl respuesta o zip con el cdr y su file respectivo
						else
							consulta.updateCabecera(
									"B"+cor.getSerie(),String.valueOf(cor.getCorrelativo()), //SERIE - NUMERO
									BeanManager.COD_BOLETA, cab.getTransaccion(),  //FACTURA - TRANSACCION
									l1.getFileAsFileInputStream(), archivo, //XML-FACTURA -> File xml
									1, null, //Tipo Homologado - Mensaje de sunat
									null,null); // XMl respuesta o zip con el cdr y su file respectivo
						respuesta1 = "Correcto";
					}
					else{//Excepcion en produccion
						
						retorno = false;
						l1 = new Lector(archivo);
						consulta.updateCabecera(
								null,null, //SERIE - NUMERO
								BeanManager.COD_BOLETA, cab.getTransaccion(),  //FACTURA - TRANSACCION
								l1.getFileAsFileInputStream(), archivo, //XML-FACTURA -> File xml
								ans, in.getMensaje(), //Tipo Homologado - Mensaje de sunat
								in.getFileAsFileInputStream(),in.getFile()); // XMl respuesta o zip con el cdr y su file respectivo
						respuesta1 = "Rechazado";
					}
					new File(nombre+".zip").delete();
					if(in != null) {
						in.deleteFiles();
						in.close();
					}
					if(l1 != null) {
						l1.deleteFiles();
						l1.close();
					}
					consulta.updateCorrelacion(cor);

					System.out.println("Generando Boleta: " + cab.getSerie()+"-"+cab.getNumero()+"::"
							+ (ans == 1 ?("B"+cor.getSerie()+"-"+cor.getCorrelativo()):"") +"::"
							+respuesta1);
				}

				System.out.println("Fin Generando...");
			}
			//return consulta.getDocumentos(fecha, BeanManager.COD_BOLETA, true);
		}catch(NullPointerException | SQLException e) {
			consulta.getConnection().rollback();
			throw e;
		}
		return retorno;
	}
	
	/**
	 * Retorna un Objeto Con la informacion generada del resumen Diario
	 * 	ej:
	 * 	Object[0] -> El ticket generado
	 * 	Object[1] -> El xml del resumen diario
	 * 	Object[2] -> el nombre del archivo
	 * 	Object[3] -> su tipo MIME (ej. text/xml)
	 * 	Object[4] -> La respuesta de la SUNAT, se espera un xml
	 * 	Object[5] -> el nombre del archivo
	 * 	Object[6] -> su tipo MIME (ej. text/xml)
	 * 	Object[7] -> El correlativo actual
	 * @param fecha
	 * @return
	 * @throws Exception
	 */
	public Object[] generarResumenDiario(String fecha) throws Exception {
		if(fecha == null)
			throw new NullPointerException("Error en la fecha: null");
		try {
			List <Cabdocumentos> boletas = consulta.getDocumentos(fecha, BeanManager.COD_BOLETA, true,true);
			Empresa empresa = consulta.getEmpresa();
			
			GregorianCalendar date = new GregorianCalendar();
			String anio = String.format("%04d", date.get(GregorianCalendar.YEAR)),
					mes = String.format("%02d", date.get(GregorianCalendar.MONTH)+1),
					dia = String.format("%02d", date.get(GregorianCalendar.DATE));
			
			String correlativo = consulta.getCorrelacionResumen(anio+mes+dia,BeanManager.COD_RESUMEN_DIARIO);
			String nombre = empresa.getRuc() + "-RC-"+anio+mes+dia+"-"+correlativo;

			
			XMLDocument resumen = XMLFactory.getXMLResumenDiario(boletas, empresa,
					nombre, correlativo, 0);
			File res = resumen.generarDocumento();
			
			Compresor comp = new Compresor();
			comp.comprimir(res.getName(), nombre);
			
			Mensajero msj = new Mensajero(Programa.sistema.getUrlProduccion(),new File(nombre+".zip"));
			int intentos = 10;
			String ticket = "-1";
			File respuesta = null;
			Lector in = null;
			while (intentos != 0) {
				try {
					respuesta = msj.sendBill(empresa.getRuc(), empresa.getUsrSecundario(), 
							empresa.getPass());
					in = new Lector(respuesta);
					ticket = in.decodeSummaryRespuesta(nombre, (Programa.sistema.getUrlProduccion()
							.equals(URLSunat.ProduccionFactura.getValor()) ));
					break;
				}catch(Exception e) {
					intentos--;
					if(intentos == 0) {
						throw new NullPointerException("El servicio de recepcion de la sunat para Resumen "
								+ "ha fallado, la informacion del dia de generacion serán restaurada. Error-code:"
								+e.getMessage());
					}
				}
			}
			Object[] retorno = new Object[8];
			
			if(ticket.equals("-1")) {
				consulta.addResumenDiario(anio+mes+dia,fecha,
						correlativo,new Lector(res),ticket,new Lector(respuesta));
				//for(Cabdocumentos boleta:boletas) 
					//consulta.updateCabecera(boleta.getTransaccion(),-1);
			}else {
				consulta.addResumenDiario(anio+mes+dia,fecha,
						correlativo,new Lector(res),ticket,new Lector(respuesta));
				for(Cabdocumentos boleta:boletas) 
					consulta.updateCabecera(boleta.getTransaccion(),-1);
			}
			
			retorno[0] = ticket;
			retorno[1] = new Lector(res).getFileAsByteArray();
			retorno[2] = res.getName();
			retorno[3] = "text/xml";
			retorno[4] = new Lector(respuesta).getFileAsByteArray();
			retorno[5] = respuesta.getName();
			retorno[6] = "text/xml";
			retorno[7] = correlativo;
			
			System.out.println("Generado... " + nombre+ ".xml" + "::"+in.getMensaje());
			
			try {
				res.delete();
				respuesta.delete();
				new File(nombre+".zip").delete();
			}catch(Exception e) {}
			return retorno;
		}catch(Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}
	/**
	 * Retorna un Objeto Con la informacion generada del resumen Diario
	 * 	ej:
	 * 	Object[0] -> El ticket generado
	 * 	Object[1] -> El xml del resumen diario
	 * 	Object[2] -> el nombre del archivo
	 * 	Object[3] -> su tipo MIME (ej. text/xml)
	 * 	Object[4] -> La respuesta de la SUNAT, se espera un xml
	 * 	Object[5] -> el nombre del archivo
	 * 	Object[6] -> su tipo MIME (ej. text/xml)
	 * 	Object[7] -> El correlativo actual
	 * @param fecha
	 * @return
	 * @throws Exception
	 */
	public Object[] generarResumenBajas(List<Cabdocumentos> datos,List<String> razones) throws Exception{
		try {
			Empresa empresa = consulta.getEmpresa();
			
			
			GregorianCalendar date = new GregorianCalendar();
			String anio = String.format("%04d", date.get(GregorianCalendar.YEAR)),
					mes = String.format("%02d", date.get(GregorianCalendar.MONTH)+1),
					dia = String.format("%02d", date.get(GregorianCalendar.DATE));
			
			String correlativo = consulta.getCorrelacionResumen(anio+mes+dia,BeanManager.COD_RESUMEN_BAJA);
			String nombre = empresa.getRuc() + "-RA-"+anio+mes+dia+"-"+correlativo;
			
			XMLDocument resumen = XMLFactory.getXMLResumenBajas(datos,empresa, 
					razones, nombre, correlativo);
			File res = resumen.generarDocumento();
			
			Compresor comp = new Compresor();
			comp.comprimir(res.getName(), nombre);
			Mensajero msj = new Mensajero(Programa.sistema.getUrlProduccion(),new File(nombre+".zip"));
			int intentos = 10;
			String ticket = "-1";
			File respuesta = null;
			Lector in = null;
			while (intentos != 0) {
				try {
					respuesta = msj.sendBill(empresa.getRuc(), empresa.getUsrSecundario(), 
							empresa.getPass());
					in = new Lector(respuesta);
					ticket = in.decodeSummaryRespuesta(nombre,(Programa.sistema.getUrlProduccion()
							.equals(URLSunat.ProduccionFactura.getValor()) ));
					break;
				}catch(Exception e) {
					intentos--;
					if(intentos == 0) {
						throw new NullPointerException("El servicio de recepcion de la sunat para Resumen "
								+ "ha fallado, la informacion del dia de generacion serán restaurada. Error-code:"
								+e.getMessage());
					}
				}
			}
			Object[] retorno = new Object[8];
			consulta.addResumen(anio+mes+dia,datos.get(0).getFechaemision(),
					correlativo,new Lector(res),ticket,new Lector(respuesta)
					,BeanManager.COD_RESUMEN_BAJA);
				
			retorno[0] = ticket;
			retorno[1] = new Lector(res).getFileAsByteArray();
			retorno[2] = res.getName();
			retorno[3] = "text/xml";
			retorno[4] = new Lector(respuesta).getFileAsByteArray();
			retorno[5] = respuesta.getName();
			retorno[6] = "text/xml";
			retorno[7] = correlativo;
			System.out.println("Generado... " + nombre+ ".xml" + "::"+in.getMensaje());
			
			if(!ticket.equals("-1")) {
				for(int i = 0 ; i < datos.size() ; i++)
					consulta.updateAnulado(datos.get(i).getTransaccion(), -1);
			}
			try {
				res.delete();
				respuesta.delete();
				new File(nombre+".zip").delete();
			}catch(Exception e) {}
			
			return retorno;
		}catch(Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}
}
