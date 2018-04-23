package com.pe.amd.modelo.app.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Compresor {
	
	ZipOutputStream os;
	FileInputStream fis;
	
	public Compresor (){
		os = null;
		fis = null;
	}
	
	/**
	 * Permite comprimir un archivo en un zip con un determinado nombre desde una direccion
	 * original hacia una objetivo
	 * 
	 * @param nombre_origen es el nombre del archivo original y debe de poseer su sufijo respectivo
	 * 		ejemplo: archivo1.txt o archivo 2.xml		
	 * 
	 * @param nombre_destino es el nombre que se le pondra al comprimido, va sin su extension o con su extension
	 * 		ejemplo: comprimido o comprimido.zip
	 * 
	 * @param path_origen comprende la direccion donde esta ubicado el archivo original y debe de terminar en
	 * 		ejemplo: c/folder1/
	 * 		el caracter '/' o '\'
	 * 
	 * @param path_destino comprende la direccion donde se ubicara el comprimido
	 * 
	 */
	public void comprimir(String nombre_origen, String nombre_destino,
			String path_origen, String path_destino) throws Exception{
		
		if( nombre_origen.isEmpty() || nombre_destino.isEmpty())
			throw new Exception("Uno de los parámetros no es válido");
		
		
		String outputFile = path_destino 
				+ (nombre_destino.contains(".zip")?nombre_destino:(nombre_destino+".zip"));
		
		new Escritor().copiarArchivo(new File(path_origen + nombre_origen), "");
		
		String inputFile = nombre_origen;
		
		FileInputStream in = new FileInputStream(inputFile);
		FileOutputStream out = new FileOutputStream(outputFile);

		byte b[] = new byte[2048];
		ZipOutputStream zipOut = new ZipOutputStream(out);
		ZipEntry entry = new ZipEntry(inputFile);

		zipOut.putNextEntry(entry);
		int len = 0;
		while ((len = in.read(b)) != -1) {
			zipOut.write(b, 0, len);
		}
		zipOut.closeEntry();
		zipOut.close();
		in.close();
		out.close();
		
		new File(nombre_origen).delete();
	}
	
	
	/**
	 * Permite comprimir un archivo en un zip con un determinado nombre desde una direccion
	 * original hacia una objetivo
	 * 
	 * @param nombre_origen es el nombre del archivo original y debe de poseer su sufijo respectivo
	 * 		ejemplo: archivo1.txt o archivo 2.xml		
	 * 
	 * @param nombre_destino es el nombre que se le pondra al comprimido, va sin su extension o con su extension
	 * 		ejemplo: comprimido o comprimido.zip
	 * @throws IOException 
	 * 
	 */
	public void comprimir(String nombre_origen, String nombre_destino) throws NullPointerException, IOException{
		
		if(nombre_origen.isEmpty() || nombre_destino.isEmpty())
			throw new NullPointerException("Uno de los parámetros no es válido");
		
		String inputFile = nombre_origen;
		String outputFile = (nombre_destino.contains(".zip")?nombre_destino:(nombre_destino+".zip"));
		
		FileInputStream in = new FileInputStream(inputFile);
		FileOutputStream out = new FileOutputStream(outputFile);

		byte b[] = new byte[2048];
		ZipOutputStream zipOut = new ZipOutputStream(out);
		ZipEntry entry = new ZipEntry(inputFile);
		zipOut.putNextEntry(entry);
		int len = 0;
		while ((len = in.read(b)) != -1) {
			zipOut.write(b, 0, len);
		}
		zipOut.closeEntry();
		zipOut.close();
		in.close();
		out.close();
	}
	
	public void cerrar(){
		try{
			os.close();
			fis.close();
		}catch(IOException e){}
	}
	
}
