import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Utility class responsible for setting product images into JLabel components.
 * It automatically searches for matching images based on the product style name.
 */
public class ProductImageSetter {

    // Folder path for all product images
    private static final String IMAGE_FOLDER = "images/";
    private static final String FALLBACK_IMAGE = "noimage.png";

    /**
     * Sets an appropriate image for the given product style.
     *
     * @param style       The style or name of the product (used as the image filename).
     * @param imageLabel  The JLabel where the image should be displayed.
     */
    public void setProductImage(String style, JLabel imageLabel) {
        if (style == null || style.trim().isEmpty()) {
            setFallbackImage(imageLabel);
            return;
        }

        String imageName = style.trim().toLowerCase().replace(" ", "_");
        String[] extensions = {".png", ".jpg", ".jpeg", ".webp"};

        boolean found = false;

        for (String ext : extensions) {
            File file = new File(IMAGE_FOLDER + imageName + ext);
            if (file.exists() && file.isFile()) {
                setScaledImage(file, imageLabel, 200, 200); // consistent size
                found = true;
                break;
            }
        }

        if (!found) {
            setFallbackImage(imageLabel);
        }

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
    }

    /**
     * Sets the fallback image when the product image is not found.
     *
     * @param label JLabel to set the default "no image" icon on.
     */
    private void setFallbackImage(JLabel label) {
        File fallbackFile = new File(IMAGE_FOLDER + FALLBACK_IMAGE);
        if (fallbackFile.exists()) {
            setScaledImage(fallbackFile, label, 150, 150);
        } else {
            // Fallback text if even the noimage.png is missing
            label.setText("Image not available");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setForeground(Color.GRAY);
            label.setFont(new Font("Arial", Font.ITALIC, 12));
        }
    }

    /**
     * Loads, scales, and sets an image into the label.
     *
     * @param file  The image file to load.
     * @param label JLabel where the image will be displayed.
     * @param width Desired width.
     * @param height Desired height.
     */
    private void setScaledImage(File file, JLabel label, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
            label.setText(null); // clear any fallback text
        } catch (Exception e) {
            System.err.println("Error loading image: " + file.getName() + " → " + e.getMessage());
            setFallbackImage(label);
        }
    }
}
