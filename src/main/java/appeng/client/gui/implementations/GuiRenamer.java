package appeng.client.gui.implementations;

import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerRenamer;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.ICustomNameObject;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;

public class GuiRenamer  extends AEBaseGui {
    private GuiTextField name;
    public GuiRenamer(InventoryPlayer ip, ICustomNameObject obj) {
        super(new ContainerRenamer(ip, obj));
        this.xSize = 256;
    }
    @Override
    public void initGui() {
        super.initGui();
        this.name = new GuiTextField(this.fontRendererObj, this.guiLeft + 13, this.guiTop + 36, 250, this.fontRendererObj.FONT_HEIGHT);
        this.name.setEnableBackgroundDrawing(false);
        this.name.setMaxStringLength(32);
        this.name.setTextColor(0xFFFFFF);
        this.name.setVisible(true);
        this.name.setFocused(true);
        ((ContainerRenamer) this.inventorySlots).setTextField(this.name);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRendererObj.drawString( GuiText.Renamer.getLocal(), 12, 8, 4210752 );
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture( "guis/renamer.png" );
        this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );
        this.name.drawTextBox();
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (key == 28) // Enter
        {
            try {
                NetworkHandler.instance.sendToServer(new PacketValueConfig("QuartzKnife.ReName", this.name.getText()));
            } catch (IOException e) {
                AELog.debug(e);
            }
            this.mc.thePlayer.closeScreen();
        }
        else if (this.name.textboxKeyTyped(character, key))
        {
            ((ContainerRenamer) this.inventorySlots).setCustomName(name.getText());
        }
        else
            super.keyTyped(character, key);
    }
}