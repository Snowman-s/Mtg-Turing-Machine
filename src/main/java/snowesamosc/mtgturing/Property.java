package snowesamosc.mtgturing;

import snowesamosc.mtgturing.cards.CardColor;
import snowesamosc.mtgturing.cards.CreatureType;

public class Property {
    private static final Property instance = new Property();
    private String language;

    private Property() {

    }

    public static Property getInstance() {
        return instance;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String translate(CardColor color) {
        return switch (this.language) {
            case "Japanese" -> switch (color) {
                case Black -> "黒";
                case White -> "白";
                case Red -> "赤";
                case Blue -> "青";
                case Green -> "緑";
            };
            default -> color.name();
        };
    }

    public String translate(CreatureType type) {
        return switch (this.language) {
            case "Japanese" -> switch (type) {
                case Aetherborn -> "霊気体";
                case Basilisk -> "バジリスク";
                case Demon -> "デーモン";
                case Cephalid -> "セファリッド";
                case Elf -> "エルフ";
                case Faerie -> "フェアリー";
                case Giant -> "巨人";
                case Illusion -> "イリュージョン";
                case Harpy -> "ハーピー";
                case Juggernaut -> "巨大戦車";
                case Kavu -> "カヴー";
                case Leviathan -> "リバイアサン";
                case Myr -> "マイア";
                case Noggle -> "ノッグル";
                case Pegasus -> "ペガサス";
                case Orc -> "オーク";
                case Rhino -> "サイ";
                case Sliver -> "スリヴァー";
                case Assassin -> "暗殺者";
                case Rat -> "ネズミ";
                case Lhurgoyf -> "ルアゴイフ";
                case Incarnation -> "インカーネーション";
                case AssemblyWorker -> "組立作業員";
                case Cleric -> "クレリック";
                case Zombie -> "ゾンビ";
                case Human -> "人間";
            };
            default -> type.name();
        };
    }
}
