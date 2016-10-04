package com.okiimport.app.mvvm.controladores;

import java.io.FileNotFoundException;
import java.util.List;

import org.zkoss.bind.BindUtils;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.okiimport.app.mvvm.AbstractRequerimientoViewModel;
import com.okiimport.app.mvvm.carga_masiva.ProcesarDatosEstrategy;
import com.okiimport.app.mvvm.model.WBookFactory.TypeDocument;


public abstract class AbstractCargaMasivaViewModel extends AbstractRequerimientoViewModel {
	
	//Atributos
	protected String ultimoArchivo="";
	
	/**METODOS PROPIOS DE LA CLASE*/
	protected void download(String path){
		try {
			Filedownload.save(path, null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void onUpload(final ProcesarDatosEstrategy<?> estrategia, final UploadEvent event, final String... notifyChange){
		Component compt = event.getTarget();
		try {
			Media media = event.getMedia();
			TypeDocument type = null;
			if(ultimoArchivo!=null && ultimoArchivo.trim().toLowerCase().equalsIgnoreCase(media.getName()))
				this.mostrarMensajeCargaMasiva("Informaci�n", 
						"Ya se ha cargado el archivo "+media.getName()+". \n�Desea cargarlo de nuevo?", estrategia, event, notifyChange);
			else if(!ultimoArchivo.trim().equalsIgnoreCase("") 
					&& !ultimoArchivo.trim().toLowerCase().equalsIgnoreCase(media.getName()))
				this.mostrarMensajeCargaMasiva("Informaci�n", "Ya se ha cargado un archivo "+ultimoArchivo+
						". \n�Desea cargar el archivo "+media.getName()+"? \nSe perderan los datos cargados con anterioridad.", 
						estrategia, event, notifyChange);
			else if((type = TypeDocument.switchName(media.getFormat())) != null){
	 			//if (binario.length <= (servicioConfiguracionGeneral.buscarTamanoMaximo() * 1048576)) {
	 				prepararCarga();
	 				if(estrategia!=null && !estrategia.cargarDatos(type, media.getStreamData(), 0)){
	 					List<?> registros = estrategia.getList();
	 					if(!registros.isEmpty()){
	 						ultimoArchivo = media.getName();
	 						archivosProcesados(registros, media, compt);
	 					}
	 					else {
	 						mostrarMensaje("Informaci�n", "�El archivo se encuentra vac�o!", Messagebox.ERROR, null , null, null);
							archivoVacio(compt);
						}
	 				}
	 				else
	 					archivoVacio(compt);
	 			//}
	 			//else{
//					binario=null;
//					Messagebox.show("�El archivo supera el tama�o l�mite de "
//							+ servicioConfiguracionGeneral.buscarTamanoMaximo()
//							+ " MB!", "Informaci�n", Messagebox.OK, Messagebox.ERROR);
//					archivoFueraLimite(compt);
//	 			}
			}
			else {
				mostrarMensaje("Informaci�n", "No es un archivo v�lido", Messagebox.ERROR, null, null, null);
				archivoNoValido(compt);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			mostrarMensaje("Informaci�n", "No es un archivo v�lido", null, null, null, null);
 			archivoNoValido(compt);
 		}
	}
	
	/**METODOS PRIVADOS DE LA CLASE*/
	private void mostrarMensajeCargaMasiva(String titulo, String mensaje, 
			final ProcesarDatosEstrategy<?> estrategia, final UploadEvent event, final String... notifyChange){
		mostrarMensaje(titulo, mensaje, 
				Messagebox.INFORMATION, 
				new Messagebox.Button[]{Messagebox.Button.YES, Messagebox.Button.NO}, 
				new EventListener<Event>() {

					@Override
					public void onEvent(Event evt) throws Exception {
						if((Messagebox.Button) evt.getData()==Messagebox.Button.YES){
							AbstractCargaMasivaViewModel.this.ultimoArchivo="";
							AbstractCargaMasivaViewModel.this.onUpload(estrategia, event, notifyChange);
							for(String field : notifyChange)
								BindUtils.postNotifyChange(null,null,AbstractCargaMasivaViewModel.this, field);
						}
					}
		            
		        }, null);
	}

	/**METODOS ABSTRACTOS DE LA CLASE*/
	protected abstract void prepararCarga();
	protected abstract void archivosProcesados(List<?> registros, Media media, Component component);
	protected abstract void archivoVacio(Component component);
//	protected abstract void archivoFueraLimite(Component component);
	protected abstract void archivoNoValido(Component component);
	
}
