package me.supcheg.seabattle.ui.mode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.supcheg.seabattle.field.BattleFieldService;
import me.supcheg.seabattle.ship.BattleShipInsertion;
import me.supcheg.seabattle.ship.BattleShipInsertionConverter;
import me.supcheg.seabattle.field.SelfField;
import me.supcheg.seabattle.config.BattleShipEntry;
import me.supcheg.seabattle.config.SeaBattleConfiguration;
import me.supcheg.seabattle.ui.BattleFieldCreation;
import me.supcheg.seabattle.ui.BattleFieldRenderer;
import me.supcheg.seabattle.ui.Terminal;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@RequiredArgsConstructor
public final class EditorMode implements Runnable {
    private final Terminal terminal;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Override
    public void run() {
        int sizeOfField = 16;
        List<BattleShipEntry> entries = List.of(
                new BattleShipEntry(6, 1),
                new BattleShipEntry(5, 2),
                new BattleShipEntry(4, 3),
                new BattleShipEntry(3, 4),
                new BattleShipEntry(2, 5),
                new BattleShipEntry(1, 6)
        );

        SeaBattleConfiguration configuration = new SeaBattleConfiguration(sizeOfField, entries);

        BattleShipInsertionConverter converter = new BattleShipInsertionConverter();
        BattleFieldService controller = new BattleFieldService(converter);
        BattleFieldCreation battleFieldCreation = new BattleFieldCreation(
                BattleFieldCreation.FieldCreationMode.ANY,
                new BattleFieldRenderer(controller),
                configuration,
                controller,
                terminal
        );

        SelfField field = battleFieldCreation.makeField();
        List<BattleShipInsertion> insertions = field.getLeftShips()
                .stream()
                .map(converter::shipToInsertion)
                .toList();
        gson.toJson(insertions, terminal.getOut());
    }
}
