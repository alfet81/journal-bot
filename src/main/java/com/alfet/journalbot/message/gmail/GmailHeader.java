package com.alfet.journalbot.message.gmail;

import module java.base;
import static com.alfet.journalbot.util.MiscUtils.asOptional;
import static org.apache.commons.lang3.EnumUtils.getEnumIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToNull;

public enum GmailHeader {
  TO, FROM, SUBJECT, DATE;

  public static Optional<GmailHeader> forName(String name) {
    var enumValue = getEnumIgnoreCase(GmailHeader.class, trimToNull(name));
    return asOptional(enumValue);
  }
}
