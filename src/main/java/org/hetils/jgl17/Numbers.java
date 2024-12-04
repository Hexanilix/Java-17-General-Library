package org.hetils;

import java.util.Random;

public class Numbers {
    public static int getWeightedRandom(int max, int mult) {
        Random r = new Random();
        int[] weights = new int[max];
        for (int i = max-1; i >= 0; i--)
            weights[i]=(max-i)*mult;
        int[] cumulativeWeights = new int[weights.length];
        cumulativeWeights[0] = weights[0];
        for (int i = 1; i < weights.length; i++)
            cumulativeWeights[i] = cumulativeWeights[i - 1] + weights[i];
        int randomValue = r.nextInt(cumulativeWeights[cumulativeWeights.length - 1]);
        for (int i = 0; i < cumulativeWeights.length; i++)
            if (randomValue < cumulativeWeights[i])
                return i;
        return 0;
    }
}
