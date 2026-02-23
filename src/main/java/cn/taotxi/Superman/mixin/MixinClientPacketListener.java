package cn.taotxi.Superman.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.taotxi.Superman.util.TickUtils;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Inject(method = "handleSetTime", at = @At("RETURN"))
    private void malilib_onUpdateTickRate(ClientboundSetTimePacket packet, CallbackInfo ci) {
        TickUtils.updateNanoTick(packet.gameTime());
    }
}