#include "stdlib.h"

/* 返回字符串 str 的长度，不包括 null 结束符 */
unsigned strlen(const char *str) {
  int result=0;

  while (*(str++) != 0)
    result++;

  return result;
}
