#include "stdlib.h"

/* 把 s2 接到 s1 的末尾，并返回 s1 */
char *strcat(char *s1, const char *s2) {
    char* result = s1;

    while (*s1 != 0)
	s1++;

    do {
	*(s1++) = *(s2);
    }
    while (*(s2++) != 0);

    return result;
}
