/* Start.s
 *	为运行在 Nachos 之上的用户程序提供的汇编语言辅助代码。
 *
 *	因为我们不想引入整个 C 库，所以在这里定义用户程序需要的东西，
 *	也就是 Start 和各个系统调用。
 */

#define START_S
#include "syscall.h"

        .text
        .align  2

/* -------------------------------------------------------------
 * __start
 *	通过调用 "main" 来开始运行一个 C 程序。
 * -------------------------------------------------------------
 */

	.globl	__start
	.ent	__start
__start:
	jal	main
	addu	$4,$2,$0
	jal	exit	 /* 如果从 main 返回了，就调用 exit(返回值) */
	.end	__start

	.globl	__main
	.ent	__main
__main:
	jr	$31
	.end	__main

/* -------------------------------------------------------------
 * 系统调用 stub：
 *	用来向 Nachos 内核发起系统调用的汇编语言辅助代码。
 *	每个系统调用对应一个 stub，它把系统调用编号放进寄存器 r2，
 *	并且不改动系统调用的参数（也就是说，arg1 在 r4，arg2 在 r5，
 *	arg3 在 r6，arg4 在 r7）
 *
 * 	返回值在 r2 中。这遵循 MIPS 上标准的 C 调用约定。
 * -------------------------------------------------------------
 */

#define SYSCALLSTUB(name, number) \
	.globl	name		; \
	.ent	name		; \
name:				; \
	addiu	$2,$0,number	; \
	syscall			; \
	j	$31		; \
	.end	name

	SYSCALLSTUB(halt, syscallHalt)
	SYSCALLSTUB(exit, syscallExit)
	SYSCALLSTUB(exec, syscallExec)
	SYSCALLSTUB(join, syscallJoin)
	SYSCALLSTUB(creat, syscallCreate)
	SYSCALLSTUB(open, syscallOpen)
	SYSCALLSTUB(read, syscallRead)
	SYSCALLSTUB(write, syscallWrite)
	SYSCALLSTUB(close, syscallClose)
	SYSCALLSTUB(unlink, syscallUnlink)
	SYSCALLSTUB(mmap, syscallMmap)
	SYSCALLSTUB(connect, syscallConnect)
	SYSCALLSTUB(accept, syscallAccept)
