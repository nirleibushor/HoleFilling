/**
 * an object which represents a hole in an image
 */
public class Hole {
    private Index[] missingPixels;
    private Index[] boundariesPixels;

    public Hole(Index[] m, Index[] b) {
        missingPixels = m;
        boundariesPixels = b;
    }

    public Index[] getBoundariesPixels() {
        return boundariesPixels;
    }

    public Index[] getMissingPixels() {
        return missingPixels;
    }

    public void setMissingPixels(Index[] m) {
        missingPixels = m;
    }

    public void setBoundariesPixels(Index[] b) {
        boundariesPixels = b;
    }
}
