import org.opencv.core.*;
import java.io.File;
import java.nio.file.Paths;

/**
 * runner class for the task's routines
 */
public class HoleFillingRunner {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        try {
            parseAgrs(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }

        logMockHoleInfo();

        runMockHoleFilling();

        System.out.println("Done ...");
    }

    private static void logMockHoleInfo() {
        System.out.format("mock hole start row = %d%n", Defs.MOCK_HOLE_START_ROW);
        System.out.format("mock hole height = %d%n", Defs.MOCK_HOLE_HEIGHT);
        System.out.format("mock hole start col = %d%n", Defs.MOCK_HOLE_START_COL);
        System.out.format("mock hole width = %d%n", Defs.MOCK_HOLE_WIDTH);
    }

    /**
     * runs the routine, which performs the task's requirements
     */
    private static void runMockHoleFilling() {
        // load color rgb image, coverts in to grayscale, and saves it in outputImgs folder
        HoleFiller hf = new HoleFiller(Defs.INPUT_IMG_PATH);
        Mat outputMat = new Mat();
        hf.getMat().convertTo(outputMat, CvType.CV_8UC1);
        hf.setImg(Utils.matToImg(outputMat));

        createOutputImgsDir();

        hf.writeImg(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR, Defs.INPUT_GRAYSCALE_IMG_NAME).toString());

        if (Defs.TEST_MODE) {
            // logs average of pixels in the locations which will hold the mock hole,
            // so would be able to compare to values we get later
            double sum = 0.0;
            int n = 0;
            for (int i = Defs.MOCK_HOLE_START_ROW; i < Defs.MOCK_HOLE_START_ROW + Defs.MOCK_HOLE_HEIGHT; i++) {
                for (int j = Defs.MOCK_HOLE_START_COL; j < Defs.MOCK_HOLE_START_COL + Defs.MOCK_HOLE_WIDTH; j++) {
                    sum += hf.getMat().get(i,j)[0];
                    n++;
                }
            }
            double avr = sum / n;
            System.out.format("before setting mock hole, average of missing pixels = %f%n", avr);
        }

        // set the mock hole
        Index[] missingPixels = MockUtils.getMockSquareHole(new Index(Defs.MOCK_HOLE_START_ROW,
                        Defs.MOCK_HOLE_START_COL), Defs.MOCK_HOLE_HEIGHT, Defs.MOCK_HOLE_WIDTH);
        MockUtils.setMockHole(hf.getMat(), missingPixels);
        hf.setHole(Utils.findHole(hf.getMat()));

        if (Defs.TEST_MODE) {
            // logs average of pixels in the locations which were set as the mock hole,
            // now we expect it to be -1.0
            double sum = 0.0;
            for (Index idx: hf.getHole().getMissingPixels()) {
                sum += hf.getMat().get(idx.getRow(), idx.getCol())[0];
            }
            int n = hf.getHole().getMissingPixels().length;
            double avr = sum / n;
            System.out.format("after setting mock hole, average of missing pixels = %f%n", avr);
        }

        // set visualization of out mock hole's boundaries, ad a black line, and output the
        // grayscale image with this visualization
        HoleFiller hf2 = new HoleFiller(Defs.INPUT_IMG_PATH);
        Utils.setVisualBoundaries(hf2.getMat(), hf.getHole().getBoundariesPixels(), 0.0);

        hf2.getMat().convertTo(outputMat, CvType.CV_8UC1);
        hf2.setImg(Utils.matToImg(outputMat));
        hf2.writeImg(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR, Defs.BOUNDARY_VIS_IMG_NAME).toString());

        // load the input image again, set our mock hole, and fill it according to the chosen algorithm
        HoleFiller hf3 = new HoleFiller(Defs.INPUT_IMG_PATH);
        MockUtils.setMockHole(hf3.getMat(), missingPixels);
        hf3.setHole(Utils.findHole(hf3.getMat()));

        // fill the hole
        if (Defs.ALG == 0) {
            Utils.fillHole(hf3.getMat(), hf3.getHole(), Defs.Z, Defs.EPSILON);
        } else if (Defs.ALG == 1) {
            Index[] b = Utils.followHolePerimeter(hf3.getMat());
            Utils.fillHoleCircular(hf3.getMat(), b);
        }

        if (Defs.TEST_MODE) {
            // logs the average of the missing pixels, after they where filled, and the average of the pixels in the
            // boundaries - we expect them to be reasonably close
            double sum = 0.0;
            for (Index idx: hf3.getHole().getMissingPixels()) {
                sum += hf3.getMat().get(idx.getRow(), idx.getCol())[0];
            }
            int n = hf3.getHole().getMissingPixels().length;
            double avr = sum / n;
            System.out.format("after filling hole, average of missing pixels = %f%n", avr);

            sum = 0.0;
            for (Index idx: hf3.getHole().getBoundariesPixels()) {
                sum += hf3.getMat().get(idx.getRow(), idx.getCol())[0];
            }
            n = hf3.getHole().getBoundariesPixels().length;
            avr = sum / n;
            System.out.format("average of boundaries pixels = %f%n", avr);
        }

        // output the final image after missing pixels where filled
        hf3.getMat().convertTo(outputMat, CvType.CV_8UC1);
        hf3.setImg(Utils.matToImg(outputMat));
        hf3.writeImg(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR, Defs.FINAL_FILLED_IMG_NAME).toString());
    }

    /**
     * parsing command line arguments to Defs, e.g input image path, epsilon and z as they are defined in the task, ext.
     * @param args String[] normally args array as it's given from main method
     */
    private static void parseAgrs(String[] args) {
        Defs.INPUT_IMG_PATH = args[0].equals(Defs.CMD_LINE_ARG_DEF) ?
                Paths.get(Defs.PROJECT_PATH, Defs.INPUT_IMAGE_NAME_DEF).toString() : args[0];

        Defs.ALG = args[1].equals(Defs.CMD_LINE_ARG_DEF) ? Defs.ALG : Integer.parseInt(args[1]);
        Defs.Z = args[2].equals(Defs.CMD_LINE_ARG_DEF) ? (Defs.ALG == 0 ? Defs.Z_DEF : Defs.Z_CIRC_DEF)
                : Integer.parseInt(args[2]);
        Defs.EPSILON = args[3].equals(Defs.CMD_LINE_ARG_DEF) ? Defs.EPSILON : Double.parseDouble(args[3]);
        Defs.TEST_MODE = args[4].equals(Defs.CMD_LINE_ARG_DEF) ? Defs.TEST_MODE : Boolean.parseBoolean(args[4]);

        String s = args[5].equals(Defs.CMD_LINE_ARG_DEF) ? null : args[5];
        if (s != null) {
            String [] stringData = s.split(" ");
            int[] intData = new int[stringData.length];
            for (int i = 0; i < stringData.length; i++) {
                intData[i] = Integer.parseInt(stringData[i]);
            }
            Defs.MOCK_HOLE_START_ROW = intData[0];
            Defs.MOCK_HOLE_HEIGHT = intData[1];
            Defs.MOCK_HOLE_START_COL = intData[2];
            Defs.MOCK_HOLE_WIDTH = intData[3];
        }
    }

    /**
     * creates a directory under the project's base path, called outputImages, in which the images created by the
     * algorithm will be created
     */
    private static void createOutputImgsDir() {
        File dir = new File(Paths.get(Defs.PROJECT_PATH, Defs.OUTPUT_IMGS_DIR).toString());
        if (!dir.exists()) {
            dir.mkdir();
        }
    }
}