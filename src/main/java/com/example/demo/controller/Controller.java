package com.example.demo.controller;

import com.example.demo.Utils;
import com.example.demo.model.Classifier;
import com.example.demo.model.TensorFlowObjectDetectionAPIModel;
import com.google.gson.Gson;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Vector;

@RestController
public class Controller implements InitializingBean {
    public static final String MODEL_NAME = "ssd_mobilenet_v1_android_export.pb";
    public static final String LABEL_NAME = "coco_labels_list.txt";
    public static final int INPUT_SIZE = 300;

    private String resourcePath = getClass().getClassLoader().getResource("").getPath() + "static/";
    private byte[] model;
    private Vector<String> label = new Vector<>();
    private TensorFlowObjectDetectionAPIModel apiModel;
//    private tesseract.TessBaseAPI tessBaseAPI = new tesseract.TessBaseAPI();

    @PostMapping(value = "/tensorflow")
    public String tensorflow(@RequestBody String image) throws Exception {

        image = image.replace("\"", "");
        image = image.replace("\\n", "");
        System.out.println(image);
        if (image.isEmpty()) {
            return null;
        }
        byte originalImageByte[] = Base64.getDecoder().decode(image);
        InputStream inputStream = new ByteArrayInputStream(originalImageByte);
        BufferedImage original = ImageIO.read(inputStream);
        if (original == null) {
            return null;
        }
        BufferedImage resized = Utils.resizeImage(original, original.getType());
        System.out.println(resized.getWidth()+","+resized.getHeight());
        byte resizedImageByte[] = ((DataBufferByte) resized.getRaster().getDataBuffer()).getData();
        System.out.println(resizedImageByte.length + "");
        List<Classifier.Recognition> results = apiModel.recognizeImage(originalImageByte);
        Gson gson = new Gson();
        return gson.toJson(results);
    }

    @PostMapping(value = "/file")
    public String file(@RequestPart("file") MultipartFile multipartFile) throws Exception {
//        System.out.println(multipartFile);
        byte b[] = multipartFile.getBytes();
        Image image = null;
        if (multipartFile.getContentType().contains("image")) {
            image = ImageIO.read(new ByteArrayInputStream(b));
        }
        String result = image.getWidth(null) + "," + image.getHeight(null);
        Gson gson = new Gson();
        System.out.println(gson.toJson(result));
        return gson.toJson(result);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        URL modelUrl = new URL("https://github.com/vietcoscc/TensorflowAPI/raw/master/src/main/resources/static/ssd_mobilenet_v1_android_export.pb");
        URL labelUrl = new URL("https://raw.githubusercontent.com/vietcoscc/TensorflowAPI/master/src/main/resources/static/coco_labels_list.txt");
        InputStream modelStream = modelUrl.openStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(modelStream, outputStream);
        model = outputStream.toByteArray();
        System.out.println(model.length + "");
        BufferedReader in = new BufferedReader(new InputStreamReader(labelUrl.openStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            label.add(inputLine);
            System.out.println("Line : " + inputLine);
        }
        in.close();
        apiModel = TensorFlowObjectDetectionAPIModel.create(model, label, INPUT_SIZE);
    }

}
