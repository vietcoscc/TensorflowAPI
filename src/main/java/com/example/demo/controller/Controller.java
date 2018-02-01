package com.example.demo.controller;

import com.example.demo.Utils;
import com.example.demo.model.Classifier;
import com.example.demo.model.TensorFlowObjectDetectionAPIModel;
import com.google.gson.Gson;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.net.URL;
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

    @PostMapping(value = "/tensorflow")
    public String file(@RequestPart("image") MultipartFile multipartFile) throws Exception {
        byte originalImageByte[] = multipartFile.getBytes();
        BufferedImage original = null;
        if (multipartFile.getContentType().contains("image")) {
            original = ImageIO.read(new ByteArrayInputStream(originalImageByte));
        }
        if (original == null) {
            return null;
        }
        BufferedImage resized = Utils.resizeImage(original, original.getType());

        byte resizedImageByte[] = ((DataBufferByte) resized.getRaster().getDataBuffer()).getData();

        System.out.println(resized.getWidth() + "," + resized.getHeight());
        List<Classifier.Recognition> results = apiModel.recognizeImage(resizedImageByte);
        for (int i = 0; i < results.size(); i++) {
            Classifier.Recognition result = results.get(i);
            result.setLocation(Utils.resized2original(resized, original, result.getLocation()));
        }

        Gson gson = new Gson();
        return gson.toJson(results);
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
        BufferedReader in = new BufferedReader(
                new InputStreamReader(labelUrl.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            label.add(inputLine);
            System.out.println("Line : " + inputLine);
        }
        in.close();

        apiModel = TensorFlowObjectDetectionAPIModel.create(model, label, INPUT_SIZE);
    }

}
