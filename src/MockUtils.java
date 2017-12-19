import org.opencv.core.Mat;
import java.util.ArrayList;

/**
 * utils class for method to simulate our mock hole
 */
public class MockUtils {

    /**
     * sets a mock hole in the given matrix
     * @param m the matrix to set the hole in
     * @param holePixels the pixel's positions in which the mock hole will be set
     */
    public static void setMockHole(Mat m, Index[] holePixels) {
        for (Index idx: holePixels) {
            m.put(idx.getRow(), idx.getCol(), Defs.HOLE_VALUE);
        }
    }

    /**
     * return an array of pixel Index objects, which corresponds to a square hole, according to the given arguments
     * @param topLeft the top left location of the hole
     * @param height height of the hole
     * @param width width of the hole
     * @return Index[] which contains positions of all pixels in the hole
     *
     * used at HoleFillingRunner.runMockHoleFilling()
     */
    public static Index[] getMockSquareHole(Index topLeft, int height, int width) {
        int startRow = topLeft.getRow();
        int startCol = topLeft.getCol();

        ArrayList<Index> idxs = new ArrayList<>();
        for (int i = startRow; i < startRow + height; i++) {
            for (int j = startCol; j < startCol + width; j++) {
                idxs.add(new Index(i, j));
            }
        }

        return idxs.toArray(new Index[idxs.size()]);
    }
}
