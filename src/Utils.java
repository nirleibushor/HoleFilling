import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.BiFunction;

import static org.opencv.core.CvType.CV_64FC1;

/**
 * a class of utils methods for hole filling
 */
public class Utils {

    /**
     * fills the hole in the given matrix, according to algorithm in section 2
     * @param m matrix to fill
     * @param hole object contains information about boundaries and missing pixels of the hole
     * @param z configurable argument as specifies in the task description
     * @param eps configurable argument as specifies in the task description
     */
    public static void fillHole(MatOfDouble m, Hole hole, int z, double eps) {
        for (Index idx : hole.getMissingPixels()) {
            MatOfDouble w = getDefaultWeights(idx, hole.getBoundariesPixels(), z, eps);
            fillMissingPixel(m, hole.getBoundariesPixels(), idx, w);
        }
    }

    /**
     * fills the hole in the given matrix, according to a given weights function
     * @param m matrix to fill
     * @param hole object contains information about boundaries and missing pixels of the hole
     */
    public static void fillHole(MatOfDouble m, Hole hole, BiFunction<Index, Index[], MatOfDouble> weightFunc) {
        for (Index idx : hole.getMissingPixels()) {
            MatOfDouble w = weightFunc.apply(idx, hole.getBoundariesPixels());
            fillMissingPixel(m, hole.getBoundariesPixels(), idx, w);
        }
    }

    /**
     * receives a matrix representing an image and the locations of the pixels in the outmost perimeter
     * of the hole in the image.
     *
     * this is the algorithm which is required in section 5
     * approximates the algorithm from section 2 by filling perimeter by perimeter of the hole,
     * from the outmost and inner.
     * @param m matrix representing an image
     * @param boundaries locations of pixels in the outmost perimeter of the hole in the image
     */
    public static void fillHoleCircular(MatOfDouble m, Index[] boundaries) {
        Index[] currPerimeter = boundaries;
        HashSet<Index> nextPerimeter; // (set is needed to prevent duplicates)
        Neighborhood n;

        boolean pixelsLeft = currPerimeter.length > 0;

        // while there is still an inner perimeter, keep updating it to be the next one, and fill all it's pixels
        // according to the previous already filled ones (or the boarder pixels for the outmost perimeter)
        while (pixelsLeft) {
            nextPerimeter = new HashSet<>();
            for (Index p: currPerimeter) {
                n = checkNeighborhood(m, p);

                // fill the current checked perimeter pixel
                // average of the pixels is used for simplification, because using weights function
                // didn't seem to make any difference make
                m.put(p.getRow(), p.getCol(), matAvrg(m, n.getImgPixels()));

                // add relevant pixels to the next inner perimeter
                nextPerimeter.addAll(Arrays.asList(n.getMissingPixels()));
            }

            // update inner perimeter to be the current one iterated
            currPerimeter = nextPerimeter.toArray(new Index[nextPerimeter.size()]);
            pixelsLeft = currPerimeter.length > 0;
        }
    }

    /**
     * @return a map which represents directions in 4-connectivity, clockwise
     */
    public static Map<Integer, int[]> getDirectionsMap() {
        Map<Integer, int[]> directions = new HashMap<>();
        directions.put(0, new int[]{0,1});
        directions.put(3, new int[]{1, 0});
        directions.put(2, new int[]{0,-1});
        directions.put(1, new int[]{-1,0});
        return directions;
    }

    /**
     * adds the first pixel of the hole in m to pixels ArrayList, if such hole exist
     * @param m matrix representing an image
     * @param pixels ArrayList to add the found pixel to
     */
    public static void addFirstMissingPixel(MatOfDouble m, ArrayList<Index> pixels) {
        outer: for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                if (m.get(i, j)[0] == Defs.HOLE_VALUE) {
                    pixels.add(new Index(i, j));
                    break outer;
                }
            }
        }
    }

    /**
     * Follows the perimeter of a hole in the matrix and returns the boarder as an array of Index objects
     *
     * In opposed to the method Utils.findHole(Mat m), here, the perimeter pixels will be ordered in a
     * "topological way", one after the other.
     *
     * This method is used to get the hole's perimeter for Utils.fillHoleCircular()
     * @param m matrix represents the image
     * @return Index[]
     */
    public static Index[] followHolePerimeter(MatOfDouble m) {
        // find the first pixel of the hole
        ArrayList<Index> perimeter = new ArrayList<>();
        addFirstMissingPixel(m, perimeter);
        if (perimeter.size() == 0) {
            return new Index[0];
        }

        // define directions, in 4-connectivity, clockwise
        Map<Integer, int[]> directions = getDirectionsMap();

        // start following the perimeter
        int dir = 0;
        Index first = perimeter.get(0);
        Index curr = perimeter.get(0), next = new Index(-1, -1);
        boolean neighborFound;

        // loop until a full round is complete
        while (curr.getRow() != first.getRow() || curr.getCol() != first.getCol() || perimeter.size() < 2) {

            // for the current pixel, find a neighbor which is the next one on the perimeter
            neighborFound = false;
            while (!neighborFound) {
                int[] currDirection = directions.get(dir);
                next = new Index(curr.getRow() + currDirection[0], curr.getCol() + currDirection[1]);
                if (m.get(next.getRow(), next.getCol())[0] == Defs.HOLE_VALUE) { // found
                    perimeter.add(next);
                    neighborFound = true;
                } else { // not found yet, go to next direction
                    dir = (dir + 3) % 4;
                }
            }
            curr = next; // after next perimeter pixel found, make it current, and keep following
        }

        // remove the last pixel, which is identical to the first, and return result
        perimeter.remove(perimeter.get(perimeter.size() - 1));
        return perimeter.toArray(new Index[perimeter.size()]);
    }

    /**
     * returns the average of the pixels of m, which are specified in the locations in idxs
     *
     * this function is used instead of a weights function, for example in cases that we know that we are
     * considering only pixels in the neighborhood of the pixel we want to fill
     * @param m
     * @param idxs
     * @return
     */
    public static double matAvrg(MatOfDouble m, Index[] idxs) {
        double sum = 0;
        for (Index idx: idxs) {
            sum += m.get(idx.getRow(), idx.getCol())[0];
        }
        return sum / idxs.length;
    }

    /**
     * uses the method below - getPixelFilling() and puts the needed value in the missing matrix pixel location
     * @param m the matrix to be manipulated
     * @param rangePositions see function below
     * @param missingIdx location of the current filled missing pixel
     * @param weights the weights function as an OpenCv MatOfDouble object
     */
    private static void fillMissingPixel(MatOfDouble m, Index[] rangePositions, Index missingIdx, MatOfDouble weights) {
        double p = getPixelFilling(m, rangePositions, weights);
        m.put(missingIdx.getRow(), missingIdx.getCol(), p);
    }

    /**
     * fill missing pixels according to the formula in section 2 (but can be used for any w)
     * @param m matrix to be manipulated
     * @param rangePositions the pixels which are considered in the weights function, e.g:
     *                    in the default weight function, these will be all the pixels on the hole's boundaries
     *                    in the circular algorithm, these will be only the non missing pixels from the current
     *                    pixel's neighborhood
     * @param weights the weights function as an OpenCv MatOfDouble object
     */
    public static double getPixelFilling(MatOfDouble m, Index[] rangePositions, MatOfDouble weights) {
        MatOfDouble rangePixels = new MatOfDouble(new Mat(rangePositions.length, 1, CV_64FC1));
        for (int i = 0; i < rangePositions.length; i++) {
            rangePixels.put(i, 0, m.get(rangePositions[i].getRow(), rangePositions[i].getCol()));
        }
        return rangePixels.dot(weights) / Core.sumElems(weights).val[0];
    }

    /**
     * returns the weights function described in section 2 of task description
     * @param pixelIdx location of the missing pixel
     * @param boundaries array of locations of pixels which are in the boundary of the hole
     * @param z configurable argument as specifies in the task description
     * @param eps configurable argument as specifies in the task description
     * @return
     */
    private static MatOfDouble getDefaultWeights(Index pixelIdx, Index[] boundaries, int z, double eps) {
        MatOfDouble w = new MatOfDouble(new Mat(boundaries.length, 1, CV_64FC1));
        for (int i = 0; i < boundaries.length; i++){
            double d = Utils.dist(pixelIdx, boundaries[i]);
            d = Math.pow(d, z) + eps;
            w.put(i, 0, 1.0 / d);
        }
        return w;
    }

    /**
     * a function which expresses the distance between two elements in a 2-dim array, based on euclidean distance
     * @param idx1
     * @param idx2
     * @return
     */
    private static double dist(Index idx1, Index idx2) {
        int y1 = idx1.getRow();
        int x1 = idx1.getCol();
        int y2 = idx2.getRow();
        int x2 = idx2.getCol();
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * returns a Hole object, based on the missing pixels in the given matrix
     * @param m matrix which is to be inspected for a hole
     * @return Hole object
     */
    public static Hole findHole(Mat m) {
        ArrayList<Index> holeArrayList = new ArrayList<>();
        HashSet<Index> boundariesSet = new HashSet<>(); // set because it's needed to prevent duplicates

        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                checkMissingPixel(i, j, m, holeArrayList);
                checkBoundaryPixel(i, j, m, boundariesSet);
            }
        }

        Index[] hole = holeArrayList.toArray(new Index[holeArrayList.size()]);
        Index[] boundaries = boundariesSet.toArray(new Index[boundariesSet.size()]);

        return new Hole(hole, boundaries);
    }

    /**
     * if the matrix's pixel in the given location is a boundary pixel of a hole (4-connectivity based)
     * it is added to the boundary pixels set
     * @param i row of pixel to be checked
     * @param j col of pixel to be checked
     * @param m matrix to be checked
     * @param boundariesPixels the set to which the pixel will be added in case it's indeed on the boundary
     */
    public static void checkBoundaryPixel(int i, int j, Mat m, HashSet<Index> boundariesPixels) {
        int jPrev = (j > 0) ? j - 1 : j;
        int iPrev = (i > 0) ? i - 1 : i;

        if (m.get(i, jPrev)[0] != Defs.HOLE_VALUE && m.get(i, j)[0] == Defs.HOLE_VALUE) {
            boundariesPixels.add(new Index(i, jPrev));
        } else if (m.get(i, jPrev)[0] == Defs.HOLE_VALUE && m.get(i, j)[0] != Defs.HOLE_VALUE) {
            boundariesPixels.add(new Index(i, j));
        }
        if (m.get(iPrev, j)[0] != Defs.HOLE_VALUE && m.get(i, j)[0] == Defs.HOLE_VALUE) {
            boundariesPixels.add(new Index(iPrev, j));
        } else if (m.get(iPrev, j)[0] == Defs.HOLE_VALUE && m.get(i, j)[0] != Defs.HOLE_VALUE) {
            boundariesPixels.add(new Index(i, j));
        }
    }

    /**
     * if the matrix's pixel in the given location is missing pixel of a hole,
     * it is added to the missing pixels arrayList
     * @param i row of pixel to be checked
     * @param j col of pixel to be checked
     * @param m matrix to be checked
     * @param missingPixels the set to which the pixel will be added in case it's indeed missing
     */
    private static void checkMissingPixel(int i, int j, Mat m, ArrayList<Index> missingPixels) {
        if (m.get(i, j)[0] == Defs.HOLE_VALUE) {
            missingPixels.add(new Index(i, j));
        }
    }

    /**
     * checks the neighboring pixels to the given index in m, and returns a Neighborhood object
     * in which all neighbors divided to missing pixels (in the hole), and existing pixels
     * @param m the matrix to be checked
     * @param idx index of pixel to be checked
     * @return Neighborhood objetc
     */
    private static Neighborhood checkNeighborhood(MatOfDouble m, Index idx) {
        int r = idx.getRow();
        int c = idx.getCol();

        ArrayList<Index> nMissing = new ArrayList<>();
        ArrayList<Index> nImg = new ArrayList<>();
        for (int[] neighbor: Defs.clockWise) {
            int rowDir = neighbor[0];
            int colDir = neighbor[1];

            int nRow = r + rowDir;
            int nCol = c + colDir;
            if (nRow >= 0 && nRow < m.rows() && nCol >= 0 && nCol < m.cols()) {
                Index nIdx = new Index(nRow, nCol);
                double p = m.get(nRow, nCol)[0];
                if (p == Defs.HOLE_VALUE) {
                    nMissing.add(nIdx);
                } else {
                    nImg.add(nIdx);
                }
            }
        }

        Index[] nMissingArray = nMissing.toArray(new Index[nMissing.size()]);
        Index[] nImgArray = nImg.toArray(new Index[nImg.size()]);

        return new Neighborhood(nMissingArray, nImgArray);
    }

    /**
     * given anm array of locations of pixels which are in the boundaries of a hole,
     * sets a line in this boundary in the matrix (to visualize the hole's boundaries)
     * @param m matrix to be checked
     * @param boundariesPixels
     * @param color
     */
    public static void setVisualBoundaries(Mat m, Index[] boundariesPixels, double color) {
        for (Index idx: boundariesPixels) {
            m.put(idx.getRow(), idx.getCol(), color);
        }
    }

    /**
     * given a matrix object, given as OpenCv Mat object, returns a BufferredImage object which holds the image
     * @param m the matrix representing the image
     * @return BufferedImage corresponds to the given m
     */
    public static BufferedImage matToImg(Mat m) {
        byte[] dstData = new byte[m.rows() * m.cols() * (int)(m.elemSize())];
        m.get(0, 0, dstData);
        BufferedImage dstImg = new BufferedImage(m.cols(), m.rows(), BufferedImage.TYPE_BYTE_GRAY);
        dstImg.getRaster().setDataElements(0, 0, m.cols(), m.rows(), dstData);
        return dstImg;
    }
}
