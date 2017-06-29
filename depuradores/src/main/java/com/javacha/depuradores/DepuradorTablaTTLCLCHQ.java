package com.javacha.depuradores;

public class DepuradorTablaTTLCLCHQ extends DepuradorTablaBase {


	public String getNombreTabla() {
		return "TLCL.TTLCLCHQ";
	}

	public String getFiltro(int meses) throws Exception {					
		String fecha2Meses = calculaFechas(2);
		String fecha12Meses = calculaFechas(12);
		String sql;
		sql =          "where ( COD_CLASEORD IN ('VCA', 'VCC') AND AUD_FMODIFIC < '" + fecha2Meses + "' )";
		sql = sql.concat(" OR ( COD_CLASEORD IN ('ICC', 'VNC') AND AUD_FMODIFIC < '" + fecha12Meses + "' )"); 
		return sql ;	
	}	


}
