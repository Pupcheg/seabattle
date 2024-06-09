package me.supcheg.seabattle.ui.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.supcheg.seabattle.Rotation;
import org.jetbrains.annotations.NotNull;

public final class BattleShipRotationArgumentType implements ArgumentType<Rotation> {

    @NotNull
    public static BattleShipRotationArgumentType rotation() {
        return new BattleShipRotationArgumentType();
    }

    @NotNull
    public static Rotation getRotation(@NotNull CommandContext<?> ctx, @NotNull String name) {
        return ctx.getArgument(name, Rotation.class);
    }

    @NotNull
    @Override
    public Rotation parse(@NotNull StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        try {
            String raw = reader.readString();
            return switch (raw.toLowerCase()) {
                case "vertical", "vert", "v" -> Rotation.VERTICAL;
                case "horizontal", "hor", "h" -> Rotation.HORIZONTAL;
                default -> throw new IllegalStateException("Unexpected value: " + raw);
            };
        } catch (Exception e) {
            reader.setCursor(cursor);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }
    }
}
