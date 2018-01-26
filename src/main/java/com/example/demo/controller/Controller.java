package com.example.demo.controller;

import com.example.demo.Person;
import com.example.demo.Utils;
import com.example.demo.model.Classifier;
import com.example.demo.model.RectF;
import com.example.demo.model.TensorFlowObjectDetectionAPIModel;
import com.google.gson.Gson;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

@RestController
public class Controller implements InitializingBean {
    public static final String MODEL_NAME = "ssd_mobilenet_v1_android_export.pb";
    public static final String LABEL_NAME = "coco_labels_list.txt";
    public static final int INPUT_SIZE = 300;

    private String resourcePath = getClass().getClassLoader().getResource("").getPath() + "/static/";
    private byte[] model;
    private Vector<String> label;
    private TensorFlowObjectDetectionAPIModel apiModel;

    @PostMapping(value = "/tensorflow")
    public String tensorflow(@RequestBody String image) throws Exception {
        if (image.isEmpty()) {
            return null;
        }

        byte originalImageByte[] = Base64Utils.decodeFromString(image);

        InputStream inputStream = new ByteArrayInputStream(originalImageByte);

        BufferedImage original = ImageIO.read(inputStream);
        if (original == null) {
            return "Null";
        }
        BufferedImage resized = Utils.resizeImage(original, original.getType());

        byte resizedImageByte[] = ((DataBufferByte) resized.getRaster().getDataBuffer()).getData();
        long start = System.currentTimeMillis();

        List<Classifier.Recognition> results = apiModel.recognizeImage(resizedImageByte);

        for (int i = 0; i < results.size(); i++) {
            Classifier.Recognition result = results.get(i);
            result.setLocation(Utils.resized2original(resized, original, result.getLocation()));
        }

        long end = System.currentTimeMillis() - start;
        RectF rectF = results.get(0).getLocation();
        BufferedImage cropedImage = original.getSubimage((int) rectF.left, (int) rectF.top, (int) (rectF.right - rectF.left), (int) (rectF.bottom - rectF.top));
        Mat cropedMat = Utils.BufferedImage2Mat(cropedImage);
        processing(cropedMat);
        System.out.println(end);

        Gson gson = new Gson();
        return gson.toJson(results);
    }

    @PostMapping("/object")
    public String object(@RequestParam("person") Person person) {
        Gson gson = new Gson();
        return gson.toJson(person);
    }

    @PostMapping("/file")
    public String file(@RequestParam(value = "file") MultipartFile file) throws Exception {
//        if (file.getContentType().contains("video")) {
//            byte b[] = file.getBytes();
//            InputStream inputStream = new ByteArrayInputStream(b);
//
//            File file1 = new File(resourcePath + System.currentTimeMillis());
//            FileOutputStream outputStream = new FileOutputStream(file1);
//            outputStream.write(b);
//            outputStream.flush();
//            outputStream.close();
//        }
        return file.getContentType();
    }

    @PostMapping(value = "/")
    public String index(@RequestParam(value = "aa") String a) {
        return a;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Path modelPath = Paths.get(resourcePath, MODEL_NAME);
        Path labelPath = Paths.get(resourcePath, LABEL_NAME);

        model = Files.readAllBytes(modelPath);
        label = new Vector<>(Files.readAllLines(labelPath));
        apiModel = TensorFlowObjectDetectionAPIModel.create(model, label, INPUT_SIZE);
    }

    private Mat processing(Mat input) {
        Mat inputClone = input.clone();
        Mat threshold = new Mat();
        Mat gray = new Mat();
        Imgproc.cvtColor(input, gray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(gray, threshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY_INV, 17, 2);
        medianBlur(threshold);
        multiDilate(threshold, 10);
        multiErode(threshold, 2);
        copyMakeBorder(threshold, 1);
        Mat canny = new Mat();
        Imgproc.Canny(threshold, canny, 200, 100);
        multiDilate(canny, 5);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(canny, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);

        MatOfPoint matOfPoint = Collections.max(contours, (l, r) -> {
            if (Imgproc.contourArea(l) > Imgproc.contourArea(r)) {
                return 1;
            }
            return 0;
        });
        float xCenter = 0, yCenter = 0;
        float length = matOfPoint.toArray().length;
        for (int i = 0; i < length; i++) {
            xCenter += matOfPoint.toArray()[i].x;
            yCenter += matOfPoint.toArray()[i].y;
        }
        xCenter /= length;
        yCenter /= length;
        System.out.println("CENTER : (" + xCenter + "," + yCenter + ")");
        System.out.println("CENTER : (" + input.width() + "," + input.height() + ")");

//        Imgproc.drawMarker(inputClone, new Point(xCenter, yCenter), new Scalar(255, 0, 0), Imgproc.MARKER_TILTED_CROSS, 20, 5, Imgproc.LINE_AA);
        Imgproc.drawContours(inputClone, contours, contours.indexOf(matOfPoint), new Scalar(255, 0, 0), 3);

//        float xCenter = 0, yCenter = 0;
//        int totalPoint = 0;
//        for (int idx = 0; idx < contours.size(); idx++) {
//            Point[] arrPoint = contours.get(idx).toArray();
//            for (int i = 0; i < arrPoint.length; i++) {
//                xCenter += arrPoint[i].x;
//                yCenter += arrPoint[i].y;
//                System.out.print("(" + arrPoint[i].x + "," + arrPoint[i].y + ")");
//            }
//            totalPoint += arrPoint.length;
//
//            System.out.println("");
////            if (Imgproc.contourArea(contours.get(idx)) < 10000) continue;
//            Imgproc.drawContours(inputClone, contours, idx, new Scalar(255, 0, 0), 2);
//        }
//        xCenter = xCenter / totalPoint;
//        yCenter = yCenter / totalPoint;
//        System.out.println("CENTER : (" + xCenter + "," + yCenter + ")");
//        Imgproc.drawMarker(inputClone, new Point(xCenter, yCenter), new Scalar(255, 0, 0), Imgproc.MARKER_TILTED_CROSS, 5, 2, Imgproc.LINE_4);
//        medianBlur(input);
//        Mat tmp = input.clone();
//        medianBlur(tmp);
        return inputClone;

    }

    private void copyMakeBorder(Mat mat, int strokeWidth) {
        Point topLeft = new Point(0, 0);
        Point topRight = new Point(mat.width(), 0);
        Point bottomLeft = new Point(0, mat.height());
        Point bottomRight = new Point(mat.width(), mat.height());

        Core.line(mat, topLeft, topRight, new Scalar(255, 255, 255), strokeWidth);
        Core.line(mat, topLeft, bottomLeft, new Scalar(255, 255, 255), strokeWidth);
        Core.line(mat, topRight, bottomRight, new Scalar(255, 255, 255), strokeWidth);
        Core.line(mat, bottomLeft, bottomRight, new Scalar(255, 255, 255), strokeWidth);

//        Core.copyMakeBorder(mat, mat, strokeWidth, strokeWidth, strokeWidth, strokeWidth, Core.BORDER_CONSTANT, new Scalar(255, 255, 255));
    }

    private void blur(Mat mat) {
        Imgproc.blur(mat, mat, new Size(10, 10));
    }

    private void medianBlur(Mat mat) {
        Imgproc.medianBlur(mat, mat, 5);
    }

    private void gaussianBlur(Mat mat) {
        Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 2);
    }

    private void multiDilate(Mat mat, int numberLoop) {
        for (int i = 0; i < numberLoop; i++) {
            dilate(mat);
        }
    }

    private void multiErode(Mat mat, int numberLoop) {
        for (int i = 0; i < numberLoop; i++) {
            erode(mat);
        }
    }

    private void multiMedianBlur(Mat mat, int numberLoop) {
        for (int i = 0; i < numberLoop; i++) {
            medianBlur(mat);
        }
    }

    private void dilate(Mat mat) {
        Imgproc.dilate(mat, mat, new Mat());
    }

    private void erode(Mat mat) {
        Imgproc.erode(mat, mat, new Mat());
    }
}
