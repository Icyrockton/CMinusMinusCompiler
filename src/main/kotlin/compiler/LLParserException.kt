package compiler

/**
 *@Author : Icyrockton
 *@Date: 2021/6/3 19:44
 **/


abstract class LLParserException(override val message: String, open val lineNumber: Int) : Exception(message)


/*
    意外的token
 */
class UnexpectedTokenException(override val message: String, override val lineNumber: Int) :
    LLParserException(message, lineNumber) {
    override fun toString(): String {
        return "语法分析错误,错误的token  :  ${message}  行号${lineNumber}"
    }
}

/*
    意外的返回类型
 */
class UnexpectedTypeException(override val message: String, override val lineNumber: Int) :
    LLParserException(message, lineNumber) {
    override fun toString(): String {
        return "语法分析错误,错误的类型  :  ${message}  行号${lineNumber}"
    }
}

/*
    意外的标识符
 */
class UnexpectedIdentifierException(override val message: String, override val lineNumber: Int) :
    LLParserException(message, lineNumber) {
    override fun toString(): String {
        return "语法分析错误,错误的ID :  ${message}  行号${lineNumber}"
    }
}


/*
    缺失一些东西
 */
class UnexpectedMissException(override val message: String, override val lineNumber: Int) :
    LLParserException(message, lineNumber) {
    override fun toString(): String {
        return "语法分析错误,缺失:  ${message}  行号${lineNumber}"
    }
}


/*
    意外的声明类型
 */
class UnexpectedVarTypeException(override val message: String, override val lineNumber: Int) :
    LLParserException(message, lineNumber) {
    override fun toString(): String {
        return "语法分析错误,错误的变量类型 :  ${message}  行号${lineNumber}"
    }
}
