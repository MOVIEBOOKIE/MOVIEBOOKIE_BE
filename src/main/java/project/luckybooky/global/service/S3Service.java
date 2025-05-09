package project.luckybooky.global.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.nimbusds.oauth2.sdk.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
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
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${spring.cloud.aws.s3.bucketName}")
    private String bucketName;

    // 이미지 단 건 업로드
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

    // 이미지 다 건 업로드
    public List<String> uploadFiles(List<MultipartFile> files) {
        List<String> fileUrls = new ArrayList<>();
        String fileName = "";
        ObjectMetadata metadata = new ObjectMetadata();

        for (MultipartFile file : files) {
            validateFile(file);

            fileName = file.getOriginalFilename();

            // 이미 존재하는지 확인
            if (amazonS3.doesObjectExist(bucketName, fileName)) {
                fileUrls.add(amazonS3.getUrl(bucketName, fileName).toString());
            }
            else {
                putImage(fileName, metadata, file);
                fileUrls.add(amazonS3.getUrl(bucketName, fileName).toString());
            }
        }

        return fileUrls;
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

    public void deleteImageFromS3(String imageAddress){
        String key = getKeyFromImageAddress(imageAddress);
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (Exception e){
            throw new S3FailureHandler(ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }

    public void deleteImagesFromS3(List<String> imageAddresses) {
        imageAddresses.stream().forEach(img -> {
            String key = getKeyFromImageAddress(img);
            try {
                amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
            } catch (Exception e) {
                throw new S3FailureHandler(ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
            }
            }
        );
    }

    private String getKeyFromImageAddress(String imageAddress){
        try {
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8");
            return decodingKey.substring(1); // 맨 앞의 '/' 제거
        } catch (MalformedURLException | UnsupportedEncodingException e){
            throw new S3FailureHandler(ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }
}
