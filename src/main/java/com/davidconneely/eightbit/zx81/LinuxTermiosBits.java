package com.davidconneely.eightbit.zx81;

import static org.fusesource.jansi.internal.CLibrary.*;

final class LinuxTermiosBits {
  private LinuxTermiosBits() {}

  /* c_cc indices */
  private static final int VTIME = 5;
  private static final int VMIN = 6;

  /* c_iflag bits */
  private static final int IGNBRK = 0x00000001; /* Ignore break condition; BL */
  private static final int BRKINT = 0x00000002; /* Signal interrupt on break; BL */
  private static final int PARMRK = 0x00000008; /* Mark parity and framing errors; BL */
  private static final int ISTRIP = 0x00000020; /* Strip 8th bit off characters; BL */
  private static final int INLCR = 0x00000040; /* Map NL to CR on input; BL */
  private static final int IGNCR = 0x00000080; /* Ignore CR; BL */
  private static final int ICRNL = 0x00000100; /* Map CR to NL on input; BL */
  private static final int IXON = 0x00000400; /* 0002000 octal; L */

  /* c_oflag bits */
  private static final int OPOST = 0x00000001; /* Perform output processing; BL */

  /* c_lflag bits */
  private static final int ISIG = 0x00000001; /* Enable signals; L */
  private static final int ICANON = 0x00000002; /* Canonical input (erase and kill processing); L */
  private static final int ECHO = 0x00000008; /* Enable echoing; BL */
  private static final int ECHONL = 0x00000040; /* Echo NL even if ECHO is off; L */
  private static final int IEXTEN = 0x00008000; /* Enable DISCARD and LNEXT; L */

  /* c_cflag bits */
  private static final int CSIZE = 0x00000030; /* Character size mask; L */
  private static final int CS8 = 0x00000030; /* 8 bits; L */
  private static final int PARENB = 0x00000100; /* Parity enable; L */

  static void cfmakeraw(final Termios termios) {
    /*termios.c_iflag &= ~(IGNBRK|BRKINT|PARMRK|ISTRIP|INLCR|IGNCR|ICRNL|IXON);*/
    /*termios.c_oflag &= ~OPOST;*/
    termios.c_lflag &= ~(ECHO /*|ECHONL*/ | ICANON /*|ISIG|IEXTEN*/);
    /*termios.c_cflag &= ~(CSIZE|PARENB);*/
    /*termios.c_cflag |= CS8;*/
    termios.c_cc[VMIN] = 1; /* read returns when one char is available.  */
    termios.c_cc[VTIME] = 0;
  }
}
