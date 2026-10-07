/* matmult.c
 *    一个对大数组做矩阵乘法的测试程序。
 *
 *    目的是给虚拟内存系统施加压力。当 Dim==20 时应该返回 7220
 */

#include "syscall.h"

#define Dim 	20	/* 所有数组加起来的总大小放不进
			 * 物理内存
			 */

int A[Dim][Dim];
int B[Dim][Dim];
int C[Dim][Dim];

int
main()
{
    int i, j, k;

    for (i = 0; i < Dim; i++)		/* 先初始化各个矩阵 */
	for (j = 0; j < Dim; j++) {
	     A[i][j] = i;
	     B[i][j] = j;
	     C[i][j] = 0;
	}

    for (i = 0; i < Dim; i++)		/* 然后把它们相乘 */
	for (j = 0; j < Dim; j++)
            for (k = 0; k < Dim; k++)
		 C[i][j] += A[i][k] * B[k][j];

    // 程序返回最后计算出的那个矩阵单元的值（7220）。
    // 你可以取消下面 printf 的注释，把这个值也打印出来，
    // 但要打印的话，你需要先让 readVirtualMemory 能正常工作

    // printf("C[%d][%d] = %d\n", Dim-1, Dim-1, C[Dim-1][Dim-1]);

    return (C[Dim-1][Dim-1]);		/* 然后就结束了 */
}
