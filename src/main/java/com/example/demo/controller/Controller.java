package com.example.demo.controller;

import com.example.demo.Utils;
import com.example.demo.model.Classifier;
import com.example.demo.model.TensorFlowObjectDetectionAPIModel;
import com.google.gson.Gson;
import org.bytedeco.javacpp.tesseract;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private Vector<String> label;
    private TensorFlowObjectDetectionAPIModel apiModel;
    private tesseract.TessBaseAPI tessBaseAPI = new tesseract.TessBaseAPI();

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

        byte resizedImageByte[] = ((DataBufferByte) resized.getRaster().getDataBuffer()).getData();
//        long start = System.currentTimeMillis();

//        if (tessBaseAPI.Init(resourcePath, "eng") != 0) {
//            System.err.println("Could not initialize tesseract.");
//            System.exit(1);
//        }
//        tessBaseAPI.SetImage(resizedImageByte,300,300,3,300*3);
//        BytePointer r = tessBaseAPI.GetUTF8Text();
//        System.out.println(r.getString()+"a");

        List<Classifier.Recognition> results = apiModel.recognizeImage(resizedImageByte);
//        for (int i = 0; i < results.size(); i++) {
//            Classifier.Recognition result = results.get(i);
//            result.setLocation(Utils.resized2original(resized, original, result.getLocation()));
//        }
//        long end = System.currentTimeMillis() - start;
//        RectF rectF = results.get(0).getLocation();
//        BufferedImage cropedImage = original.getSubimage((int) rectF.left, (int) rectF.top, (int) (rectF.right - rectF.left), (int) (rectF.bottom - rectF.top));
//
//        Mat cropedMat = Utils.BufferedImage2Mat(cropedImage);
//        MatOfPoint mat = processing(cropedMat);

//        BufferedImage processedImage = Utils.Mat2BufferedImage(mat);

        Gson gson = new Gson();
        return gson.toJson(results);

//        System.out.println(end);
//        return
//        if (mat != null) {
//            Contour contour = new Contour(rectF.left, rectF.top, mat.toList());
//            return gson.toJson(contour);
//        }else {
//            return null;
//        }

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

    @GetMapping(value = "/{id}")
    public String get(@PathVariable("id") String param) {
        return param;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Path modelPath = Paths.get(resourcePath, MODEL_NAME);
        Path labelPath = Paths.get(resourcePath, LABEL_NAME);

        model = Files.readAllBytes(modelPath);
        label = new Vector<>(Files.readAllLines(labelPath));
        apiModel = TensorFlowObjectDetectionAPIModel.create(model, label, INPUT_SIZE);
    }
}
