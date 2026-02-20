package com.betsoft.casino.mp.model;

import java.io.Serializable;
import java.util.List;

public interface ISpin extends Serializable {
    List<Integer> getReels();
    double getWin();
}
