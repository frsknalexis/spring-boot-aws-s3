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

import com.developer.UInvFISI.entity.InformeTrabajo;
import com.developer.UInvFISI.entity.Trabajo;
import com.developer.UInvFISI.service.AmazonService;
import com.developer.UInvFISI.service.InformeTrabajoService;
import com.developer.UInvFISI.service.TrabajoService;
import com.developer.UInvFISI.util.Constantes;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/informetrabajo")
public class InformeTrabajoRestController {

	@Autowired
	@Qualifier("informeTrabajoService")
	private InformeTrabajoService informeTrabajoService;
	
	@Autowired
	@Qualifier("trabajoService")
	private TrabajoService trabajoService;
	
	@Autowired
	@Qualifier("amazonService")
	private AmazonService amazonService;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@GetMapping("/informestrabajos")
	public ResponseEntity<List<InformeTrabajo>> findAll() {
		
		try {
			
			List<InformeTrabajo> trabajos = informeTrabajoService.findAll();
			if(trabajos.isEmpty()) {
				
				return new ResponseEntity<List<InformeTrabajo>>(HttpStatus.NO_CONTENT);
			}
			else {
				
				return new ResponseEntity<List<InformeTrabajo>>(trabajos, HttpStatus.OK);
			}
		}
		catch(Exception e) {
			
			return new ResponseEntity<List<InformeTrabajo>>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/informestrabajos/trabajo/{trabajoId}")
	public ResponseEntity<List<InformeTrabajo>> findInformesTrabajosByTrabajoId(@PathVariable(value="trabajoId") Integer trabajoId) {
		
		try {
			
			List<InformeTrabajo> trabajos = informeTrabajoService.findByTrabajoTrabajoId(trabajoId);
			if(trabajos.isEmpty()) {
				
				return new ResponseEntity<List<InformeTrabajo>>(HttpStatus.NO_CONTENT);
			}
			else {
				
				return new ResponseEntity<List<InformeTrabajo>>(trabajos, HttpStatus.OK);
			}
		}
		catch(Exception e) {
			
			return new ResponseEntity<List<InformeTrabajo>>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value="/save/trabajo/{trabajoId}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseBaseOperacion> saveInformeTrabajo(@PathVariable(value="trabajoId") Integer trabajoId,
			@RequestParam(value=Constantes.INFORME_TRABAJO_JSON_PARAM, required=true) String informeTrabajoJson,
			@RequestParam(value=Constantes.INFORME_TRABAJO_FILE_PARAM, required=true) MultipartFile file) {
		
		Trabajo trabajo = null;
		
		try {
			
			if(trabajoId != null && trabajoId > 0) {
				
				trabajo = trabajoService.getByTrabajoId(trabajoId);
				if(trabajo == null) {
					
					return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.NO_CONTENT);
				}
			}
			
			InformeTrabajo informeTrabajo = objectMapper.readValue(informeTrabajoJson, InformeTrabajo.class);
			
			if(!file.isEmpty()) {
				
				String nombreFichero = amazonService.uploadFile(file);
				informeTrabajo.setNombreFichero(nombreFichero);
				informeTrabajo.setFormatoFichero(file.getContentType());
				informeTrabajo.setTamanioFichero(Long.toString(file.getSize()));
			}
			
			informeTrabajo.setTrabajo(trabajo);
			informeTrabajoService.saveOrUpdate(informeTrabajo);
			ResponseBaseOperacion response = new ResponseBaseOperacion(Constantes.CREATED_MESSAGE, informeTrabajo);
			return new ResponseEntity<ResponseBaseOperacion>(response, HttpStatus.CREATED);
		}
		catch(Exception e) {
			
			return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping(value="/update/trabajo/{trabajoId}/informeTrabajo/{informeTrabajoId}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseBaseOperacion> updateInformeTrabajo(@PathVariable(value="trabajoId") Integer trabajoId,
			@PathVariable(value="informeTrabajoId") Integer informeTrabajoId, @RequestParam(value=Constantes.INFORME_TRABAJO_JSON_PARAM, required=true) String informeTrabajoJson,
			@RequestParam(value=Constantes.INFORME_TRABAJO_FILE_PARAM, required=true) MultipartFile file) {
		
		Trabajo trabajo = null;
		InformeTrabajo informeTrabajoOld = null;
		
		try {
			
			if(trabajoId != null && trabajoId > 0) {
				
				trabajo = trabajoService.getByTrabajoId(trabajoId);
				if(informeTrabajoId != null && informeTrabajoId > 0) {
					
					informeTrabajoOld = informeTrabajoService.getByInformeTrabajoId(informeTrabajoId);
					if(informeTrabajoOld == null) {
						
						return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.NO_CONTENT);
					}
				}
			}
			
			InformeTrabajo informeTrabajo = objectMapper.readValue(informeTrabajoJson, InformeTrabajo.class);
			
			if(!file.isEmpty()) {
				
				if(informeTrabajoOld.getInformeTrabajoId() != null && informeTrabajoOld.getInformeTrabajoId() > 0
						&& informeTrabajoOld.getNombreFichero() != null && informeTrabajoOld.getNombreFichero().length() > 0) {
					
					amazonService.deleteFile(informeTrabajoOld.getNombreFichero());
				}
				
				String nombreFichero = amazonService.uploadFile(file);
				informeTrabajoOld.setNombreFichero(nombreFichero);
				informeTrabajoOld.setFormatoFichero(file.getContentType());
				informeTrabajoOld.setTamanioFichero(Long.toString(file.getSize()));
			}
			
			informeTrabajoOld.setTrabajo(trabajo);
			informeTrabajoOld.setAsunto(informeTrabajo.getAsunto());
			informeTrabajoService.saveOrUpdate(informeTrabajoOld);
			ResponseBaseOperacion response = new ResponseBaseOperacion(Constantes.UPDATED_MESSAGE, informeTrabajoOld);
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
