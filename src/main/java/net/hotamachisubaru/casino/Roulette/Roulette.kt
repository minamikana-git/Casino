package net.hotamachisubaru.casino.Roulette

import net.hotamachisubaru.casino.Casino
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class Roulette {
    private fun addToJackpotFromBet(betAmount: Int) {
        val plugin = Casino.getPlugin<Casino>(Casino::class.java)
        val increaseRate = plugin.getJackpotConfig().getDouble("jackpot_increase_rate", 0.05)
        val amountToAdd = (betAmount * increaseRate).toInt()
        plugin.addToJackpot(amountToAdd)
    }

    private fun checkJackpot(player: Player) {
        val plugin = Casino.getPlugin<Casino>(Casino::class.java)
        val winChance = plugin.getJackpotConfig().getDouble("jackpot_win_chance", 0.01)

        // ランダムに抽選
        if (Math.random() < winChance) {
            val jackpotAmount = plugin.getJackpotAmount()
            player.sendMessage("おめでとうございます！ジャックポットを当てました！獲得額: " + jackpotAmount + " チップ！")
            plugin.addChips(player, jackpotAmount)
            plugin.resetJackpot()
        } else {
            player.sendMessage("ジャックポットには当選しませんでしたが、また挑戦してください！")
        }
    }


    private fun executeRoulette(player: Player, bet: String, betAmount: Int) {
        val plugin = Casino.getPlugin<Casino>(Casino::class.java)

        if (plugin.getChips(player) < betAmount) {
            player.sendMessage("チップが足りません。最低 " + betAmount + " チップが必要です。")
            return
        }

        // 賭け金の一部をジャックポットに加算
        addToJackpotFromBet(betAmount)

        // ルーレットの結果をランダムに決定
        val outcomes = mutableListOf<String?>("赤", "黒", "緑")
        val result = outcomes.get(Random().nextInt(outcomes.size))
        player.sendMessage("ルーレットは " + result + " に止まりました！")

        // プレイヤーの賭けと結果を比較して勝敗を決定
        if (bet == result) {
            player.sendMessage("おめでとう！勝ちました！")
            plugin.addChips(player, betAmount * 2) // 勝利時は2倍のチップを与える

            // ジャックポットの抽選
            checkJackpot(player)
        } else {
            player.sendMessage("残念、負けました。次の挑戦をお待ちしています！")
            plugin.removeChips(player, betAmount)
        }
    }


    companion object {
        @JvmStatic
        fun openRouletteGUI(player: Player) {
            val rouletteGUI = Bukkit.createInventory(null, 9, "ルーレットホイール")

            // 賭けのオプションを設定
            rouletteGUI.setItem(0, createBetItem("赤", Material.RED_WOOL))
            rouletteGUI.setItem(1, createBetItem("黒", Material.BLACK_WOOL))
            rouletteGUI.setItem(2, createBetItem("緑", Material.GREEN_WOOL))
            // 他のスロットは空にしておく
            for (i in 3..8) {
                rouletteGUI.setItem(i, ItemStack(Material.AIR))
            }

            player.openInventory(rouletteGUI)
        }


        private fun createBetItem(name: String?, material: Material): ItemStack {
            val item = ItemStack(material)
            val meta = item.getItemMeta()
            if (meta != null) {
                meta.setDisplayName(name)
                item.setItemMeta(meta)
            }
            return item
        }
    }
}

