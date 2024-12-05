package org.hetils.jgl17;

public class Bidarrays {
    public static double[][] normalize(double[][] map) {
        double m = 0;
        for (double[] doubles : map)
            for (double aDouble : doubles) if (aDouble > m) m = aDouble;
        double asp = 1d/m;
        for (int i = 0; i < map.length; i++)
            for (int j = 0; j < map[i].length; j++)
                map[i][j] *= asp;
        return map;
    }
}
