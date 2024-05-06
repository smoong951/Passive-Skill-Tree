package daripher.skilltree.skill.bonus.player;

import com.google.gson.*;
import daripher.skilltree.client.screen.SkillTreeEditorScreen;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class CommandBonus implements SkillBonus<CommandBonus> {
  private @Nonnull String command;
  private @Nonnull String removeCommand;
  private @Nonnull String description;

  public CommandBonus(
      @Nonnull String command, @Nonnull String removeCommand, @NotNull String description) {
    this.command = command;
    this.removeCommand = removeCommand;
    this.description = description;
  }

  @Override
  public void onSkillLearned(ServerPlayer player, boolean firstTime) {
    if (!firstTime) return;
    issueCommand(player, this.command);
  }

  @Override
  public void onSkillRemoved(ServerPlayer player) {
    issueCommand(player, this.removeCommand);
  }

  private void issueCommand(ServerPlayer player, String command) {
    if (command.isEmpty()) return;
    MinecraftServer server = player.getServer();
    if (server == null) return;
    CommandSourceStack commandSourceStack = server.createCommandSourceStack();
    String playerName = player.getGameProfile().getName();
    command = command.replaceAll("<p>", playerName);
    server.getCommands().performPrefixedCommand(commandSourceStack, command);
  }

  @Override
  public SkillBonus.Serializer getSerializer() {
    return PSTSkillBonuses.COMMAND.get();
  }

  @Override
  public CommandBonus copy() {
    return new CommandBonus(command, removeCommand, description);
  }

  @Override
  public CommandBonus multiply(double multiplier) {
    return this;
  }

  @Override
  public boolean canMerge(SkillBonus<?> other) {
    return false;
  }

  @Override
  public boolean sameBonus(SkillBonus<?> other) {
    if (!(other instanceof CommandBonus otherBonus)) return false;
    return otherBonus.command.equals(this.command);
  }

  @Override
  public SkillBonus<CommandBonus> merge(SkillBonus<?> other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MutableComponent getTooltip() {
    Style style = TooltipHelper.getSkillBonusStyle(isPositive());
    return Component.translatable(description).withStyle(style);
  }

  @Override
  public boolean isPositive() {
    return true;
  }

  @Override
  public void addEditorWidgets(
      SkillTreeEditorScreen editor, int index, Consumer<CommandBonus> consumer) {
    editor.addLabel(0, 0, "Command", ChatFormatting.GOLD);
    editor.shiftWidgets(0, 19);
    editor
        .addTextArea(0, 0, 200, 70, command)
        .setResponder(
            v -> {
              setCommand(v);
              consumer.accept(this.copy());
            });
    editor.shiftWidgets(0, 75);
    editor.addLabel(0, 0, "Remove Command", ChatFormatting.GOLD);
    editor.shiftWidgets(0, 19);
    editor
        .addTextArea(0, 0, 200, 70, removeCommand)
        .setResponder(
            v -> {
              setRemoveCommand(v);
              consumer.accept(this.copy());
            });
    editor.shiftWidgets(0, 75);
    editor.addLabel(0, 0, "Description", ChatFormatting.GOLD);
    editor.shiftWidgets(0, 19);
    editor
        .addTextArea(0, 0, 200, 70, description)
        .setResponder(
            v -> {
              setDescription(v);
              consumer.accept(this.copy());
            });
    editor.shiftWidgets(0, 75);
  }

  public void setCommand(@Nonnull String command) {
    this.command = command;
  }

  public void setRemoveCommand(@Nonnull String removeCommand) {
    this.removeCommand = removeCommand;
  }

  public void setDescription(@Nonnull String description) {
    this.description = description;
  }

  public static class Serializer implements SkillBonus.Serializer {
    @Override
    public CommandBonus deserialize(JsonObject json) throws JsonParseException {
      String command = json.get("command").getAsString();
      String removeCommand =
          json.has("remove_command") ? json.get("remove_command").getAsString() : "";
      String description = json.has("description") ? json.get("description").getAsString() : "";
      return new CommandBonus(command, removeCommand, description);
    }

    @Override
    public void serialize(JsonObject json, SkillBonus<?> bonus) {
      if (!(bonus instanceof CommandBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      json.addProperty("command", aBonus.command);
      json.addProperty("remove_command", aBonus.removeCommand);
      json.addProperty("description", aBonus.description);
    }

    @Override
    public CommandBonus deserialize(CompoundTag tag) {
      String command = tag.getString("command");
      String removeCommand = tag.contains("remove_command") ? tag.getString("remove_command") : "";
      String description = tag.contains("description") ? tag.getString("description") : "";
      return new CommandBonus(command, removeCommand, description);
    }

    @Override
    public CompoundTag serialize(SkillBonus<?> bonus) {
      if (!(bonus instanceof CommandBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      tag.putString("command", aBonus.command);
      tag.putString("remove_command", aBonus.removeCommand);
      tag.putString("description", aBonus.description);
      return tag;
    }

    @Override
    public CommandBonus deserialize(FriendlyByteBuf buf) {
      String command = buf.readUtf();
      String removeCommand = buf.readUtf();
      String description = buf.readUtf();
      return new CommandBonus(command, removeCommand, description);
    }

    @Override
    public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
      if (!(bonus instanceof CommandBonus commandBonus)) {
        throw new IllegalArgumentException();
      }
      buf.writeUtf(commandBonus.command);
      buf.writeUtf(commandBonus.removeCommand);
      buf.writeUtf(commandBonus.description);
    }

    @Override
    public SkillBonus<?> createDefaultInstance() {
      return new CommandBonus("give <p> minecraft:apple", "", "Grants an apple when learned");
    }
  }
}
