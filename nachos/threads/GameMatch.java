package nachos.threads;

import nachos.machine.*;

/**
 * <i>GameMatch</i> 把能力等级相同的 player 线程组成固定人数的小组，让它们
 * 彼此进行比赛。请使用 <i>Lock</i> 和 <i>Condition</i> 来实现
 * <i>GameMatch</i> 类，把 player 线程同步成小组。
 */
public class GameMatch {

    /* player 能力的三个等级。 */
    public static final int abilityBeginner = 1,
	abilityIntermediate = 2,
	abilityExpert = 3;

    /**
     * 分配（创建）一个新的 GameMatch，并指定组成一场比赛所需的同等级 player
     * 线程数。你的实现可以假定这个数总是大于零。
     */
    public GameMatch (int numPlayersInMatch) {
    }

    /**
     * 等待足够数量的同等级 player 线程组成一场比赛，只有在比赛组成后才返回。
     * 随着时间推移可以组成很多场比赛，但任何一个 player 线程只能被分到一场
     * 比赛中。
     *
     * 返回所组成比赛的比赛编号。第一场返回的比赛编号为 1，之后每一场比赛的
     * 编号都加一，与能力等级无关。任何两场比赛的编号都不能相同，比赛编号应该
     * 严格单调递增，并且编号之间不能有空缺。
     *
     * @param ability 应该是 abilityBeginner、abilityIntermediate 或
     * abilityExpert 之一；否则返回 -1。
     */
    public int play (int ability) {
	return -1;
    }
}
