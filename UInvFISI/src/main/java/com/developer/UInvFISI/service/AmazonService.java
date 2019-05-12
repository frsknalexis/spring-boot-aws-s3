package com.developer.UInvFISI.service;

import org.springframework.web.multipart.MultipartFile;

public interface AmazonService {

	String uploadFile(MultipartFile file);
	
	void deleteFile(String fileName);
}
