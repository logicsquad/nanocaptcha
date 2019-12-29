package net.logicsquad.nanocaptcha.content;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Produces text of a given length from a given array of characters.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * @author <a href="mailto:paulh@logicsquad.net">Paul Hoadley</a>
 * @since 1.0
 */
public abstract class DefaultContentProducer implements ContentProducer {

    private static final Random RAND = new SecureRandom();
    
    private final int _length;
    private final char[] _srcChars;
    
    public DefaultContentProducer(int length, char[] srcChars) {
    	_length = length;
    	_srcChars = copyOf(srcChars, srcChars.length);
    }
    
    @Override
    public String getContent() {
        String capText = "";
        for (int i = 0; i < _length; i++) {
            capText += _srcChars[RAND.nextInt(_srcChars.length)];
        }

        return capText;
    }
    
    private static char[] copyOf(char[] original, int newLength) {
        char[] copy = new char[newLength];
        System.arraycopy(original, 0, copy, 0,
                Math.min(original.length, newLength));
        return copy;
    }
}
