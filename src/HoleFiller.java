import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import static org.opencv.core.CvType.CV_64FC1;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

/**
 * a class which uses to load an image, manipulate it, and output the manipulated image
 */
public class HoleFiller {

    private String inputImgPath;
    private BufferedImage img;

    // holds pixel values in double type, in order to run our algorithm's mathematical operations conveniently
    private MatOfDouble mat;

    // holds information of the hole in the image
    private Hole hole;

    public HoleFiller(String imgPath) {
        inputImgPath = imgPath;
        this.loadGrayScaleImg(imgPath);
    }

    /**
     * loads rgb image from the specified path, coverts it to grayscale and sets the following members:
     * inputImgPath, img, mat, scaledMat
     * @param path path to the input image
     */
    private void loadGrayScaleImg(String path){
        try {
            File input = new File(path);
            BufferedImage srcImg = ImageIO.read(input);

            byte[] srcData = ((DataBufferByte) srcImg.getRaster().getDataBuffer()).getData();
            Mat srcMat = new Mat(srcImg.getHeight(), srcImg.getWidth(), CvType.CV_8UC3);
            srcMat.put(0, 0, srcData);

            Mat dstMat = new Mat(srcImg.getHeight(),srcImg.getWidth(),CvType.CV_8UC1);
            Imgproc.cvtColor(srcMat, dstMat, Imgproc.COLOR_RGB2GRAY);

            byte[] dstData = new byte[dstMat.rows() * dstMat.cols() * (int)(dstMat.elemSize())];
            dstMat.get(0, 0, dstData);
            BufferedImage dstImg = new BufferedImage(dstMat.cols(),dstMat.rows(), BufferedImage.TYPE_BYTE_GRAY);
            dstImg.getRaster().setDataElements(0, 0, dstMat.cols(), dstMat.rows(), dstData);

            mat = new MatOfDouble();
            dstMat.convertTo(mat, CV_64FC1);

            img = dstImg;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * output an image to the specified path, according to the values in *** this.mat ***
     * @param outputPath absolute path to output the image
     */
    public void writeImg(String outputPath) {
        try {
            String[] inputPathSplit = inputImgPath.split("\\.");
            String inputType = inputPathSplit[inputPathSplit.length-1];
            File ouptut = new File(outputPath + "." + inputType);
            ImageIO.write(img, inputType, ouptut);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void setImg(BufferedImage bi) {
        img = bi;
    }

    public void setMat(MatOfDouble sm) {
        mat = sm;
    }

    public void setHole(Hole h) {
        hole = h;
    }


    public BufferedImage getImg() {
        return img;
    }

    public MatOfDouble getMat() {
        return mat;
    }

    public Hole getHole() {
        return hole;
    }
}