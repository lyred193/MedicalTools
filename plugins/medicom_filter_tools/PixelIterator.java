import ij.process.FloatProcessor;

import java.awt.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

class PixelIterator implements Iterator<Neighborhood3x3> {
    private final int width;
    private final float[] pixels;
    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;
    private final int rowOffset;
    private int x;
    private int y;
    private final Neighborhood3x3 neighborhood3x3 = new Neighborhood3x3();

    public PixelIterator(final FloatProcessor fp) {
        final Rectangle roi = fp.getRoi();
        width = fp.getWidth();
        pixels = (float[]) fp.getPixels();
        xMin = roi.x + 1;
        xMax = roi.x + roi.width - 2;
        yMin = roi.y + 1;
        yMax = roi.y + roi.height - 2;
        rowOffset = width;
        rewind();
    }

    @Override
    public boolean hasNext() {
        return x < xMax || y < yMax;
    }

    @Override
    public Neighborhood3x3 next() {
        // Update center location
        if (x < xMax) {
            ++x;
        } else if (y < yMax) {
            x = xMin;
            ++y;
//      IJ.showProgress(y, yMax);
        } else {
            throw new NoSuchElementException("No more elements.");
        }

        // ENH: offset can be incremented rather than computed each time
        final int offset = x + y * width;

        // Update neighborhood information
        neighborhood3x3.neighbor4 = pixels[offset - rowOffset - 1];
        neighborhood3x3.neighbor3 = pixels[offset - rowOffset];
        neighborhood3x3.neighbor2 = pixels[offset - rowOffset + 1];

        neighborhood3x3.neighbor5 = pixels[offset - 1];
        neighborhood3x3.center = pixels[offset];
        neighborhood3x3.neighbor1 = pixels[offset + 1];

        neighborhood3x3.neighbor6 = pixels[offset + rowOffset - 1];
        neighborhood3x3.neighbor7 = pixels[offset + rowOffset];
        neighborhood3x3.neighbor8 = pixels[offset + rowOffset + 1];

        neighborhood3x3.x = x;
        neighborhood3x3.y = y;
        neighborhood3x3.offset = offset;

        return neighborhood3x3;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Method remove() not supported.");
    }

    /**
     * Reset iterator to its initial position
     */
    public void rewind() {
        x = xMin - 1;
        y = yMin;
    }


}
