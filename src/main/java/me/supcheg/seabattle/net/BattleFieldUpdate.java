package me.supcheg.seabattle.net;

import lombok.Data;
import me.supcheg.seabattle.field.SelfField;

@Data
public final class BattleFieldUpdate {
    private final SelfField battleField;
}
