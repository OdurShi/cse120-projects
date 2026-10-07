/*
 * K&R 书中一个简单伪随机数生成器的实现。
 */

#include "stdlib.h"

unsigned int next = 93186752;

/* 简单的随机数生成器（mod 32768） */
int
rand (void)
{
    next = next * 1103515245 + 12345;
    return (unsigned int)(next/65536) % 32768;
}

/* 把种子设为一个新值。 */
void
srand(unsigned int seed)
{
    next = seed;
}
