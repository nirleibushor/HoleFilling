/**
 * an object which represents a hole in a pixel's index in an image
 */
public class Index {
    private int row;
    private int col;

    public Index(int r, int c) {
        row = r;
        col = c;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof Index) && ((Index) other).row == row && ((Index) other).col == col;
    }

    @Override
    public int hashCode() {
        return row - col;
    }

    @Override
    public String toString() {
        return "(" + Integer.toString(row) + ", " + Integer.toString(col) + ")";
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int r) {
        row = r;
    }

    public void setCol(int c) {
        col = c;
    }
}