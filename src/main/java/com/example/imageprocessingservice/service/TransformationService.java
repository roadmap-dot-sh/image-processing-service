/*
 * TransformationService.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.imageprocessingservice.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * TransformationService.java
 *
 * @author Nguyen
 */
@Service
@Slf4j
public class TransformationService {
    public byte[] applyTransformations(byte[] imageData, Map<String, Object> transformations, String originalFormat) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

        if (transformations.containsKey("resize")) {
            image = resize(image, (Map<String, Object>) transformations.get("resize"));
        }

        if (transformations.containsKey("crop")) {
            image = crop(image, (Map<String, Object>) transformations.get("crop"));
        }

        if (transformations.containsKey("rotate")) {
            image = rotate(image, (Double) transformations.get("rotate"));
        }

        if (transformations.containsKey("flip")) {
            image = flip(image, (String) transformations.get("flip"));
        }

        if (transformations.containsKey("mirror")) {
            image = mirror(image);
        }

        if (transformations.containsKey("filters")) {
            image = applyFilters(image, (Map<String, Object>) transformations.get("filters"));
        }

        if (transformations.containsKey("watermark")) {
            image = addWatermark(image, (Map<String, Object>) transformations.get("watermark"));
        }

        if (transformations.containsKey("compress")) {
            return compress(image, (Map<String, Object>) transformations.get("compress"),
                    (String) transformations.getOrDefault("format", originalFormat));
        }

        // Convert to specified format
        String outputFormat = (String) transformations.getOrDefault("format", originalFormat);
        return convertToFormat(image, outputFormat);
    }

    private BufferedImage crop(BufferedImage image, Map<String, Object> params) {
        int width = (int) params.getOrDefault("width", image.getWidth());
        int height = (int) params.getOrDefault("height", image.getHeight());
        boolean maintainAspectRatio = (boolean) params.getOrDefault("maintainAspectRatio", true);

        try {
            if (maintainAspectRatio) {
                return Thumbnails.of(image)
                        .size(width, height)
                        .keepAspectRatio(true)
                        .asBufferedImage();
            } else {
                return Thumbnails.of(image)
                        .size(width, height)
                        .keepAspectRatio(false)
                        .asBufferedImage();
            }
        } catch (IOException e) {
            log.error("Failed to resize image", e);
            throw new RuntimeException("Failed to resize image", e);
        }
    }

    private BufferedImage resize(BufferedImage image, Map<String, Object> params) {
        int width = (int) params.get("width");
        int height = (int) params.get("height");
        int x = (int) params.getOrDefault("x", 0);
        int y = (int) params.getOrDefault("y", 0);

        return image.getSubimage(x, y, width, height);
    }

    private BufferedImage rotate(BufferedImage image, double angle) {
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int newWidth = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
        int newHeight = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = rotated.createGraphics();

        g2d.translate((newWidth - image.getWidth()) / 2, (newHeight - image.getHeight()) / 2);
        g2d.rotate(rads, image.getWidth() / 2, image.getHeight() / 2);
        g2d.drawRenderedImage(image, null);
        g2d.dispose();

        return rotated;
    }

    private BufferedImage flip(BufferedImage image, String direction) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage flipped = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = flipped.createGraphics();

        if ("horizontal".equalsIgnoreCase(direction)) {
            g2d.drawImage(image, width, 0, -width, height, null);
        } else if ("vertical".equalsIgnoreCase(direction)) {
            g2d.drawImage(image, 0, height, width, -height, null);
        } else {
            g2d.drawImage(image, 0, 0, null);
        }

        g2d.dispose();
        return flipped;
    }

    private BufferedImage mirror(BufferedImage image) {
        return flip(image, "horizontal");
    }

    private BufferedImage applyFilters(BufferedImage image, Map<String, Object> filters) {
        BufferedImage filtered = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb, true);

                if (filters.containsKey("grayscale") && (boolean) filters.get("grayscale")) {
                    int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                    color = new Color(gray, gray, gray, color.getAlpha());
                }

                if (filters.containsKey("sepia") && (boolean) filters.get("sepia")) {
                    int tr = (int) (0.393 * color.getRed() + 0.769 * color.getGreen() + 0.189 * color.getBlue());
                    int tg = (int) (0.349 * color.getRed() + 0.686 * color.getGreen() + 0.168 * color.getBlue());
                    int tb = (int) (0.272 * color.getRed() + 0.534 * color.getGreen() + 0.131 * color.getBlue());
                    color = new Color(Math.min(255, tr), Math.min(255, tg), Math.min(255, tb), color.getAlpha());
                }

                if (filters.containsKey("brightness")) {
                    double brightness = (double) filters.get("brightness");
                    int r = (int) (color.getRed() * brightness);
                    int g = (int) (color.getGreen() * brightness);
                    int b = (int) (color.getBlue() * brightness);
                    color = new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b), color.getAlpha());
                }

                filtered.setRGB(x, y, color.getRGB());
            }
        }

        return filtered;
    }

    private BufferedImage addWatermark(BufferedImage image, Map<String, Object> watermarkParams) {
        String text = (String) watermarkParams.get("text");
        int fontSize = (int) watermarkParams.getOrDefault("fontSize", 30);
        String colorStr = (String) watermarkParams.getOrDefault("color", "white");
        String position = (String) watermarkParams.getOrDefault("position", "bottom-right");

        BufferedImage watermarked = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g2d = watermarked.createGraphics();
        g2d.drawImage(image, 0, 0, null);

        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        g2d.setColor(Color.decode(colorStr));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        int x = 0, y = 0;
        switch (position.toLowerCase()) {
            case "top-left":
                x = 10;
                y = textHeight;
                break;
            case "top-right":
                x = watermarked.getWidth() - textWidth - 10;
                y = textHeight;
                break;
            case "bottom-left":
                x = 10;
                y = watermarked.getHeight() - 10;
                break;
            default: // bottom-right
                x = watermarked.getWidth() - textWidth - 10;
                y = watermarked.getHeight() - 10;
        }

        g2d.drawString(text, x, y);
        g2d.dispose();

        return watermarked;
    }

    private byte[] compress(BufferedImage image, Map<String, Object> params, String format) throws IOException {
        float quality = ((Double) params.getOrDefault("quality", 0.8f)).floatValue();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        if ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format)) {
            Thumbnails.of(image)
                    .scale(1.0)
                    .outputQuality(quality)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
        } else {
            Thumbnails.of(image)
                    .scale(1.0)
                    .outputQuality(quality)
                    .outputFormat(format)
                    .toOutputStream(outputStream);
        }

        return outputStream.toByteArray();
    }

    private byte[] convertToFormat(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return outputStream.toByteArray();
    }
}
