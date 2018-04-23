package com.pe.amd.modelo.app.out;
import java.util.ArrayList;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

public class Ticket {
	private PrintService findPrintService(String printerName,
			PrintService[] services) {
		for (PrintService service : services) {
			if (service.getName().equalsIgnoreCase(printerName)) {
				return service;
			}
		}
		return null;
	}
public void printBytes(String printerName, byte[] bytes) {
		
		DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();

		PrintService printService[] = PrintServiceLookup.lookupPrintServices(
				flavor, pras);
		PrintService service = findPrintService(printerName, printService);

		DocPrintJob job = service.createPrintJob();

		try {

			Doc doc = new SimpleDoc(bytes, flavor, null);

			job.print(doc, null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void printString(String printerName, String text) {
		
		// find the printService of name printerName
		DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();

		PrintService printService[] = PrintServiceLookup.lookupPrintServices(
				flavor, pras);
		PrintService service = findPrintService(printerName, printService);

		DocPrintJob job = service.createPrintJob();

		try {

			byte[] bytes;

			// important for umlaut chars
			bytes = text.getBytes("CP437");

			Doc doc = new SimpleDoc(bytes, flavor, null);

			
			job.print(doc, null);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		Ticket ticket = new Ticket(47);
		ticket.addLinea("REPUESTOS CARBUSS", true);
		ticket.separar();
		ticket.addLinea("RUC", "12456378451",5);
		ticket.addLinea("TELEFONO", "72488849",5);
		ticket.addLinea("CELULAR" , "901105909",5);
		ticket.addLinea("DIRECCION", "CARLOS A.SACO 220 - La Perla - Callao XXXXX YYYYYY AAAAAAAA",5);

		int[] limites = {5,8,8,14,6,6};
		ticket.separar();
		String[][] tabla = {{"PRUEBA1","PRUEBA2","PRUEBA3","PRUEBA4","PRUEBA5","PRUEBA6"}
				,{"AAAA","BBBBBBBBB","CCC","DDDDDDDD","EEEEEEEEEEEE","FFFFFF"}
				,{"AAAA","BBBBBBBB","CCC","DDDDDDDD","EEEEEEEEEE","FFFFFF"}
				,{"AAAA","BBBBBBBBB","CCC","DDDDDDDD","EEEEEEEEEEE","FFFFFF"}
				};

		ticket.addLineas(tabla,limites);
		ticket.separar();
		
		String[][] tabla1 = {{"PRUEBA1","PRUEBA2","PRUEBA3","PRUEBA4","PRUEBA5","PRUEBA6"}
		,{"AAAA","BBBBBBBBBBBB","CCC","DDDDDDDDDD","EEEEEEEEEEEE","FFFFFF"}
		,{"AAAA","BBBBBBBBBBBB","CCC","DDDDDDDDDD","EEEEEEEEEEEE","FFFFFF"}
		,{"AAAA","BBBBBBBBBBBB","CCC","DDDDDDDDDD","EEEEEEEEEEEE","FFFFFF"}
		};
		
		ticket.addLineas(tabla1,limites);
		ticket.separar();
		System.out.println(ticket.printTicket());
		ticket.printString("POS-80C",ticket.printTicket() );
		ticket.printString("POS-80C", "\n\n\n\n");
		byte[] cutP = new byte[] { 0x1d, 'V', 1 }; 
		ticket.printBytes("POS-80C", cutP);
		
		
	}
	
	private ArrayList<String> lineas;
	private int width_limit;
	
	public Ticket(int width_limit) {
		this.width_limit = width_limit;
		lineas = new ArrayList<>();
	}
	
	public String printTicket() {
		String ticket = "";
		for(int i = 0 ; i < lineas.size() ; i++)
			ticket += lineas.get(i) + "\n";
		
		return ticket;
	}
	public void separar() {
		String linea = "";
		for(int i = 0 ; i < this.width_limit ; i++)
			linea += "-";
		this.addLinea(linea);
	}
	/**
	 * Agregas una linea a la representacion del ticket
	 * @param linea
	 */
	public void addLinea(String linea) {

		while(linea.length() > this.width_limit) {
			lineas.add( linea.substring(0, this.width_limit-1) + "-");
			linea = (linea.substring(this.width_limit-1));
		}
		lineas.add(linea);
	}
	/**
	 * Agregas una linea a la representacion del ticket con un padd determinado
	 * @param linea
	 * @param padd
	 */
	public void addLinea(String linea, int padd) {
		if(padd >= this.width_limit)
			throw new IllegalArgumentException("La cantidad de espacios "
					+ "no puede ser mayor o igual que la longitud del documentos");
		if(padd < 0 )
			throw new IllegalArgumentException("La cantidad de espacios "
					+ "no puede ser negativa");
		
		String pad = "";
		for(int i = 0 ; i < padd ; i++)
			pad+=" ";
		
		while(linea.length() + padd > this.width_limit) {
			lineas.add(padd + linea.substring(0, this.width_limit-1) + "-");
			linea = (padd + linea.substring(this.width_limit-1));
		}
		lineas.add(pad+linea);
	}
	public void addLinea(String linea, boolean centrado) {
		if(centrado) {
			while(linea.length() > this.width_limit) {
				this.addLinea(linea.substring(0, this.width_limit));
				linea = linea.substring(this.width_limit);
			}
			double pad = ( this.width_limit - linea.length() )/2 ;
			this.addLinea(linea,(int) pad);
		}else
			this.addLinea(linea);
	}
	public void addLinea(String att, String val) {
		if(att.length() >= this.width_limit )
			throw new IllegalArgumentException ("El atributo no puede exceder "
					+ "el tamaño del documento");
		String linea = att + ":" + val;		
		if(linea.length() > this.width_limit){
			lineas.add(linea.substring(0, this.width_limit));
			linea = linea.substring(this.width_limit);
			this.addLinea(linea, att.length()+1);
		}else
			lineas.add(linea);
	}
	public void addLinea(String att, String val, int padd) {
		if(att.length() + padd >= this.width_limit )
			throw new IllegalArgumentException ("El atributo no puede exceder "
					+ "el tamaño del documento");
		String pad = "";
		for(int i = 0 ; i < padd ; i++)
			pad+=" ";
		
		String linea = pad + att + ":" + val;		
		if(linea.length() > this.width_limit){
			lineas.add(linea.substring(0, this.width_limit));
			linea = linea.substring(this.width_limit);
			this.addLinea(linea, att.length()+1 + padd);
		}else
			lineas.add(linea);
	}
	
	public void addLineas(String[][] data) {
		if(data == null)
			throw new IllegalArgumentException("La data no puede ser nula");
		else if( data.length == 0)
			throw new IllegalArgumentException("Debe de haber por lo menos una fila");
		else if( data[0].length == 0)
			throw new IllegalArgumentException ("Debe de haber por lo menos una columna");
		
		for(int i = 0 ; i < data.length - 1 ; i++) {
			addLinea(data[i]);
			this.separar();
		}
		addLinea(data[data.length-1]);
	}
	public void addLinea(String[] linea) {
		int limit = ( this.width_limit / linea.length ) - 1 ;
		String l;
		String[] aux = new String[linea.length];
		System.arraycopy(linea, 0, aux, 0, linea.length);
		boolean cont;
		do {
			int inc = (this.width_limit % linea.length);
			l = "";
			cont = false;
			for(int i = 0 ; i < aux.length - 1 ; i++) {
				if(aux[i].length() > limit) {
					cont = true;
					l+="|"+ aux[i].substring(0, limit);
					aux[i] = aux[i].substring(limit);
				}
				else {
					l+="|"+String.format("%-"+(inc != 0?String.valueOf(limit+1):String.valueOf(limit))
							+"s", aux[i]);
					aux[i] = "";
				}
				if(inc != 0) inc--;
			}
			if(aux[aux.length-1].length() > limit) {
				cont = true;
				l+="|"+aux[aux.length-1].substring(0, limit)+"|";
				aux[aux.length-1] = aux[aux.length-1].substring(limit);
			}
			else {
				l += "|"+String.format("%-"+(inc != 0?String.valueOf(limit+1):String.valueOf(limit))
						+"s", aux[aux.length-1])+"|";
				aux[aux.length-1] = "";
			}

			this.lineas.add(l);
		}while(cont);
	}
	public void addLineas(String[][] data, int[] limites) {
		if(data == null)
			throw new IllegalArgumentException("La data no puede ser nula");
		else if( data.length == 0)
			throw new IllegalArgumentException("Debe de haber por lo menos una fila");
		else if( data[0].length == 0)
			throw new IllegalArgumentException ("Debe de haber por lo menos una columna");
		else if(data[0].length != limites.length)
			throw new IllegalArgumentException("Los limites y las columnas han de tener la misma longitud");
		
		int c = 0;
		for(int i = 0 ; i < limites.length ; i++)
			c += limites[i];
		
		if(c != this.width_limit) 
			throw new IllegalArgumentException("Los limites no estan bien definidos, la suma de todas las columnas "
					+ "debe ser igual al ancho del documento");
		
		
		for(int i = 0 ; i < data.length - 1 ; i++) {
			addLinea(data[i],limites);
			this.separar();
		}
		addLinea(data[data.length-1],limites);
	}

	public void addLinea(String[] linea, int[] limites) {
		String l;
		String[] aux = new String[linea.length];
		System.arraycopy(linea, 0, aux, 0, linea.length);
		boolean cont;
		do {
			cont = false;
			l = "";
			for(int i = 0 ; i < linea.length - 1; i++) {
				if(limites[i] - 1 <= 0) throw new IllegalArgumentException("Hay una celda cuya longitud es menor a 1");
				if(aux[i].length() > limites[i] - 1) {
					cont = true;
					l+="|"+ aux[i].substring(0, limites[i]-1);
					aux[i] = aux[i].substring(limites[i]-1);
				}
				else {
					l+="|"+String.format("%-"+String.valueOf(limites[i]-1)+"s", aux[i]);
					aux[i] = "";
				}
			}
			if(limites[aux.length-1] - 1 <= 0) throw new IllegalArgumentException("Hay una celda cuya longitud es menor a 1");
			if(aux[aux.length-1].length() > limites[aux.length-1] - 1) {
				cont = true;
				l+="|"+ aux[aux.length-1].substring(0, limites[aux.length-1]-1)+"|";
				aux[aux.length-1] = aux[aux.length-1].substring(limites[aux.length-1]-1);
			}
			else {
				l+="|"+String.format("%-"+String.valueOf(limites[aux.length-1]-1)+"s", aux[aux.length-1])+"|";
				aux[aux.length-1] = "";
			}
			this.lineas.add(l);
		}while(cont);
	}
	
}
