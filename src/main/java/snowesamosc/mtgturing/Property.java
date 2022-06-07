package snowesamosc.mtgturing;

import snowesamosc.mtgturing.cards.CardColor;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.CardType;
import snowesamosc.mtgturing.cards.CreatureToken;

import java.util.stream.Collectors;

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

    public String translate(CardSubType type) {
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

                case Plains -> "平地";
                case Island -> "島";
                case Swamp -> "沼";
                case Mountain -> "山";
                case Forest -> "森";
            };
            default -> type.name();
        };
    }

    public String translate(CardType type) {
        return switch (this.language) {
            case "Japanese" -> switch (type) {
                case Land -> "土地";
                case Creature -> "クリーチャー";
                case Artifact -> "アーティファクト";
                case Enchantment -> "エンチャント";
                case Instant -> "インスタント";
                case Sorcery -> "ソーサリー";
            };
            default -> type.name();
        };
    }

    public String abilitiesString(boolean isHexproof, boolean isShroud, boolean isPhasing) {
        var ability = new StringBuilder();
        if (isHexproof) {
            ability.append(switch (this.language) {
                case "Japanese" -> "呪禁";
                default -> "Hexproof";
            }).append(", ");
        }
        if (isShroud) {
            ability.append(switch (this.language) {
                case "Japanese" -> "被覆";
                default -> "Shroud";
            }).append(", ");
        }
        if (isPhasing) {
            ability.append(switch (this.language) {
                case "Japanese" -> "フェイジング";
                default -> "Phasing";
            }).append(", ");
        }

        var ret = ability.toString();

        //足しすぎた文字の削除
        return ret.isEmpty() ? "" : ret.substring(0, ret.length() - 2);
    }

    public String getTokenCardName(CreatureToken token) {
        return switch (this.getLanguage()) {
            case "Japanese" ->
                    token.originalCardSubTypesForName().stream().map(this::translate).collect(Collectors.joining("・")) + "・トークン";
            default ->
                    token.originalCardSubTypesForName().stream().map(this::translate).collect(Collectors.joining(" ")) + " Token";
        };
    }
}
