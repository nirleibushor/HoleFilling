/**
 * an object which represents the neighboring pixels of image's pixel
 */
public class Neighborhood {
    private Index[] missingPixels;
    private Index[] imgPixels;

    public Neighborhood(Index[] m, Index[] ip) {
        missingPixels = m;
        imgPixels = ip;
    }

    public Index[] getMissingPixels() {
        return missingPixels;
    }

    public Index[] getImgPixels() {
        return imgPixels;
    }

    public void setImgPixels(Index[] ip) {
        imgPixels = ip;
    }

    public void setMissingPixels(Index[] mp) {
        missingPixels = mp;
    }
}
