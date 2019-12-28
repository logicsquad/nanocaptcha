package net.logicsquad.nanocaptcha.gimpy;

import static net.logicsquad.nanocaptcha.util.ImageUtil.applyFilter;

import java.awt.image.BufferedImage;
import com.jhlabs.image.BlockFilter;

public class BlockGimpyRenderer implements GimpyRenderer {
	
	private static final int DEF_BLOCK_SIZE = 3;
	private final int _blockSize;
	
	public BlockGimpyRenderer() {
		this(DEF_BLOCK_SIZE);
	}
	
	public BlockGimpyRenderer(int blockSize) {
		_blockSize = blockSize;
	}
	
	@Override
	public void gimp(BufferedImage image) {
		BlockFilter filter = new BlockFilter();
		filter.setBlockSize(_blockSize);
		applyFilter(image, filter);
	}
}
