package me.supcheg.seabattle;

import me.supcheg.seabattle.config.MutableBattleShipEntry;
import me.supcheg.seabattle.config.SeaBattleConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class SelfFieldGenerator {
    private final SeaBattleConfiguration config;
    private final BattleFieldService controller;
    private final RandomGenerator random;
    private final List<MutableBattleShipEntry> entries;
    private final SelfField selfField;

    public SelfFieldGenerator(@NotNull SeaBattleConfiguration config, @NotNull BattleFieldService controller) {
        this.config = config;
        this.controller = controller;
        this.random = ThreadLocalRandom.current();
        this.selfField = new SelfField(config.getFieldSize());
        this.entries = config.getShipEntries().stream().map(MutableBattleShipEntry::new).toList();
    }

    @NotNull
    public List<BattleShipInsertion> generateInsertions() {
        List<BattleShipInsertion> insertions = new ArrayList<>();

        int mainTries = 50;
        mainInsertion:
        while (true) {
            if (--mainTries == 0) {
                throw new IllegalStateException("Unable to generate field");
            }
            for (MutableBattleShipEntry entry : entries) {
                currentInsertion:
                for (int i = 0; i < entry.getAmount(); i++) {

                    for (int tries = 0; tries < 50; tries++) {
                        BattleShipInsertion insertion = randomInsertion(entry.getLength());
                        if (controller.canPlace(selfField, insertion).isSuccess()) {
                            insertions.add(insertion);
                            selfField.getLeftShips().add(controller.insertionToShip(insertion));
                            continue currentInsertion;
                        }
                    }
                    insertions.clear();
                    continue mainInsertion;

                }
            }
            break;
        }
        return insertions;
    }


    @NotNull
    private BattleShipInsertion randomInsertion(int length) {
        return new BattleShipInsertion(
                randomPosition(),
                length,
                randomRotation()
        );
    }

    @NotNull
    private Position randomPosition() {
        return new Position(random.nextInt(config.getFieldSize() - 1), random.nextInt(config.getFieldSize() - 1));
    }

    @NotNull
    private Rotation randomRotation() {
        return random.nextBoolean() ? Rotation.HORIZONTAL : Rotation.VERTICAL;
    }
}
