package com.betsoft.casino.mp.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum SpecialWeaponType {
    HolyArrows(4, "Shotgun", "special_1",
            Arrays.asList(779, 808, 821), false), // type 1  - ShotGun
    Bomb(1, "Grenade", "grenade",
            Arrays.asList(779, 808, 821), false), // type 2   - Grenade
    MachineGun(10, "Machine Gun", "machine_gun",
            Arrays.asList(779, 808, 821), false), // type 3  - Chain Gun
    Ricochet(5, "Laser", "laser",
            Arrays.asList(779, 808, 821, 826, 843, 848, 859, 862), false), // type 3  - Laser
    Plasma(1, "Plasma Rifle", "plasmagun",
            Arrays.asList(779, 808, 821, 829, 838, 856, 859, 862), false), //Guarantee knockout (*) - Plasma
    Landmines(1, "Mine Launcher", "mine_launcher",
            Arrays.asList(779, 808, 821, 829), false), //
    Railgun(1, "Railgun", "railgun",
            Arrays.asList(779, 808, 821, 838, 856), false), //
    ArtilleryStrike(1, "Artillery Strike", "artillery_strike",
            Arrays.asList(779, 808, 821, 826, 829, 838, 843, 848, 856, 859, 862), false), //
    RocketLauncher(1, "Rocket Launcher", "rocket_launcher",
            Arrays.asList(779, 808, 821), false), //
    Flamethrower(1, "Flamethrower", "flamethrower",
            Arrays.asList(779, 808, 821, 829, 838, 856, 859, 862), false), //
    Cryogun(1, "Cryogun", "cryogun",
            Arrays.asList(779, 808, 821, 829, 838, 856, 859, 862), false), //
    DoubleStrengthPowerUp(1, "Rapid Fire Pistol", "special_2",
            Arrays.asList(779, 808, 821), false),
    Airstrike(1, "Free Bonus Strike", "Airstrike",
            Collections.emptyList(), true),
    Lightning(1, "Lightning", "Lightning", Arrays.asList(826, 843, 848), false),
    Napalm(1, "Napalm", "Napalm", Arrays.asList(826, 843, 848), false),
    Nuke(1, "Nuke", "Nuke", Arrays.asList(826, 843, 848), false),
    LevelUp(1, "Level UP", "LevelUP", Arrays.asList(856, 862, 867), false),
    PowerUp_ArtilleryStrike(1, "PowerUp Artillery Strike", "PowerUp Artillery Strike", Arrays.asList(862), false, true),
    PowerUp_Flamethrower(1, "PowerUp Flamethrower", "PowerUp Artillery Strike", Arrays.asList(862), false, true),
    PowerUp_Cryogun(1, "PowerUp Cryogun", "PowerUp Cryogun", Arrays.asList(862), false, true),
    PowerUp_Laser(1, "PowerUp Laser", "PowerUp Laser", Arrays.asList(862), false, true),
    PowerUp_Plasma(1, "PowerUp Plasma", "PowerUp Artillery Strike", Arrays.asList(862), false, true);

    private int availableShots;
    private String title;
    private String mathTitle;
    private List<Integer> availableGameIds;
    private boolean isInternalServerShot;
    private boolean isPowerUp;

    SpecialWeaponType(int availableShots, String title, String mathTitle, List<Integer> availableGameIds
            , boolean isInternalServerShot) {
        this.availableShots = availableShots;
        this.title = title;
        this.mathTitle = mathTitle;
        this.availableGameIds = availableGameIds;
        this.isInternalServerShot = isInternalServerShot;
        this.isPowerUp = false;
    }

    SpecialWeaponType(int availableShots, String title, String mathTitle, List<Integer> availableGameIds
            , boolean isInternalServerShot, boolean isPowerUp) {
        this.availableShots = availableShots;
        this.title = title;
        this.mathTitle = mathTitle;
        this.availableGameIds = availableGameIds;
        this.isInternalServerShot = isInternalServerShot;
        this.isPowerUp = isPowerUp;
    }

    public boolean isInternalServerShot() {
        return isInternalServerShot;
    }

    public List<Integer> getAvailableGameIds() {
        return availableGameIds;
    }

    public void setAvailableGameIds(List<Integer> availableGameIds) {
        this.availableGameIds = availableGameIds;
    }

    public int getId() {
        return ordinal();
    }

    public int getAvailableShots() {
        return availableShots;
    }

    public String getTitle() {
        return title;
    }

    public String getMathTitle() {
        return mathTitle;
    }

    public void setMathTitle(String mathTitle) {
        this.mathTitle = mathTitle;
    }

    public boolean isPowerUp() {
        return isPowerUp;
    }

    public static SpecialWeaponType getByMathTitle(String mathTitle) {
        for (SpecialWeaponType type : values()) {
            if (type.getMathTitle().equals(mathTitle)) {
                return type;
            }
        }
        return null;
    }

    public static SpecialWeaponType getByTitle(String title) {
        for (SpecialWeaponType type : values()) {
            if (type.getTitle().equals(title)) {
                return type;
            }
        }
        return null;
    }

    public boolean isAvailable(GameType gameType) {
        return availableGameIds.contains((int) gameType.getGameId());
    }
}
