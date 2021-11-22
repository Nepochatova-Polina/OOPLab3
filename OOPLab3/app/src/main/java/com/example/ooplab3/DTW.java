package com.example.ooplab3;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;

public class DTW {

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static Float minOf(Float p1, Float p2, Float p3) {
        return Float.min(p1, Float.min(p2, p3));
    }

    private static Float distance(Float p1, Float p2) {
        return (p1 - p2) * (p1 - p2);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Float distance(List<Float> lhs, List<Float> rhs) {

        final Float[][] matrix = new Float[lhs.size()][rhs.size()];

        matrix[0][0] = distance(lhs.get(0), rhs.get(0));

        for (int i = 1; i < lhs.size(); i++) {
            matrix[i][0] = matrix[i - 1][0] + distance(lhs.get(i), rhs.get(0));
        }
        for (int i = 1; i < rhs.size(); i++) {
            matrix[0][i] = matrix[0][i - 1] + distance(lhs.get(0), rhs.get(i));
        }
        for (int i = 1; i < lhs.size(); i++) {
            for (int j = 1; j < rhs.size(); j++) {
                matrix[i][j] = minOf(matrix[i - 1][j], matrix[i][j - 1], matrix[i - 1][j - 1])
                        + distance(lhs.get(i), rhs.get(j));
            }
        }
        int curI = lhs.size() - 1;
        int curJ = rhs.size() - 1;
        Float totalSum = 0f;

        while (true) {

            totalSum += matrix[curI][curJ];
            if (curI == 0 && curJ == 0) {
                break;
            }
            Float min1 = curI > 0
                    ? matrix[curI - 1][curJ]
                    : 1e8f;
            Float min2 = curJ > 0
                    ? matrix[curI][curJ - 1]
                    : 1e8f;
            Float min3 = curI > 0 && curJ > 0
                    ? matrix[curI - 1][curJ - 1]
                    : 1e8f;

            Float calcMin = minOf(min1, min2, min3);

            if (calcMin.equals(min1)) {
                curI--;
            } else if (calcMin.equals(min2)) {
                curJ--;
            } else {
                curI--;
                curJ--;
            }
        }
        return totalSum;
    }
}