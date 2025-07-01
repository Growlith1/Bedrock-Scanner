
package com.example.bedrockscanner;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType.IntegerArgument;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class BedrockScannerMod implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("scanbedrock")
                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, 32))
                .then(CommandManager.argument("y", IntegerArgumentType.integer(1, 255))
                .executes(context -> scanBedrock(context,
                    IntegerArgumentType.getInteger(context, "radius"),
                    IntegerArgumentType.getInteger(context, "y"))))));
        });
    }

    private int scanBedrock(CommandContext<ServerCommandSource> context, int radius, int y) {
        ServerWorld world = Objects.requireNonNull(context.getSource().getWorld());
        BlockPos origin = context.getSource().getPlayer().getBlockPos();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("bedrock_scan.txt", false));
            for (int dx = -radius * 16; dx <= radius * 16; dx++) {
                for (int dz = -radius * 16; dz <= radius * 16; dz++) {
                    BlockPos pos = new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
                    if (world.getBlockState(pos).isOf(Blocks.BEDROCK)) {
                        writer.write(pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "\n");
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            context.getSource().sendError(Text.literal("Failed to write file: " + e.getMessage()));
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.literal("Bedrock scan complete."), false);
        return 1;
    }
}
