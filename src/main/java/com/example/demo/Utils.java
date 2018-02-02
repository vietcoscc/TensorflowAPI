package com.example.demo;


import com.example.demo.model.RectF;
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

    public static Rectangle rectF2Rectangle(RectF rectF) {
        return new Rectangle((int) rectF.left, (int) rectF.top, (int) (rectF.right - rectF.left), (int) (rectF.bottom - rectF.top));
    }

    public static RectF resized2original(BufferedImage resized, BufferedImage original, RectF rectF) {
        Rectangle rectangle = Utils.rectF2Rectangle(rectF);
        float w = original.getWidth() * 1.0f / resized.getWidth();
        float h = original.getHeight() * 1.0f / resized.getHeight();

        float x = (rectangle.x * w);
        float y = (rectangle.y * h);
        float width = (rectangle.width * w);
        float height = (rectangle.height * h);
        return new RectF(x, y, x + width, y + height);
    }

}
