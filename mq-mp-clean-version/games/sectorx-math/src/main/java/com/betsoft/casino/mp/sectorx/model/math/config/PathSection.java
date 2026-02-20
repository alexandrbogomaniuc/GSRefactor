package com.betsoft.casino.mp.sectorx.model.math.config;

import java.util.List;

public class PathSection {
    private int idx;
    private List<PredefinedPathParam> pathSections;

    public PathSection(int idx, List<PredefinedPathParam> pathSections) {
        this.idx = idx;
        this.pathSections = pathSections;
    }

    public List<PredefinedPathParam> getPathSections() {
        return pathSections;
    }

    public void setPathSections(List<PredefinedPathParam> pathSections) {
        this.pathSections = pathSections;
    }
}
