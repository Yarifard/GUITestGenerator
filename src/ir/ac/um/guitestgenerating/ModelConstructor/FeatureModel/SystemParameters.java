package ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel;

public  class SystemParameters {
    private static double alpha;
    private static double beta;
    private static double gama;
    private static double lambda;
    private static double threshold;

    public SystemParameters(double alpha,double beta,double gama,double lambda, double threshold){
        this.alpha = alpha;
        this.beta = beta;
        this.gama = gama;
        this.lambda = lambda;
        this.threshold = threshold;
    }

    public static void setAlpha(double alpha) {
        SystemParameters.alpha = alpha;
    }

    public static void setBeta(double beta) {
        SystemParameters.beta = beta;
    }

    public static void setGama(double gama) {
        SystemParameters.gama = gama;
    }
    public static void setLambda(double lambda) {
        SystemParameters.lambda = lambda;
    }

    public static void setThreshold(double threshold) {
        SystemParameters.threshold = threshold;
    }

    public static double getLambda() {
        return lambda;
    }

    public static double getAlpha() {
        return alpha;
    }

    public static double getGama() {

        return gama;
    }

    public static double getBeta() {
        return beta;
    }

    public static double getThreshold() {
        return threshold;
    }

    public static void setParameter() {
        ParameterConfigurationWindow window = new ParameterConfigurationWindow();
        window.createLayout();
        alpha       = window.getAlphaValue();
        beta        = window.getBethaValue();
        gama        = window.getGhamaValue();
        lambda      = window.getThetaValue();
        threshold   = window.getThresholdValue();
    }
}

