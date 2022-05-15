package snowesamosc.mtgturing;

public enum CardList {
    RotlungReanimator("Rotlung Reanimator"),
    XathridNecromancer("Xathrid Necromancer"),
    CloakOfInvisibility("Cloak of Invisibility"),
    WheelOfSunAndMoon("Wheel of Sun and Moon"),
    IllusoryGains("Illusory Gains"),
    SteelyResolve("Steely Resolve"),
    DreadOfNight("Dread of Night"),
    FungusSliver("Fungus Sliver"),
    SharedTriumph("Shared Triumph"),
    WildEvocation("Wild Evocation"),
    Recycle("Recycle"),
    PrivilegedPosition("Privileged Position"),
    Vigor("Vigor"),
    MesmericOrb("Mesmeric Orb"),
    AncientTomb("Ancient Tomb"),
    PrismaticOmen("Prismatic Omen"),
    Choke("Choke"),
    BlazingArchon("Blazing Archon"),
    CleansingBeam("Cleansing Beam"),
    OliviaVoldaren("Olivia Voldaren"),
    SoulSnuffers("Soul Snuffers"),
    PrismaticLace("Prismatic Lace"),
    CoalitionVictory("Coalition Victory"),
    ;

    private final String originalName;

    CardList(String originalName) {
        this.originalName = originalName;
    }

    public String getOriginalName() {
        return this.originalName;
    }
}
