package me.supcheg.seabattle.ui.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.supcheg.seabattle.Position;
import org.jetbrains.annotations.NotNull;

public final class PositionArgumentType implements ArgumentType<Position> {

    @NotNull
    public static PositionArgumentType position() {
        return new PositionArgumentType();
    }

    @NotNull
    public static Position getPosition(@NotNull CommandContext<?> ctx, @NotNull String name) {
        return ctx.getArgument(name, Position.class);
    }

    @NotNull
    @Override
    public Position parse(@NotNull StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        try {
            String raw = reader.readString();
            return Position.fromString(raw);
        } catch (Exception e) {
            reader.setCursor(cursor);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }
    }
}
