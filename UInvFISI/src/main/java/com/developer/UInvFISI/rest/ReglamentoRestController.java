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

import com.developer.UInvFISI.entity.Reglamento;
import com.developer.UInvFISI.service.AmazonService;
import com.developer.UInvFISI.service.ReglamentoService;
import com.developer.UInvFISI.util.Constantes;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/reglamento")
public class ReglamentoRestController {

	@Autowired
	@Qualifier("reglamentoService")
	private ReglamentoService reglamentoService;
	
	@Autowired
	@Qualifier("amazonService")
	private AmazonService amazonService;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@GetMapping("/reglamentos")
	public ResponseEntity<List<Reglamento>> findAll() {
		
		try {
			
			List<Reglamento> reglamentos = reglamentoService.findAll();
			if(reglamentos.isEmpty()) {
				
				return new ResponseEntity<List<Reglamento>>(HttpStatus.NO_CONTENT);
			}
			else {
				
				return new ResponseEntity<List<Reglamento>>(reglamentos, HttpStatus.OK);
			}
		}
		catch(Exception e) {
			
			return new ResponseEntity<List<Reglamento>>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value="/save", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseBaseOperacion> saveReglamento(@RequestParam(value=Constantes.REGLAMENTO_FILE_PARAM, required=true) MultipartFile file,
			@RequestParam(value=Constantes.REGLAMENTO_JSON_PARAM, required=true) String reglamentoJson) {
		
		try {
			
			Reglamento reglamento = objectMapper.readValue(reglamentoJson, Reglamento.class);
			if(!file.isEmpty()) {
				
				String nombreFichero = amazonService.uploadFile(file);
				reglamento.setNombreFichero(nombreFichero);
				reglamento.setFormatoFichero(file.getContentType());
				reglamento.setTamanioFichero(Long.toString(file.getSize()));
			}
			
			reglamentoService.saveOrUpdate(reglamento);
			ResponseBaseOperacion response = new ResponseBaseOperacion(Constantes.CREATED_MESSAGE, reglamento);
			return new ResponseEntity<ResponseBaseOperacion>(response, HttpStatus.CREATED);
		}
		catch(Exception e) {
			
			return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping(value="/update/{reglamentoId}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseBaseOperacion> updateReglamento(@PathVariable(value="reglamentoId") Integer reglamentoId,
			@RequestParam(value=Constantes.REGLAMENTO_FILE_PARAM, required=true) MultipartFile file, 
			@RequestParam(value=Constantes.REGLAMENTO_JSON_PARAM, required=true) String reglamentoJson) {
		
		Reglamento reglamentoOld = null;
		
		try {
			
			if(reglamentoId != null && reglamentoId > 0) {
				
				reglamentoOld = reglamentoService.getByReglamentoId(reglamentoId);
				if(reglamentoOld == null) {
					
					return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.NO_CONTENT); 
				}
			}
			
			Reglamento reglamento = objectMapper.readValue(reglamentoJson, Reglamento.class);
			
			if(!file.isEmpty()) {
				
				if(reglamentoOld.getReglamentoId()!= null && reglamentoOld.getReglamentoId() > 0
					&& reglamentoOld.getNombreFichero() != null && reglamentoOld.getNombreFichero().length() > 0) {
					
					amazonService.deleteFile(reglamentoOld.getNombreFichero());
				}
				
				String nombreFichero = amazonService.uploadFile(file);
				reglamentoOld.setNombreFichero(nombreFichero);
				reglamentoOld.setFormatoFichero(file.getContentType());
				reglamentoOld.setTamanioFichero(Long.toString(file.getSize()));
			}
			
			reglamentoOld.setAsunto(reglamento.getAsunto());
			reglamentoService.saveOrUpdate(reglamentoOld);
			ResponseBaseOperacion response = new ResponseBaseOperacion(Constantes.UPDATED_MESSAGE, reglamentoOld);
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
