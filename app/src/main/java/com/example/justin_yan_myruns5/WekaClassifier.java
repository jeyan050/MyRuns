package com.example.justin_yan_myruns5;

class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.N1f44bfbb0(i);
        return p;
    }
    static double N1f44bfbb0(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() <= 13.390311) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() > 13.390311) {
            p = WekaClassifier.N87a4d381(i);
        }
        return p;
    }
    static double N87a4d381(Object []i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 1;
        } else if (((Double) i[64]).doubleValue() <= 14.534508) {
            p = WekaClassifier.N498485a22(i);
        } else if (((Double) i[64]).doubleValue() > 14.534508) {
            p = 2;
        }
        return p;
    }
    static double N498485a22(Object []i) {
        double p = Double.NaN;
        if (i[4] == null) {
            p = 1;
        } else if (((Double) i[4]).doubleValue() <= 14.034383) {
            p = WekaClassifier.N9d36a833(i);
        } else if (((Double) i[4]).doubleValue() > 14.034383) {
            p = 1;
        }
        return p;
    }
    static double N9d36a833(Object []i) {
        double p = Double.NaN;
        if (i[7] == null) {
            p = 1;
        } else if (((Double) i[7]).doubleValue() <= 4.804712) {
            p = 1;
        } else if (((Double) i[7]).doubleValue() > 4.804712) {
            p = 2;
        }
        return p;
    }
}