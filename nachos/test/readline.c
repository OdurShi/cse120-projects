#include "stdio.h"
#include "stdlib.h"

void readline(char *s, int maxlength) {
  int i = 0;

  while (1) {
    char c = getch();
    /* 如果是行尾，就收尾 */
    if (c == '\n') {
      putchar('\n');
      s[i] = 0;
      return;
    }
    /* 否则如果是退格…… */
    else if (c == '\b') {
      /* 如果没有可删除的字符，就响铃 */
      if (i == 0) {
	beep();
      }
      /* 否则删掉一个字符 */
      else {
	printf("\b \b");
	i--;
      }
    }
    /* 否则如果是无效字符或者已经没有空间了，就响铃 */
    else if (c < 0x20 || i+1 == maxlength) {
      beep();
    }
    /* 否则把这个字符加进去 */
    else {
      s[i++] = c;
      putchar(c);
    }
  }
}
