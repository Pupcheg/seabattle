package me.supcheg.seabattle.ui;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lombok.RequiredArgsConstructor;
import me.supcheg.seabattle.BattleFieldService;
import me.supcheg.seabattle.BattleShip;
import me.supcheg.seabattle.BattleShipInsertion;
import me.supcheg.seabattle.Position;
import me.supcheg.seabattle.Rotation;
import me.supcheg.seabattle.SelfField;
import me.supcheg.seabattle.SelfFieldGenerator;
import me.supcheg.seabattle.config.BattleShipEntry;
import me.supcheg.seabattle.config.MutableBattleShipEntry;
import me.supcheg.seabattle.config.SeaBattleConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static me.supcheg.seabattle.ui.argument.BattleShipRotationArgumentType.getRotation;
import static me.supcheg.seabattle.ui.argument.BattleShipRotationArgumentType.rotation;
import static me.supcheg.seabattle.ui.argument.JsonArrayArgumentType.getJsonArray;
import static me.supcheg.seabattle.ui.argument.JsonArrayArgumentType.jsonArray;
import static me.supcheg.seabattle.ui.argument.PositionArgumentType.getPosition;
import static me.supcheg.seabattle.ui.argument.PositionArgumentType.position;

@RequiredArgsConstructor
public final class BattleFieldCreation {
    private final SeaBattleConfiguration configuration;
    private final FieldCreationMode mode;
    private final BattleFieldRenderer renderer;
    private final BattleFieldService controller;
    private final Terminal terminal;
    private final CommandDispatcher<Object> commandDispatcher;
    private final SelfField field;
    private final Map<Integer, MutableBattleShipEntry> leftEntries;
    private final Gson gson;
    private boolean done;

    public BattleFieldCreation(@NotNull FieldCreationMode mode,
                               @NotNull BattleFieldRenderer renderer,
                               @NotNull SeaBattleConfiguration configuration,
                               @NotNull BattleFieldService controller,
                               @NotNull Terminal terminal) {
        this.configuration = configuration;
        this.gson = new Gson();
        this.mode = mode;
        this.renderer = renderer;
        this.leftEntries = configuration.getShipEntries().stream()
                .collect(Collectors.toMap(BattleShipEntry::getLength, MutableBattleShipEntry::new));
        this.controller = controller;
        this.terminal = terminal;
        this.commandDispatcher = buildCommandDispatcher();
        this.field = new SelfField(configuration.getFieldSize());
    }

    @NotNull
    private CommandDispatcher<Object> buildCommandDispatcher() {
        CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();

        dispatcher.register(literal("place")
                .then(argument("position", position())
                        .then(argument("length", integer())
                                .then(argument("rotation", rotation())
                                        .executes(this::placeShip)
                                )
                        )
                )
        );

        dispatcher.register(literal("remove")
                .then(argument("position", position())
                        .executes(this::removeShip)
                )
        );

        dispatcher.register(literal("done")
                .executes(this::markAsDone)
        );

        dispatcher.register(literal("load")
                .then(argument("json", jsonArray(gson))
                        .executes(this::load)
                )
        );

        dispatcher.register(literal("generate")
                .executes(this::generateField)
        );

        return dispatcher;
    }

    @NotNull
    public SelfField makeField() {
        do {
            terminal.clear();
            printFieldEditor();
            handleNextCommand();
        } while (!done);
        return field;
    }

    private void handleNextCommand() {
        try {
            commandDispatcher.execute(terminal.nextNonBlankLine(), null);
        } catch (Exception e) {
            terminal.println("Invalid command. Error: " + e.getMessage());
            terminal.pause();
        }
    }

    private int generateField(@NotNull CommandContext<Object> ctx) {
        List<BattleShipInsertion> insertions = new SelfFieldGenerator(configuration, controller).generateInsertions();
        insertions.forEach(this::placeInsertion);
        return Command.SINGLE_SUCCESS;
    }

    private int load(@NotNull CommandContext<Object> ctx) {
        JsonArray json = getJsonArray(ctx, "json");
        List<BattleShipInsertion> entries = gson.fromJson(json, new TypeToken<List<BattleShipInsertion>>() {}.getType());

        int placed = entries.stream()
                .mapToInt(this::placeInsertion)
                .sum();

        terminal.printf("Loaded %d of %d ships\n", placed, entries.size());

        return placed;
    }

    private int markAsDone(@NotNull CommandContext<Object> ctx) {
        if (mode == FieldCreationMode.VALID_ONLY && !isDone()) {
            terminal.println("Not all ships are put in their places");
            terminal.pause();
            return 0;
        }
        done = true;
        return 1;
    }

    private int removeShip(@NotNull CommandContext<Object> ctx) {
        Position position = getPosition(ctx, "position");

        BattleShip ship = controller.findShipByAnyPosition(field, position);
        if (ship == null) {
            terminal.println("No ship found at this position");
            terminal.pause();
            return 0;
        }

        leftEntries.get(ship.getAllPositions().size()).incrementAmount();
        field.getLeftShips().remove(ship);

        return Command.SINGLE_SUCCESS;
    }

    private int placeShip(@NotNull CommandContext<Object> ctx) {
        int length = getInteger(ctx, "length");
        Position position = getPosition(ctx, "position");
        Rotation rotation = getRotation(ctx, "rotation");

        if (!hasLeftShipWithLength(length)) {
            terminal.printf("No ships left with %d length\n", length);
            terminal.pause();
            return 0;
        }

        BattleShipInsertion insertion = new BattleShipInsertion(
                position,
                length,
                rotation
        );

        return placeInsertion(insertion);
    }

    private int placeInsertion(@NotNull BattleShipInsertion insertion) {
        BattleFieldService.PlaceResult placeResult = controller.canPlace(field, insertion);
        if (!placeResult.isSuccess()) {
            terminal.println(placeResult.getMessage());
            terminal.pause();
            return 0;
        }

        Objects.requireNonNull(leftEntries.get(insertion.getLength()), "No entry for len=" + insertion.getLength())
                .subtractAmount();
        field.getLeftShips().add(controller.insertionToShip(insertion));
        return Command.SINGLE_SUCCESS;
    }

    private boolean isDone() {
        return leftEntries.values().stream()
                .mapToInt(MutableBattleShipEntry::getAmount)
                .noneMatch(amount -> amount > 0);
    }

    private boolean hasLeftShipWithLength(int length) {
        MutableBattleShipEntry entry = leftEntries.get(length);
        return entry != null && entry.getAmount() > 0;
    }

    private void printFieldEditor() {
        renderer.renderField(field, terminal.getOut());
        renderShipsInfo(leftEntries.values(), terminal.getOut());
    }

    private void renderShipsInfo(@NotNull Collection<MutableBattleShipEntry> entries, @NotNull PrintStream out) {
        out.print("Left ships: ");
        entries.stream()
                .filter(entry -> entry.getAmount() != 0)
                .sorted(Comparator.comparingInt(MutableBattleShipEntry::getLength).reversed())
                .forEach((entry) -> out.printf("%d: %d units. ", entry.getLength(), entry.getAmount()));
        out.println();
    }

    public enum FieldCreationMode {
        VALID_ONLY, ANY
    }

}
