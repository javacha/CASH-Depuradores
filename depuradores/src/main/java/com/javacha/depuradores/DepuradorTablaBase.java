package com.javacha.depuradores;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;


public class DepuradorTablaBase {

	Connection conn; 
	public final int REGS_FETCH = 100000; 
	String razonBorradoParcial = "" ; 
	ParametrosDepuracionTabla parametros ; 
	
	public DepuradorTablaBase() {}
	
	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public void setParametros(ParametrosDepuracionTabla parametros) {
		this.parametros = parametros;
	}

	public DepuradorTablaBase(Connection conn, ParametrosDepuracionTabla parametros) {
		this.parametros = parametros; 
		this.conn = conn; 
	}
	

	public String getNombreTabla() {
		return parametros.getNombreTabla();
	}
		
	
	public String getFiltro() throws Exception {
		String fecha = calculaFechas(parametros.getMesesRegistrosActivos()); 
		return "where AUD_FMODIFIC < '" + fecha + "'" ;		
	}	
	

	public int depuraTabla() throws Exception {
		java.sql.PreparedStatement statement ;
		String query ;
		
		Calendar horaInicio, horaCheck; 
		int regsBorrados = 0, minutosCorriendo = 0, segundos, regsAffected = 666, cantCiclos=1;
		
		//////////////////////////////////////////
		
		conn.setAutoCommit(false);
		
		
		horaInicio = Calendar.getInstance();		
		int registrosFetch;
		
		if (parametros.getTopeRegistrosBorrados()< REGS_FETCH){
			registrosFetch = parametros.getTopeRegistrosBorrados();
		}else{
			registrosFetch = REGS_FETCH;
		}
		
		query = "delete from " + 
	               "(select * from " + getNombreTabla() + " " + getFiltro() + 
	               "  FETCH FIRST " + registrosFetch + " ROWS ONLY)" ;
		statement = conn.prepareStatement(query);		
		
		while (regsBorrados < parametros.getTopeRegistrosBorrados() && minutosCorriendo < parametros.getTopeMinutosCorriendo() && regsAffected > 0) {			
			regsAffected = statement.executeUpdate();
			conn.commit(); 
			
			regsBorrados+=regsAffected; 			
			horaCheck = Calendar.getInstance();
			segundos =( horaCheck.get(Calendar.HOUR_OF_DAY) *3600 + horaCheck.get(Calendar.MINUTE) *60 + horaCheck.get(Calendar.SECOND)  ) - 
	  		          ( horaInicio.get(Calendar.HOUR_OF_DAY)*3600 + horaInicio.get(Calendar.MINUTE)*60 + horaInicio.get(Calendar.SECOND) ) ; 
			minutosCorriendo = segundos / 60; 
			
			System.out.println("\t\t\t\t\tciclo " + cantCiclos + " ---" + (new Date()));
			cantCiclos++;
			
		}
		statement.close() ;	
		statement=null; 
		conn.setAutoCommit(true);
		
		if (regsBorrados >= parametros.getTopeRegistrosBorrados()) {
			razonBorradoParcial = "Corte por tope de registros a borrar (" + parametros.getTopeRegistrosBorrados() + " regs)" ;
		}				
		
		if (minutosCorriendo >= parametros.getTopeMinutosCorriendo()) {
			razonBorradoParcial = "Corte por tope de tiempo ejecutando(" + parametros.getTopeMinutosCorriendo() + " mins)" ;

		}		
		
		return regsBorrados;		

	}
	
	public int getRegistrosABorrar() throws Exception {
		java.sql.Statement statement ;
		ResultSet resultSet;
		String query ;
		int resultado=0 ; 
		query = "select count(*) from " + getNombreTabla() + " " + getFiltro() ;
		statement = conn.createStatement();
		resultSet = statement.executeQuery(query);

		while(resultSet.next()) {				
			resultado = resultSet.getInt(1) ;
		}
		resultSet.close();
		resultSet=null ;
		statement.close() ;		
		statement=null; 

		return resultado;
	} 

	public int getRegistrosTotales() throws Exception {
		java.sql.Statement statement ;
		ResultSet resultSet;
		String query ;
		int resultado=0 ; 
		
		query = "select count(*) from " + getNombreTabla();
		statement = conn.createStatement();
		resultSet = statement.executeQuery(query);

		while(resultSet.next()) {				
			resultado = resultSet.getInt(1) ;
		}
		resultSet.close();
		resultSet=null ;
		statement.close() ;		
		statement=null; 		

		return resultado;
	}


	public String getMotivoBorradoParcial() {
		return razonBorradoParcial;
	}
	
	protected int diffMins(Calendar inicio, Calendar actual) {
		int segundos =( actual.get(Calendar.MINUTE)*60 + actual.get(Calendar.SECOND) ) - 
					  ( inicio.get(Calendar.MINUTE)*60 + inicio.get(Calendar.SECOND) ) ; 
		return segundos / 60; 
	}

	protected String calculaFechas(int cantidadMeses) throws Exception {
		java.sql.Statement statement ;
		ResultSet resultSet;
		String query ;
		String fecha=null;
		
		// Para DB2
		query = "SELECT CHAR(CURRENT DATE - " + cantidadMeses + " MONTHS, ISO) FROM SYSIBM.SYSDUMMY1" ;
		
		// Para MariaDB		
		//query = "SELECT DATE_FORMAT(  ADDDATE(NOW(), INTERVAL -" + cantidadMeses + " MONTH), '%Y-%m-%d')";

		statement = conn.createStatement();
		resultSet = statement.executeQuery(query);			
		while(resultSet.next()) {				
			fecha = resultSet.getString(1).trim();
		}
		resultSet.close();
		resultSet=null ;
		statement.close() ;		
		statement=null; 	
		return fecha;		
	}	
	
}
