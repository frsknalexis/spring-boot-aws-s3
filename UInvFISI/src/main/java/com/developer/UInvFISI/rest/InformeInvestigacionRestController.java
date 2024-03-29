package com.developer.UInvFISI.rest;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.developer.UInvFISI.entity.Asignacion;
import com.developer.UInvFISI.entity.InformeInvestigacion;
import com.developer.UInvFISI.service.AmazonService;
import com.developer.UInvFISI.service.AsignacionService;
import com.developer.UInvFISI.service.InformeInvestigacionService;
import com.developer.UInvFISI.util.Constantes;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/informeinvestigacion")
public class InformeInvestigacionRestController {

	@Autowired
	@Qualifier("informeInvestigacionService")
	private InformeInvestigacionService informeInvestigacionService;
	
	@Autowired
	@Qualifier("asignacionService")
	private AsignacionService asignacionService;
	
	@Autowired
	@Qualifier("amazonService")
	private AmazonService amazonService;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@GetMapping("/informes")
	public ResponseEntity<List<InformeInvestigacion>> findAll() {
		
		try {
			
			List<InformeInvestigacion> informes = informeInvestigacionService.findAll();
			if(informes.isEmpty()) {
				
				return new ResponseEntity<List<InformeInvestigacion>>(HttpStatus.NO_CONTENT);
			}
			else {
				
				return new ResponseEntity<List<InformeInvestigacion>>(informes, HttpStatus.OK);
			}
		}
		catch(Exception e) {
			
			return new ResponseEntity<List<InformeInvestigacion>>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/informes/asignacion/{asignacionId}")
	public ResponseEntity<List<InformeInvestigacion>> findInformesByAsignacionId(@PathVariable(value="asignacionId") Integer asignacionId) {
		
		try {
			
			List<InformeInvestigacion> informes = informeInvestigacionService.findByAsignacionAsignacionId(asignacionId);
			if(informes.isEmpty()) {
				
				return new ResponseEntity<List<InformeInvestigacion>>(HttpStatus.NO_CONTENT);
			}
			else {
				
				return new ResponseEntity<List<InformeInvestigacion>>(informes, HttpStatus.OK);
			}
		}
		catch(Exception e) {
			
			return new ResponseEntity<List<InformeInvestigacion>>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value="/save/asignacion/{asignacionId}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseBaseOperacion> saveInformeInvestigacion(@PathVariable(value="asignacionId") Integer asignacionId,
			@RequestParam(value=Constantes.INFORME_INVESTIGACION_JSON_PARAM, required=true) String informeJson,
			@RequestParam(value=Constantes.INFORME_INVESTIGACION_FILE_PARAM, required=true) MultipartFile file) {
		
		Asignacion asignacion = null;
		
		try {
			
			if(asignacionId != null && asignacionId > 0) {
				
				asignacion = asignacionService.getByAsignacionId(asignacionId);
				if(asignacion == null) {
					
					return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.NO_CONTENT);
				}
			}
			
			InformeInvestigacion informeInvestigacion = objectMapper.readValue(informeJson, InformeInvestigacion.class);
			
			if(!file.isEmpty()) {
				
				String nombreFichero = amazonService.uploadFile(file);
				informeInvestigacion.setNombreFichero(nombreFichero);
				informeInvestigacion.setFormatoFichero(file.getContentType());
				informeInvestigacion.setTamanioFichero(Long.toString(file.getSize()));
			}
			
			informeInvestigacion.setAsignacion(asignacion);
			informeInvestigacionService.saveOrUpdate(informeInvestigacion);
			ResponseBaseOperacion response = new ResponseBaseOperacion(Constantes.CREATED_MESSAGE, informeInvestigacion);
			return new ResponseEntity<ResponseBaseOperacion>(response, HttpStatus.OK);
		}
		catch(Exception e) {
			
			return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping(value="/update/asignacion/{asignacionId}/informeInvestigacion/{informeAsignacionId}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseBaseOperacion> updateInformeInvestigacion(@PathVariable(value="asignacionId") Integer asignacionId,
			@PathVariable(value="informeAsignacionId") Integer informeAsignacionId, @RequestParam(value=Constantes.INFORME_INVESTIGACION_JSON_PARAM, required=true) String informeJson,
			@RequestParam(value=Constantes.INFORME_INVESTIGACION_FILE_PARAM, required=true) MultipartFile file) {
		
		Asignacion asignacion = null;
		InformeInvestigacion informeInvestigacionOld = null;
		
		try {
			
			if(asignacionId != null && asignacionId > 0) {
				
				asignacion = asignacionService.getByAsignacionId(asignacionId);
				if(informeAsignacionId != null && informeAsignacionId > 0) {
					informeInvestigacionOld = informeInvestigacionService.getByInformeAsignacionId(informeAsignacionId);
					if(informeInvestigacionOld == null) {
						
						return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.NO_CONTENT);
					}
				}
			}
			
			InformeInvestigacion informeInvestigacion = objectMapper.readValue(informeJson, InformeInvestigacion.class);
			
			if(!file.isEmpty()) {
				
				if(informeInvestigacionOld.getInformeAsignacionId() != null && informeInvestigacionOld.getInformeAsignacionId() > 0 
						&& informeInvestigacionOld.getNombreFichero() != null && informeInvestigacionOld.getNombreFichero().length() > 0) {
					
					amazonService.deleteFile(informeInvestigacionOld.getNombreFichero());
				}
				
				String nombreFichero = amazonService.uploadFile(file);
				informeInvestigacionOld.setNombreFichero(nombreFichero);
				informeInvestigacionOld.setFormatoFichero(file.getContentType());
				informeInvestigacionOld.setTamanioFichero(Long.toString(file.getSize()));
			}
			
			informeInvestigacionOld.setAsignacion(asignacion);
			informeInvestigacionOld.setAsunto(informeInvestigacion.getAsunto());
			informeInvestigacionService.saveOrUpdate(informeInvestigacionOld);
			ResponseBaseOperacion response = new ResponseBaseOperacion(Constantes.UPDATED_MESSAGE, informeInvestigacionOld);
			return new ResponseEntity<ResponseBaseOperacion>(response, HttpStatus.OK);
			
		}
		catch(Exception e) {
			
			return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping(value=Constantes.DOWNLOAD_URI)
	public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String filename, HttpServletRequest request) {
		
		 ByteArrayInputStream bis = amazonService.getResource(filename);
		
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, String.format(Constantes.FILE_DOWNLOAD_HTTP_HEADER, filename))
				.body(new InputStreamResource(bis));
	}
	
	@GetMapping(value=Constantes.VIEW_PDF_URI)
	public ResponseEntity<InputStreamResource> viewPDF(@PathVariable String filename, HttpServletRequest request) {
		
		 ByteArrayInputStream bis = amazonService.getResource(filename);
			
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, String.format(Constantes.VIEW_PDF_HTTP_HEADER, filename))
				.body(new InputStreamResource(bis));
	}
}
