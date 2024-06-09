package me.supcheg.seabattle.ui.argument;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonArrayArgumentType implements ArgumentType<JsonArray> {
    private final Gson gson;

    @NotNull
    public static JsonArrayArgumentType jsonArray(@NotNull Gson gson) {
        return new JsonArrayArgumentType(gson);
    }

    @NotNull
    public static JsonArray getJsonArray(@NotNull CommandContext<?> ctx, @NotNull String name) {
        return ctx.getArgument(name, JsonArray.class);
    }

    @NotNull
    @Override
    public JsonArray parse(@NotNull StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        try {
            return gson.fromJson(reader.getRemaining(), JsonArray.class);
        } catch (Exception e) {
            reader.setCursor(cursor);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }
    }
}
