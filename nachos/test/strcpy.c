#include "stdlib.h"

/* 把 src 复制到 dst，并返回 dst */
char *strcpy(char *dst, const char *src) {
  int n=0;
  char *result = dst;

  do {
    *(dst++) = *src;
    n++;
  }
  while (*(src++) != 0);

  return result;
}
