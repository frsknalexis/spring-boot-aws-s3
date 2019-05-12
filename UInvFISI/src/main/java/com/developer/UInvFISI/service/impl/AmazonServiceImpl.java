package com.developer.UInvFISI.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.developer.UInvFISI.service.AmazonService;

@Service("amazonService")
public class AmazonServiceImpl implements AmazonService {

	@Value("${dev.s3.bucket}")
	private String awsBucketName;
	
	@Autowired
	private AmazonS3 amazonS3;
	
	@Override
	public String uploadFile(MultipartFile multipartFile) {
	
		try {
			
			String fileName = "";
			if(!multipartFile.isEmpty()) {
				
				File file = convertMultipartFileToFile(multipartFile);
				fileName = multipartFile.getOriginalFilename();
				uploadFileToS3Bucket(fileName, file, awsBucketName);
			}
			return fileName;
		}
		catch(AmazonServiceException ase) {
			ase.getMessage();
		}
		return null;
	}

	@Override
	public void deleteFile(String fileName) {
		
		try {
			
			deleteFileFromS3Bucket(fileName, awsBucketName);
		}
		catch(AmazonServiceException ase) {
			ase.getMessage();
		}
	}
	
	private void uploadFileToS3Bucket(String fileName, File file, String bucketName) {
		
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file);
		putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
		amazonS3.putObject(putObjectRequest);
	}
	
	private S3Object getObjectFromS3Bucket(String fileName, String bucketName) {
		
		S3Object s3Object = amazonS3.getObject(bucketName, fileName);
		return s3Object;
	}
	
	private void deleteFileFromS3Bucket(String fileName, String bucketName) {
		
		DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, fileName);
		amazonS3.deleteObject(deleteObjectRequest);
	}
	
	private File convertMultipartFileToFile(MultipartFile file) {
		
		File convertedFile = new File(file.getOriginalFilename());
		
		try {
			
			FileOutputStream fos = new FileOutputStream(convertedFile);
			fos.write(file.getBytes());
			fos.close();
		}
		catch(IOException e) {
			e.getMessage();
		}
		return convertedFile;
	}
}
