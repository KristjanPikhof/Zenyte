package Utility.Trivia;

import BotUtils.eActions;
import java.util.logging.Logger;

import net.runelite.api.ChatMessageType;
import simple.robot.api.ClientContext;

import static eApiAccess.eAutoResponser.randomSleeping;

public class eTriviaInfo {

    public static ClientContext ctx;
    public eTriviaInfo(ClientContext ctx) {
        eTriviaInfo.ctx = ctx;
    }
    public static String triviaAnswer;
    private static final Logger logger = Logger.getLogger(eTriviaInfo.class.getName());

    // Trivia action
    public static void sendAnswer(String answer) {
        if (answer == null) {
            return;
        }

        if (ctx.dialogue.dialogueOpen()) {
            ctx.dialogue.clickContinue();
        }

        StringBuilder writeAnswer = new StringBuilder("::ans ");
        writeAnswer.append(answer);

        Thread thread = new Thread(() -> {
            try {
                int sleepTime = randomSleeping(5000, 10000);
                eActions.updateStatus(eActions.getCurrentTimeFormatted() + " [Trivia] Sleeping for " + sleepTime + "ms");
                Thread.sleep(sleepTime); // Randomized delay
                ctx.keyboard.sendKeys(writeAnswer.toString());
                triviaAnswer = null;
                eActions.updateStatus(eActions.getCurrentTimeFormatted() + " [Trivia] Question answered!");
                eActions.updateStatus(eActions.getCurrentTimeFormatted() + " [Trivia] A: " + answer);
            } catch (InterruptedException e) {
                logger.severe("Error while sending answer: " + e.getMessage());
            }
        });
        thread.start();
    }

    public static void handleBroadcastMessage(ChatMessageType type, String message) {
        if (type == ChatMessageType.BROADCAST) {
            String gameMessageTrimmed = message.replaceAll("<[^>]+>", "").trim();
            if (gameMessageTrimmed.contains("Trivia")) {
                handleTriviaQuestion(gameMessageTrimmed);
            }
        }
    }

    public static void handleTriviaQuestion(String gameMessageTrimmed) {
        for (eTriviaInfo.TriviaQuestion triviaQuestion : eTriviaInfo.TriviaQuestion.values()) {
            if (gameMessageTrimmed.contains(triviaQuestion.getQuestion())) {
                triviaAnswer = triviaQuestion.getAnswer();
                sendAnswer(triviaAnswer);
                break;
            }
        }
    }

    public enum TriviaQuestion {
        FALADOR_MASSACRE("year did the Falador Massacre originally occur", "2006"),
        DWARHAMMER_DROPRATE("dragon warhammer is a 1 in", "2000"),
        MAX_COMBAT("is the maximum combat level", "126"),
        MYTH_GUILD("boss slayer master located", "Myth guild"),
        DRAMEN_STAFF("need to travel by Fairy Ring", "Dramen"),
        KALPHITE_QUEEN("is combat level of the Kalphite Queen", "333"),
        WYSON("discovered giant moles", "Wyson"),
        ARDY_COURSE("highest level rooftop Agility course", "Ardougne"),
        STEAM_RUNES("Steam runes are a combination of water and which other rune", "Fire"),
        SLAY_MASTERS("slayer masters are there in zenyte", "9"),
        DRAGON_WHAMMER_SPEC("stat does the dragon warhammer special attack decrease", "defence"),
        CORP_WILDY_LVL("Wilderness level is the Corporal Beasts lair", "21"),
        ELY_DROP_RATE("lysian sigil is a 1 in ????", "2304"),
        DRAGON_MACE("special attack energy does the Dragon mace special attack use", "25%"),
        INFERNAL_EELS("fishing level is required to fish Infernal eels", "80"),
        ANDREW_GOWER("one of the original creators of Runescape", "Andrew Gower"),
        KARAMBWANJI("is the name of the stackable fish that", "Karambwanji"),
        THIEVING_MASTER_FARMER("level is required to pickpocket Master farmers", "38"),
        FIGHT_CAVES("wave number do donators start the fight caves at", "31"),
        DRAGON_HARPOON("combine a smouldering stone to a dragon harpoon", "85"),
        WESTERN_ACHIEVEMENT("Achievement Diary gives you Unlimited Teleports to the Piscatoris Fishing Colony", "Western province"),
        ENCHANT_DCROSSBOW_BOLT("dragonstone crossbow bolts cost 1 cosmic, 10 soul, and 15 of", "Earth"),
        LUCKY_IMPLING("99 Hunter to catch barehanded", "Lucky"),
        MAX_ITEMS("max limit for a single item stack in Runescape", "2147483647"),
        POISON_BOLTS("type of enchanted bolt can inflict poison damage", "Emerald"),
        INCREDIBLE_REFLEXES("activate the Incredible Reflexes", "34"),
        MINE_CRYSTAL_LVL("mining level to be able to mine crystal shards", "94"),
        LUNAR_SPIN_FLAX("spin per cast of the Lunar Spin Flax", "5"),
        ALI_RESCUE("OSRS quest can you complete to pass through the gates to Al Kharid for free", "Prince Ali Rescue"),
        KYLIE_MINNOW("the fish you can trade to an NPC named Kylie","Minnow"),
        GRAIN_PLENTY("floor is the Grain of Plenty located on","Second"),
        CONVERT_VOID("many points is it to upgrade a SINGLE piece of void into elite", "40"),
        FIRELIGHTERS_TYPES("many types of firelighters are", "5"),
        JATIZSO_ORES("Jatizso contains adamantite, tin, coal, and which other", "Mithril"),
        WINTERTODT_CONTINENT("continent is Wintertodt located on", "Zeah"),
        ZENYTE_HOME("name of the town where Zenyte's home is", "Edgeville"),
        CERBERUS_RED_DROP("name of the red crystal dropped by Cerberus", "Primordial"),
        FAIRY_MISCELLANIA("is the fairy ring code for Miscellania", "cip"),
        CERBERUS_PURPLE_DROP("name of the purple crystal dropped by Cerberus", "Eternal"),
        TOAD_LEGS("??? legs are used in Gnome cooking", "Toad"),
        BARROWS_GLOVES("defense level is required to wear Barrows gloves", "40"),
        PICKPOCKET_ELF("thieving level is required to pickpocket an elf", "85"),
        INFERNO_WAVES("many inferno waves are there", "69"),
        SKILLING_OUTFIT_RATE("drop rate of any skilling outfit piece whilst", "1/1000"),
        SANDSTONE_WEIGHT("largest weight of sandstone you can mine", "10kg"),
        SMOKE_DUNGEON("equipment is required to survive the Smoke Dungeon", "Face mask"),
        TORN_SCROLL("do you obtain a torn prayer scroll", "Cox"),
        KRAKEN_ENHANCE("you need 10 of to enchant the trident of the", "Kraken"),
        THIRDAGE_CLOAK("3rd age cloak is obtained from which tier clues", "Elite"),
        FULL_GRACE("Marks of Grace does full Graceful cost on Zenyte", "104"),
        ZENYTE_MAX_TOTAL("is the current max total level in Zenyte", "2179"),
        DS2_FINAL_BOSS("final boss you defeat in Dragon Slayer II", "Galvek"),
        COMBAT_ARCHIEVEMENTS("you the jad slayer helmet recolour", "Elite"),
        CRYSTAL_MINE("is the required mining level to be able to mine crystal", "94"),
        UPGRADE_VOID("many points is it to upgrade a SINGLE piece of void into elite", "40"),
        WHITE_CASTLE("what city is the White Knights castle located", "Falador"),
        SUPER_DEFENCE_MIX("level to make super defense mix", "71"),
        DRAGON_MACE_SPEC("does the Dragon mace special attack use", "25%"),
        NPC_NIEVE("NPC to be found wearing an Elysian Spirit Shield", "Nieve"),
        DARK_CORE_PET("drops the pet Dark core", "Corp"),
        SAILING_SKILL("Name one skill that was polled in OSRS but didn", "Sailing"),
        ZULRAH_LVL("combat level of Zulrah", "725"),
        MORE_VOTE_REWARDS("can earn more vote rewards by having what", "2fa"),
        MAGIC_TREE("tree requires 75 Woodcutting to cut", "Magic"),
        MAGIC_ECCENCE("level is required to make a Magic Essence mix", "61"),
        TWISTED_BOW("s damage is scaled based on the targets", "Twisted bow"),
        HUNTER_TRAPS("hunter level is required to lay 5 traps", "80"),
        CRYSTAL_EQUIPMENT("agility level is required to use crystal equipment", "50"),
        B0ATY_NUMBER("is the famous 'b0aty' number", "73"),
        WILD_PIE("A Wild Pie is made from raw rabbit,", "Chompy"),
        DRAGON_DAGGER("one of the items unlocked upon completion of The Lost City", "Dragon dagger"),
        ZENYTE_YEAR("zenyte released to the public?", "2019"),
        TOB_ZENYTE("Theatre of Blood release to the public on Zenyte?", "2022"),
        ZENYTE_JACKIE("person that sells various skilling tools at home", "Jackie"),
        SLAYER_CHAELDAR("name of the Fairy Slayer Master", "Chaeldar"),
        BALLISTA_AMMMO("type of ammunition is used in the ballista", "Javelins"),
        GODS_ALE("do you obtain an ale of the gods", "clue scroll reward"),
        GUTHIX_BOOK("name of the completed god book of Guthix", "Balance"),
        INJURE_CERBERUS("Slayer level do you need to be able to injure Cerberus", "91"),
        RUNES_IN_RUNEPACK("many runes are inside of a rune pack", "100"),
        GOD_HIDE_SETS("hide sets are there in game?", "6"),
        ADAMANT_BAR("smithing an Adamant bar in a regular furnace", "6"),
        HUNTER_SIREN("Hunter level is required in order to catch a Greater Siren", "87"),
        SUPERCOMPOST_MADEOF("type of compost do coconut shells produce when placed into a composting", "supercompost"),
        WILDY_BOSS_DRAGON("considered a Wilderness boss", "King Black Dragon"),
        COOKING_SHARKS("cooking level do you need to be able to cook sharks", "80"),
        RUBY_AMULET("magic level is required to enchant a ruby", "49"),
        JAD_HELMET("tier completion unlocks you the jad slayer helmet recolour", "Elite"),
        AVERIC_DEFENDER("is the best defender in the game called", "Avernic defender"),
        DROP_DWH("is the rarest unique drop from Lizardman Shamans", "Dragon warhammer"),
        CRAFT_ONYX("Crafting level is required to be able to cut Onyx", "67"),
        BOB_MERCHANT("is Bob the axe merchant located", "Lumbridge"),
        GRAIN_OF_PLENTY("Grain of Plenty located on in the Stronghold of Security", "Second"),
        BARROWS_REPAIR("NPC that will repair your Barrows armor for fee", "Bob"),
        GRAVEYARD_TELE("spellbook do you need to be on to teleport to the Salve Graveyard", "Arceuus"),
        GRAATOR_RACE("race of monster is General Graardor", "Ourg"),
        REV_CAVES("Rev caves, this wilderness dungeons actual", "Forinthry"),
        SARA_BREW("level is required to be able to make a Saradomin brew", "81"),
        GOD_BANDOS("Bandos is the god of .", "War"),
        XARPUS_BOSS("second to last boss in the Theatre of Blood", "Xarpus"),
        DURADER_SLAYER("slayer level is required to use Duradel", "50"),
        BLACK_CHIN("required to catch a black chinchompa", "73"),
        VESTA_ITEM("ancient warriors whose armour you can get from Wilderness drops", "Vesta"),
        WOODCUTTING_PET("kind of animal is the Woodcutting pet", "Beaver"),
        NEWSPAPERS_VARROCK("NPC that sells newspapers in Varrock", "Benny"),
        THIEVING_PET("name of the Thieving pet", "Rocky"),
        HARD_CLUE_VOTE("does a hard clue bottle cost in the", "9"),
        MAX_CAPE_MAC("in order to buy a skill cape in Zenyte", "Mac"),
        DRAGON_FH("monster drops the Dragon full helm", "Mithril dragon"),
        ZAHUR("NPC that can crush your herblore secondaries", "Zahur"),
        SUPERIOR_MINING_GLOVES("mining level is required to wear Superior mining gloves", "55"),
        SPECTRAL_SHIELD("shield reduces the effectiveness of all Prayer draining", "Spectral spirit shield"),
        CANOE_TRANSPORTATION("method of transport can take you all the way along the River Lum", "Canoe"),
        DEFENDERS_ZENYTE("many different types of defenders are there in Zenyte", "9"),
        QP_NEEDED_BGLOVES("quest points do you need to be able to defeat the Culiniromancer", "175"),
        RING_WDISCOUNT("name of the ring that can charm certain", "Charos"),
        HIGHEST_CLUES("name of the highest tier of clue scroll", "Master"),
        TREE_LIKE_YAK("tree smells of yak", "Arctic pine"),
        WOAD_SEED("farming level is required to plant a woad seed", "25"),
        GOD_ARMADYL("Armadyl is the god of ...", "Justice"),
        VARROCK_TOBY("Taskmaster for the Varrock", "Toby"),
        T10_EMBLEM_POINTS("for selling a T10", "5,000,000"),
        SARA_BREW_HERB("herb do you need to make a Saradomin brew", "Toadflax"),
        LOWEST_RATE_ON_ZENYTE("is the lowest XP rate you can have on Zenyte", "5x"),
        HYDRA_COMBAT("combat level is the Alchemical Hydra", "426"),
        HEAVIEST_ITEM("heaviest item you can carry in the Weapon slot", "Barrelchest anchor"),
        BARRELCHEST_ANCHOR_COST("a Barrelchest Anchor cost in the slayer reward shop", "200"),
        TYPES_OF_WANDS("types of wands are there", "6"),
        OVERLOAD_DAMAGE("name of the potion that boosts your stats but damages you", "Overload");

        private final String question;
        private final String answer;

        TriviaQuestion(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }
        public String getAnswer() {
            return answer;
        }
    }

}
