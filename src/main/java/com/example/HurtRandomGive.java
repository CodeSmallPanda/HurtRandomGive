package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HurtRandomGive implements ModInitializer {
	private static final List<Item> ALL_ITEMS = new ArrayList<>();
	private static final ConcurrentHashMap<PlayerEntity, Long> cooldowns = new ConcurrentHashMap<>();
	private static final long COOLDOWN_MS = 500;

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			if (ALL_ITEMS.isEmpty()) {
				for (Identifier id : Registries.ITEM.getIds()) {
					Item item = Registries.ITEM.get(id);
					if (item != Items.AIR) {
						ALL_ITEMS.add(item);
					}
				}
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			cooldowns.remove(handler.player);
		});

		net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayerEntity player) {
				if (amount <= 0 || "cactus".equals(source.getName())) {
					return true;
				}

				long currentTime = System.currentTimeMillis();
				Long lastHitTime = cooldowns.get(player);
				if (lastHitTime != null && currentTime - lastHitTime < COOLDOWN_MS) {
					return true;
				}
				cooldowns.put(player, currentTime);

				processInventoryReplacement(player);
			}
			return true;
		});
	}

	/**
	 * 核心方法：遍历背包每个格子，进行一对一的双倍数量随机物品替换。
	 * 新物品的数量 = 原格子物品数量 * 2
	 */
	private void processInventoryReplacement(PlayerEntity player) {
		Random random = player.getRandom();

		// 遍历玩家库存的每一个格子（包括主背包和快捷栏，通常为0到35号格子）
		for (int slotIndex = 0; slotIndex < player.getInventory().size(); slotIndex++) {
			ItemStack originalStack = player.getInventory().getStack(slotIndex);

			// 跳过空格子
			if (originalStack.isEmpty()) {
				continue;
			}

			// 获取原格子中物品的数量
			int originalCount = originalStack.getCount();
			// 计算新物品应有的数量
			int newCount = originalCount * 2;

			// 从全局物品列表中随机选择一个新物品（排除空气）
			Item randomItem = ALL_ITEMS.get(random.nextInt(ALL_ITEMS.size()));

			// 创建新的物品堆叠，数量为原数量的两倍
			ItemStack newStack = new ItemStack(randomItem, newCount);

			// 将新物品直接放入原格子，替换掉旧物品
			player.getInventory().setStack(slotIndex, newStack);
		}
	}
}