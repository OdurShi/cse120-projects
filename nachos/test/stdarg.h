/* GNU 的 stdarg.h。
   注意，va_arg 中使用的类型应当与实参**经过默认类型提升之后**的实际类型
   一致。
   因此，va_arg (..., short) 是无效的。  */

#ifndef _STDARG_H
#ifndef _ANSI_STDARG_H_
#ifndef __need___va_list
#define _STDARG_H
#define _ANSI_STDARG_H_
#endif /* 未定义 __need___va_list */
#undef __need___va_list

#ifdef __clipper__
#include "va-clipper.h"
#else
#ifdef __m88k__
#include "va-m88k.h"
#else
#ifdef __i860__
#include "va-i860.h"
#else
#ifdef __hppa__
#include "va-pa.h"
#else
#ifdef __mips__
#include "va-mips.h"
#else
#ifdef __sparc__
#include "va-sparc.h"
#else
#ifdef __i960__
#include "va-i960.h"
#else
#ifdef __alpha__
#include "va-alpha.h"
#else
#if defined (__H8300__) || defined (__H8300H__) || defined (__H8300S__)
#include "va-h8300.h"
#else
#if defined (__PPC__) && (defined (_CALL_SYSV) || defined (_WIN32))
#include "va-ppc.h"
#else
#ifdef __arc__
#include "va-arc.h"
#else
#ifdef __M32R__
#include "va-m32r.h"
#else
#ifdef __sh__
#include "va-sh.h"
#else
#ifdef __mn10300__
#include "va-mn10300.h"
#else
#ifdef __mn10200__
#include "va-mn10200.h"
#else
#ifdef __v850__
#include "va-v850.h"
#else

/* 定义 __gnuc_va_list。  */

#ifndef __GNUC_VA_LIST
#define __GNUC_VA_LIST
#if defined(__svr4__) || defined(_AIX) || defined(_M_UNIX) || defined(__NetBSD__)
typedef char *__gnuc_va_list;
#else
typedef void *__gnuc_va_list;
#endif
#endif

/* 如果这次 include 来自用户程序，
   就为用户定义标准的宏。  */
#ifdef _STDARG_H

/* 参数列表中一个类型为 TYPE 的参数所需的空间大小。
   TYPE 也可以是一个表达式，此时使用该表达式的类型。  */

#if defined(sysV68)
#define __va_rounded_size(TYPE)  \
  (((sizeof (TYPE) + sizeof (short) - 1) / sizeof (short)) * sizeof (short))
#else
#define __va_rounded_size(TYPE)  \
  (((sizeof (TYPE) + sizeof (int) - 1) / sizeof (int)) * sizeof (int))
#endif

#define va_start(AP, LASTARG) 						\
 (AP = ((__gnuc_va_list) __builtin_next_arg (LASTARG)))

#undef va_end
void va_end (__gnuc_va_list);		/* 在 libgcc.a 中定义 */
#define va_end(AP)	((void)0)

/* 我们先转换成 void *，再转换成 TYPE *，因为这样可以避免
   关于提高对齐要求的警告。  */

#if (defined (__arm__) && ! defined (__ARMEB__)) || defined (__i386__) || defined (__i860__) || defined (__ns32000__) || defined (__vax__)
/* 这是给小端机器用的；较小的参数向上填充。  */
#define va_arg(AP, TYPE)						\
 (AP = (__gnuc_va_list) ((char *) (AP) + __va_rounded_size (TYPE)),	\
  *((TYPE *) (void *) ((char *) (AP) - __va_rounded_size (TYPE))))
#else /* 大端 */
/* 这是给大端机器用的；较小的参数向下填充。  */
#define va_arg(AP, TYPE)						\
 (AP = (__gnuc_va_list) ((char *) (AP) + __va_rounded_size (TYPE)),	\
  *((TYPE *) (void *) ((char *) (AP)					\
		       - ((sizeof (TYPE) < __va_rounded_size (char)	\
			   ? sizeof (TYPE) : __va_rounded_size (TYPE))))))
#endif /* 大端 */

/* 把 __gnuc_va_list 复制到另一个同类型的变量中。  */
#define __va_copy(dest, src) (dest) = (src)

#endif /* _STDARG_H */

#endif /* 非 v850 */
#endif /* 非 mn10200 */
#endif /* 非 mn10300 */
#endif /* 非 sh */
#endif /* 非 m32r */
#endif /* 非 arc */
#endif /* 非使用 V.4 调用序列的 powerpc */
#endif /* 非 h8300 */
#endif /* 非 alpha */
#endif /* 非 i960 */
#endif /* 非 sparc */
#endif /* 非 mips */
#endif /* 非 hppa */
#endif /* 非 i860 */
#endif /* 非 m88k */
#endif /* 非 clipper */

#ifdef _STDARG_H
/* 如果需要，就根据 __gnuc_va_list 定义 va_list。 */
/* 当从 stdio.h 中 include 时，我们故意不定义 va_list，
   因为 ANSI C 规定 stdio.h 不应该定义 va_list。stdio.h
   需要访问这个数据类型，但不能使用这个名字。它应该使用
   __gnuc_va_list 这个名字，这是安全的，因为这个名字是保留给实现使用的。  */

#ifdef _HIDDEN_VA_LIST  /* 在 OSF1 上，这表示 varargs.h 处于"加载了一半"的状态。  */
#undef _VA_LIST
#endif

#ifdef _BSD_VA_LIST
#undef _BSD_VA_LIST
#endif

#if defined(__svr4__) || (defined(_SCO_DS) && !defined(__VA_LIST))
/* SVR4.2 把 _VA_LIST 用作 va_list 的内部别名，
   所以我们必须避免在这里测试或设置它。
   SVR4 在 stdarg.h 中把 _VA_LIST 用作一个标志，但我们
   应该不会与之冲突。  */
#ifndef _VA_LIST_
#define _VA_LIST_
#ifdef __i860__
#ifndef _VA_LIST
#define _VA_LIST va_list
#endif
#endif /* __i860__ */
typedef __gnuc_va_list va_list;
#ifdef _SCO_DS
#define __VA_LIST
#endif
#endif /* _VA_LIST_ */
#else /* 非 __svr4__ || _SCO_DS */

/* 宏 _VA_LIST_ 和这个文件在 Ultrix 上使用的是同一个东西。
   但在 BSD NET2 上，我们不能测试、定义或取消定义它。
   （注意，NET 2 的 ansi.h 中关于 _VA_LIST_ 的注释
   是不正确的——参见 stdio.h！）  */
#if !defined (_VA_LIST_) || defined (__BSD_NET2__) || defined (____386BSD____) || defined (__bsdi__) || defined (__sequent__) || defined (__FreeBSD__) || defined(WINNT)
/* 宏 _VA_LIST_DEFINED 在 Windows NT 3.5 中使用  */
#ifndef _VA_LIST_DEFINED
/* 宏 _VA_LIST 在 SCO Unix 3.2 中使用。  */
#ifndef _VA_LIST
/* 宏 _VA_LIST_T_H 在 Bull dpx2 中使用  */
#ifndef _VA_LIST_T_H
typedef __gnuc_va_list va_list;
#endif /* 未定义 _VA_LIST_T_H */
#endif /* 未定义 _VA_LIST */
#endif /* 未定义 _VA_LIST_DEFINED */
#if !(defined (__BSD_NET2__) || defined (____386BSD____) || defined (__bsdi__) || defined (__sequent__) || defined (__FreeBSD__))
#define _VA_LIST_
#endif
#ifndef _VA_LIST
#define _VA_LIST
#endif
#ifndef _VA_LIST_DEFINED
#define _VA_LIST_DEFINED
#endif
#ifndef _VA_LIST_T_H
#define _VA_LIST_T_H
#endif

#endif /* 未定义 _VA_LIST_，某些系统除外 */

#endif /* 非 __svr4__ */

#endif /* _STDARG_H */

#endif /* 未定义 _ANSI_STDARG_H_ */
#endif /* 未定义 _STDARG_H */
