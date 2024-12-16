package com.detector.imagedetection.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.detector.imagedetection.exceptions.model.BadRequestException;
import com.detector.imagedetection.exceptions.model.InternalServerErrorException;
import com.detector.imagedetection.external.ImaggaService;
import com.detector.imagedetection.model.Image;
import com.detector.imagedetection.model.Tag;
import com.detector.imagedetection.repository.ImageRepository;

@Service
public class ImageService {

    @Value("${imagga.api.key}")
    private String imaggaApiKeyString;

    @Value("${imagga.api.secret}")
    private String imaggaApiSecret;

    @Value("${imagga.api.url}")
    private String imagga_endpoint_url;

    private final ImageRepository imageRepository;
    private final ImaggaService imaggaService;

    public ImageService(ImageRepository imageRepository, ImaggaService imaggaService) {
        this.imageRepository = imageRepository;
        this.imaggaService = imaggaService;
    }

    /**
     * Get a list of all Images
     * @return 
     */
    public List<Image> getImages() {
        return imageRepository.findAll();
    }

    /**
     * Get image by Id
     * @param id
     * @return
     */
    public Image getImageById(Long id) {

        if(imageRepository.findById(id).isPresent()){
            return imageRepository.findById(id).get();
        }
        return null;
    }

    /**
     * Detect image from image URL
     * @param url
     * @param label
     * @param detectImage
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public Image detectImageFromUrl(String url, String label, boolean detectImage) {
        if(StringUtils.isEmpty(url)) {
            throw new BadRequestException("Image URL not supplied");
        }
        Image image = new Image();

        if(detectImage) {
            try {
                BufferedReader connectionInput = new BufferedReader(new InputStreamReader(imaggaService.getStsreamForImageUrl(url)));

                String jsonResponse = connectionInput.readLine();
                processJsonResponse(image, jsonResponse);

                connectionInput.close();
            } catch (IOException e) {
                throw new InternalServerErrorException(e.getMessage());
            } catch (JSONException e) {
                throw new InternalServerErrorException(e.getMessage());
            }
        }
        setImageLabel(label, image);
        imageRepository.save(image);

        return image;
    }

    /**
     * Get all images by given list of tags
     * @param tags
     * @return
     */
    //TODO - Use native query for retrieval
    public List<Image> getImagesByTags(String tags) {
        List<Image> images = new ArrayList<>();
        List<String> tagList = Arrays.asList(tags.split(","));
        imageRepository.findAll().forEach(image -> {
            tagList.forEach(inputTag -> {
                image.getTags().forEach(tag -> {
                    if(tag.getDescription().equals(inputTag)) {
                        images.add(image);
                    }
                });
            });
        });
        return images;
    }

    /**
     * Detect image from file path
     * @param filepath
     * @param label
     * @param detectImage
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public Image detectImageFromFile(String filepath, String label, boolean detectImage) {
        if(StringUtils.isEmpty(filepath)) {
            throw new BadRequestException("Image filepath not supplied");
        }
        Image image = new Image();

        if(detectImage) {
            try {
                File fileToUpload = new File(filepath);

            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "Image Upload";

            HttpURLConnection connection = getConnection(imagga_endpoint_url);
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty(
                "Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream request = new DataOutputStream(connection.getOutputStream());

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" + fileToUpload.getName() + "\"" + crlf);
            request.writeBytes(crlf);


            InputStream inputStream = new FileInputStream(fileToUpload);
            int bytesRead;
            byte[] dataBuffer = new byte[1024];
            while ((bytesRead = inputStream.read(dataBuffer)) != -1) {
            request.write(dataBuffer, 0, bytesRead);
            }

            request.writeBytes(crlf);
            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
            request.flush();
            request.close();

            InputStream responseStream = new BufferedInputStream(connection.getInputStream());

            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

            String line = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();

            String response = stringBuilder.toString();
            processJsonResponse(image, response);

            inputStream.close();
            responseStream.close();
            connection.disconnect();
            } catch (IOException e) {
                throw new InternalServerErrorException(e.getMessage());
            } catch (JSONException e) {
                throw new InternalServerErrorException(e.getMessage());
            }
        }

        setImageLabel(label, image);
        imageRepository.save(image);

        return image;
    }

    /**
     * Process Imagga image detection JSON response
     * @param image
     * @param jsonResponse
     * @throws JSONException
     */
    private void processJsonResponse(Image image, String jsonResponse) throws JSONException {
        //TODO Utilize a JSON library to serialize json response
        JSONObject json = new JSONObject(jsonResponse.toString());

        JSONObject tags = new JSONObject(json.get("result").toString());

        JSONArray resultTag = tags.getJSONArray("tags");

        for(int i = 0; i < resultTag.length(); i++) {
            Tag imageTag = new Tag();
            if(Double.parseDouble(resultTag.getJSONObject(i).get("confidence").toString()) > 30) {
                JSONObject tag = new JSONObject(resultTag.getJSONObject(i).get("tag").toString());
                imageTag.setDescription(tag.get("en").toString());
                image.getTags().add(imageTag);
            }
        }
    }

    /**
     * Initialize HTTP Url Connection
     * @param requestUrl
     * @return
     * @throws IOException
     */
    private HttpURLConnection getConnection(String requestUrl) throws IOException {
        String credentialsToEncode = imaggaApiKeyString + ":" + imaggaApiSecret;
        String basicAuth = Base64.getEncoder().encodeToString(credentialsToEncode.getBytes(StandardCharsets.UTF_8));

        
        URL urlObject = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();

        connection.setRequestProperty("Authorization", "Basic " + basicAuth);

        return connection;
    }

    /**
     * Set Image label
     * @param label
     * @param image
     */
    private void setImageLabel(String label, Image image) {
        if(StringUtils.isNotEmpty(label)) {
            image.setLabel(label);
        } else {
            image.setLabel(RandomStringUtils.randomAlphabetic(10));
        }
    }
    
}
