package daripher.skilltree.skill.bonus.player;

import com.google.gson.*;
import daripher.skilltree.client.screen.SkillTreeEditorScreen;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTEventListeners;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.EventListenerBonus;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.event.BlockEventListener;
import daripher.skilltree.skill.bonus.event.SkillEventListener;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class InflictDamageBonus implements EventListenerBonus<InflictDamageBonus> {
  private float chance;
  private float damage;
  private SkillEventListener eventListener;

  public InflictDamageBonus(float chance, float damage, SkillEventListener eventListener) {
    this.chance = chance;
    this.damage = damage;
    this.eventListener = eventListener;
  }

  public InflictDamageBonus(float chance, float damage) {
    this(chance, damage, new BlockEventListener());
  }

  @Override
  public void applyEffect(LivingEntity target) {
    target.hurt(DamageSource.MAGIC, damage);
  }

  @Override
  public SkillBonus.Serializer getSerializer() {
    return PSTSkillBonuses.INFLICT_DAMAGE.get();
  }

  @Override
  public InflictDamageBonus copy() {
    return new InflictDamageBonus(chance, damage, eventListener);
  }

  @Override
  public InflictDamageBonus multiply(double multiplier) {
    if (chance < 1) {
      chance *= (float) multiplier;
    } else {
      damage *= (float) multiplier;
    }
    return this;
  }

  @Override
  public boolean canMerge(SkillBonus<?> other) {
    if (!(other instanceof InflictDamageBonus otherBonus)) return false;
    if (otherBonus.chance < 1 && this.chance < 1 && otherBonus.damage != this.damage) {
      return false;
    }
    return Objects.equals(otherBonus.eventListener, this.eventListener);
  }

  @Override
  public SkillBonus<EventListenerBonus<InflictDamageBonus>> merge(SkillBonus<?> other) {
    if (!(other instanceof InflictDamageBonus otherBonus)) {
      throw new IllegalArgumentException();
    }
    if (otherBonus.chance < 1 && this.chance < 1) {
      return new InflictDamageBonus(otherBonus.chance + this.chance, damage, eventListener);
    } else {
      return new InflictDamageBonus(chance, otherBonus.damage + this.damage, eventListener);
    }
  }

  @Override
  public MutableComponent getTooltip() {
    String damageDescription = TooltipHelper.formatNumber(damage);
    String targetDescription = eventListener.getTarget().name().toLowerCase();
    String bonusDescription = getDescriptionId() + "." + targetDescription;
    if (chance < 1) {
      bonusDescription += ".chance";
    }
    MutableComponent tooltip = Component.translatable(bonusDescription, damageDescription);
    if (chance < 1) {
      tooltip =
          TooltipHelper.getSkillBonusTooltip(
              tooltip, chance, AttributeModifier.Operation.MULTIPLY_BASE);
    }
    tooltip = eventListener.getTooltip(tooltip);
    return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(isPositive()));
  }

  @Override
  public boolean isPositive() {
    return chance > 0 ^ eventListener.getTarget() == Target.PLAYER;
  }

  @Override
  public SkillEventListener getEventListener() {
    return eventListener;
  }

  @Override
  public void addEditorWidgets(
      SkillTreeEditorScreen editor,
      int row,
      Consumer<EventListenerBonus<InflictDamageBonus>> consumer) {
    editor.addLabel(0, 0, "Chance", ChatFormatting.GOLD);
    editor.addLabel(110, 0, "Duration", ChatFormatting.GOLD);
    editor.shiftWidgets(0, 19);
    editor
        .addNumericTextField(0, 0, 90, 14, chance)
        .setNumericResponder(
            v -> {
              setChance(v.floatValue());
              consumer.accept(this.copy());
            });
    editor
        .addNumericTextField(110, 0, 90, 14, damage)
        .setNumericResponder(
            v -> {
              setDamage(v.intValue());
              consumer.accept(this.copy());
            });
    editor.shiftWidgets(0, 19);
    editor.addLabel(0, 0, "Event", ChatFormatting.GOLD);
    editor.shiftWidgets(0, 19);
    editor
        .addDropDownList(0, 0, 200, 14, 10, eventListener, PSTEventListeners.eventsList())
        .setToNameFunc(e -> Component.literal(PSTEventListeners.getName(e)))
        .setResponder(
            e -> {
              setEventListener(e);
              consumer.accept(this.copy());
              editor.rebuildWidgets();
            });
    editor.shiftWidgets(0, 19);
    eventListener.addEditorWidgets(
        editor,
        e -> {
          setEventListener(e);
          consumer.accept(this.copy());
        });
  }

  public void setEventListener(SkillEventListener eventListener) {
    this.eventListener = eventListener;
  }

  public void setChance(float chance) {
    this.chance = chance;
  }

  public void setDamage(float damage) {
    this.damage = damage;
  }

  public static class Serializer implements SkillBonus.Serializer {
    @Override
    public InflictDamageBonus deserialize(JsonObject json) throws JsonParseException {
      float chance = json.get("chance").getAsFloat();
      float damage = json.get("damage").getAsInt();
      InflictDamageBonus bonus = new InflictDamageBonus(chance, damage);
      bonus.eventListener = SerializationHelper.deserializeEventListener(json);
      return bonus;
    }

    @Override
    public void serialize(JsonObject json, SkillBonus<?> bonus) {
      if (!(bonus instanceof InflictDamageBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      json.addProperty("chance", aBonus.chance);
      json.addProperty("damage", aBonus.damage);
      SerializationHelper.serializeEventListener(json, aBonus.eventListener);
    }

    @Override
    public InflictDamageBonus deserialize(CompoundTag tag) {
      float chance = tag.getFloat("chance");
      float damage = tag.getFloat("damage");
      InflictDamageBonus bonus = new InflictDamageBonus(chance, damage);
      bonus.eventListener = SerializationHelper.deserializeEventListener(tag);
      return bonus;
    }

    @Override
    public CompoundTag serialize(SkillBonus<?> bonus) {
      if (!(bonus instanceof InflictDamageBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      tag.putFloat("chance", aBonus.chance);
      tag.putFloat("damage", aBonus.damage);
      SerializationHelper.serializeEventListener(tag, aBonus.eventListener);
      return tag;
    }

    @Override
    public InflictDamageBonus deserialize(FriendlyByteBuf buf) {
      float amount = buf.readFloat();
      float damage = buf.readFloat();
      InflictDamageBonus bonus = new InflictDamageBonus(amount, damage);
      bonus.eventListener = NetworkHelper.readEventListener(buf);
      return bonus;
    }

    @Override
    public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
      if (!(bonus instanceof InflictDamageBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      buf.writeFloat(aBonus.chance);
      buf.writeFloat(aBonus.damage);
      NetworkHelper.writeEventListener(buf, aBonus.eventListener);
    }

    @Override
    public SkillBonus<?> createDefaultInstance() {
      return new InflictDamageBonus(0.05f, 5f);
    }
  }
}
