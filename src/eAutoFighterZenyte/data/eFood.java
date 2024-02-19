package eAutoFighterZenyte.data;

public enum eFood {

    NONE("None", -1),
    SALMON("Salmon", 329),
    TROUT("Trout", 333),
    TUNA("Tuna", 361),
    LOBSTER("Lobster", 379),
    SWORDFISH("Swordfish", 373),
    MONKFISH("Monkfish", 7946),
    SHARK("Shark", 385),
    TUNA_POTATO("Tuna potato", 7060),
    SEA_TURTLE("Sea turtle", 397),
    DARK_CRAB("Dark crab", 11936),
    MANTA_RAY("Manta ray", 391),
    KARAMBWAN("Cooked karambwan", 3144),
    PINEAPPLE_PIZZA("Pineapple pizza", 2301, 2303),
    ANGLERFISH("Anglerfish", 13441);

    private final String name;
    private final int[] itemId;

    eFood(final String name, final int... itemId) {
        this.name = name;
        this.itemId = itemId;
    }

    public int[] getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }


}