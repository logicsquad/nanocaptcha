package net.logicsquad.nanocaptcha.gimpy;

import java.awt.image.BufferedImage;
import com.jhlabs.image.BlockFilter;

/**
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 */
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
		GimpyRenderer.applyFilter(image, filter);
	}
}
