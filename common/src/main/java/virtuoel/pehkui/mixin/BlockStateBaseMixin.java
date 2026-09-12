package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;
import virtuoel.pehkui.util.PehkuiBlockStateExtensions;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin implements PehkuiBlockStateExtensions
{
	@Shadow
	public abstract VoxelShape getShape(BlockGetter level, BlockPos pos);
	
	@Shadow
	public abstract Block getBlock();
	
	@Override
	public VoxelShape pehkui_getOutlineShape(BlockGetter world, BlockPos pos)
	{
		return getShape(world, pos);
	}
	
	@Override
	public Block pehkui_getBlock()
	{
		return getBlock();
	}
}
