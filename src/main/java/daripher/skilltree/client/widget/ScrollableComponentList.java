package daripher.skilltree.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ScrollableComponentList extends AbstractWidget {
  private final int maxHeight;
  private List<Component> components = new ArrayList<>();
  private int maxLines;
  private int scroll;

  public ScrollableComponentList(int y, int maxHeight) {
    super(0, y, 0, 0, Component.empty());
    this.maxHeight = maxHeight;
  }

  @Override
  public void renderButton(
      @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
    if (components.isEmpty()) return;
    renderBackground(poseStack);
    renderText(poseStack);
    renderScrollBar(poseStack);
  }

  private void renderBackground(@NotNull PoseStack poseStack) {
    fill(poseStack, x, y, x + width, y + height, 0xDD000000);
  }

  private void renderText(@NotNull PoseStack poseStack) {
    Font font = Minecraft.getInstance().font;
    for (int i = scroll; i < maxLines + scroll; i++) {
      Component component = components.get(i);
      int x = this.x + 5;
      int y = this.y + 5 + (i - scroll) * (font.lineHeight + 3);
      drawString(poseStack, font, component, x, y, 0x7B7BE5);
    }
  }

  private void renderScrollBar(@NotNull PoseStack poseStack) {
    if (components.size() > maxLines) {
      int scrollSize = height * maxLines / components.size();
      int maxScroll = components.size() - maxLines;
      int scrollShift = (int) ((height - scrollSize) / (float) maxScroll * scroll);
      int x = this.x + width - 3;
      int y = this.y + scrollShift;
      fill(poseStack, x, this.y, this.x + width, this.y + height, 0xDD222222);
      fill(poseStack, x, y, this.x + width, this.y + scrollShift + scrollSize, 0xDD888888);
    }
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
    int maxScroll = components.size() - maxLines;
    if (amount < 0 && scroll < maxScroll) scroll++;
    if (amount > 0 && scroll > 0) scroll--;
    return true;
  }

  public void setComponents(List<Component> components) {
    maxLines = components.size();
    this.components = components;
    width = 0;
    Font font = Minecraft.getInstance().font;
    for (Component stat : components) {
      int statWidth = font.width(stat);
      if (statWidth > width) width = statWidth;
    }
    width += 14;
    height = components.size() * (font.lineHeight + 3) + 10;
    while (height > maxHeight) {
      height -= font.lineHeight + 3;
      maxLines--;
    }
  }

  @Override
  public void updateNarration(@NotNull NarrationElementOutput output) {}
}
