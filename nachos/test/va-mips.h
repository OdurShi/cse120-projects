/* ---------------------------------------- */
/*         MIPS/GNU CC 的 VARARGS           */
/*                                          */
/*                                          */
/*                                          */
/*                                          */
/* ---------------------------------------- */


/* 这些宏为 GNU C 实现可变参数（varargs）——无论是传统风格还是 ANSI 风格。  */

/* 定义 __gnuc_va_list。  */

#ifndef __GNUC_VA_LIST
#define __GNUC_VA_LIST
#if defined (__mips_eabi) && ! defined (__mips_soft_float) && ! defined (__mips_single_float)

typedef struct {
  /* 指向浮点（FP）寄存器的指针。  */
  char *__fp_regs;
  /* 剩余的浮点寄存器个数。  */
  int __fp_left;
  /* 指向通用（GP）寄存器的指针，其后紧跟栈上的参数。  */
  char *__gp_regs;
} __gnuc_va_list;

#else /* ! (defined (__mips_eabi) && ! defined (__mips_soft_float) && ! defined (__mips_single_float)) */

typedef char * __gnuc_va_list;

#endif /* ! (defined (__mips_eabi) && ! defined (__mips_soft_float) && ! defined (__mips_single_float)) */
#endif /* 未定义 __GNUC_VA_LIST */

/* 如果这是给 libc 内部使用的，除了 __gnuc_va_list 之外
   什么都不要定义。  */
#if defined (_STDARG_H) || defined (_VARARGS_H)

#ifndef _VA_MIPS_H_ENUM
#define _VA_MIPS_H_ENUM
enum {
  __no_type_class = -1,
  __void_type_class,
  __integer_type_class,
  __char_type_class,
  __enumeral_type_class,
  __boolean_type_class,
  __pointer_type_class,
  __reference_type_class,
  __offset_type_class,
  __real_type_class,
  __complex_type_class,
  __function_type_class,
  __method_type_class,
  __record_type_class,
  __union_type_class,
  __array_type_class,
  __string_type_class,
  __set_type_class,
  __file_type_class,
  __lang_type_class
};
#endif

/* 在 GCC 2 中，我们希望在参数列表声明的末尾加上省略号。
   GCC 1 无法解析它。  */

#if __GNUC__ > 1
#define __va_ellipsis ...
#else
#define __va_ellipsis
#endif

#ifdef __mips64
#define __va_rounded_size(__TYPE)  \
  (((sizeof (__TYPE) + 8 - 1) / 8) * 8)
#else
#define __va_rounded_size(__TYPE)  \
  (((sizeof (__TYPE) + sizeof (int) - 1) / sizeof (int)) * sizeof (int))
#endif

#ifdef __mips64
#define __va_reg_size 8
#else
#define __va_reg_size 4
#endif

/* 获取 _MIPS_SIM_ABI64 等的定义。  */
#ifdef _MIPS_SIM
#include <sgidefs.h>
#endif

#ifdef _STDARG_H
#if defined (__mips_eabi)
#if ! defined (__mips_soft_float) && ! defined (__mips_single_float)
#ifdef __mips64
#define va_start(__AP, __LASTARG)					\
  (__AP.__gp_regs = ((char *) __builtin_next_arg (__LASTARG)		\
		     - (__builtin_args_info (2) < 8			\
			? (8 - __builtin_args_info (2)) * __va_reg_size	\
			: 0)),						\
   __AP.__fp_left = 8 - __builtin_args_info (3),			\
   __AP.__fp_regs = __AP.__gp_regs - __AP.__fp_left * __va_reg_size)
#else /* ! defined (__mips64) */
#define va_start(__AP, __LASTARG)					\
  (__AP.__gp_regs = ((char *) __builtin_next_arg (__LASTARG)		\
		     - (__builtin_args_info (2) < 8			\
			? (8 - __builtin_args_info (2)) * __va_reg_size	\
			: 0)),						\
   __AP.__fp_left = (8 - __builtin_args_info (3)) / 2,			\
   __AP.__fp_regs = __AP.__gp_regs - __AP.__fp_left * 8,		\
   __AP.__fp_regs = (char *) ((int) __AP.__fp_regs & -8))
#endif /* ! defined (__mips64) */
#else /* ! (! defined (__mips_soft_float) && ! defined (__mips_single_float) ) */
#define va_start(__AP, __LASTARG)					\
  (__AP = ((__gnuc_va_list) __builtin_next_arg (__LASTARG)		\
	   - (__builtin_args_info (2) >= 8 ? 0				\
	      : (8 - __builtin_args_info (2)) * __va_reg_size)))
#endif /* ! (! defined (__mips_soft_float) && ! defined (__mips_single_float) ) */
#else /* ! defined (__mips_eabi) */
#define va_start(__AP, __LASTARG) \
  (__AP = (__gnuc_va_list) __builtin_next_arg (__LASTARG))
#endif /* ! (defined (__mips_eabi) && ! defined (__mips_soft_float) && ! defined (__mips_single_float)) */
#else /* ! _STDARG_H */
#define va_alist  __builtin_va_alist
#ifdef __mips64
/* 这里假定 `long long int' 总是 64 位的类型。  */
#define va_dcl    long long int __builtin_va_alist; __va_ellipsis
#else
#define va_dcl    int __builtin_va_alist; __va_ellipsis
#endif
#if defined (__mips_eabi)
#if ! defined (__mips_soft_float) && ! defined (__mips_single_float)
#ifdef __mips64
#define va_start(__AP)							\
  (__AP.__gp_regs = ((char *) __builtin_next_arg ()			\
		     - (__builtin_args_info (2) < 8			\
			? (8 - __builtin_args_info (2)) * __va_reg_size	\
			: __va_reg_size)),				\
   __AP.__fp_left = 8 - __builtin_args_info (3),			\
   __AP.__fp_regs = __AP.__gp_regs - __AP.__fp_left * __va_reg_size)
#else /* ! defined (__mips64) */
#define va_start(__AP)							\
  (__AP.__gp_regs = ((char *) __builtin_next_arg ()			\
		     - (__builtin_args_info (2) < 8			\
			? (8 - __builtin_args_info (2)) * __va_reg_size	\
			: __va_reg_size)),				\
   __AP.__fp_left = (8 - __builtin_args_info (3)) / 2,			\
   __AP.__fp_regs = __AP.__gp_regs - __AP.__fp_left * 8,		\
   __AP.__fp_regs = (char *) ((int) __AP.__fp_regs & -8))
#endif /* ! defined (__mips64) */
#else /* ! (! defined (__mips_soft_float) && ! defined (__mips_single_float)) */
#define va_start(__AP)							\
  (__AP = ((__gnuc_va_list) __builtin_next_arg ()			\
	   - (__builtin_args_info (2) >= 8 ? __va_reg_size		\
	      : (8 - __builtin_args_info (2)) * __va_reg_size)))
#endif /* ! (! defined (__mips_soft_float) && ! defined (__mips_single_float)) */
/* _MIPS_SIM_ABI64 需要另一套代码。  */
#elif defined(_MIPS_SIM) && (_MIPS_SIM == _MIPS_SIM_ABI64 || _MIPS_SIM == _MIPS_SIM_NABI32)
#define va_start(__AP)							\
  (__AP = (__gnuc_va_list) __builtin_next_arg ()			\
   + (__builtin_args_info (2) >= 8 ? -8 : 0))
#else
#define va_start(__AP)  __AP = (char *) &__builtin_va_alist
#endif
#endif /* ! _STDARG_H */

#ifndef va_end
void va_end (__gnuc_va_list);		/* 在 libgcc.a 中定义 */
#endif
#define va_end(__AP)	((void)0)

#if defined (__mips_eabi)

#if ! defined (__mips_soft_float) && ! defined (__mips_single_float)
#ifdef __mips64
#define __va_next_addr(__AP, __type)					\
  ((__builtin_classify_type (*(__type *) 0) == __real_type_class	\
    && __AP.__fp_left > 0)						\
   ? (--__AP.__fp_left, (__AP.__fp_regs += 8) - 8)			\
   : (__AP.__gp_regs += __va_reg_size) - __va_reg_size)
#else
#define __va_next_addr(__AP, __type)					\
  ((__builtin_classify_type (*(__type *) 0) == __real_type_class	\
    && __AP.__fp_left > 0)						\
   ? (--__AP.__fp_left, (__AP.__fp_regs += 8) - 8)			\
   : (((__builtin_classify_type (* (__type *) 0) < __record_type_class	\
	&& __alignof__ (__type) > 4)					\
       ? __AP.__gp_regs = (char *) (((int) __AP.__gp_regs + 8 - 1) & -8) \
       : (char *) 0),							\
      (__builtin_classify_type (* (__type *) 0) >= __record_type_class	\
       ? (__AP.__gp_regs += __va_reg_size) - __va_reg_size		\
       : ((__AP.__gp_regs += __va_rounded_size (__type))		\
	  - __va_rounded_size (__type)))))
#endif
#else /* ! (! defined (__mips_soft_float) && ! defined (__mips_single_float)) */
#ifdef __mips64
#define __va_next_addr(__AP, __type)					\
  ((__AP += __va_reg_size) - __va_reg_size)
#else
#define __va_next_addr(__AP, __type)					\
  (((__builtin_classify_type (* (__type *) 0) < __record_type_class	\
     && __alignof__ (__type) > 4)					\
    ? __AP = (char *) (((int) __AP + 8 - 1) & -8)			\
    : (char *) 0),							\
   (__builtin_classify_type (* (__type *) 0) >= __record_type_class	\
    ? (__AP += __va_reg_size) - __va_reg_size				\
    : ((__AP += __va_rounded_size (__type))				\
       - __va_rounded_size (__type))))
#endif
#endif /* ! (! defined (__mips_soft_float) && ! defined (__mips_single_float)) */

#ifdef __MIPSEB__
#define va_arg(__AP, __type)						\
  ((__va_rounded_size (__type) <= __va_reg_size)			\
   ? *(__type *) (void *) (__va_next_addr (__AP, __type)		\
			   + __va_reg_size				\
			   - sizeof (__type))				\
   : (__builtin_classify_type (*(__type *) 0) >= __record_type_class	\
      ? **(__type **) (void *) (__va_next_addr (__AP, __type)		\
				+ __va_reg_size				\
				- sizeof (char *))			\
      : *(__type *) (void *) __va_next_addr (__AP, __type)))
#else
#define va_arg(__AP, __type)						\
  ((__va_rounded_size (__type) <= __va_reg_size)			\
   ? *(__type *) (void *) __va_next_addr (__AP, __type)		\
   : (__builtin_classify_type (* (__type *) 0) >= __record_type_class	\
      ? **(__type **) (void *) __va_next_addr (__AP, __type)		\
      : *(__type *) (void *) __va_next_addr (__AP, __type)))
#endif

#else /* ! defined (__mips_eabi) */

/* 我们先转换成 void *，再转换成 TYPE *，因为这样可以避免
   关于提高对齐要求的警告。  */
/* __mips64 的情况与 32 位的情况是相反的，因为标准的 32 位调用约定会把
   所有小于一个字（word）的参数左对齐，而 __mips64 的调用约定不会
   （因此它们是右对齐的）。  */
#ifdef __mips64
#ifdef __MIPSEB__
#define va_arg(__AP, __type)                                    \
  ((__type *) (void *) (__AP = (char *) ((((__PTRDIFF_TYPE__)__AP + 8 - 1) & -8) \
					 + __va_rounded_size (__type))))[-1]
#else
#define va_arg(__AP, __type)                                    \
  ((__AP = (char *) ((((__PTRDIFF_TYPE__)__AP + 8 - 1) & -8)	\
		     + __va_rounded_size (__type))),		\
   *(__type *) (void *) (__AP - __va_rounded_size (__type)))
#endif

#else /* 非 __mips64 */

#ifdef __MIPSEB__
/* 用于大端机器。  */
#define va_arg(__AP, __type)					\
  ((__AP = (char *) ((__alignof__ (__type) > 4			\
		      ? ((int)__AP + 8 - 1) & -8		\
		      : ((int)__AP + 4 - 1) & -4)		\
		     + __va_rounded_size (__type))),		\
   *(__type *) (void *) (__AP - __va_rounded_size (__type)))
#else
/* 用于小端机器。  */
#define va_arg(__AP, __type)						    \
  ((__type *) (void *) (__AP = (char *) ((__alignof__(__type) > 4	    \
					  ? ((int)__AP + 8 - 1) & -8	    \
					  : ((int)__AP + 4 - 1) & -4)	    \
					 + __va_rounded_size(__type))))[-1]
#endif
#endif
#endif /* ! defined (__mips_eabi)  */

/* 把 __gnuc_va_list 复制到另一个同类型的变量中。  */
#define __va_copy(dest, src) (dest) = (src)

#endif /* defined (_STDARG_H) || defined (_VARARGS_H) */
