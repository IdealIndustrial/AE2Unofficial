package appeng.client.render.effects;

import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.client.texture.ExtraBlockTextures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EnergyFx extends EntityBreakingFX
{

	private final IIcon particleTextureIndex;

	private final int startBlkX;
	private final int startBlkY;
	private final int startBlkZ;

	@Override
	public int getFXLayer()
	{
		return 1;
	}

	public EnergyFx(World par1World, double par2, double par4, double par6, Item par8Item) {
		super( par1World, par2, par4, par6, par8Item );
		particleGravity = 0;
		this.particleBlue = 255;
		this.particleGreen = 255;
		this.particleRed = 255;
		this.particleAlpha = 1.4f;
		this.particleScale = 3.5f;
		this.particleTextureIndex = ExtraBlockTextures.BlockEnergyParticle.getIcon();

		startBlkX = MathHelper.floor_double( posX );
		startBlkY = MathHelper.floor_double( posY );
		startBlkZ = MathHelper.floor_double( posZ );
	}

	public void fromItem(ForgeDirection d)
	{
		this.posX += 0.2 * d.offsetX;
		this.posY += 0.2 * d.offsetY;
		this.posZ += 0.2 * d.offsetZ;
		this.particleScale *= 0.8f;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		this.particleScale *= 0.89f;
		this.particleAlpha *= 0.89f;
	}

	@Override
	public void renderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7)
	{
		float f6 = this.particleTextureIndex.getMinU();
		float f7 = this.particleTextureIndex.getMaxU();
		float f8 = this.particleTextureIndex.getMinV();
		float f9 = this.particleTextureIndex.getMaxV();
		float f10 = 0.1F * this.particleScale;

		float f11 = (float) (this.prevPosX + (this.posX - this.prevPosX) * par2 - interpPosX);
		float f12 = (float) (this.prevPosY + (this.posY - this.prevPosY) * par2 - interpPosY);
		float f13 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * par2 - interpPosZ);
		float f14 = 1.0F;

		int blkX = MathHelper.floor_double( posX );
		int blkY = MathHelper.floor_double( posY );
		int blkZ = MathHelper.floor_double( posZ );

		if ( blkX == startBlkX && blkY == startBlkY && blkZ == startBlkZ )
		{
			par1Tessellator.setColorRGBA_F( this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha );
			par1Tessellator.addVertexWithUV( f11 - par3 * f10 - par6 * f10, f12 - par4 * f10, f13 - par5 * f10 - par7 * f10,
					f7, f9 );
			par1Tessellator.addVertexWithUV( f11 - par3 * f10 + par6 * f10, f12 + par4 * f10, f13 - par5 * f10 + par7 * f10,
					f7, f8 );
			par1Tessellator.addVertexWithUV( f11 + par3 * f10 + par6 * f10, f12 + par4 * f10, f13 + par5 * f10 + par7 * f10,
					f6, f8 );
			par1Tessellator.addVertexWithUV( f11 + par3 * f10 - par6 * f10, f12 - par4 * f10, f13 + par5 * f10 - par7 * f10,
					f6, f9 );
		}
	}

}