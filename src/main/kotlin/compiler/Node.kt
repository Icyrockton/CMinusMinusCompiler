package compiler

import org.jetbrains.annotations.Contract

/**
 *@Author : Icyrockton
 *@Date: 2021/6/3 18:49
 **/
sealed class Node {
    object End : Node()
    sealed class NonTerminal(val name: String, var value: String = "") : Node()
    object F : NonTerminal("F") //function
    object P : NonTerminal("P") //program
    object TYPE : NonTerminal("TYPE") //变量类型 int | string | bool
    object ID : NonTerminal("ID")
    object D : NonTerminal("D")
    object D_ALL : NonTerminal("D_ALL")
    object D_SAME : NonTerminal("D_SAME")
    object V : NonTerminal("V")
    object V_ASSIGN : NonTerminal("V_ASSIGN")
    object INT_CONSTANT : NonTerminal("INT_CONSTANT")
    object CHAR_CONSTANT : NonTerminal("CHAR_CONSTANT")
    object STRING_CONSTANT : NonTerminal("STRING_CONSTANT")
    object LOGICAL_OP : NonTerminal("LOGICAL_OP")
    object ASSG : NonTerminal("ASSG")
    object ASSG_OP : NonTerminal("ASSG_OP")
    object S : NonTerminal("S")
    object S_ALL : NonTerminal("S_ALL")
    object S_IF : NonTerminal("S_IF")
    object S_ELSE : NonTerminal("S_ELSE")
    object S_WHILE : NonTerminal("S_WHILE")
    object S_FOR : NonTerminal("S_FOR")
    object S_RETURN : NonTerminal("S_RETURN")
    object S_ASSG : NonTerminal("S_ASSG")
    object S_BLOCK : NonTerminal("S_BLOCK")
    object S_END : NonTerminal("S_END")
    object E_OP : NonTerminal("E_OP")
    object E_LOGICAL : NonTerminal("E_LOGICAL")
    object E_LOGICAL_SEMI : NonTerminal("E_LOGICAL_SEMI")
    object E_LOGICAL_T : NonTerminal("E_LOGICAL_T")
    object E_LOGICAL_T_SEMI : NonTerminal("E_LOGICAL_T_SEMI")
    object E_LOGICAL_F : NonTerminal("E_LOGICAL_F")
    object E_ALGO : NonTerminal("E_ALGO")
    object E_ALGO_OP : NonTerminal("E_ALGO_OP")
    object E_ALGO_SEMI : NonTerminal("E_ALGO_SEMI")
    object E_ALGO_T : NonTerminal("E_ALGO_T")
    object E_ALGO_T_SEMI : NonTerminal("E_ALGO_T_SEMI")
    object E_ALGO_F : NonTerminal("E_ALGO_F")

    //语义动作
    sealed class Action(val name: String) : Node()
    object ActionAdd : Action("ActionAdd")
    object ActionSub : Action("ActionSub")
    object ActionWithBracket : Action("ActionWithBracket")   // E -> (E)
    object ActionIntConstant : Action("ActionIntConstant")   // E -> int
    object ActionIdentifier : Action("ActionIdentifier")   // E -> id
    object ActionAddOp : Action("ActionAddOp")   // OP切换到 +
    object ActionSubOp : Action("ActionSubOp")   // OP切换到 -
    object ActionMulOp : Action("ActionMulOp")   // OP切换到 *
    object ActionDivOp : Action("ActionDivOp")   // OP切换到 /
    object ActionAddOrSub : Action("ActionAddOrSub") //归约的语义动作 产生新的四元式 操作符是 + 或者 -
    object ActionMulOrDiv : Action("ActionMulOrDiv") //归约的语义动作 产生新的四元式 操作符是 * 或者 /
    object ActionAssign : Action("ActionMulOrDiv") //归约的语义动作 产生新的赋值四元式语句

    object ActionAndOp : Action("ActionAndOp")   // OP切换到 &&
    object ActionOrOp : Action("ActionOrOp")   // OP切换到 ||
    object ActionLessOrEqualOp : Action("ActionLessOrEqualOp")   // OP切换到 <=
    object ActionGreaterOrEqualOp : Action("ActionGreaterOrEqualOp")   // OP切换到 >=
    object ActionLessOp : Action("ActionLessOp")   // OP切换到 <
    object ActionGreaterOp : Action("ActionGreaterOp")   // OP切换到 >
    object ActionEqualOp : Action("ActionEqualOp")   // OP切换到 ==
    object ActionNotEqualOp : Action("ActionNotEqualOp")   // OP切换到 !=
    object ActionAnd : Action("ActionAnd")   // 规约产生 && 四元式
    object ActionOr : Action("ActionOr")   // 规约产生  || 四元式
    object ACTION_BEGIN_IF_TRUE : Action("ACTION_BEGIN_IF_TRUE")
    object ACTION_IF_TRUE_DONE : Action("ACTION_IF_TRUE_DONE")
    object ACTION_IF_DONE : Action("ACTION_IF_DONE")
    object ACTION_ELSE_DONE : Action("ACTION_ELSE_DONE")
    object ACTION_BEGIN_ELSE : Action("ACTION_BEGIN_ELSE")
    object ACTION_FOR_INIT : Action("ACTION_FOR_INIT")
    object ACTION_FOR_TERMINATE : Action("ACTION_FOR_TERMINATE")
    object ACTION_FOR_INCREMENT : Action("ACTION_FOR_INCREMENT")
    object ACTION_FOR_DONE : Action("ACTION_FOR_DONE")
    object ACTION_WHILE_BEGIN : Action("ACTION_WHILE_BEGIN")
    object ACTION_WHILE_TERMINATE : Action("ACTION_WHILE_TERMINATE")
    object ACTION_WHILE_DONE : Action("ACTION_WHILE_DONE")


    object ActionLogical : Action("ActionLogical")   //  E_LOGICAL_F -> E_ALGO logical_op E_ALGO @ActionLogical
    object ActionLogicalWithBracket : Action("ActionLogicalWithBracket")   // E_LOGICAL_F	-> ( E_LOGICAL )  @ActionLogicalWithBracket



    data class Terminal(val value: String) : Node() {
        override fun toString(): String {
            return this.value

        }
    }

    override fun toString(): String {
        return when (this) {
            is NonTerminal -> this.name
            is Terminal -> this.value
            is End -> "END"
            is Action -> this.name
            else -> "Unknown"
        }
    }

}

fun Node.isNonterminal(): Boolean = this is Node.NonTerminal

fun Node.isTerminal(): Boolean = this is Node.Terminal

fun Node.isEnd(): Boolean = this is Node.End

fun Node.isAction(): Boolean = this is Node.Action



fun Node.print() {
    print("<")
    if (this.isNonterminal()) {
        print("非终结符,")
    } else if (this.isTerminal()) {
        print("终结符,")
    } else if (this.isEnd()) {
        print("结束符,")
    } else if (this.isAction()) {
        print("语义动作,")
    }
    print(this.toString())
    print(">")
}


//四元式 （ op , arg1 , arg2 , result )
data class Quadruples(
    val id: Int,
    val op: String,
    val arg1: String? = null,
    val arg2: String? = null,
    var result: String? = null
) {
    override fun toString(): String {
        return "(${id}) 【${op},${arg1 ?: "_"},${arg2 ?: "_"},${result ?: "_"}】"
    }
}