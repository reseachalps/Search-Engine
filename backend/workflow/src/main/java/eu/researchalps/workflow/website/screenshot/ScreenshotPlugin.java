package eu.researchalps.workflow.website.screenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import eu.researchalps.db.model.Website;
import eu.researchalps.service.ScreenshotStorageService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class ScreenshotPlugin extends QueueComponent implements QueueListener<ScreenshotPlugin.Out> {
    public static final int THUMBNAIL_WIDTH = 320;
    public static final int THUMBNAIL_HEIGHT = 188;

    public static final MessageQueue<In> QUEUE_IN = MessageQueue.get("SCREENSHOT", In.class);
    public static final MessageQueue<Out> QUEUE_OUT = MessageQueue.get("SCREENSHOT_OUT", Out.class);

    public void execute(String id, String url) {
        this.queueService.push(new In(id, url), QUEUE_IN, QUEUE_OUT);
    }

    @Autowired
    private ScreenshotStorageService service;

    @Override
    public void receive(Out m) {
        // this url has failed... just ignore
        if (m.png == null || m.png.contains("Unsafe JavaScript"))
            return;

        if (m.id == null) {
            m.id = Website.idFromUrl(m.url);
        }

        byte[] screenshot = getScreenshotFromBinary(Base64.decodeBase64(m.png));
        service.store(m.id, screenshot);
    }

    /**
     * Generate a screenshot instance with the correct png size
     *
     * @param fullSizeImage The full size image
     * @return A screenshot instance full prepared
     */
    private byte[] getScreenshotFromBinary(byte[] fullSizeImage) {
        byte[] thumbnail;
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(fullSizeImage));
            BufferedImage resizedImage = getScaledInstance(img, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", baos);
            thumbnail = baos.toByteArray();
            return thumbnail;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to render", e);
        }
    }

    /**
     * Taken from https://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
     * <p>
     * Convenience method that returns a scaled instance of the provided {@code BufferedImage}.
     *
     * @param img           the original image to be scaled
     * @param targetWidth   the desired width of the scaled instance, in pixels
     * @param targetHeight  the desired height of the scaled instance, in pixels
     * @param hint          one of the rendering hints that corresponds to {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *                      {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *                      {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *                      {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step scaling technique that provides higher quality
     *                      than the usual one-step technique (only useful in downscaling cases, where {@code targetWidth} or
     *                      {@code targetHeight} is smaller than the original dimensions, and generally only when the
     *                      {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    protected BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint,
                                              boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    @Override
    public MessageQueue<Out> getQueue() {
        return QUEUE_OUT;
    }

    public static class Out {
        public String id;
        public String url;
        public String png;
        public Set<String> techno;
    }

    public static class In {
        public String id;
        public String url;

        public In(String id, String uniqueUrl) {
            this.id = id;
            this.url = uniqueUrl;
        }

        public In() {
        }
    }
}
