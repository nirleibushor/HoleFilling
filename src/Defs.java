/**
 * class which holds different definitions and constants which are used across the program
 * some definitions, including those which are required in the task (z, epsilon) are configurable from
 * command line - please see README and HoleFillingRunner.parseAgrs() for more info
 */
public class Defs {
    public static double EPSILON = 1e-10;

    public static final int Z_DEF = 4;
    public static final int Z_CIRC_DEF = 1;
    public static int Z;

    public final static double HOLE_VALUE = -1.0;

    // if true some logs will be printed to standard output, which will enable us to track our algorithm's performance
    public static boolean TEST_MODE = false;

    // if 0 full un-approximating algorithm (in section 2 in task's description) will run
    // if 1 approximating algorithm (section 5) will run
    public static int ALG = 0;

    public static int MOCK_HOLE_START_ROW = 120;
    public static int MOCK_HOLE_START_COL = 200;
    public static int MOCK_HOLE_HEIGHT = 10;
    public static int MOCK_HOLE_WIDTH = 10;

    // file system paths, and output file names
    public static String PROJECT_PATH = System.getProperty("user.dir");
    public final static String INPUT_IMAGE_NAME_DEF = "example_input_img.jpg";
    public static String INPUT_IMG_PATH;
    public static String OUTPUT_IMGS_DIR = "outputImgs";
    public final static String INPUT_GRAYSCALE_IMG_NAME = "inputGrayscaleImg";
    public final static String BOUNDARY_VIS_IMG_NAME = "boundaryVisualized";
    public final static String FINAL_FILLED_IMG_NAME = "finalFilledImg";

    // this uses to recognise whether a certain cmd line argument was given or not,
    // used in HoleFillingRunner.parseArgs()
    public final static String CMD_LINE_ARG_DEF = "[default]";

    // uses to iterate over a pixel's neighboring pixels. see usage at Utils.checkNeighborhood()
    public final static int[][] clockWise = new int[][] {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
}
