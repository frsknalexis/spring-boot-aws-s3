package com.developer.UInvFISI.rest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

import com.developer.UInvFISI.entity.AsignacionDocente;
import com.developer.UInvFISI.entity.InformeTrimestral;
import com.developer.UInvFISI.service.AsignacionDocenteService;
import com.developer.UInvFISI.service.InformeTrimestralService;
import com.developer.UInvFISI.util.Constantes;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/informetrimestral")
public class InformeTrimestralRestController {

	@Autowired
	@Qualifier("informeTrimestralService")
	private InformeTrimestralService informeTrimestralService;
	
	@Autowired
	@Qualifier("asignacionDocenteService")
	private AsignacionDocenteService asignacionDocenteService;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@GetMapping("/informestrimestrales")
	public ResponseEntity<List<InformeTrimestral>> findAll() {
		
		try {
			
			List<InformeTrimestral> informesTrimestrales = informeTrimestralService.findAll();
			if(informesTrimestrales.isEmpty()) {
				
				return new ResponseEntity<List<InformeTrimestral>>(HttpStatus.NO_CONTENT);
			}
			else {
				
				return new ResponseEntity<List<InformeTrimestral>>(informesTrimestrales, HttpStatus.OK);
			}
		}
		catch(Exception e) {
			
			return new ResponseEntity<List<InformeTrimestral>>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/informestrimestrales/asignacion/{asignacionId}")
	public ResponseEntity<List<InformeTrimestral>> findInformesTrimestralesByAsignacionId(@PathVariable(value="asignacionId") Integer asignacionId) {
		
		try {
			
			List<InformeTrimestral> informesTrimestrales = informeTrimestralService.findByAsignacionDetalleAsignacionAsignacionId(asignacionId);
			if(informesTrimestrales.isEmpty()) {
				
				return new ResponseEntity<List<InformeTrimestral>>(HttpStatus.NO_CONTENT);
			}
			else {
				
				return ResponseEntity.ok()
						.body(informesTrimestrales);
			}
		}
		catch(Exception e) {
			
			return new ResponseEntity<List<InformeTrimestral>>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/informestrimestrales/asignacionDocente/{asignacionDetalleId}")
	public ResponseEntity<List<InformeTrimestral>> findInformesTrimestralesByAsignacionDetalleId(@PathVariable(value="asignacionDetalleId") Integer asignacionDetalleId) {
		
		try {
			
			List<InformeTrimestral> informesTrimestrales = informeTrimestralService.findByAsignacionDetalleAsignacionDetalleId(asignacionDetalleId);
			if(informesTrimestrales.isEmpty()) {
				
				return new ResponseEntity<List<InformeTrimestral>>(HttpStatus.NO_CONTENT);
			}
			else {
				
				return new ResponseEntity<List<InformeTrimestral>>(informesTrimestrales, HttpStatus.OK);
			}
		}
		catch(Exception e) {
			
			return new ResponseEntity<List<InformeTrimestral>>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value="/save/asignacionDocente/{asignacionDetalleId}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseBaseOperacion> saveInformeTrimestral(@PathVariable(value="asignacionDetalleId") Integer asignacionDetalleId,
			@RequestParam(value=Constantes.INFORME_TRIMESTRAL_JSON_PARAM, required=true) String informeTrimestralJson,
			@RequestParam(value=Constantes.INFORME_TRIMESTRAL_FILE_PARAM, required=true) MultipartFile file) {
		
		AsignacionDocente asignacionDetalle = null;
		
		try {
			
			if(asignacionDetalleId != null && asignacionDetalleId > 0) {
				
				asignacionDetalle = asignacionDocenteService.getAsignacionDocenteById(asignacionDetalleId);
				if(asignacionDetalle == null) {
					
					return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.NO_CONTENT);
				}
			}
			
			InformeTrimestral informeTrimestral = objectMapper.readValue(informeTrimestralJson, InformeTrimestral.class);
			
			if(!file.isEmpty()) {
				
				Path rootPath = Paths.get(Constantes.UPLOAD_FOLDER_BASE).resolve(Constantes.FOLDER_INFORMES_TRIMESTRALES)
										.resolve(file.getOriginalFilename());
				Path rootAbsolutePath = rootPath.toAbsolutePath();
				
				try {
					
					Files.copy(file.getInputStream(), rootAbsolutePath);
					informeTrimestral.setNombreFichero(file.getOriginalFilename());
					informeTrimestral.setFormatoFichero(file.getContentType());
					informeTrimestral.setTamanioFichero(file.getSize());
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			
			informeTrimestral.setAsignacionDetalle(asignacionDetalle);
			informeTrimestralService.saveOrUpdate(informeTrimestral);
			ResponseBaseOperacion response = new ResponseBaseOperacion(Constantes.CREATED_MESSAGE, informeTrimestral);
			return new ResponseEntity<ResponseBaseOperacion>(response, HttpStatus.CREATED);
		}
		catch(Exception e) {
			
			return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping(value="/update/asignacionDocente/{asignacionDetalleId}/informeTrimestral/{informeTrimestralId}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseBaseOperacion> updateInformeTrimestral(@PathVariable(value="asignacionDetalleId") Integer asignacionDetalleId,
			@PathVariable(value="informeTrimestralId") Integer informeTrimestralId, 
			@RequestParam(value=Constantes.INFORME_TRIMESTRAL_JSON_PARAM, required=true) String informeTrimestralJson,
			@RequestParam(value=Constantes.INFORME_TRIMESTRAL_FILE_PARAM, required=true) MultipartFile file) {
		
		AsignacionDocente asignacionDetalle = null;
		InformeTrimestral informeTrimestralOld = null;
		
		try {
			
			if(asignacionDetalleId != null && asignacionDetalleId > 0) {
				
				asignacionDetalle = asignacionDocenteService.getAsignacionDocenteById(asignacionDetalleId);
				if(informeTrimestralId != null && informeTrimestralId > 0) {
					
					informeTrimestralOld = informeTrimestralService.getInformeTrimestralById(informeTrimestralId);
					if(informeTrimestralOld == null) {
						
						return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.NO_CONTENT);
					}
				}
			}
			
			InformeTrimestral informeTrimestral = objectMapper.readValue(informeTrimestralJson, InformeTrimestral.class);
			
			if(!file.isEmpty()) {
				
				if(informeTrimestralOld.getInformeTrimestralId() != null && informeTrimestralOld.getInformeTrimestralId() > 0
						&& informeTrimestralOld.getNombreFichero() != null && informeTrimestralOld.getNombreFichero().length() > 0) {
					
					Path rootPath = Paths.get(Constantes.UPLOAD_FOLDER_BASE).resolve(Constantes.FOLDER_INFORMES_TRIMESTRALES)
											.resolve(informeTrimestralOld.getNombreFichero()).toAbsolutePath();
					File archivo = rootPath.toFile();
					
					if(archivo.exists() && archivo.canRead()) {
						archivo.delete();
					}
				}
				
				Path rootPath = Paths.get(Constantes.UPLOAD_FOLDER_BASE).resolve(Constantes.FOLDER_INFORMES_TRIMESTRALES)
										.resolve(file.getOriginalFilename());
				Path rootAbsolutePath = rootPath.toAbsolutePath();
				
				try {
					
					Files.copy(file.getInputStream(), rootAbsolutePath);
					informeTrimestralOld.setNombreFichero(file.getOriginalFilename());
					informeTrimestralOld.setFormatoFichero(file.getContentType());
					informeTrimestralOld.setTamanioFichero(file.getSize());
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			
			informeTrimestralOld.setAsignacionDetalle(asignacionDetalle);
			informeTrimestralOld.setDescripcion(informeTrimestral.getDescripcion());
			informeTrimestralOld.setTrimestre(informeTrimestral.getTrimestre());
			informeTrimestralService.saveOrUpdate(informeTrimestralOld);
			ResponseBaseOperacion response = new ResponseBaseOperacion(Constantes.UPDATED_MESSAGE, informeTrimestralOld);
			return ResponseEntity.ok()
					.body(response);
			
		}
		catch(Exception e) {
			
			return new ResponseEntity<ResponseBaseOperacion>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping(value=Constantes.DOWNLOAD_URI)
	public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request) {
		
		Path pathFile = Paths.get(Constantes.UPLOAD_FOLDER_BASE).resolve(Constantes.FOLDER_INFORMES_TRIMESTRALES)
								.resolve(filename).toAbsolutePath();
		Resource resource = null;
		
		try {
			
			resource = new UrlResource(pathFile.toUri());
			if(!resource.exists() || !resource.isReadable()) {
				throw new RuntimeException("Error: no se puede leer el archivo: " + pathFile.toString());
			}
		}
		catch(MalformedURLException e) {
			e.printStackTrace();
		}
		
		String contentType = null;
		
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		if(contentType == null) {
			contentType = Constantes.DEFAULT_CONTENT_TYPE;
		}
		
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, String.format(Constantes.FILE_DOWNLOAD_HTTP_HEADER, resource.getFilename()))
				.body(resource);
	}
	
	@GetMapping(value=Constantes.VIEW_PDF_URI)
	public ResponseEntity<Resource> viewPDF(@PathVariable String filename, HttpServletRequest request) {
		
		Path pathFile = Paths.get(Constantes.UPLOAD_FOLDER_BASE).resolve(Constantes.FOLDER_INFORMES_TRIMESTRALES)
									.resolve(filename).toAbsolutePath();
		Resource resource = null;
		
		try {
			resource = new UrlResource(pathFile.toUri());
			if(!resource.exists() || !resource.isReadable()) {
				throw new RuntimeException("Error: no se puede leer el archivo: " + pathFile.toString());
			}
		}
		catch(MalformedURLException e) {
			e.printStackTrace();
		}
		
		String contentType = null;
		
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		if(contentType == null) {
			contentType = Constantes.DEFAULT_CONTENT_TYPE;
		}
		
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, String.format(Constantes.VIEW_PDF_HTTP_HEADER, resource.getFilename()))
				.body(resource);
	}
}
