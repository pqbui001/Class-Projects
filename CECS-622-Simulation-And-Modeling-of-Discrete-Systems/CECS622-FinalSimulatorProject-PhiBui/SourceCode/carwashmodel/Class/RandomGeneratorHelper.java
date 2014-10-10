/*
 * University of Louisville
 * Spring 2014 - CECS 622
 * Project Title: Car Wash Queuing Model Simulation
 * Author: Phi Bui
 */
package carwashmodel.Class;

/**
 *
 * @author Phi Bui
 */
public class RandomGeneratorHelper {

    /**
     * =========================================================================
     * double NormalDistribution()
     * =========================================================================
     * Returns a standard normal distributed real.
     * =========================================================================
     */
    public static double NormalDistribution() {

        double p0 = 0.322232431088;
        double q0 = 0.099348462606;
        double p1 = 1.0;
        double q1 = 0.588581570495;
        double p2 = 0.342242088547;
        double q2 = 0.531103462366;
        double p3 = 0.204231210245e-1;
        double q3 = 0.103537752850;
        double p4 = 0.453642210148e-4;
        double q4 = 0.385607006340e-2;
        double u, t, p, q;

        //Assign random values to variables;
        u = Math.random();
        t = Math.random();
        p = Math.random();
        q = Math.random();

        t = (u < 0.5) ? Math.sqrt(-2 * Math.log(u)) : Math.sqrt(-2 * Math.log(1.0 - u));

        p = p0 + t * (p1 + t * (p2 + t * (p3 + t * p4)));
        q = q0 + t * (q1 + t * (q2 + t * (q3 + t * q4)));

        return ((u < 0.5) ? ((p / q) - t) : (t - (p / q)));

    } //END NormalDistribution method

    /**
     * =========================================================================
     * double ExponentialDistribution()
     * =========================================================================
     * Returns an exponentially distributed positive real. NOTE: use m > 0
     * =========================================================================
     */
    public static double ExponentialDistribution(double m) {
        double r = Math.random();
        return (-m * Math.log(1.0 - r));
    } //END ExponentialDistribution method

    /**
     * =========================================================================
     * double GaussDistribution()
     * =========================================================================
     * Returns a Gaussian distributed real. NOTE: use s > 0
     * =========================================================================
     */
    public static double GaussDistribution(double m, double s) {
        double r = NormalDistribution();
        return (m + s * r);
    } //END GaussDistribution method

    /**
     * =========================================================================
     * double UniformDistribution()
     * =========================================================================
     * Returns a uniformly distributed real between [a,b). NOTE: use a < b
     * =========================================================================
     */
    public static double UniformDistribution(double a, double b) {
        double r = Math.random();
        return (a + (b - a) * r);
    } //END UniformDistribution method

    /**
     * =========================================================================
     * double UniformDistribution()
     * =========================================================================
     * Returns a uniformly distributed integer between [a,b]. NOTE: use a < b
     * =========================================================================
     */
    public static double UniformDiscreetDistribution(int a, int b) {
        double r = Math.random();
        return (a + (int) (b - a + 1) * r);
    } //END UniformDistribution method

}
