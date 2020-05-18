import ij.IJ;
import ij.process.FloatProcessor;

import java.text.DecimalFormat;

abstract class BaseAnisotropicDiffusion {

    // Properties
    private int numberOfIterations = 100;
    private double timeStep = 0.05;
    private double meanSquareError = 0.01;

    // Internal variables
    private final DecimalFormat decimalFormat = new DecimalFormat("0.######");
    private double time;


    public int getNumberOfIterations() {
        return numberOfIterations;
    }


    public void setNumberOfIterations(final int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }


    public double getTimeStep() {
        return timeStep;
    }


    public void setTimeStep(final double timeStep) {
        this.timeStep = timeStep;
    }


    public double getMeanSquareError() {
        return meanSquareError;
    }


    public void setMeanSquareError(final double meanSquareError) {
        this.meanSquareError = meanSquareError;
    }


    /**
     * Performs anisotropic diffusion. Makes <code>numberOfIterations</code> calls to {@link
     * #diffuse(ij.process.FloatProcessor, ij.process.FloatProcessor)}, updating value of
     * <code>time</code> before each call.
     *
     * @param src input image to which to apply anisotropic diffusion.
     * @return result of anisotropic diffusion filtering.
     */
    public FloatProcessor process(final FloatProcessor src) {
        //notifyProgressListeners(0, "");

        FloatProcessor fp1 = (FloatProcessor) src.duplicate();
        FloatProcessor fp2 = (FloatProcessor) src.duplicate();

        for (int i = 0; i < numberOfIterations; i++) {
            time = i * timeStep;
            diffuse(fp1, fp2);

            // swap
            final FloatProcessor tmp = fp2;
            fp2 = fp1;
            fp1 = tmp;

            // test change in images
            final double mse = meanSquareDifference((float[]) fp1.getPixels(), (float[]) fp2.getPixels());
            final String msg = "Iteration: " + i + ", mean square error: " + decimalFormat.format(mse);
            //notifyProgressListeners((double) (i + 1) / (double) numberOfIterations, msg);
            IJ.log(msg);
            if (mse <= meanSquareError) {
                break;
            }
        }

        //notifyProgressListeners(1, "");

        return fp1;
    }


    /**
     * Perform single diffusion operation, called iteratively by {@link
     * #process(ij.process.FloatProcessor)}.
     *
     * @param src  source image
     * @param dest destination image
     */
    protected abstract void diffuse(final FloatProcessor src, final FloatProcessor dest);


    protected double time() {
        return time;
    }


    private double meanSquareDifference(final float[] a, final float[] b) {
        assert a != null;
        assert b != null;
        assert a.length == b.length;

        if (a.length == 0) {
            return 0;
        }

        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            final float d = a[i] - b[i];
            sum += d * d;
        }

        return Math.sqrt(sum / a.length);
    }
}
