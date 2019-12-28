package net.logicsquad.nanocaptcha.content;

/**
 * TextProducer implementation that will return Chinese characters.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public class ChineseContentProducer implements ContentProducer {
    
	static final int DEFAULT_LENGTH = 5;    
    // Here's hoping none of the characters in this range are offensive.
    static final int CODE_POINT_START = 0x4E00;
    static final int CODE_POINT_END = 0x4F6F;
    private static final int NUM_CHARS = CODE_POINT_END - CODE_POINT_START;
    private static final char[] CHARS;
    
    static {
    	CHARS = new char[NUM_CHARS];
    	for (char c = CODE_POINT_START, i = 0; c < CODE_POINT_END; c++, i++) {
    		CHARS[i] = Character.valueOf(c);
    	}
    }

    private final ContentProducer _txtProd;	// Decorator
    
    public ChineseContentProducer() {
    	this(DEFAULT_LENGTH);
    }
    
    public ChineseContentProducer(int length) {
    	_txtProd = new DefaultContentProducer(length, CHARS);
    }

    @Override
    public String getContent() {
    	return _txtProd.getContent();
    }
}
