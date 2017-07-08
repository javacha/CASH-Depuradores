package com.javacha.depuradores;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 
 * @author A116135 - Javier Da Silva Clase que depuracion de tablas de CASH
 * 
 *         Esquema de depuración generico de tablas. Cada tabla implementa su
 *         algoritmo de depuración.
 *
 */
public class TLCLDepuraTablas {

	final static Logger logger = Logger.getLogger(TLCLDepuraTablas.class);

	protected Connection connDB;
	Properties propDB;
	List<ParametrosDepuracionTabla> tablas;

	String fecha2Meses;
	String fecha12Meses;
	boolean modoDUMMY = false;

	static final String version = "v1.0";

	public TLCLDepuraTablas() {
		// TODO Auto-generated constructor stub
	}

	public static Connection getConection(Properties props) throws Exception {
		logger.info("creando conexion a base de datos...");
		Connection conn = null;

		try {
			Class.forName(props.getProperty("driver"));
		} catch (ClassNotFoundException e) {
			logger.error("ERROR al instanciar driver " + props.getProperty("driver"));
			e.printStackTrace();
		}

		try {
			conn = DriverManager.getConnection(props.getProperty("url"), props.getProperty("username"),
					props.getProperty("password"));
		} catch (SQLException e) {
			logger.error("ERROR con parametros de conexion -> ".concat(props.getProperty("url")).concat(" / ")
					.concat(props.getProperty("username")).concat(" / ").concat(props.getProperty("password")));
			
			throw new Exception(e); 
		}
		return conn;
	}

	protected void closeConection() {
		try {
			connDB.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	protected void printHelp() {
		logger.info("Parametros:");
		logger.info("\tarchivo cfg para acceso de base de datos");
		logger.info("\tarchivo cfg datos de depuracion");
	}

	protected int validaParams(String[] args) {
		Properties archCFG;

		String driver, url, username, password;

		// valido cant de parametros
		if (args.length != 2) {
			error("ERROR: cantidad de parámetros errónea.");
			printHelp();
			return 1;
		}

		// valido el archivo CFG de BD
		try {
			FileInputStream f = new FileInputStream(args[0]);
			archCFG = new Properties();
			archCFG.load(f);
			f.close();

			url = archCFG.getProperty("url");
			driver = archCFG.getProperty("driver");
			username = archCFG.getProperty("username");
			password = archCFG.getProperty("password");

			if (url == null) {
				error("ERROR: parametro url no seteado en cfg de BD");
				return 3;
			}
			if (driver == null) {
				error("ERROR: parametro driver no seteado en cfg de BD");
				return 3;
			}
			if (username == null) {
				error("ERROR: parametro username no seteado en cfg de BD");
				return 3;
			}
			if (password == null) {
				error("ERROR: parametro password no seteado en cfg de BD");
				return 3;
			}

		} catch (Exception e) {
			error("ERROR: no se puede cargar archivo CFG " + args[0]);
			return 21;
		}
		
		try {
			connDB = getConection(archCFG);
		} catch (Exception e) {
			return 8;
		} 

		int ret = cargarParametrosDepuracion(args[1]);

		return ret;
	}

	
	/**
	 * Carga la lista de tablas con sus parametros de depuracion
	 * @param props Property del proceso
	 * @return
	 */
	private List<ParametrosDepuracionTabla> armaListatablas(Properties props) {
		List<ParametrosDepuracionTabla> ret = new ArrayList<ParametrosDepuracionTabla>();
		String nombreTabla="";

		Enumeration eKeys = props.keys();
		for (; eKeys.hasMoreElements();) {

			String key = (String) eKeys.nextElement();
			if (key.startsWith("tabla")) {
				
				nombreTabla = key.substring(6, key.length()); 
				logger.info("cargando parametros tabla ".concat(nombreTabla));

				ParametrosDepuracionTabla param;
				try {
					param = cargaTablaFromProp(nombreTabla, props.getProperty(key));
					ret.add(param);
				} catch (Exception e) {
					logger.error("Error en datos de depurador. Verificar");
				}
			}

		}
		return ret;
	}

	/**
	 * Parsea los datos de depuracion de una tabla
	 * @param tabla Nombre de la tabla
	 * @param paramTabla Parametros de depuracion
	 * @return
	 * @throws Exception
	 */
	private ParametrosDepuracionTabla cargaTablaFromProp(String tabla, String paramTabla) throws Exception {
		ParametrosDepuracionTabla ret = new ParametrosDepuracionTabla();

		String[] datos = paramTabla.split(",");
		if (datos.length < 4) {
			throw new Exception("Error en parametros de depuracion");
		}

		ret.setNombreTabla(tabla);
		ret.setMesesRegistrosActivos(Integer.parseInt(datos[0].trim()));
		ret.setTopeMinutosCorriendo(Integer.parseInt(datos[1].trim()));
		ret.setTopeRegistrosBorrados(Integer.parseInt(datos[2].trim()));
		ret.setDepuradorImpl(datos[3].trim());
		return ret;
	}

	
	
	/**
	 * Inicio del proceso.
	 * 
	 * @param args
	 *            Parametros de entrada del proceso. Param1: arch. properties
	 *            base de datos Param2: arch. properties de depuracion
	 */
	public void procesar(String[] args) {
		int ret;

		logger.info("iniciando depurador...");

		// valido params
		ret = validaParams(args);
		if (ret > 0)
			System.exit(ret);

		if (modoDUMMY) {
			log("****************************");
			log("*****    MODO DUMMY    *****");
			log("****************************");
			log(" ");
		}

		try {
			depurarTablas();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
		/*
		 * if (tablas.size() > 0) { if (conectaBD(baseTLCL) > 0)
		 * System.exit(99); log("Voy a depurar base " + baseTLCL) ;
		 * depurarTablas(baseTLCL) ; desConectaBD(); }
		 */
	}

	protected void depurarTablas() throws Exception {
		String tablas[];
		int regs, regsABorrar;
		
		for (Iterator iterator = this.tablas.iterator(); iterator.hasNext();) {
			ParametrosDepuracionTabla paramTabla = (ParametrosDepuracionTabla) iterator.next();
						
			logger.info("Voy a depurar la tabla: " + paramTabla.getNombreTabla()) ;
			
			DepuradorTablaBase depurador = (DepuradorTablaBase) Class.forName(paramTabla.getDepuradorImpl()).newInstance();
							
			depurador.setConn(connDB);
			depurador.setParametros(paramTabla) ;
 
			
			logger.info("    Filtro a aplicar              : " + depurador.getFiltro()) ;
			logger.info("    Tope de registros             : " + paramTabla.getTopeRegistrosBorrados()) ;
			logger.info("    Tope cantidad de minutos      : " + paramTabla.getTopeMinutosCorriendo()) ;
			logger.info("    Cantidad de regitros totales  : ") ;
			logger.info("         " + format(depurador.getRegistrosTotales())) ;
			
			logger.info("    Cantidad de regitros a borrar : " ) ;
			regsABorrar = depurador.getRegistrosABorrar(); 
			logger.info("         " + format(regsABorrar)) ;			
			
			
			if (!modoDUMMY && regsABorrar > 0) {
				logger.info("    Depurando..." ) ;				
				regs = depurador.depuraTabla() ;				
				logger.info("    Tabla depurada!"); 				
				logger.info("    Cantidad de regitros depurados: " + format(regs)) ;
				
				if (regs < regsABorrar) {
					logger.info("    " + depurador.getMotivoBorradoParcial()) ;
				}
				logger.info("    Cantidad de regitros en tabla : " ) ;
				logger.info("         " + format(depurador.getRegistrosTotales())) ;
			}			
		}
	}

	protected int cargarParametrosDepuracion(String archivo) {
		// valido el archivo CFG de depuracion

		FileInputStream f  = null;
		Properties archCFG = new Properties();
		
		
		try {
			f = new FileInputStream(archivo);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			archCFG.load(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			f.close();
		} catch (Exception e) {}

		tablas = armaListatablas(archCFG);

		return 0;
	}

	public void log(String data) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		System.out.println("** INF **  " + sdf.format(new Date()) + "  " + data);
	}

	public void error(String data) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		System.out.println("** ERR **  " + sdf.format(new Date()) + "  " + data);
	}

	protected String format(int valor) {
		StringBuffer buff = new StringBuffer(String.valueOf(valor));
		while (buff.length() < 9)
			buff.insert(0, " ");
		return buff.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TLCLDepuraTablas depuradores = new TLCLDepuraTablas();
		depuradores.log("Iniciando...");
		depuradores.procesar(args);	
		depuradores.log("Proceso finalizado");		
	}

}
