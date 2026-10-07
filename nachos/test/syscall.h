/**
 * Nachos 的系统调用接口。这些是用户程序可以通过 syscall 指令调用的 Nachos
 * 内核操作。
 *
 * 这个接口源自 UNIX 的 syscall。这里的信息大部分抄自 UNIX 的 man page。
 */

#ifndef SYSCALL_H
#define SYSCALL_H

/**
 * 系统调用编号，通过 $r0 传递，告诉内核要执行哪个系统调用。
 */
#define	syscallHalt		0
#define	syscallExit		1
#define	syscallExec		2
#define	syscallJoin		3
#define	syscallCreate		4
#define	syscallOpen		5
#define	syscallRead		6
#define	syscallWrite		7
#define	syscallClose		8
#define	syscallUnlink		9
#define syscallMmap		10
#define syscallConnect		11
#define syscallAccept		12
#define syscallSignal		13
#define syscallSigreturn	14
#define syscallAlarm		15

/* 不希望汇编器看到 C 代码，但 start.s 会 include syscall.h。 */
#ifndef START_S

/* 进程被创建时，已经有两个流是打开的。文件描述符 0 指向键盘输入（UNIX 的
 * stdin），文件描述符 1 指向显示输出（UNIX 的 stdout）。不需要事先调用
 * open()，就可以读文件描述符 0、写文件描述符 1。
 */
#define fdStandardInput		0
#define fdStandardOutput	1

/* 系统调用接口。这些是 Nachos 内核为了能运行用户程序而需要支持的操作。
 *
 * 用户程序只需调用相应的过程，就能调用其中每一个；一段汇编语言 stub 会把
 * syscall 编号（见上文）存入 $r0，然后执行一条 syscall 指令。接着内核的
 * 异常处理函数就会被调用。
 */

/* 通过调用 Machine.halt() 让 Nachos 机器停机。只有根进程（第一个进程，由
 * UserKernel.run() 执行）才允许执行这个 syscall。其他任何进程都应该忽略这个
 * syscall 并立即返回。
 */
void halt();

/* 进程管理相关的 SYSCALL：exit()、exec()、join() */

/**
 * 立即终止当前进程。属于这个进程的所有打开的文件描述符都会被关闭。这个进程的
 * 所有子进程都不再有父进程。
 *
 * status 会作为这个进程的退出状态返回给父进程，父进程可以用 join syscall
 * 获取它。
 *
 * exit() 永远不会返回。
 */
void exit(int status);

/**
 * 在一个新的子进程中，用指定的参数执行存储在指定文件中的程序。子进程有一个
 * 新的、唯一的进程 ID，并且开始时 stdin 已作为文件描述符 0 打开，stdout 已
 * 作为文件描述符 1 打开。
 *
 * file 是一个以 null 结尾的字符串，指定包含可执行文件的文件名。注意这个
 * 字符串必须包含 ".coff" 扩展名。
 *
 * argc 指定要传给子进程的参数个数。这个数必须是非负的。
 *
 * argv 是一个指针数组，其中的指针指向以 null 结尾的字符串，表示要传给子进程
 * 的参数。argv[0] 指向第一个参数，argv[argc-1] 指向最后一个参数。如果 argc
 * 为 0，就忽略 argv 参数。否则，argv 数组应该包含 argc 个有效的、以 null
 * 结尾的字符串，如果不是这样就是一个错误。
 *
 * exec() 返回子进程的进程 ID，这个 ID 可以传给 join()。出错时返回 -1。
 */
int exec(char *file, int argc, char *argv[]);

/**
 * 暂停当前进程的执行，直到由 processID 参数指定的子进程退出。如果调用时子进程
 * 已经退出，就立即返回。当前进程恢复执行时，它会与这个子进程断绝关系
 * （disown），这样之后就不能再对那个进程使用 join()。
 *
 * processID 是子进程的进程 ID，由 exec() 返回。
 *
 * status 指向一个整数，子进程的退出状态会存到这里。这就是子进程传给 exit()
 * 的值。如果子进程是因为未处理的异常而退出的，存进去的值是未定义的。status
 * 是一个可选参数：如果 status 是空指针（0x0），join 照常工作，只是不返回子
 * 进程的退出值。
 *
 * 如果子进程正常退出，返回 1。如果子进程因为未处理的异常而退出，返回 0。如果
 * processID 指向的不是当前进程的子进程，返回 -1。
 */
int join(int processID, int *status);

/* 文件管理相关的 SYSCALL：creat、open、read、write、close、unlink
 *
 * 文件描述符是一个小的非负整数，指向磁盘上的一个文件或者一个流（例如
 * console 输入、console 输出和网络连接）。可以把文件描述符传给 read() 和
 * write() 来读写对应的文件/流。也可以把文件描述符传给 close()，以释放这个
 * 文件描述符以及所有相关的资源。
 */

/**
 * 尝试打开指定名字的磁盘文件，如果文件不存在就创建它，并返回一个可以用来
 * 访问这个文件的文件描述符。如果文件已经存在，creat 会把它截断（清空）。
 *
 * 注意 creat() 只能用来在磁盘上创建文件；creat() 永远不会返回指向流的文件
 * 描述符。
 *
 * 返回新的文件描述符；如果出错，返回 -1。
 */
int creat(char *name);

/**
 * 尝试打开指定名字的文件，并返回一个文件描述符。
 *
 * 注意 open() 只能用来打开磁盘上的文件；open() 永远不会返回指向流的文件
 * 描述符。
 *
 * 返回新的文件描述符；如果出错，返回 -1。
 */
int open(char *name);

/**
 * 尝试从 fileDescriptor 指向的文件或流中，读取最多 count 个字节到 buffer 中。
 *
 * 成功时，返回读到的字节数。如果文件描述符指向磁盘上的文件，文件位置会前进
 * 这么多字节。
 *
 * 如果这个数小于请求的字节数，不一定是错误。如果文件描述符指向磁盘上的文件，
 * 这表示已经到达文件末尾。如果文件描述符指向一个流，这表示目前实际可用的
 * 字节比请求的少，但将来可能会有更多字节可用。注意：read() 永远不会等待流
 * 有更多数据；它总是立即返回尽可能多的数据（可能只有 1 个字节）。
 *
 * 出错时返回 -1，并且新的文件位置是未定义的。以下情况可能导致出错：
 * fileDescriptor 无效；buffer 的一部分是只读的或无效的；或者网络流已经被
 * 远程主机终止，并且没有更多数据可用。
 */
int read(int fileDescriptor, void *buffer, int count);

/**
 * 尝试把 buffer 中最多 count 个字节写到 fileDescriptor 指向的文件或流中。
 * write() 可能在这些字节真正被刷新到文件或流之前就返回。不过，如果内核队列
 * 暂时满了，向流写数据可能会阻塞。
 *
 * 成功时，返回写入的字节数（零表示什么都没写入），并且文件位置会前进这么多
 * 字节。如果这个数小于请求的字节数，那就**是**一个错误。对于磁盘文件，这
 * 表示磁盘已满。对于流，这表示在所有数据传输完之前，流就被远程主机终止了。
 *
 * 出错时返回 -1，并且新的文件位置是未定义的。以下情况可能导致出错：
 * fileDescriptor 无效；buffer 的一部分是无效的；或者网络流已经被远程主机
 * 终止。
 */
int write(int fileDescriptor, void *buffer, int count);

/**
 * 关闭一个文件描述符，使它不再指向任何文件或流，并且可以被重用。与这个文件
 * 描述符相关的资源会被释放。
 *
 * 成功时返回 0；如果出错，返回 -1。
 */
int close(int fileDescriptor);

/**
 * 从文件系统中删除一个文件。
 *
 * 注意 unlink 不会关闭任何文件。如果这个进程或其他进程打开了这个文件，
 * StubFileSystem 中的底层文件系统实现会妥善处理这种情况（这个进程会请求
 * 文件系统删除这个文件，但文件系统要等到所有其他进程都用完这个文件之后，
 * 才会真正删除它）。
 *
 * 成功时返回 0；如果出错，返回 -1。
 */
int unlink(char *name);

/**
 * 把 fileDescriptor 引用的文件映射到内存中的 address 处。文件最大可以是
 * 0x7FFFFFFF 字节。
 *
 * 为了保持一致性，在这个文件描述符被关闭之前，之后对它调用 read() 和
 * write() 都会失败（返回 -1）。
 *
 * 当文件描述符被关闭时，这个映射中所有剩余的 dirty 页都会被刷新到磁盘，并且
 * 这个映射会被移除。
 *
 * 成功时返回文件的长度；如果出错，返回 -1。
 */
int mmap(int fileDescriptor, char *address);

/**
 * 尝试向指定远程主机上的指定端口发起一个新连接，并返回一个指向这个连接的新
 * 文件描述符。如果远程主机没有立即响应，connect() 也不会放弃。
 *
 * 返回新的文件描述符；如果出错，返回 -1。
 */
int connect(int host, int port);

/**
 * 尝试在指定的本地端口上接受一个连接，并返回一个指向这个连接的文件描述符。
 *
 * 如果这个端口上有待处理的连接请求，就取出一个请求，并向远程主机发送一个
 * 确认（这样它的 connect() 调用就可以返回）。由于远程主机永远不会取消连接
 * 请求，accept() 不需要等待远程主机确认连接（也就是说，两次握手就够了；不需要
 * TCP 的三次握手）。
 *
 * 如果没有待处理的连接请求，立即返回 -1。
 *
 * 无论哪种情况，accept() 都不会等待远程主机就返回。
 *
 * 返回一个指向这个连接的新文件描述符；如果出错，返回 -1。
 */
int accept(int port);

/**
 * 信号值
 */
#define	SIGSEGV		3	/* 写一个只读页 */
#define	SIGBUS		5	/* 未对齐的虚拟地址 */
#define	SIGFPE		6	/* 除以零 */
#define	SIGILL		7	/* 非法指令 */
#define	SIGALRM		8	/* alarm 定时器到期 */


/**
 * 把 sighandler 注册为信号 signum 的信号处理函数。信号处理函数必须是一个有效
 * 的虚拟地址，并且信号编号必须是上面列出的信号值之一。
 *
 * 信号处理函数是一个指向函数的指针，这个函数接受一个整数作为参数。这个整数
 * 就是触发调用这个处理函数的信号（一个信号处理函数可以用来处理多个信号）。
 * 如果信号处理函数希望在它执行完后程序继续执行（例如，它处理了信号，并且没有
 * 调用 exit()），那么它必须通过显式调用 sigreturn() 来返回。
 *
 * 成功时返回 0；如果出错，返回 -1。
 */
int signal(int signum, void (*sighandler)(int));

/**
 * 从信号处理函数返回。如果信号处理函数没有用 exit() 终止进程，那么它所有的
 * 返回路径都必须调用 sigreturn()。
 *
 * 如果在信号处理函数之外调用，返回 -1 表示错误。在处理函数内部调用时，它永远
 * 不会返回。
 */
int sigreturn(void);

/**
 * 注册一个异步的 alarm，在机器至少执行了给定数目的 tick 之后触发。alarm 触发
 * 时，如果进程为 SIGALRM 信号注册了处理函数，就调用这个处理函数。如果 alarm
 * 触发了但没有注册 SIGALRM 处理函数，这个 alarm 会被静默忽略，不影响进程的
 * 执行。
 *
 * alarm() 只安排一次 alarm 触发。如果进程想要重复的 alarm 信号，它的 SIGALRM
 * 处理函数必须调用 alarm() 来安排下一次 alarm。
 *
 * 在之前安排的 alarm 触发之前调用 alarm()，会取消之前的 alarm 并安排一个新的。
 * 如果之前有 alarm，alarm() 返回它剩余的 tick 数；如果之前没有 alarm，返回 0。
 * 用 0 或负数 tick 调用 alarm() 会取消任何待触发的 alarm，并且不安排新的
 * alarm（这不算错误）。
 */
int alarm(int ticks);



#endif /* START_S */

#endif /* SYSCALL_H */
