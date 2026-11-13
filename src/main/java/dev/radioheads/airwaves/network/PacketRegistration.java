package dev.radioheads.airwaves.network;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.network.payload.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Airwaves.MOD_ID)
public class PacketRegistration {
    @SubscribeEvent
    public static void registerPayload(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(TunePayload.TYPE, TunePayload.CODEC, TunePayload::onTune);
        registrar.playToServer(MicrophonePayload.TYPE, MicrophonePayload.CODEC, MicrophonePayload::onUse);
        registrar.playToServer(HfPanelPayload.TYPE, HfPanelPayload.CODEC, HfPanelPayload::onTurn);
        registrar.playToServer(TelephoneKeyPayload.TYPE, TelephoneKeyPayload.CODEC, TelephoneKeyPayload::onKey);
        registrar.playToServer(CableCutPayload.TYPE, CableCutPayload.CODEC, CableCutPayload::onCut);
        registrar.playToClient(RadioLinkPayload.TYPE, RadioLinkPayload.CODEC, RadioLinkPayload::onReceive);
        registrar.playToClient(RadioSoundPayload.TYPE, RadioSoundPayload.CODEC, RadioSoundPayload::onReceive);
        registrar.playToClient(RadioSongPayload.TYPE, RadioSongPayload.CODEC, RadioSongPayload::onReceive);
    }
}
