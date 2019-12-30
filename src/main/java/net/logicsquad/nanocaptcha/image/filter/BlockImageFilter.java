package net.logicsquad.nanocaptcha.image.filter;

import java.awt.image.BufferedImage;
import com.jhlabs.image.BlockFilter;

/**
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public class BlockImageFilter implements ImageFilter {
	
	private static final int DEF_BLOCK_SIZE = 3;
	private final int _blockSize;
	
	public BlockImageFilter() {
		this(DEF_BLOCK_SIZE);
	}
	
	public BlockImageFilter(int blockSize) {
		_blockSize = blockSize;
	}
	
	@Override
	public void filter(BufferedImage image) {
		BlockFilter filter = new BlockFilter();
		filter.setBlockSize(_blockSize);
		ImageFilter.applyFilter(image, filter);
	}
}
