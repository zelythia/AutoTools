package net.zelythia.fabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface ClientBlockBreakEvent {

    Event<ClientBlockBreakEvent> EVENT = EventFactory.createArrayBacked(ClientBlockBreakEvent.class,
            (listeners) -> (levelAccessor, blockPos, blockState) -> {
                if (levelAccessor.isClientSide()) {
                    for (ClientBlockBreakEvent event : listeners) {
                        event.clientBlockBreak(levelAccessor, blockPos, blockState);
                    }
                }
            });


    void clientBlockBreak(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState);
}