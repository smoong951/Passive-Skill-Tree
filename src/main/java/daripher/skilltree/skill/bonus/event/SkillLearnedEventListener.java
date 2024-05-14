package daripher.skilltree.skill.bonus.event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.screen.SkillTreeEditorScreen;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTEventListeners;
import daripher.skilltree.init.PSTLivingMultipliers;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.EventListenerBonus;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.multiplier.NoneLivingMultiplier;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class SkillLearnedEventListener implements SkillEventListener {
  private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;

  public void onEvent(@Nonnull Player player, @Nonnull EventListenerBonus<?> skill) {
    skill.multiply(playerMultiplier.getValue(player)).applyEffect(player);
  }

  @Override
  public MutableComponent getTooltip(Component bonusTooltip) {
    MutableComponent eventTooltip = Component.translatable(getDescriptionId(), bonusTooltip);
    eventTooltip = playerMultiplier.getTooltip(eventTooltip, SkillBonus.Target.PLAYER);
    return eventTooltip;
  }

  @Override
  public SkillBonus.Target getTarget() {
    return SkillBonus.Target.PLAYER;
  }

  @Override
  public SkillEventListener.Serializer getSerializer() {
    return PSTEventListeners.SKILL_LEARNED.get();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SkillLearnedEventListener listener = (SkillLearnedEventListener) o;
    return Objects.equals(playerMultiplier, listener.playerMultiplier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(playerMultiplier);
  }

  @Override
  public void addEditorWidgets(
      SkillTreeEditorScreen editor, Consumer<SkillEventListener> consumer) {
    editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GREEN);
    editor.shiftWidgets(0, 19);
    editor
        .addDropDownList(0, 0, 200, 14, 10, playerMultiplier, PSTLivingMultipliers.multiplierList())
        .setToNameFunc(m -> Component.literal(PSTLivingMultipliers.getName(m)))
        .setResponder(
            m -> {
              setPlayerMultiplier(m);
              consumer.accept(this);
              editor.rebuildWidgets();
            });
    editor.shiftWidgets(0, 19);
    playerMultiplier.addEditorWidgets(
        editor,
        m -> {
          setPlayerMultiplier(m);
          consumer.accept(this);
        });
  }

  public SkillLearnedEventListener setPlayerMultiplier(LivingMultiplier playerMultiplier) {
    this.playerMultiplier = playerMultiplier;
    return this;
  }

  public static class Serializer implements SkillEventListener.Serializer {
    @Override
    public SkillEventListener deserialize(JsonObject json) throws JsonParseException {
      SkillLearnedEventListener listener = new SkillLearnedEventListener();
      listener.setPlayerMultiplier(
          SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier"));
      return listener;
    }

    @Override
    public void serialize(JsonObject json, SkillEventListener listener) {
      if (!(listener instanceof SkillLearnedEventListener aListener)) {
        throw new IllegalArgumentException();
      }
      SerializationHelper.serializeLivingMultiplier(
          json, aListener.playerMultiplier, "player_multiplier");
    }

    @Override
    public SkillEventListener deserialize(CompoundTag tag) {
      SkillLearnedEventListener listener = new SkillLearnedEventListener();
      listener.setPlayerMultiplier(
          SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier"));
      return listener;
    }

    @Override
    public CompoundTag serialize(SkillEventListener listener) {
      if (!(listener instanceof SkillLearnedEventListener aListener)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      SerializationHelper.serializeLivingMultiplier(
          tag, aListener.playerMultiplier, "player_multiplier");
      return tag;
    }

    @Override
    public SkillEventListener deserialize(FriendlyByteBuf buf) {
      SkillLearnedEventListener listener = new SkillLearnedEventListener();
      listener.setPlayerMultiplier(NetworkHelper.readLivingMultiplier(buf));
      return listener;
    }

    @Override
    public void serialize(FriendlyByteBuf buf, SkillEventListener listener) {
      if (!(listener instanceof SkillLearnedEventListener aListener)) {
        throw new IllegalArgumentException();
      }
      NetworkHelper.writeLivingMultiplier(buf, aListener.playerMultiplier);
    }

    @Override
    public SkillEventListener createDefaultInstance() {
      return new SkillLearnedEventListener();
    }
  }
}
