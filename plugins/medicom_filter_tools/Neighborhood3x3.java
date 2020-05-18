class Neighborhood3x3 {
    private final float[] neighbors = new float[9];
    /**
     * Value of pixel located at the center of neighborhood, that is at  (<code>x</code>,<code>y</code>).
     */
    public float center;
    /**
     * Value of pixel located at (<code>x+1</code>,<code>y</code>).
     */
    public float neighbor1;
    /**
     * Value of pixel located at (<code>x+1</code>,<code>y-1</code>).
     */
    public float neighbor2;
    /**
     * Value of pixel located at (<code>x</code>,<code>y-1</code>-1).
     */
    public float neighbor3;
    /**
     * Value of pixel located at (<code>x</code>-1,<code>y</code>-1).
     */
    public float neighbor4;
    /**
     * Value of pixel located at (<code>x</code>-1,<code>y</code>).
     */
    public float neighbor5;
    /**
     * Value of pixel located at (<code>x</code>-1,<code>y</code>+1).
     */
    public float neighbor6;
    /**
     * Value of pixel located at (<code>x</code>,<code>y</code>+1).
     */
    public float neighbor7;
    /**
     * Value of pixel located at (<code>x</code>+1,<code>y</code>+1).
     */
    public float neighbor8;
    public int x;
    public int y;
    public int offset;

    public float[] getNeighbors() {
        neighbors[0] = center;
        neighbors[1] = neighbor1;
        neighbors[2] = neighbor2;
        neighbors[3] = neighbor3;
        neighbors[4] = neighbor4;
        neighbors[5] = neighbor5;
        neighbors[6] = neighbor6;
        neighbors[7] = neighbor7;
        neighbors[8] = neighbor8;

        return neighbors;
    }
}
