# C--编译器

------

## 运行图

![](https://github.com/Icyrockton/CMinusMinusCompiler/blob/master/img/img_1.png)
![](https://github.com/Icyrockton/CMinusMinusCompiler/blob/master/img/img_2.png)
![](https://github.com/Icyrockton/CMinusMinusCompiler/blob/master/img/img_3.png)

## 特性

- 词法分析
- LL(1)语法分析(基于栈的递归)
- 中间代码(IR)四元式生成
- Jetpack Compose UI界面
- Junit5简单测试

## 文件结构

- compiler/LexAnalyzer.kt **词法分析**
- compiler/LexAnalyzerTest.kt **词法分析测试**
- compiler/LLParser.kt **LL(1)语法分析+内嵌语义分析(四元式生成)**
- compiler/LLParserTest.kt **语法分析测试**
- compiler/Token.kt **词法分析Token**
- compiler/Node.kt **产生式的结点(非终结符,终结符,语义动作等)**
- compiler/LLParserException.kt  LexException.kt **异常**
- ui/main.kt **UI界面**
- C-- grammar productions.txt **产生式规范**

## 支持的语法

- int类型变量
- 可嵌套算数表达式 + - * /  
- 可嵌套布尔表达式 != , = , >= , <= , > , < , && ,  ||
- 可嵌套的if,while,for控制流

## 已知问题

- 布尔表达式如果是&&,|| 必须在外面套一层括号 例如 (a > 10) && (b< 100) ,改写为 ((a > 10) && (b< 100))
- int不支持负数 (>=0)
- 变量名需要用空格与运算符进行间隔 例如 (a>=10) 改写为 (a >= 10)


## Credit

- xiangxianzhang / Compiler
https://github.com/xiangxianzhang/Compiler
  
- C--语言规范
  https://www2.cs.arizona.edu/~debray/Teaching/CSc453/DOCS/cminusminusspec.html
  
- C语言的文法结构 https://www.cnblogs.com/lger/p/6146867.html

## 产生式

**以@开头的为语义结点，生成对应的四元式**

- P ->  FP|ε 
- TYPE -> int | string | bool
- F -> TYPE id () { D_ALL S_ALL }
- V -> id V_ASSIGN    
- V_ASSIGN -> = E_ALGO @ActionAssign |ε

- D_ALL -> D D_ALL | ε
- D -> TYPE V D_SAME ;

- D_SAME -> , V D_SAME | ε

- E_LOGICAL 	   -> E_LOGICAL_T E_LOGICAL_SEMI @ActionAnd
- E_LOGICAL_SEMI -> && E_LOGICAL @ActionAndOp

- E_LOGICAL_T      -> E_LOGICAL_F E_LOGICAL_T_SEMI @ActionOr
- E_LOGICAL_T_SEMI -> || E_LOGICAL_T @ActionOrOp

- E_LOGICAL_F		 -> 
    - E_ALGO logical_op E_ALGO @ActionLogical
    - | ( E_LOGICAL )  @ActionLogicalWithBracket
    - | ! E_LOGICAL    @ActionLogicalNot
    
- E_ALGO      -> E_ALGO_T E_ALGO_SEMI @ActionAddOrSub
- E_ALGO_OP -> E_ALGO|ε
- E_ALGO_SEMI -> 
    - | +  E_ALGO @ActionAddOp
    - | -  E_ALGO @ActionSubOp
    - | ε

- E_ALGO_T 		-> E_ALGO_F E_ALGO_T_SEMI @ActionMulOrDiv
- E_ALGO_T_SEMI   -> *  E_ALGO_T @ActionMulOp
    - | /  E_ALGO_T @ActionDivOp
    - | ε

- E_ALGO_F 		-> (E_ALGO) | int | id


- logical_op ->  == | != | <= | >= | < | >

- ASSG -> id = E_ALGO @ActionAssign
- ASSG_OP -> ASSG | ε

- S_ALL -> S S_ALL | ε

-  S -> S_IF | S_WHILE | S_FOR | S_RETURN | S_ASSG | S_BLOCK | S_END

- S_IF -> if ( E_LOGICAL_F ) @ACTION_BEGIN_IF_TRUE S @ACTION_IF_TRUE_DONE S_ELSE
- S_ELSE -> else @ACTION_BEGIN_ELSE S @ACTION_ELSE_DONE | ε @ACTION_ELSE_DONE
- S_WHILE -> while ( @ACTION_WHILE_BEGIN E_LOGICAL @ACTION_FOR_TERMINATE ) S @ACTION_FOR_DONE
- S_FOR -> for ( ASSG_OP @ACTION_FOR_INIT ; E_LOGICAL_OP @ACTION_FOR_TERMINATE ; ASSG_OP @ACTION_FOR_INCREMENT )  S @ACTION_FOR_DONE
- S_RETURN -> return E_ALGO_OP ;
- S_ASSG -> ASSG ;
- S_BLOCK -> { S_ALL }
- S_END -> ;