package net.logicsquad.nanocaptcha.content;

public class ArabicContentProducer implements ContentProducer {
    static final int DEFAULT_LENGTH = 5;

    private static char[] ARABIC_CHARS = { '\u0627', '\u0628', '\u062a',
            '\u062b', '\u062c', '\u062d', '\u062e', '\u062f', '\u0630',
            '\u0631', '\u0632', '\u0633', '\u0634', '\u0635', '\u0636',
            '\u0637', '\u0638', '\u0639', '\u063a', '\u0641', '\u0642',
            '\u0643', '\u0644', '\u0645', '\u0646', '\u0647', '\u0648',
            '\u064a' };
    
    private final ContentProducer _txtProd;
    
    public ArabicContentProducer() {
        this(DEFAULT_LENGTH);
    }
    
    public ArabicContentProducer(int length) {
        _txtProd = new DefaultContentProducer(length, ARABIC_CHARS);
    }
    
    @Override
    public String getContent() {
        return new StringBuffer(_txtProd.getContent()).reverse().toString();
    }
}