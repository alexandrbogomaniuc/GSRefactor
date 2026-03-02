package com.betsoft.casino.mp.bgdragonstone.model.math.slot;

import com.betsoft.casino.mp.bgdragonstone.model.math.config.SlotParams;
import com.betsoft.casino.mp.model.ISpinResult;
import com.abs.casino.common.util.RNG;
import java.util.ArrayList;
import java.util.List;

public class MiniSlot {

    private final SlotParams params;

    public MiniSlot(SlotParams params) {
        this.params = params;
    }

    public List<ISpinResult> doSpins() {
        List<ISpinResult> results = new ArrayList<>();
        for (int i = 0; i < params.getSpins(); i++) {
            results.add(doSpin());
        }
        return results;
    }

    public List<ISpinResult> doSpinsWithTestStand(int combination) {
        List<ISpinResult> results = new ArrayList<>();
        for (int i = 0; i < params.getSpins() - 1; i++) {
            results.add(doSpin());
        }
        results.add(doSpinWithCombination(combination));
        return results;
    }

    public SpinResult doSpin() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < getReelsCount(); i++) {
            positions.add(RNG.nextInt(getReelSize(i)));
        }

        int firstSymbol = getSymbol(0, positions.get(0));
        boolean win = isWin(firstSymbol, positions);
        int payment = win ? params.getPay(firstSymbol) : 0;

        return new SpinResult(positions, payment);
    }

    private boolean isWin(int firstSymbol, List<Integer> positions) {
        for (int i = 1; i < getReelsCount(); i++) {
            if (getSymbol(i, positions.get(i)) != firstSymbol) {
                return false;
            }
        }
        return true;
    }

    public SpinResult doSpinWithCombination(int combination) {
        List<Integer> positions;
        int firstSymbol;

        positions = new ArrayList<>();
        for (int i = 0; i < getReelsCount(); i++) {
            positions.add(getReelPositionWithSymbol(i, combination));
        }
        firstSymbol = getSymbol(0, positions.get(0));

        return new SpinResult(positions, params.getPay(firstSymbol));
    }

    private int getReelPositionWithSymbol(int reel, int symbol) {
        int position = 0;
        int reelSize = getReelSize(reel);
        while (position < reelSize) {
            if (getSymbol(reel, position) == symbol) {
                return position;
            }
            position++;
        }
        return RNG.nextInt(reelSize);
    }


    private int getSymbol(int reel, int position) {
        return params.getReels()[reel][position];
    }

    private int getReelsCount() {
        return params.getReels().length;
    }

    private int getReelSize(int reel) {
        return params.getReels()[reel].length;
    }

}
