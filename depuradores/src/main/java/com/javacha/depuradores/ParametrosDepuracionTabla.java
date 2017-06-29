package com.javacha.depuradores;

public class ParametrosDepuracionTabla {

	protected String nombreTabla;
	protected int topeMinutosCorriendo;
	protected int topeRegistrosBorrados;
	protected int mesesRegistrosActivos;
	protected String depuradorImpl;
	
	public String getNombreTabla() {
		return nombreTabla;
	}
	public void setNombreTabla(String nombreTabla) {
		this.nombreTabla = nombreTabla;
	}
	public int getTopeMinutosCorriendo() {
		return topeMinutosCorriendo;
	}
	public void setTopeMinutosCorriendo(int topeMinutosCorriendo) {
		this.topeMinutosCorriendo = topeMinutosCorriendo;
	}
	public int getTopeRegistrosBorrados() {
		return topeRegistrosBorrados;
	}
	public void setTopeRegistrosBorrados(int topeRegistrosBorrados) {
		this.topeRegistrosBorrados = topeRegistrosBorrados;
	}
	public int getMesesRegistrosActivos() {
		return mesesRegistrosActivos;
	}
	public void setMesesRegistrosActivos(int mesesRegistrosActivos) {
		this.mesesRegistrosActivos = mesesRegistrosActivos;
	}
	public String getDepuradorImpl() {
		return depuradorImpl;
	}
	public void setDepuradorImpl(String depuradorImpl) {
		this.depuradorImpl = depuradorImpl;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ParametrosDepuracionTabla [nombreTabla=").append(nombreTabla).append(", topeMinutosCorriendo=")
				.append(topeMinutosCorriendo).append(", topeRegistrosBorrados=").append(topeRegistrosBorrados)
				.append(", mesesRegistrosActivos=").append(mesesRegistrosActivos).append(", depuradorImpl=")
				.append(depuradorImpl).append("]");
		return builder.toString();
	}
	
	
	
}
