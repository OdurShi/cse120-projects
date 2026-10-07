#include "stdlib.h"

/* 按字典序比较 a 和 b 的前 n 个字符 */
int strncmp(const char* a, const char* b, int n)
{
  assert(n > 0);

  do {
    if (*a < *b)
      return -1;
    if (*a > *b)
      return 1;
    n--;
    a++;
    b++;
  }
  while (n > 0);

  return 0;
}
