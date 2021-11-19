package com.example.ooplab3;

public class DTW {
    public DTW() {}

    public static Result compute(final float[] pSample, final float[] pTemplate) {
        // Declare Iteration Constants.
        final int sampleLength = pSample.length;
        final int templateLength = pTemplate.length;
        // Ensure the samples are valid.
        if (sampleLength == 0 || templateLength == 0) {
            // Assert a bad result.
            return new Result(new int[][]{ /* No path data. */}, Double.NaN);
        }
        // Define the Scalar Qualifier.
        int K = 1;
        // Allocate the Warping Path. (Math.max(N, M) <= K < (N + M).
        final int[][] WarpingPath = new int[sampleLength + templateLength][2];
        // Declare the Euclid Distances(d).
        final double[][] euclidDistance = new double[sampleLength][templateLength];
        // Declare the tramsform Matrix(D).
        final double[][] transformMatrix = new double[sampleLength][templateLength];
        // Declare the MinimaBuffer.
        final double[] MinBuffer = new double[3];
        int i, j;
        for (i = 0; i < sampleLength; i++) {
            final float lSample = pSample[i];
            for (j = 0; j < templateLength; j++) {
                // Calculate the Distance between the Sample and the Template for this Index (Euclid distanse)
                euclidDistance[i][j] = getDistanceBetween(lSample, pTemplate[j]);
            }
        }
        // Initialize the Matrix of transformations.
        transformMatrix[0][0] = euclidDistance[0][0];
        for (i = 1; i < sampleLength; i++) {
            transformMatrix[i][0] = euclidDistance[i][0] + transformMatrix[i - 1][0];
        }
        for (j = 1; j < templateLength; j++) {
            transformMatrix[0][j] = euclidDistance[0][j] + transformMatrix[0][j - 1];
        }
        //matrix of transformations
        for (i = 1; i < sampleLength; i++) {
            for (j = 1; j < templateLength; j++) {
                transformMatrix[i][j] = (Math.min(Math.min(transformMatrix[i - 1][j], transformMatrix[i - 1][j - 1]), transformMatrix[i][j - 1])) + euclidDistance[i][j];
                //D[i][j] = d[i][j] + min(D[i-1][j],D[i-1][j-1],D[i][j-1])
            }
        }
        // Update iteration varaibles.
        i = WarpingPath[K - 1][0] = (sampleLength - 1);
        j = WarpingPath[K - 1][1] = (templateLength - 1);
        // Whilst there are samples to process...
        while ((i + j) != 0) {
            // Handle the offset.
            if (i == 0) {
                j--;
            } else if (j == 0) {
                i--;
            } else {
                // Update the contents of the MinimaBuffer.
                MinBuffer[0] = transformMatrix[i - 1][j];
                MinBuffer[1] = transformMatrix[i][j - 1];
                MinBuffer[2] = transformMatrix[i - 1][j - 1];
                // Calculate the Index of the Minimum.
                final int lMinimumIndex = getMinimumIndex(MinBuffer);
                // Declare booleans.
                final boolean lMinIs0 = (lMinimumIndex == 0);
                final boolean lMinIs1 = (lMinimumIndex == 1);
                final boolean lMinIs2 = (lMinimumIndex == 2);
                // Update the iteration components.
                i -= (lMinIs0 || lMinIs2) ? 1 : 0;
                j -= (lMinIs1 || lMinIs2) ? 1 : 0;
            }
            K++;
            // Update the Warping Path.
            WarpingPath[K - 1][0] = i;
            WarpingPath[K - 1][1] = j;
        }
        // Return the Result. (Calculate the Warping Path and the Distance.)
        return new Result(reverse(WarpingPath, K), ((transformMatrix[sampleLength - 1][templateLength - 1]) / K));
    }

    /*Changes the order of the warping path, in increasing order.*/
    private static int[][] reverse(final int[][] pPath, final int pK) {
        // Allocate the Path.
        final int[][] lPath = new int[pK][2];
        // Iterate.
        for (int i = 0; i < pK; i++) {
            // Iterate.
            for (int j = 0; j < 2; j++) {
                // Update the Path.
                lPath[i][j] = pPath[pK - i - 1][j];
            }
        }
        // Return the Allocated Path.
        return lPath;
    }
    /* Computes a distance between two points.*/
    public static double getDistanceBetween(double p1, double p2) {
        return (p1 - p2) * (p1 - p2);
    }
    /* Finds the index of the minimum element from the given array.*/
    public static int getMinimumIndex(final double[] array) {
        // Declare iteration variables.
        int index = 0;
        double value = array[0];
          for (int i = 1; i < array.length; i++) {
            // .Is the current value smaller?
            final boolean smaller = array[i] < value;
            // Update the search metrics.
            value = smaller ? array[i] : value;
            index = smaller ? i : index;
        }
        // Return the Index.
        return index;
    }
}