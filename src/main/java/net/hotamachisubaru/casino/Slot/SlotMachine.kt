package net.hotamachisubaru.casino.Slot

import net.hotamachisubaru.casino.Casino
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object SlotMachine {
    val plugin: Casino = Casino.getPlugin<Casino>(Casino::class.java)

    @JvmStatic
    fun openSlotGUI(player: Player) {
        val slotGUI = Bukkit.createInventory(null, 9, "スロットマシン")

        // 初期状態のスロットをセットアップ
        for (i in 0..8) {
            slotGUI.setItem(i, randomSlotItem)
        }

        player.openInventory(slotGUI)

        // スロットの回転アニメーション
        object : BukkitRunnable() {
            var tick: Int = 0

            override fun run() {
                if (tick < 20) { // 20回アイテムを回転させる
                    for (i in 0..8) {
                        slotGUI.setItem(i, randomSlotItem)
                    }
                    player.updateInventory()
                    tick++
                } else {
                    // 最終結果を表示し、報酬を与える
                    val result = calculateSlotResult(slotGUI)
                    giveRewards(player, result)
                    checkJackpot(player) // ジャックポットの抽選を追加

                    // GUIを数秒後に閉じるタスクをスケジュール
                    object : BukkitRunnable() {
                        override fun run() {
                            if (player.getOpenInventory() != null &&
                                player.getOpenInventory().getTopInventory() === slotGUI
                            ) {
                                player.closeInventory() // GUIを閉じる
                            }
                        }
                    }.runTaskLater(plugin, 60L) // 60L = 3秒後に実行

                    this.cancel() // アニメーション終了
                }
            }
        }.runTaskTimer(plugin, 0L, 5L) // 5Lは0.25秒ごとに実行
    }

    private val randomSlotItem: ItemStack?
        // スロットのアイテムをランダムに取得するメソッド
        get() {
            val slotItems = plugin.getSlotItems()
            if (slotItems.isEmpty()) {
                return null // 設定ファイルにアイテムがない場合はnullを返す
            }

            val randomItem = slotItems.get(Random().nextInt(slotItems.size))
            return ItemStack(randomItem)
        }

    // スロットの結果を計算するメソッド
    private fun calculateSlotResult(slotGUI: Inventory): SlotResult {
        // 例として、3つ同じアイテムが揃えば勝ち
        val first = slotGUI.getItem(3)
        val second = slotGUI.getItem(4)
        val third = slotGUI.getItem(5)

        val win =
            first != null && second != null && third != null && first.getType() == second.getType() && second.getType() == third.getType()

        return SlotResult(win, if (first != null) first.getType() else Material.AIR)
    }

    // プレイヤーに報酬を与えるメソッド
    private fun giveRewards(player: Player, result: SlotResult) {
        if (result.isWin()) {
            player.sendMessage("おめでとう！" + result.getRewardType() + "を獲得しました！")
            player.getInventory().addItem(ItemStack(result.getRewardType(), 5))
        } else {
            player.sendMessage("残念、今回はハズレです。")
        }
    }

    // ジャックポットの抽選を行うメソッド
    private fun checkJackpot(player: Player) {
        val winChance = plugin.getJackpotConfig().getDouble("jackpot_win_chance", 0.01)

        // ランダムに抽選
        if (Math.random() < winChance) {
            val jackpotAmount = plugin.getJackpotAmount()
            player.sendMessage("ジャックポット獲得！獲得額: " + jackpotAmount + " チップ！")
            plugin.addChips(player, jackpotAmount)
            plugin.resetJackpot()
        } else {
            player.sendMessage("JP抽選はハズレ！また挑戦してね！")
        }
    }

    // 賭け金の一部をジャックポットに加算するメソッド
    private fun addToJackpotFromBet(betAmount: Int) {
        val increaseRate = plugin.getJackpotConfig().getDouble("jackpot_increase_rate", 0.05)
        val amountToAdd = (betAmount * increaseRate).toInt()
        plugin.addToJackpot(amountToAdd)
    }
}
