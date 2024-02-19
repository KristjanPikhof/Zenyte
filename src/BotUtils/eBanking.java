package BotUtils;

import simple.hooks.filters.SimpleBank;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;

import java.util.List;

public class eBanking {
    private static ClientContext ctx;

    public eBanking(ClientContext ctx) {
        eBanking.ctx = ctx;
    }

    //// Bank task methods ////
    public static void bankTask(boolean canUseDepositBox,
                                int bankChestPreferenceOffset,
                                int minItemsInv,
                                boolean withdrawItems,
                                String withdrawItemName,
                                int withdrawAmount,
                                String... notToDepositItemNames) {

        int inventoryPopulation = ctx.inventory.populate().population();

        if (bankIsOpen() && inventoryPopulation >= minItemsInv) {
            eActions.status = "Depositing items";
            ctx.bank.depositAllExcept(notToDepositItemNames);
            if (withdrawItems && !BotUtils.eActions.hasItemsInInventory(null, withdrawItemName)) {
                eActions.status = "Withdrawing items";
                withdrawItem(withdrawItemName, withdrawAmount);
            }
            eActions.status = "Closing bank";
            ctx.bank.closeBank();
            return;
        }

        if (!bankIsOpen()) {
            openClosestBank(canUseDepositBox, bankChestPreferenceOffset);
        }
    }

    public static void bankTask(boolean canUseDepositBox,
                                int bankChestPreferenceOffset,
                                int minItemsInv,
                                boolean withdrawItems,
                                int withdrawItemId,
                                int withdrawAmount,
                                int... notToDepositItemIds) {

        int inventoryPopulation = ctx.inventory.populate().population();

        if (bankIsOpen() && inventoryPopulation >= minItemsInv) {
            eActions.status = "Depositing items";
            ctx.bank.depositAllExcept(notToDepositItemIds);
            if (withdrawItems && !BotUtils.eActions.hasItemsInInventory(eActions.StackableType.BOTH, withdrawItemId)) {
                eActions.status = "Withdrawing items";
                withdrawItem(withdrawItemId, withdrawAmount);
            }
            eActions.status = "Closing bank";
            ctx.bank.closeBank();
            return;
        }

        if (!bankIsOpen()) {
            openClosestBank(canUseDepositBox, bankChestPreferenceOffset);
        }
    }

    public static void bankTask(boolean canUseDepositBox,
                                int bankChestPreferenceOffset,
                                int minItemsInv,
                                boolean withdrawItems,
                                List<String> withdrawItemNames,
                                int withdrawAmount,
                                String... notToDepositItemNames) {

        int inventoryPopulation = ctx.inventory.populate().population();

        if (bankIsOpen() && inventoryPopulation >= minItemsInv) {
            eActions.status = "Depositing items";
            ctx.bank.depositAllExcept(notToDepositItemNames);

            if (withdrawItems) {
                for(String itemName : withdrawItemNames) {
                    if(!BotUtils.eActions.hasItemsInInventory(null, itemName)) {
                        eActions.status = "Withdrawing items";
                        withdrawItem(itemName, withdrawAmount);
                    }
                }
            }
            eActions.status = "Closing bank";
            ctx.bank.closeBank();
            return;
        }

        if (!bankIsOpen()) {
            openClosestBank(canUseDepositBox, bankChestPreferenceOffset);
        }
    }

    public static void bankTask(boolean canUseDepositBox,
                                int bankChestPreferenceOffset,
                                int minItemsInv,
                                boolean withdrawItems,
                                List<Integer> withdrawItemIds,
                                int withdrawAmount,
                                int... notToDepositItemIds) {

        int inventoryPopulation = ctx.inventory.populate().population();

        if (bankIsOpen() && inventoryPopulation >= minItemsInv) {
            eActions.status = "Depositing items";
            ctx.bank.depositAllExcept(notToDepositItemIds);

            if (withdrawItems) {
                for(int itemId : withdrawItemIds) {
                    if(!BotUtils.eActions.hasItemsInInventory(null, itemId)) {
                        eActions.status = "Withdrawing items";
                        withdrawItem(itemId, withdrawAmount);
                    }
                }
            }
            eActions.status = "Closing bank";
            ctx.bank.closeBank();
            return;
        }

        if (!bankIsOpen()) {
            openClosestBank(canUseDepositBox, bankChestPreferenceOffset);
        }
    }
    //// Bank task methods ////

    public static void openClosestBank(boolean canUseDepositBox, int bankChestPreferenceOffset) {

        SimpleObject bankChest = getClosestBankChest();
        SimpleNpc banker = getClosestBanker();
        SimpleObject depositBox = getClosestDepositBox();

        double distToBankChest = (bankChest != null) ? ctx.players.getLocal().getLocation().distanceTo(bankChest.getLocation()) : Double.MAX_VALUE;
        double distToBanker = (banker != null) ? ctx.players.getLocal().getLocation().distanceTo(banker.getLocation()) : Double.MAX_VALUE;
        double distToDepositBox = (depositBox != null) ? ctx.players.getLocal().getLocation().distanceTo(depositBox.getLocation()) : Double.MAX_VALUE;

        // Introduce a preference offset for the bank chest.
        distToBankChest -= bankChestPreferenceOffset;

        // Determine the closest banking method
        double minDistance = Math.min(distToBankChest, Math.min(distToBanker, distToDepositBox));

        if (minDistance == distToBankChest) {
            useBankObject(bankChest);
        } else if (minDistance == distToBanker) {
            useBanker(banker);
        } else if (minDistance == distToDepositBox && canUseDepositBox) {
            useBankObject(depositBox);
        } else {
            eActions.updateStatus("No bank found nearby");
        }
    }

    public static void useBankObject(SimpleObject objectName) {
        if (objectName == null) return;

        if (!objectName.visibleOnScreen()) {
            if (ctx.players.getLocal().getLocation().getRegionID() == 6198) {
                ctx.pathing.step(1591, 3477);
            } else {
                ctx.pathing.step(objectName.getLocation());
            }
        } else {
            if (BotUtils.eActions.menuActionMode) {
                objectName.menuAction("Bank");
            } else {
                objectName.click(0);
            }
            ctx.onCondition(eBanking::bankIsOpen, 250, 20);
        }

/*        if (eActions.targetIsVisible(objectName, eActions.getPlayerLocation(), ctx)) {
            if (BotUtils.eActions.menuActionMode) {
                objectName.menuAction("Bank");
            } else {
                objectName.click(0);
            }
            ctx.onCondition(eBanking::bankIsOpen, 250, 20);
        }*/
    }

    public static void useBanker(SimpleNpc bankerNpc) {
        if (bankerNpc == null) return;

        if (!bankerNpc.visibleOnScreen()) {
            ctx.pathing.step(bankerNpc.getLocation());
        } else {
            BotUtils.eActions.interactWith(bankerNpc, "Bank");
            ctx.onCondition(eBanking::bankIsOpen, 250, 20);
        }
/*        if (eActions.targetIsVisible(bankerNpc, eActions.getPlayerLocation(), ctx)) {
            BotUtils.eActions.interactWith(bankerNpc, "Bank");
            ctx.onCondition(eBanking::bankIsOpen, 250, 20);
        }*/

    }

    public static SimpleObject getClosestDepositBox() {
        return ctx.objects.populate().filter(eData.Banking.DEPOSIT_BOX).nearest().next();
    }

    public static SimpleObject getClosestBankChest() {
        return ctx.objects.populate().filter(eData.Banking.BANK_NAMES).nearest().next();
    }

    public static SimpleNpc getClosestBanker() {
        return ctx.npcs.populate().filter(eData.Banking.BANKER_NAMES).nearest().next();
    }

    public static boolean bankIsOpen() {
        SimpleBank bank = ctx.bank;
        return bank.bankOpen() || bank.depositBoxOpen();
    }

    public static void withdrawItem(String itemName, int withdrawAmount) {
        if (ctx.bank.bankOpen() && !ctx.bank.populate().filter(itemName).isEmpty()){
            ctx.bank.withdraw(itemName, withdrawAmount);
            ctx.onCondition(() -> BotUtils.eActions.hasItemsInInventory(null, itemName), 250, 10);
        }
    }

    public static void withdrawItem(int itemId, int withdrawAmount) {
        if (ctx.bank.bankOpen() && !ctx.bank.populate().filter(itemId).isEmpty()){
            ctx.bank.withdraw(itemId, withdrawAmount);
            ctx.onCondition(() -> BotUtils.eActions.hasItemsInInventory(null, itemId), 250, 10);
        }
    }
}
