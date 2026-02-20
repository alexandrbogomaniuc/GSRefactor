package com.betsoft.casino.mp.model;


/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface ITransportSeat {
    int getId();

    void setId(int id);

    String getNickname();

    void setNickname(String nickname);

    long getEnterDate();

    void setEnterDate(long enterDate);

    double getTotalScore();

    void setTotalScore(double totalScore);

    double getCurrentScore();

    void setCurrentScore(double currentScore);

    IAvatar getAvatar();

    void setAvatar(IAvatar avatar);

    int getSpecialWeaponId();

    void setSpecialWeaponId(int specialWeaponId);

    int getLevel();

    int getUnplayedFreeShots();

    void setUnplayedFreeShots(int unplayedFreeShots);

    double getTotalDamage();

    void setTotalDamage(double damage);

    long getRoundWin();

    void setRoundWin(long roundWin);
}
