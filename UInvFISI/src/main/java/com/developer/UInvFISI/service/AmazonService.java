package com.developer.UInvFISI.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.springframework.web.multipart.MultipartFile;

public interface AmazonService {

	String uploadFile(MultipartFile file);
	
	void deleteFile(String fileName);
	
	ByteArrayOutputStream loadResource(String fileName);
	
	ByteArrayInputStream getResource(String fileName);
}
