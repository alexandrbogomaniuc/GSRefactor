package com.betsoft.casino.mp.movement.generators;

public class PathParam {
    private MobOrientation naturalOrientation;
    private boolean needRotate;
    private double effectiveLenPath;
    private double endpointsDistLimit;
    private boolean controlTangentialDir;
    private double thresholdCurvature;
    private boolean controlOrientation;
    private int alpha1;
    private int alpha2;
    private int alpha3;
    private int alpha4;
    private int alpha5;
    private int alpha6;
    private int alpha7;
    private int alpha8;

    public PathParam(MobOrientation naturalOrientation, boolean needRotate, double effectiveLenPath, double endpointsDistLimit,
                     boolean controlTangentialDir, double thresholdCurvature, boolean controlOrientation, int alpha1, int alpha2, int alpha3,
                     int alpha4, int alpha5, int alpha6, int alpha7, int alpha8) {
        this.naturalOrientation = naturalOrientation;
        this.needRotate = needRotate;
        this.effectiveLenPath = effectiveLenPath;
        this.endpointsDistLimit = endpointsDistLimit;
        this.controlTangentialDir = controlTangentialDir;
        this.thresholdCurvature = thresholdCurvature;
        this.controlOrientation = controlOrientation;
        this.alpha1 = alpha1;
        this.alpha2 = alpha2;
        this.alpha3 = alpha3;
        this.alpha4 = alpha4;
        this.alpha5 = alpha5;
        this.alpha6 = alpha6;
        this.alpha7 = alpha7;
        this.alpha8 = alpha8;
    }

    public MobOrientation getNaturalOrientation() {
        return naturalOrientation;
    }

    public void setNaturalOrientation(MobOrientation naturalOrientation) {
        this.naturalOrientation = naturalOrientation;
    }

    public boolean isNeedRotate() {
        return needRotate;
    }

    public void setNeedRotate(boolean needRotate) {
        this.needRotate = needRotate;
    }

    public double getEffectiveLenPath() {
        return effectiveLenPath;
    }

    public void setEffectiveLenPath(double effectiveLenPath) {
        this.effectiveLenPath = effectiveLenPath;
    }

    public double getEndpointsDistLimit() {
        return endpointsDistLimit;
    }

    public void setEndpointsDistLimit(double endpointsDistLimit) {
        this.endpointsDistLimit = endpointsDistLimit;
    }

    public boolean isControlTangentialDir() {
        return controlTangentialDir;
    }

    public void setControlTangentialDir(boolean controlTangentialDir) {
        this.controlTangentialDir = controlTangentialDir;
    }

    public double getThresholdCurvature() {
        return thresholdCurvature;
    }

    public void setThresholdCurvature(double thresholdCurvature) {
        this.thresholdCurvature = thresholdCurvature;
    }

    public boolean isControlOrientation() {
        return controlOrientation;
    }

    public void setControlOrientation(boolean controlOrientation) {
        this.controlOrientation = controlOrientation;
    }

    public int getAlpha1() {
        return alpha1;
    }

    public void setAlpha1(int alpha1) {
        this.alpha1 = alpha1;
    }

    public int getAlpha2() {
        return alpha2;
    }

    public void setAlpha2(int alpha2) {
        this.alpha2 = alpha2;
    }

    public int getAlpha3() {
        return alpha3;
    }

    public void setAlpha3(int alpha3) {
        this.alpha3 = alpha3;
    }

    public int getAlpha4() {
        return alpha4;
    }

    public void setAlpha4(int alpha4) {
        this.alpha4 = alpha4;
    }

    public int getAlpha5() {
        return alpha5;
    }

    public void setAlpha5(int alpha5) {
        this.alpha5 = alpha5;
    }

    public int getAlpha6() {
        return alpha6;
    }

    public void setAlpha6(int alpha6) {
        this.alpha6 = alpha6;
    }

    public int getAlpha7() {
        return alpha7;
    }

    public void setAlpha7(int alpha7) {
        this.alpha7 = alpha7;
    }

    public int getAlpha8() {
        return alpha8;
    }

    public void setAlpha8(int alpha8) {
        this.alpha8 = alpha8;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PathParam{");
        sb.append("naturalOrientation=").append(naturalOrientation);
        sb.append(", needRotate=").append(needRotate);
        sb.append(", effectiveLenPath=").append(effectiveLenPath);
        sb.append(", endpointsDistLimit=").append(endpointsDistLimit);
        sb.append(", controlTangentialDir=").append(controlTangentialDir);
        sb.append(", thresholdCurvature=").append(thresholdCurvature);
        sb.append(", controlOrientation=").append(controlOrientation);
        sb.append(", alpha1=").append(alpha1);
        sb.append(", alpha2=").append(alpha2);
        sb.append(", alpha3=").append(alpha3);
        sb.append(", alpha4=").append(alpha4);
        sb.append(", alpha5=").append(alpha5);
        sb.append(", alpha6=").append(alpha6);
        sb.append(", alpha7=").append(alpha7);
        sb.append(", alpha8=").append(alpha8);
        sb.append('}');
        return sb.toString();
    }
}
