package Client;

import javax.swing.text.*;

public class TextLimitDocument extends PlainDocument {

  private final int limit;

  public TextLimitDocument(int limit) {
    this.limit = limit;
  }

  @Override
  public void insertString(int offs, String str, AttributeSet a)
      throws BadLocationException {
    if (str == null)
      return;

    if ((getLength() + str.length()) <= limit) {
      super.insertString(offs, str, a);
    }
  }
}