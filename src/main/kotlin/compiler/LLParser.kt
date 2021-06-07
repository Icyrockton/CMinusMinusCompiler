package compiler

import compiler.Token.Companion.isEqual
import compiler.Token.Companion.isID
import compiler.Token.Companion.isLogicalOP
import compiler.Token.Companion.isVarType
import java.util.*

/**
 *@Author : Icyrockton
 *@Date: 2021/6/3 17:40
 **/
class LLParser {

    private val analysisStack = Stack<Node>() //分析栈
    private val semanticStack = Stack<String>() //语义栈
    private val tokens = mutableListOf<Token>()
    private var parseCount = 0

    private fun Stack<Node>.printInfo() {
        println("分析次数：${++parseCount}")
        this.forEach { node ->
            node.print()
            print(',')
        }
        println()
    }

    fun parse(inputTokens: List<Token>, printAnalysisInfo: Boolean = false, printGeneratedQuadruples: Boolean = false) {
        tokens.clear()
        tokens.addAll(inputTokens)
        generatedQuadruplesList.clear()
        tempID = 0
        quadruplesID  = 0
        analysisStack.apply {
            add(Node.End)
            add(Node.P) //程序开始符号
        }

        while (analysisStack.isNotEmpty()) {

            if (printAnalysisInfo) {
                analysisStack.printInfo()
                println("语义栈:")
                semanticStack.forEach {
                    print(it.toString())
                    print(",")
                }
                println("-------------")
            }


            val top = analysisStack.pop()

            if (top.isNonterminal()) {
                //是非终结符
                parseNonTerminal(top as Node.NonTerminal)

            } else if (top.isTerminal()) {
                parseTerminal(top as Node.Terminal)
            } else if (top.isEnd()) {

            } else if (top.isAction()) {
                parseAction(top as Node.Action)
            }
        }

        if (printGeneratedQuadruples)
            printGeneratedQuadruples()
    }

    private fun parseNonTerminal(nonTerminal: Node.NonTerminal) {
        val topToken = tokens.firstOrNull()

        when (nonTerminal) {
            Node.P -> {

                if (topToken != null) {
                    if (topToken.isVarType()) {  // P -> FP
                        analysisStack.push(Node.P)  //逆序压栈
                        analysisStack.push(Node.F)
                    } else {
                        throw UnexpectedTypeException("${topToken.value}", topToken.line)
                    }
                } else { // P -> ε

                }
            }
            Node.F -> {  // F -> TYPE ID () { D S }
                analysisStack.apply {
                    push(Node.Terminal("}"))
                    push(Node.S_ALL)
                    push(Node.D_ALL)
                    push(Node.Terminal("{"))
                    push(Node.Terminal(")"))
                    push(Node.Terminal("("))
                    push(Node.ID)
                    push(Node.TYPE)
                }
            }
            Node.TYPE -> { // TYPE -> int | string | bool
                if (topToken != null) {
                    if (topToken.isVarType()) {  // int | string | bool
                        println("移除${tokens.removeFirst()}")
                    } else { // 错误
                        throw UnexpectedTypeException("${topToken.value}", topToken.line)
                    }
                } else {
                    throw UnexpectedMissException("缺失类型 int,string,bool,void", -1)
                }
            }
            Node.ID -> { //标识符
                if (topToken != null) {
                    if (topToken.isID()) {
                        // ID值进入语义栈中
                        semanticStack.push(topToken.value)
                        println("移除${tokens.removeFirst()}")
                    } else {
                        throw UnexpectedIdentifierException("${topToken.value}", topToken.line)
                    }
                } else {
                    throw UnexpectedMissException("缺失的标识符ID", -1)
                }
            }
            Node.D_ALL -> { //声明变量的语句
                if (topToken != null) { // D_ALL -> D D_ALL
                    if (topToken.isVarType()) {
                        analysisStack.apply {
                            push(Node.D_ALL)
                            push(Node.D)
                        }
                    } else {
//                        throw UnexpectedVarTypeException("${topToken.value}",topToken.line)
                    }
                } else {  // D_ALL -> ε

                }
            }
            Node.D -> {
                if (topToken != null) {
                    if (topToken.isVarType()) { // D -> TYPE V D_SAME ;
                        analysisStack.apply {
                            push(Node.Terminal(";"))
                            push(Node.D_SAME)
                            push(Node.V)
                            push(Node.TYPE)
                        }
                    }
                } else {
                    throw UnexpectedVarTypeException("期待的变量类型为 int,bool,string", 0)
                }

            }
            Node.V -> {
                if (topToken != null) {
                    if (topToken.isID()) {  // V -> id V_ASSIGN
                        analysisStack.apply {
                            push(Node.V_ASSIGN)
                            push(Node.ID)
                        }
                    } else {
                        throw UnexpectedIdentifierException("${topToken.value}", topToken.line)
                    }
                } else {

                }
            }
            Node.D_SAME -> {
                if (topToken != null && topToken.type == TokenType.Delimiter && topToken.value == ",") {
                    //D_SAME -> , V D_SAME
                    analysisStack.apply {
                        push(Node.D_SAME)
                        push(Node.V)
                        push(Node.Terminal(","))
                    }
                } else {  //  D_SAME -> ε

                }
            }
            Node.V_ASSIGN -> {
                if (topToken != null) {
                    if (topToken.isEqual()) {
                        analysisStack.apply {   // V_ASSIGN -> = E_ALGO @ActionAssign
                            push(Node.ActionAssign)
                            push(Node.E_ALGO)
                            push(Node.Terminal("="))
                        }
                    } else {

                    }
                } else { // V-ASSIGN -> ε

                }
            }
            Node.E_ALGO_F -> {
                if (topToken != null) {
                    if (topToken.type == TokenType.Delimiter && topToken.value == "(") {
                        // E_ALGO_F -> (E)
                        analysisStack.apply {
                            push(Node.ActionWithBracket)
                            push(Node.Terminal(")"))
                            push(Node.E_ALGO)
                            push(Node.Terminal("("))
                        }
                    } else if (topToken.type == TokenType.IntConstant) {
                        //E_ALGO_F -> int
                        analysisStack.push(Node.ActionIntConstant)
                    } else if (topToken.type == TokenType.Identifier) {
                        //E_ALGO_F -> id
                        analysisStack.push(Node.ActionIdentifier)

                    } else {
                        //
                    }
                } else {

                }
            }


            Node.E_ALGO -> {
                //E_ALGO -> E_ALGO_T E_ALGO_SEMI @ActionAddOrSub
                analysisStack.apply {
                    push(Node.ActionAddOrSub)
                    push(Node.E_ALGO_SEMI)
                    push(Node.E_ALGO_T)
                }
            }
            Node.E_ALGO_OP -> {

            }
            Node.E_ALGO_SEMI -> {
                if (topToken != null) {
                    if (topToken.type == TokenType.Operator && (topToken.value == "+" || topToken.value == "-")) {
                        //E_ALGO_SEMI -> +  E_ALGO
                        //			     -  E_ALGO
                        analysisStack.apply {
                            if (topToken.value == "+") {
                                push(Node.ActionAddOp)
                            } else if (topToken.value == "-") {
                                push(Node.ActionSubOp)
                            }
                            push(Node.E_ALGO)
                            push(Node.Terminal(topToken.value))
                        }
                    } else {

                    }
                }       // E_ALGO_SEMI ->  ε
                else {

                }
            }
            Node.E_ALGO_T -> {
                //E_ALGO_T  -> E_ALGO_F E_ALGO_T_SEMI @ActionMulOrDiv
                analysisStack.apply {
                    push(Node.ActionMulOrDiv)
                    push(Node.E_ALGO_T_SEMI)
                    push(Node.E_ALGO_F)
                }
            }
            Node.E_ALGO_T_SEMI -> {
                if (topToken != null) {
                    if (topToken.type == TokenType.Operator && (topToken.value == "*" || topToken.value == "/")) {
                        //E_ALGO_T_SEMI ->   *  E_ALGO_T
                        //			         /  E_ALGO_T
                        analysisStack.apply {
                            if (topToken.value == "*") {
                                push(Node.ActionMulOp)
                            } else if (topToken.value == "/") {
                                push(Node.ActionDivOp)
                            }
                            push(Node.E_ALGO_T)
                            push(Node.Terminal(topToken.value))
                        }
                    } else {

                    }
                }       // E_ALGO_T_SEMI ->  ε
                else {

                }
            }
            Node.E_LOGICAL -> {
                //E_LOGICAL -> E_LOGICAL_T E_LOGICAL_SEMI @ActionAnd
                analysisStack.apply {
                    push(Node.ActionAnd)
                    push(Node.E_LOGICAL_SEMI)
                    push(Node.E_LOGICAL_T)
                }
            }
            Node.E_LOGICAL_SEMI -> {
                if (topToken != null) {
                    if (topToken.value == "&&"){
                        // E_LOGICAL_SEMI -> && E_LOGICAL @ActionAndOp
                        analysisStack.apply {
                            push(Node.ActionAndOp)
                            push(Node.E_LOGICAL)
                            push(Node.Terminal("&&"))
                        }
                    }
                    else{

                    }
                }
            }
            Node.E_LOGICAL_T -> {
                //E_LOGICAL_T -> E_LOGICAL_F E_LOGICAL_T_SEMI @ActionOr
                analysisStack.apply {
                    push(Node.ActionOr)
                    push(Node.E_LOGICAL_T_SEMI)
                    push(Node.E_LOGICAL_F)
                }
            }
            Node.E_LOGICAL_T_SEMI ->{
                if (topToken != null) {
                    if (topToken.value == "||"){
                        // E_LOGICAL_T_SEMI -> || E_LOGICAL_T @ActionOrOp
                        analysisStack.apply {
                            push(Node.ActionOrOp)
                            push(Node.E_LOGICAL_T)
                            push(Node.Terminal("||"))
                        }
                    }
                    else{

                    }
                }
            }
            Node.E_LOGICAL_F ->{
                if (topToken != null) {
                    if (topToken.type == TokenType.Delimiter && topToken.value == "("){
                        // E_LOGICAL_F -> ( E_LOGICAL ) @ActionLogicalWithBracket
                        analysisStack.apply {
                            push(Node.ActionLogicalWithBracket)
                            push(Node.Terminal(")"))
                            push(Node.E_LOGICAL)
                            push(Node.Terminal("("))
                        }
                    }
                    else{
                        // E_LOGICAL_F	-> E_ALGO logical_op E_ALGO @ActionLogical
                        analysisStack.apply {
                            push(Node.ActionLogical)
                            push(Node.E_ALGO)
                            push(Node.LOGICAL_OP)
                            push(Node.E_ALGO)
                        }
                    }
                }
            }
            Node.LOGICAL_OP ->{
                if (topToken != null) {
                    if (topToken.isLogicalOP()){
                        analysisStack.apply {
                            when(topToken.value!!){
                                "==" -> push(Node.ActionEqualOp)
                                "!=" -> push(Node.ActionNotEqualOp)
                                "<" -> push(Node.ActionLessOp)
                                "<=" -> push(Node.ActionLessOrEqualOp)
                                ">=" -> push(Node.ActionGreaterOrEqualOp)
                                ">" -> push(Node.ActionGreaterOp)
                            }
                            push(Node.Terminal(topToken.value))

                        }

                    }
                }
            }
            Node.S_ALL -> {
                if (topToken != null) {
                    // S_ALL -> S S_ALL
                    if ((topToken.type == TokenType.KeyWord && (topToken.value == "if" || topToken.value == "while" || topToken.value == "for" || topToken.value == "return"))
                        || topToken.type == TokenType.Identifier || (topToken.type == TokenType.Delimiter && (topToken.value == "{" || topToken.value == ";"))
                    ) {
                        analysisStack.apply {
                            push(Node.S_ALL)
                            push(Node.S)
                        }
                    }
                } else {  //S_ALL -> ε

                }
            }
            Node.S -> {
                if (topToken != null) {
                    if (topToken.type == TokenType.KeyWord && topToken.value == "if") {
                        // S -> S_IF
                        analysisStack.push(Node.S_IF)
                    } else if (topToken.type == TokenType.KeyWord && topToken.value == "while") {
                        // S -> S_WHILE
                        analysisStack.push(Node.S_WHILE)
                    } else if (topToken.type == TokenType.KeyWord && topToken.value == "for") {
                        // S -> S_FOR
                        analysisStack.push(Node.S_FOR)
                    } else if (topToken.type == TokenType.KeyWord && topToken.value == "return") {
                        // S -> S_RETURN
                        analysisStack.push(Node.S_RETURN)
                    } else if (topToken.type == TokenType.Identifier) {
                        // S -> S_ASSG
                        analysisStack.push(Node.S_ASSG)
                    } else if (topToken.type == TokenType.Delimiter && topToken.value == "{") {
                        // S -> S_BLOCK
                        analysisStack.push(Node.S_BLOCK)
                    } else if (topToken.type == TokenType.Delimiter && topToken.value == ";") {
                        // S -> S_END
                        analysisStack.push(Node.S_END)
                    }
                } else {

                }
            }
            Node.S_IF -> {
                if (topToken != null) {
                    if (topToken.type == TokenType.KeyWord && topToken.value == "if") {
                        //S_IF -> if ( E_LOGICAL ) @ACTION_BEGIN_IF_TRUE S @ACTION_IF_TRUE_DONE S_ELSE
                        analysisStack.apply {
                            push(Node.S_ELSE)
                            push(Node.ACTION_IF_TRUE_DONE)
                            push(Node.S)
                            push(Node.ACTION_BEGIN_IF_TRUE)
                            push(Node.Terminal(")"))
                            push(Node.E_LOGICAL_F)
                            push(Node.Terminal("("))
                            push(Node.Terminal("if"))
                        }
                    } else {

                    }
                } else {

                }
            }
            Node.ASSG -> {
                if (topToken != null) {

                    if (topToken.isID()) {
                        // ASSG -> id = E_ALGO
                        analysisStack.apply {
                            push(Node.ActionAssign)
                            push(Node.E_ALGO)
                            push(Node.Terminal("="))
                            push(Node.ID)
                        }

                    } else {

                    }
                }
            }
            Node.S_ELSE -> {
                if (topToken != null) {
                    if (topToken.type == TokenType.KeyWord && topToken.value == "else") {
                        //S_ELSE -> else @ACTION_BEGIN_ELSE S @ACTION_ELSE_DONE
                        analysisStack.apply {
                            push(Node.ACTION_ELSE_DONE)
                            push(Node.S)
                            push(Node.ACTION_BEGIN_ELSE)
                            push(Node.Terminal("else"))
                        }
                    }
                    //S_ELSE -> ε
                    else {
                        //S_ELSE ->  @ACTION_BEGIN_ELSE @ACTION_ELSE_DONE
                        analysisStack.apply {
                            push(Node.ACTION_ELSE_DONE)
                            push(Node.ACTION_BEGIN_ELSE)
                        }
                    }
                } else {
                    //S_ELSE ->  @ACTION_BEGIN_ELSE @ACTION_ELSE_DONE
                    analysisStack.apply {
                        push(Node.ACTION_ELSE_DONE)
                        push(Node.ACTION_BEGIN_ELSE)
                    }
                }
            }
            Node.S_WHILE -> {
                if (topToken != null) {
                    if (topToken.type == TokenType.KeyWord && topToken.value == "while") {
                        // S_WHILE -> while ( @ACTION_WHILE_BEGIN E_LOGICAL @ACTION_WHILE_TERMINATE ) S @ACTION_WHILE_DONE
                        analysisStack.apply {
                            push(Node.ACTION_WHILE_DONE)
                            push(Node.S)
                            push(Node.Terminal(")"))
                            push(Node.ACTION_WHILE_TERMINATE)
                            push(Node.E_LOGICAL)
                            push(Node.ACTION_WHILE_BEGIN)
                            push(Node.Terminal("("))
                            push(Node.Terminal("while"))
                        }
                    }
                } else {

                }
            }
            Node.S_FOR -> {
                if (topToken != null) {
                    if (topToken.type == TokenType.KeyWord && topToken.value == "for") {
                        //S_FOR -> for ( ASSG_OP @ACTION_FOR_INIT ; E_LOGICAL_OP @ACTION_FOR_TERMINATE ; ASSG_OP @ACTION_FOR_INCREMENT )  S @ACTION_FOR_DONE
                        analysisStack.apply {
                            push(Node.ACTION_FOR_DONE)
                            push(Node.S)
                            push(Node.Terminal(")"))
                            push(Node.ACTION_FOR_INCREMENT)
                            push(Node.ASSG)
                            push(Node.Terminal(";"))
                            push(Node.ACTION_FOR_TERMINATE)
                            push(Node.E_LOGICAL)
                            push(Node.Terminal(";"))
                            push(Node.ACTION_FOR_INIT)
                            push(Node.ASSG)
                            push(Node.Terminal("("))
                            push(Node.Terminal("for"))
                        }
                    }
                } else {

                }
            }
            Node.S_RETURN -> {
                if (topToken != null) {
                    //S_RETURN -> return E_ALGO_OP ;
                    if (topToken.type == TokenType.KeyWord && topToken.value == "return") {
                        analysisStack.apply {
                            push(Node.Terminal(";"))
                            push(Node.E_ALGO)
                            push(Node.Terminal("return"))
                        }
                    }
                } else {

                }
            }
            Node.S_ASSG -> {
                if (topToken != null) {
                    if (topToken.type == TokenType.Identifier) {
                        analysisStack.apply {
                            push(Node.Terminal(";"))
                            push(Node.ASSG)
                        }
                    }
                } else {

                }
            }
            Node.S_BLOCK -> {
                if (topToken != null) {
                    if (topToken.type == TokenType.Delimiter && topToken.value == "{") {
                        // S_BLOCK -> { S_ALL }
                        analysisStack.apply {
                            push(Node.Terminal("}"))
                            push(Node.S_ALL)
                            push(Node.Terminal("{"))
                        }
                    }
                } else {

                }
            }
            Node.S_END -> {
                if (topToken != null) {
                    if (topToken.type == TokenType.Delimiter && topToken.value == ";") {
                        analysisStack.push(Node.Terminal(";"))
                    }
                } else {

                }
            }

        }
    }

    private var currentOp: String? = null //当前的Op操作符
    private val generatedQuadruplesList = mutableListOf<Quadruples>() //生成的四元式
     val quadruplesList  get() =  generatedQuadruplesList.toList()


    //执行语义动作 生成四元式
    private fun parseAction(action: Node.Action) {
        when (action) {
            Node.ActionIdentifier, Node.ActionIntConstant -> {  // E_ALGO_F -> id | int  语义动作 向上传递id的变量,int整形变量
                if (tokens.first().value != null) {
                    Node.E_ALGO_F.value = tokens.first().value!!
                    semanticStack.push(Node.E_ALGO_F.value)
                    println("移除${tokens.removeFirst()}")
                } else {
                    if (action is Node.ActionIdentifier)
                        throw UnexpectedIdentifierException("变量名称为null", tokens.first().line)
                    else
                        throw UnexpectedMissException("缺少整形常量", tokens.first().line)
                }
            }
            Node.ActionWithBracket -> {  //E_ALGO_F -> (E_ALGO)
                //直接把E_ALGO的值赋值给E_ALGO_F
                Node.E_ALGO_F.value = Node.E_ALGO.value //向上传递
            }
            Node.ActionAddOrSub -> {  //规约产生新的四元式
                //E_ALGO -> E_ALGO_T E_ALGO_SEMI @ActionAddOrSub
                if (currentOp != null && (currentOp == "+" || currentOp == "-")) {
                    val temp = newTemp()
                    val arg2 = semanticStack.pop()
                    val arg1 = semanticStack.pop()
                    generatedQuadruplesList.add(Quadruples(newQuadruplesID(), currentOp!!, arg1, arg2, temp))
                    Node.E_ALGO.value = temp
                    semanticStack.push(temp)
                    clearOp()
                }
            }
            Node.ActionMulOrDiv -> {
                //E_ALGO_T -> E_ALGO_F E_ALGO_T_SEMI @ActionMulOrDiv
                if (currentOp != null && (currentOp == "*" || currentOp == "/")) {
                    val temp = newTemp()
                    val arg2 = semanticStack.pop()
                    val arg1 = semanticStack.pop()
                    generatedQuadruplesList.add(Quadruples(newQuadruplesID(), currentOp!!, arg1, arg2, temp))
                    Node.E_ALGO_T.value = temp
                    println("????? #ActionMulOrDiv ${temp}")
                    semanticStack.push(temp)
                    clearOp()
                }
            }
            Node.ActionAddOp -> {
                currentOp="+"
            }
            Node.ActionSubOp -> {
                currentOp="-"
            }
            Node.ActionMulOp -> {
                currentOp="*"
            }
            Node.ActionDivOp -> {
                currentOp="/"
            }
            Node.ActionAndOp -> {
                currentOp ="&&"
            }

            Node.ActionOrOp -> {
                currentOp = "||"
            }
            Node.ActionAnd -> {
                if (currentOp != null && currentOp =="&&"){
                    val temp = newTemp()
                    val arg2=semanticStack.pop()
                    val arg1=semanticStack.pop()
                    generatedQuadruplesList.add(Quadruples(newQuadruplesID(),"&&",arg1,arg2,temp))
                    semanticStack.push(temp)
                    clearOp()
                }
            }
            Node.ActionOr -> {
                if (currentOp != null && currentOp =="||"){
                    val temp = newTemp()
                    val arg2=semanticStack.pop()
                    val arg1=semanticStack.pop()
                    generatedQuadruplesList.add(Quadruples(newQuadruplesID(),"||",arg1,arg2,temp))
                    semanticStack.push(temp)
                    clearOp()
                }
            }
            Node.ActionLessOp -> {
                semanticStack.push("<")
            }
            Node.ActionGreaterOp -> {
                semanticStack.push(">")
            }
            Node.ActionLessOrEqualOp ->{
                semanticStack.push("<=")
            }
            Node.ActionGreaterOrEqualOp ->{
                semanticStack.push(">=")
            }
            Node.ActionEqualOp ->{
                semanticStack.push("==")
            }
            Node.ActionNotEqualOp ->{
                semanticStack.push("!=")
            }
            Node.ActionLogical -> { //逻辑运算符 != == <= >= < > 四元式生成
                val arg2= semanticStack.pop()
                val logicalOp = semanticStack.pop()
                val arg1 = semanticStack.pop()
                val temp = newTemp()
                generatedQuadruplesList.add(Quadruples(newQuadruplesID(),logicalOp,arg1,arg2,temp))
                Node.E_LOGICAL_F.value = temp
                semanticStack.push(temp)
            }
            Node.ActionLogicalWithBracket -> { // E_LOGICAL_F -> ( E_LOGICAL )
                Node.E_LOGICAL_F.value = Node.E_LOGICAL.value
            }
            Node.ActionAssign -> { //赋值语句
                val arg1 = semanticStack.pop()
                val result = semanticStack.pop()
                println("进阿里么${arg1} ${result}")
                generatedQuadruplesList.add(Quadruples(newQuadruplesID(), "=", arg1,null,result))
            }
            Node.ACTION_BEGIN_IF_TRUE -> {
                val arg1= semanticStack.pop()
                //真条件
                generatedQuadruplesList.add(Quadruples(newQuadruplesID(), "if", arg1,"goto", (quadruplesID + 2).toString()))
                //假条件
                generatedQuadruplesList.add(Quadruples(newQuadruplesID(), "goto", null,null,"0"))
                ifFalseChain.push(quadruplesID)
            }
            Node.ACTION_IF_TRUE_DONE -> {
                generatedQuadruplesList.add(Quadruples(newQuadruplesID(),"goto",null,null,"0"))
                ifTrueFinishedChain.push(quadruplesID)
            }
            Node.ACTION_BEGIN_ELSE ->{
                //回填false链
                while (ifFalseChain.isNotEmpty()){
                    val id  = ifFalseChain.pop() - 1
                    generatedQuadruplesList[id].result = (quadruplesID+1).toString()
                }
            }
            Node.ACTION_ELSE_DONE ->{
                //回填true的最后一个四元式
                while (ifTrueFinishedChain.isNotEmpty()){
                    val id  = ifTrueFinishedChain.pop() - 1
                    generatedQuadruplesList[id].result = (quadruplesID+1).toString()
                }
            }
            Node.ACTION_FOR_INIT -> {
                //保存初始化语句后的第一条四元式序号
                forInitFinished.push(quadruplesID + 1)
            }
            Node.ACTION_FOR_TERMINATE -> {
                //for语句的逻辑判断
                val arg1= semanticStack.pop()
                //真条件
                generatedQuadruplesList.add(Quadruples(newQuadruplesID(), "if", arg1,"goto","0"))
                forTrueChain.push(quadruplesID) //需要回填 真条件所转移到的位置
                //假条件
                generatedQuadruplesList.add(Quadruples(newQuadruplesID(), "goto", null,null,"0"))
                forFalseChain.push(quadruplesID) //需要回填 假条件所转移到的位置
                forConditionChain.push(quadruplesID + 1) // quadruplesID + 1 为递增语句的开始
            }
            Node.ACTION_FOR_INCREMENT -> { //递增
                if (forInitFinished.isNotEmpty()){ //转移到条件语句 进行判断
                    val id  = forInitFinished.pop()
                    generatedQuadruplesList.add(Quadruples(newQuadruplesID(), "goto", null,null,"${id}"))
                }
                //回填真链
                if (forTrueChain.isNotEmpty()){
                    val id  = forTrueChain.pop() - 1
                    generatedQuadruplesList[id].result = (quadruplesID+1).toString()
                }
            }
            Node.ACTION_FOR_DONE -> {  // 转移到递增条件
                if (forConditionChain.isNotEmpty()){ //转移到递增语句
                    val id  = forConditionChain.pop()
                    generatedQuadruplesList.add(Quadruples(newQuadruplesID(), "goto", null,null,"${id}"))
                }
                //回填假链
                if (forFalseChain.isNotEmpty()){
                    val id  = forFalseChain.pop() - 1
                    generatedQuadruplesList[id].result = (quadruplesID+1).toString()
                }
            }
            Node.ACTION_WHILE_BEGIN -> {
                whileConditionChain.push(quadruplesID + 1) //记录条件语句的第一条四元式编号
            }
            Node.ACTION_WHILE_TERMINATE -> {
                val arg1= semanticStack.pop()
                //真条件
                generatedQuadruplesList.add(Quadruples(newQuadruplesID(), "if", arg1,"goto", (quadruplesID + 2).toString()))
                //假条件
                generatedQuadruplesList.add(Quadruples(newQuadruplesID(), "goto", null,null,"0"))
                whileFalseConditionChain.push(quadruplesID)
            }
            Node.ACTION_WHILE_DONE -> {
                if (whileConditionChain.isNotEmpty()){
                    val jump  = whileConditionChain.pop()
                    generatedQuadruplesList.add(Quadruples(newQuadruplesID(), "goto", null,null,"${jump}"))
                }
                //回填假条件
                if (whileFalseConditionChain.isNotEmpty()){
                    val id  = whileFalseConditionChain.pop() - 1
                    generatedQuadruplesList[id].result = (quadruplesID+1).toString()
                }
            }
        }
    }
    private val  ifTrueFinishedChain = Stack<Int>()
    private val ifFalseChain = Stack<Int>()
    private val forInitFinished = Stack<Int>()
    private val forTrueChain = Stack<Int>()
    private val forFalseChain = Stack<Int>()
    private val forConditionChain = Stack<Int>()
    private val whileConditionChain = Stack<Int>()
    private val whileFalseConditionChain = Stack<Int>()

    companion object {
        var tempID = 0
        var quadruplesID = 0
    }

    private fun printGeneratedQuadruples() {
        println("\n\n生成的四元式：\n")
        generatedQuadruplesList.forEach(::println)
    }

    //返回四元式的序号
    private fun newQuadruplesID(): Int = ++quadruplesID

    private fun clearOp() {
        currentOp = null
    }

    //返回自定义变量的名称
    private fun newTemp(): String = "@T${++tempID}"

    private fun parseTerminal(terminal: Node.Terminal) {
        val topToken = tokens.firstOrNull()
        if (topToken != null) {
            println("终结符   ${topToken.value}")
            if (terminal.value == topToken.value) {
                println("移除token ${topToken.value}")
                tokens.removeFirst()
            } else {
                throw UnexpectedMissException("终结符${terminal.value}", topToken.line)
            }
        } else {
            throw UnexpectedMissException("终结符${terminal.value}", -1)
        }

    }

}


