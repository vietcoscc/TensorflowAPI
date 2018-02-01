package com.example.demo;


import net.coobird.thumbnailator.Thumbnails;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Utils {
    public static BufferedImage resizeImage(BufferedImage originalImage, int type) {
        BufferedImage resizedImage = new BufferedImage(300, 300, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, 300, 300, null);
        g.dispose();
        return resizedImage;
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        try {
            return Thumbnails.of(img).size(newW, newH).asBufferedImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
//
//    public static BufferedImage Mat2BufferedImage(Mat matrix) throws IOException {
//        if (matrix == null) {
//            return null;
//        }
//        MatOfByte mob = new MatOfByte();
//        Highgui.imencode(".jpg", matrix, mob);
//        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
//    }
//
//    public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
//        if (image == null) {
//            return null;
//        }
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        ImageIO.write(image, "jpg", byteArrayOutputStream);
//        byteArrayOutputStream.flush();
//        return Highgui.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Highgui.CV_LOAD_IMAGE_UNCHANGED);
//    }
//
//
//    public static Rectangle rectF2Rectangle(RectF rectF) {
//        return new Rectangle((int) rectF.left, (int) rectF.top, (int) (rectF.right - rectF.left), (int) (rectF.bottom - rectF.top));
//    }
//
//    public static RectF resized2original(BufferedImage resized, BufferedImage original, RectF rectF) {
//        Rectangle rectangle = Utils.rectF2Rectangle(rectF);
//        float w = original.getWidth() * 1.0f / resized.getWidth();
//        float h = original.getHeight() * 1.0f / resized.getHeight();
//
//        float x = (rectangle.x * w);
//        float y = (rectangle.y * h);
//        float width = (rectangle.width * w);
//        float height = (rectangle.height * h);
//        return new RectF(x, y, x + width, y + height);
//    }
//
//    public static void copyMakeBorder(Mat mat, int strokeWidth) {
//        Point topLeft = new org.opencv.core.Point(0, 0);
//        Point topRight = new org.opencv.core.Point(mat.width(), 0);
//        Point bottomLeft = new org.opencv.core.Point(0, mat.height());
//        Point bottomRight = new Point(mat.width(), mat.height());
//
//        Core.line(mat, topLeft, topRight, new Scalar(255, 255, 255), strokeWidth);
//        Core.line(mat, topLeft, bottomLeft, new Scalar(255, 255, 255), strokeWidth);
//        Core.line(mat, topRight, bottomRight, new Scalar(255, 255, 255), strokeWidth);
//        Core.line(mat, bottomLeft, bottomRight, new Scalar(255, 255, 255), strokeWidth);
//
////        Core.copyMakeBorder(mat, mat, strokeWidth, strokeWidth, strokeWidth, strokeWidth, Core.BORDER_CONSTANT, new Scalar(255, 255, 255));
//    }
//
//    public static void blur(Mat mat) {
//        Imgproc.blur(mat, mat, new Size(10, 10));
//    }
//
//    public static void medianBlur(Mat mat) {
//        Imgproc.medianBlur(mat, mat, 5);
//    }
//
//    public static void gaussianBlur(Mat mat) {
//        Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 2);
//    }
//
//    public static void multiDilate(Mat mat, int numberLoop) {
//        for (int i = 0; i < numberLoop; i++) {
//            dilate(mat);
//        }
//    }
//
//    public static void multiErode(Mat mat, int numberLoop) {
//        for (int i = 0; i < numberLoop; i++) {
//            erode(mat);
//        }
//    }
//
//    public static void multiMedianBlur(Mat mat, int numberLoop) {
//        for (int i = 0; i < numberLoop; i++) {
//            medianBlur(mat);
//        }
//    }
//
//    public static void dilate(Mat mat) {
//        Imgproc.dilate(mat, mat, new Mat());
//    }
//
//    public static void erode(Mat mat) {
//        Imgproc.erode(mat, mat, new Mat());
//    }
//
//    public static MatOfPoint processing(Mat input) {
//        Mat inputClone = input.clone();
//        Mat threshold = new Mat();
//        Mat gray = new Mat();
//        Imgproc.cvtColor(input, gray, Imgproc.COLOR_RGB2GRAY);
//        Imgproc.adaptiveThreshold(gray, threshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
//                Imgproc.THRESH_BINARY_INV, 17, 2);
//        medianBlur(threshold);
//        multiDilate(threshold, 10);
//        multiErode(threshold, 2);
//        copyMakeBorder(threshold, 1);
//        Mat canny = new Mat();
//        Imgproc.Canny(threshold, canny, 200, 100);
//        multiDilate(canny, 5);
//        List<MatOfPoint> contours = new ArrayList<>();
//        Imgproc.findContours(canny, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);
//
//        MatOfPoint matOfPoint = Collections.max(contours, (l, r) -> {
//            if (Imgproc.contourArea(l) > Imgproc.contourArea(r)) {
//                return 1;
//            }
//            return 0;
//        });
//
//        float xCenter = 0, yCenter = 0;
//        float length = matOfPoint.toArray().length;
//        for (int i = 0; i < length; i++) {
//            xCenter += matOfPoint.toArray()[i].x;
//            yCenter += matOfPoint.toArray()[i].y;
//        }
//        xCenter /= length;
//        yCenter /= length;
//
//        System.out.println("CENTER : (" + xCenter + "," + yCenter + ")");
//        System.out.println("CENTER : (" + input.width() + "," + input.height() + ")");
////        Imgproc.drawMarker(inputClone, new Point(xCenter, yCenter), new Scalar(255, 0, 0), Imgproc.MARKER_TILTED_CROSS, 20, 5, Imgproc.LINE_AA);
//        Imgproc.drawContours(inputClone, contours, contours.indexOf(matOfPoint), new Scalar(255, 0, 0), 3);
////        float xCenter = 0, yCenter = 0;
////        int totalPoint = 0;
////        for (int idx = 0; idx < contours.size(); idx++) {
////            Point[] arrPoint = contours.get(idx).toArray();
////            for (int i = 0; i < arrPoint.length; i++) {
////                xCenter += arrPoint[i].x;
////                yCenter += arrPoint[i].y;
////                System.out.print("(" + arrPoint[i].x + "," + arrPoint[i].y + ")");
////            }
////            totalPoint += arrPoint.length;
////
////            System.out.println("");
//////            if (Imgproc.contourArea(contours.get(idx)) < 10000) continue;
////            Imgproc.drawContours(inputClone, contours, idx, new Scalar(255, 0, 0), 2);
////        }
////        xCenter = xCenter / totalPoint;
////        yCenter = yCenter / totalPoint;
////        System.out.println("CENTER : (" + xCenter + "," + yCenter + ")");
////        Imgproc.drawMarker(inputClone, new Point(xCenter, yCenter), new Scalar(255, 0, 0), Imgproc.MARKER_TILTED_CROSS, 5, 2, Imgproc.LINE_4);
////        medianBlur(input);
////        Mat tmp = input.clone();
////        medianBlur(tmp);
//        return matOfPoint;
//
//    }
}
