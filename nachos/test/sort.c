/* sort.c
 *    一个对大量整数排序的测试程序。
 *
 *    目的是给虚拟内存系统施加压力。要增加这个程序的内存用量，只需增大
 *    SORTSHIFT。数组的大小是 (SORTSIZE)(2^(SORTSHIFT+2))。
 */

#include "syscall.h"

/* 物理内存的大小；加上代码，空间就不够用了！ */
#define SORTSIZE	256
#define SORTSHIFT	0

int array[SORTSIZE<<SORTSHIFT];

#define	A(i)	(array[(i)<<SORTSHIFT])

void swap(int* x, int* y)
{
  int temp = *x;
  *x = *y;
  *y = temp;
}

int
main()
{
  int i, j;

  /* 先把数组初始化为逆序 */
  for (i=0; i<SORTSIZE; i++)
    A(i) = (SORTSIZE-1)-i;

  /* 然后排序！ */
  for (i=0; i<SORTSIZE-1; i++) {
    for (j=i; j<SORTSIZE; j++) {
      if (A(i) > A(j))
	swap(&A(i), &A(j));
    }
  }

  /* 最后，验证结果 */
  for (i=0; i<SORTSIZE; i++) {
    if (A(i) != i)
      return 1;
  }

  /* 如果成功，返回 0 */
  return 0;
}
