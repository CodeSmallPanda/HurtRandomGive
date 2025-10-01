package com.example.mixin;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 这个Mixin用于修改物品的最大堆叠上限。
 */
@Mixin(Item.class) // 指定要混入的目标类是Item
public class ExampleMixin {

	@Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
	private void removeStackLimit(CallbackInfoReturnable<Integer> cir) {
		// 当该方法被调用时，直接设置返回值为9999，并取消原方法的执行
		cir.setReturnValue(9999);
	}
}
