package nachos.threads;

import nachos.machine.*;

/**
 * <i>SquadMatch</i> 把三种不同能力的 player 线程组合在一起，让它们彼此进行
 * 比赛。请使用 <i>Lock</i> 和 <i>Condition</i> 来实现 <i>SquadMatch</i>
 * 类，把 player 线程同步成这样的小组。
 */
public class SquadMatch {

    /**
     * 分配（创建）一个新的 SquadMatch，用来把不同能力的 player 组成一个小队
     * 进行比赛。
     */
    public SquadMatch () {
    }

    /**
     * 等待与 wizard 线程和 thief 线程组成一个小队，只有当三种 player 线程都
     * 调用了这个 SquadMatch 之后才返回。一个小队总是由三个线程组成，并且只能
     * 由三种不同类型的线程组成。随着时间推移可以组成很多场比赛，但任何一个
     * player 线程只能被分到一场比赛中。
     */
    public void warrior () {
    }

    /**
     * 等待与 warrior 线程和 thief 线程组成一个小队，只有当三种 player 线程都
     * 调用了这个 SquadMatch 之后才返回。一个小队总是由三个线程组成，并且只能
     * 由三种不同类型的线程组成。随着时间推移可以组成很多场比赛，但任何一个
     * player 线程只能被分到一场比赛中。
     */
    public void wizard () {
    }

    /**
     * 等待与 warrior 线程和 wizard 线程组成一个小队，只有当三种 player 线程都
     * 调用了这个 SquadMatch 之后才返回。一个小队总是由三个线程组成，并且只能
     * 由三种不同类型的线程组成。随着时间推移可以组成很多场比赛，但任何一个
     * player 线程只能被分到一场比赛中。
     */
    public void thief () {
    }
}
