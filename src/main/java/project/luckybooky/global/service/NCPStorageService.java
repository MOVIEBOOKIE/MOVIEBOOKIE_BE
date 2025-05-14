package project.luckybooky.global.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.handler.S3FailureHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NCPStorageService {
    private final AmazonS3 amazonS3;
    @Value("${ncp.storage.bucketName}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
        validateFile(file);

        String fileName = file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();

        // 이미 존재하는지 확인
        if (amazonS3.doesObjectExist(bucketName, fileName)) {
            return amazonS3.getUrl(bucketName, fileName).toString();
        }

        putImage(fileName, metadata, file);

        // 업로드된 파일의 URL 반환
        return amazonS3.getUrl(bucketName, fileName).toString();
    }

    // s3에 등록
    private void putImage(String fileName, ObjectMetadata metadata, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new S3FailureHandler(ErrorCode.FILE_NOT_UPLOADED);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new S3FailureHandler(ErrorCode.FILE_IS_EMPTY);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new S3FailureHandler(ErrorCode.FILE_NOT_IMAGE);
        }
    }
}
